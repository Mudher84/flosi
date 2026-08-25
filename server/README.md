# Flosi Backend

Secure server-side layer for Google Play entitlement verification and ZainCash Payment Gateway v2.

## What is implemented

- Firebase ID-token authentication for app-to-server calls.
- Google Play subscription verification for `flosi_monthly` through Android Publisher API.
- ZainCash OAuth2 client-credentials token handling.
- ZainCash payment init, inquiry, signed redirect verification, and signed webhook verification.
- No merchant secret, API key, or Google service-account secret is stored in the Android app or repository.

## Production activation

1. Deploy this `server/` directory on a Node.js 22 HTTPS host.
2. Configure the variables in `.env.example` as server secrets.
3. In Google Play Console create subscription product `flosi_monthly`, add a monthly base plan, activate it, and grant the service account access to the app/subscriptions API.
4. Set `GOOGLE_APPLICATION_CREDENTIALS` to the service-account file on the server only.
5. Finish ZainCash merchant onboarding and replace the UAT base URL/credentials with the production values supplied by ZainCash.
6. Register `/v1/zaincash/webhook` as the production notification URL with ZainCash. Use HTTPS.

ZainCash public v2 is a payment gateway. It does not expose a public consumer-wallet balance/transaction-reading API, so Flosi must not claim that it can sync a user's ZainCash wallet balance unless ZainCash provides a separate official account-data API during onboarding.

## Local validation

```bash
npm install
npm run check
npm start
```

`GET /health` should return `{ "ok": true, "service": "flosi-backend" }`.
