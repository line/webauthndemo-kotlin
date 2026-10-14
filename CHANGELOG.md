# Change Log

## 1.1.4 (2026-10-14)

### Added
- Run without a FIDO2 server: when no server URL is configured, the app uses `FakeRelyingParty`, which issues challenges and checks that each response carries the one it issued. The title bar of the main screen shows which relying party is in use.

### Changed
- Update webauthn-kotlin dependency from v1.1.2 to v1.1.4
- The FIDO2 server URL is set as `fido2.rpBaseUrl` in `local.properties` or as a Gradle property, instead of in `RetrofitClient.kt`. See step 6 of Getting Started in the README.

### Fixed
- Read the registration and authentication options from `credentialCreationOptions.publicKey` and `credentialRequestOptions.publicKey` in the server response, where line-fido2-server returns them. Reading them from the top level of the response failed every ceremony with `RpException`.
- Skip credential types and COSE algorithms in `pubKeyCredParams` that the SDK has no constant for, instead of failing registration.

## 1.1.2 (2025-09-12)

### Changed
- Update webauthn-kotlin dependency from v1.1.0 to v1.1.2 (skipping v1.1.1)
- Fix build issues related to updated library version
- change groupId from `jp.co.lycorp` to `com.linecorp`