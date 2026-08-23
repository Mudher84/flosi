# Flosi Final Android Validation

Date: 2026-08-23

## Result

**PASS**

The final validation workflow ran on Ubuntu with Java 17 and Gradle 8.13, matching the repository wrapper.

Validated tasks:

- `testDebugUnitTest` — PASS
- `assembleDebug` — PASS
- Debug APK artifact upload — PASS

## Fix discovered by the final build

The first validation run failed at Kotlin compilation because `AccountEditScreen.kt` used `6.dp` without importing `androidx.compose.ui.unit.dp`.

The missing import was fixed on `main` in commit:

`87b7abdb0fd81b461c2fb804766751662818682c`

The validation PR was rerun after the fix and completed successfully.

## Validation run

- Workflow: `Android Debug Build`
- Run ID: `32652206876`
- Job: `build` — SUCCESS
- Unit tests — SUCCESS
- Debug APK build — SUCCESS
- Artifact: `flosi-debug-apk`
- Artifact ID: `9496522654`
- Artifact size: `19,919,922` bytes
- Artifact SHA-256 digest: `fe4d26ce0bfd30241581d05f4e42e58813988b2fc476f256e604653a1d51a3b8`

## Financial test coverage included

- Invoice fractional quantities and rounding.
- Discount, tax, paid amount and remaining balance consistency.
- Rejection of invalid discounts and overpayments.
- Direct currency conversion.
- Reverse currency conversion.
- Two-hop currency conversion.
- Missing exchange-rate fail-closed behavior.
- Invalid exchange-rate rejection.

The temporary validation PR was closed without merge after the successful run. The one-time audit workflow was removed after validation.
