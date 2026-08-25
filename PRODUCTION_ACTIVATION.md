# Flosi Production Activation

The application code is prepared for the remaining production services. These items require account-side credentials or approval and cannot be completed by source-code changes alone.

## Google Play subscription

- Product ID: `flosi_monthly`
- Create and activate a monthly base plan in Google Play Console.
- Configure price/countries and test accounts.
- Give a Google Cloud service account Android Publisher access to `com.flosi.app`.
- Deploy `server/` and set `GOOGLE_APPLICATION_CREDENTIALS` on the server.
- Configure Google Play Real-time Developer Notifications before public rollout.

## ZainCash

- Finish Business/Merchant onboarding.
- Obtain production `client_id`, `client_secret`, API key, service type, and production base URL.
- Put credentials only in the deployed `server/` environment.
- Register the HTTPS webhook URL `/v1/zaincash/webhook` with ZainCash.
- Never put merchant secrets in the Android APK.

ZainCash Payment Gateway v2 supports payments, inquiry, callbacks/webhooks and reversals. Public documentation does not provide consumer wallet balance/transaction synchronization; account-data synchronization must remain disabled until ZainCash supplies an official API for it.

## Banks

Only enable automatic bank synchronization for a bank/provider that supplies an official API/Open Banking authorization flow. Until then, use the existing statement import path rather than storing bank usernames/passwords.

## Languages

Arabic is the production baseline. Do not re-enable legacy mixed-language overlays. Add future languages from a single canonical translation source and validate each language before exposing it in settings.
