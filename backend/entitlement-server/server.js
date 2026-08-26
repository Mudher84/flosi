import express from 'express';
import admin from 'firebase-admin';
import { google } from 'googleapis';
import crypto from 'node:crypto';

admin.initializeApp();
const db = admin.firestore();
const auth = admin.auth();
const androidPublisher = google.androidpublisher('v3');
const app = express();
app.use(express.json({ limit: '64kb' }));

const PACKAGE_NAME = process.env.FLOSI_PACKAGE_NAME || 'com.flosi.app';
const TRIAL_DAYS = Number(process.env.FLOSI_TRIAL_DAYS || '30');
const TRIAL_MS = TRIAL_DAYS * 86_400_000;
const PRODUCT_IDS = new Set([
  process.env.FLOSI_MONTHLY_PRODUCT_ID || 'flosi_monthly',
  process.env.FLOSI_ANNUAL_PRODUCT_ID || 'flosi_annual'
]);

function sha256(value) {
  return crypto.createHash('sha256').update(String(value)).digest('hex');
}

async function requireUser(req) {
  const header = req.get('authorization') || '';
  if (!header.startsWith('Bearer ')) throw Object.assign(new Error('unauthorized'), { status: 401 });
  return auth.verifyIdToken(header.slice(7), true);
}

async function verifyPlaySubscription(purchaseToken) {
  if (!purchaseToken) return { active: false };
  const googleAuth = new google.auth.GoogleAuth({
    scopes: ['https://www.googleapis.com/auth/androidpublisher']
  });
  const client = await googleAuth.getClient();
  const result = await androidPublisher.purchases.subscriptionsv2.get({
    auth: client,
    packageName: PACKAGE_NAME,
    token: purchaseToken
  });
  const data = result.data || {};
  const allowedProduct = (data.lineItems || []).some(line => PRODUCT_IDS.has(line.productId));
  const activeStates = new Set([
    'SUBSCRIPTION_STATE_ACTIVE',
    'SUBSCRIPTION_STATE_IN_GRACE_PERIOD'
  ]);
  return {
    active: allowedProduct && activeStates.has(data.subscriptionState),
    expiryTime: (data.lineItems || []).map(x => x.expiryTime).filter(Boolean).sort().at(-1) || null
  };
}

async function getOrCreateTrial(deviceId, uid) {
  if (!/^[a-f0-9]{64}$/i.test(deviceId || '')) throw Object.assign(new Error('invalid device'), { status: 400 });
  const deviceKey = sha256(`flosi-trial-v1|${deviceId}`);
  const deviceRef = db.collection('trialDevices').doc(deviceKey);
  const userRef = db.collection('trialUsers').doc(uid);
  return db.runTransaction(async tx => {
    const [deviceSnap, userSnap] = await Promise.all([tx.get(deviceRef), tx.get(userRef)]);
    const now = Date.now();
    let trialStart;
    if (deviceSnap.exists) {
      trialStart = Number(deviceSnap.data().trialStart || now);
    } else if (userSnap.exists) {
      trialStart = Number(userSnap.data().trialStart || now);
      tx.set(deviceRef, { trialStart, firstUid: uid, createdAt: admin.firestore.FieldValue.serverTimestamp() });
    } else {
      trialStart = now;
      tx.set(deviceRef, { trialStart, firstUid: uid, createdAt: admin.firestore.FieldValue.serverTimestamp() });
    }
    if (!userSnap.exists) {
      tx.set(userRef, { trialStart, firstDeviceKey: deviceKey, createdAt: admin.firestore.FieldValue.serverTimestamp() });
    } else {
      const userStart = Number(userSnap.data().trialStart || trialStart);
      trialStart = Math.min(trialStart, userStart);
    }
    return { trialStart, trialEndsAt: trialStart + TRIAL_MS, now };
  });
}

app.post('/v1/entitlement', async (req, res) => {
  try {
    const decoded = await requireUser(req);
    const deviceId = String(req.body?.deviceId || '');
    const purchaseToken = typeof req.body?.purchaseToken === 'string' ? req.body.purchaseToken : null;

    let play = { active: false };
    if (purchaseToken) {
      play = await verifyPlaySubscription(purchaseToken);
      const tokenHash = sha256(purchaseToken);
      await db.collection('purchaseTokens').doc(tokenHash).set({
        uid: decoded.uid,
        active: play.active,
        checkedAt: admin.firestore.FieldValue.serverTimestamp()
      }, { merge: true });
    }

    const trial = await getOrCreateTrial(deviceId, decoded.uid);
    res.json({
      active: Boolean(play.active),
      trialEndsAt: trial.trialEndsAt,
      serverNow: trial.now,
      subscriptionExpiry: play.expiryTime || null
    });
  } catch (error) {
    console.error(error);
    res.status(error.status || 500).json({ error: error.message || 'server_error' });
  }
});

app.get('/health', (_req, res) => res.json({ ok: true }));

const port = Number(process.env.PORT || 8080);
app.listen(port, () => console.log(`Flosi entitlement server listening on ${port}`));
