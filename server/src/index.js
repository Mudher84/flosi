import express from 'express';
import admin from 'firebase-admin';
import { google } from 'googleapis';
import jwt from 'jsonwebtoken';
import crypto from 'node:crypto';

const app = express();
app.disable('x-powered-by');
app.use(express.json({ limit: '64kb' }));
app.use(express.urlencoded({ extended: false, limit: '64kb' }));

if (!admin.apps.length) admin.initializeApp();

const PACKAGE_NAME = process.env.FLOSI_ANDROID_PACKAGE || 'com.flosi.app';
const PRODUCT_ID = 'flosi_monthly';
const ZAIN_BASE = (process.env.ZAINCASH_BASE_URL || 'https://pg-api-uat.zaincash.iq').replace(/\/$/, '');

function required(name) {
  const value = process.env[name];
  if (!value) throw new Error(`Missing required environment variable: ${name}`);
  return value;
}

async function requireUser(req, res, next) {
  try {
    const header = req.get('authorization') || '';
    if (!header.startsWith('Bearer ')) return res.status(401).json({ error: 'unauthorized' });
    req.user = await admin.auth().verifyIdToken(header.slice(7), true);
    next();
  } catch {
    res.status(401).json({ error: 'unauthorized' });
  }
}

app.get('/health', (_req, res) => res.json({ ok: true, service: 'flosi-backend' }));

app.post('/v1/subscriptions/google/verify', requireUser, async (req, res) => {
  try {
    const purchaseToken = String(req.body?.purchaseToken || '').trim();
    const productId = String(req.body?.productId || '').trim();
    if (!purchaseToken || productId !== PRODUCT_ID) return res.status(400).json({ error: 'invalid_request' });

    const auth = new google.auth.GoogleAuth({ scopes: ['https://www.googleapis.com/auth/androidpublisher'] });
    const androidpublisher = google.androidpublisher({ version: 'v3', auth });
    const response = await androidpublisher.purchases.subscriptionsv2.get({
      packageName: PACKAGE_NAME,
      token: purchaseToken
    });

    const data = response.data || {};
    const state = data.subscriptionState || 'SUBSCRIPTION_STATE_UNSPECIFIED';
    const expiryTimes = (data.lineItems || []).map(x => Date.parse(x.expiryTime || '')).filter(Number.isFinite);
    const latestExpiry = expiryTimes.length ? Math.max(...expiryTimes) : 0;
    const stateCanEntitle = new Set([
      'SUBSCRIPTION_STATE_ACTIVE',
      'SUBSCRIPTION_STATE_IN_GRACE_PERIOD',
      'SUBSCRIPTION_STATE_CANCELED'
    ]).has(state);
    const entitled = stateCanEntitle && latestExpiry > Date.now();

    res.json({
      entitled,
      productId: PRODUCT_ID,
      subscriptionState: state,
      expiryTimeMillis: latestExpiry || null,
      userId: req.user.uid
    });
  } catch (error) {
    console.error('google_verify_failed', error?.message || error);
    res.status(502).json({ error: 'verification_failed' });
  }
});

let cachedZainToken = null;
let cachedZainExpiry = 0;
async function zainToken() {
  if (cachedZainToken && Date.now() < cachedZainExpiry - 60_000) return cachedZainToken;
  const body = new URLSearchParams({
    grant_type: 'client_credentials',
    client_id: required('ZAINCASH_CLIENT_ID'),
    client_secret: required('ZAINCASH_CLIENT_SECRET'),
    scope: 'payment:read payment:write reverse:write'
  });
  const response = await fetch(`${ZAIN_BASE}/oauth2/token`, {
    method: 'POST',
    headers: { 'content-type': 'application/x-www-form-urlencoded' },
    body
  });
  const data = await response.json().catch(() => ({}));
  if (!response.ok || !data.access_token) throw new Error(`ZainCash OAuth failed: ${response.status}`);
  cachedZainToken = data.access_token;
  cachedZainExpiry = Date.now() + Math.max(60, Number(data.expires_in || 3600)) * 1000;
  return cachedZainToken;
}

app.post('/v1/zaincash/payments', requireUser, async (req, res) => {
  try {
    const amount = Number(req.body?.amount);
    const orderId = String(req.body?.orderId || '').trim();
    const phone = String(req.body?.phone || '').trim();
    if (!Number.isFinite(amount) || amount <= 0 || !orderId || orderId.length > 120) {
      return res.status(400).json({ error: 'invalid_request' });
    }

    const token = await zainToken();
    const payload = {
      language: 'Ar',
      externalReferenceId: crypto.randomUUID(),
      orderId,
      serviceType: required('ZAINCASH_SERVICE_TYPE'),
      amount: { value: amount, currency: 'IQD' },
      redirectUrls: {
        successUrl: required('ZAINCASH_SUCCESS_URL'),
        failureUrl: required('ZAINCASH_FAILURE_URL')
      }
    };
    if (phone) payload.customer = { phone };

    const response = await fetch(`${ZAIN_BASE}/api/v2/payment-gateway/transaction/init`, {
      method: 'POST',
      headers: { authorization: `Bearer ${token}`, 'content-type': 'application/json' },
      body: JSON.stringify(payload)
    });
    const data = await response.json().catch(() => ({}));
    if (!response.ok) {
      console.error('zaincash_init_failed', response.status, data);
      return res.status(502).json({ error: 'zaincash_unavailable' });
    }
    res.json({ transactionId: data.transactionId, redirectUrl: data.redirectUrl, externalReferenceId: payload.externalReferenceId });
  } catch (error) {
    console.error('zaincash_init_error', error?.message || error);
    res.status(502).json({ error: 'zaincash_unavailable' });
  }
});

app.get('/v1/zaincash/payments/:transactionId', requireUser, async (req, res) => {
  try {
    const id = String(req.params.transactionId || '');
    if (!/^[0-9a-f-]{36}$/i.test(id)) return res.status(400).json({ error: 'invalid_transaction' });
    const token = await zainToken();
    const response = await fetch(`${ZAIN_BASE}/api/v2/payment-gateway/transaction/inquiry/${encodeURIComponent(id)}`, {
      headers: { authorization: `Bearer ${token}` }
    });
    const data = await response.json().catch(() => ({}));
    if (!response.ok) return res.status(502).json({ error: 'zaincash_unavailable' });
    res.json(data);
  } catch (error) {
    console.error('zaincash_inquiry_error', error?.message || error);
    res.status(502).json({ error: 'zaincash_unavailable' });
  }
});

function verifyZainJwt(token) {
  return jwt.verify(token, required('ZAINCASH_API_KEY'), { algorithms: ['HS256'] });
}

app.get('/v1/zaincash/redirect/success', (req, res) => {
  try {
    const payload = verifyZainJwt(String(req.query.token || ''));
    res.status(200).send(`<!doctype html><meta charset="utf-8"><title>Flosi</title><body dir="rtl" style="font-family:sans-serif;padding:32px"><h2>تمت العملية</h2><p>يمكنك الرجوع إلى Flosi.</p><script>setTimeout(()=>location.href='flosi://zaincash/success',400)</script></body>`);
    console.log('zaincash_success', payload?.transactionId || payload?.data?.transactionId || 'verified');
  } catch {
    res.status(400).send('Invalid callback');
  }
});

app.get('/v1/zaincash/redirect/failure', (req, res) => {
  try { if (req.query.token) verifyZainJwt(String(req.query.token)); } catch { /* UX-only failure redirect */ }
  res.status(200).send(`<!doctype html><meta charset="utf-8"><title>Flosi</title><body dir="rtl" style="font-family:sans-serif;padding:32px"><h2>لم تكتمل العملية</h2><p>يمكنك الرجوع إلى Flosi والمحاولة مجدداً.</p></body>`);
});

app.post('/v1/zaincash/webhook', (req, res) => {
  try {
    const payload = verifyZainJwt(String(req.body?.webhook_token || ''));
    // Production persistence must use eventId as an idempotency key.
    console.log('zaincash_webhook', payload?.eventId || 'verified', payload?.data?.currentStatus || 'unknown');
    res.sendStatus(200);
  } catch {
    res.sendStatus(400);
  }
});

const port = Number(process.env.PORT || 8080);
app.listen(port, () => console.log(`Flosi backend listening on ${port}`));
