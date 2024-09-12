# WebAuthnDemo Kotlin

## Introduction

Welcome to the WebAuthnDemo Kotlin! This sample application demonstrates how to integrate and utilize the [webauthn-kotlin SDK](https://github.com/line/webauthn-kotlin.git) in an Android application. It provides practical examples of using the SDK for implementing strong, password-less authentication using both biometric and device credential authenticators.

## Features

- Demonstrates the use of `Biometric` and `DeviceCredential`.
- Includes examples of registering and authenticating credentials.
- Provides an example implementation of the `RelyingParty` interface.

## Prerequisites

Before you begin, ensure you have the following installed:

- Gradle >= 7.6.3
- Android API level >= 28

## Getting Started

To get started with the webauthndemo-kotlin, follow these steps:

### 1. Clone the git repositories:

```bash
# webauthndemo-kotlin
$ git clone https://github.com/line/webauthndemo-kotlin.git
# webauthn-kotlin
$ git clone https://github.com/line/webauthn-kotlin.git
```

### 2. Build the SDK using Gradle to create AAR file:

1. Open `webauthn-kotlin` in Android Studio.
2. Open `Run Anything` (Double `Ctrl`) or Gradle - Execute Gradle Task.
3. Run `gradle assembleRelease` task.

Or you can use the command line:

```bash
$ cd webauthn-kotlin
$ ./gradlew assembleRelease
```

### 3. Publish the AAR file to the local Maven repository:

1. Open `Run Anything` (Double `Ctrl`) or Gradle - Execute Gradle Task.
2. Run `gradle publishToMavenLocal` task.

Or you can use the command line:

```bash
$ ./gradlew publishToMavenLocal
```

### 4. Launch a WebAuthn server.

We use the FIDO2 server [here](https://github.com/line/line-fido2-server) to run
the sample application. Please refer to the [README](https://github.com/line/line-fido2-server/blob/main/README.md#how-to-run)
for instructions on how to run the server.

When setting up the server, you need to register the `origin` for your Android application. The `origin` is derived from the `facetId` as described in the [FIDO AppID and Facet Specification](https://fidoalliance.org/specs/common-specs/fido-appid-and-facets-v2.1-ps-20220523.html#processing-rules-for-appid-and-facetid-assertions) for the Android case.

#### How to Obtain Facet ID for FIDO2 Server Registration

##### Debug Mode

To obtain the `facet ID` in `debug` mode, use the following command:

```sh
keytool -exportcert \
    -alias androiddebugkey \
    -keystore $HOME/.android/debug.keystore \
    -rfc | \
    openssl x509 -inform PEM -outform DER | \
    openssl dgst -sha256 -binary | \
    openssl base64 | \
    tr -d '=' | \
    tr -d '\n'
```
##### Release Mode

To obtain the facet ID in release mode, you need to replace the alias and keystore with your release key alias and keystore file. Use the following command:

```sh
keytool -exportcert \
    -alias your_release_key_alias \
    -keystore path/to/path_of_your-release-key.jks \
    -rfc | \
    openssl x509 -inform PEM -outform DER | \
    openssl dgst -sha256 -binary | \
    openssl base64 | \
    tr -d '=' | \
    tr -d '\n'
```

For instructions on creating `keystore.jks` / `keystore.properties` for release mode, please refer to the [Additional configuration](#additional-configuration) section below.


### 5. Change the domain of relying party in [`RetrofitClient.kt`](./app/src/main/java/com/lycorp/webauthn/sample/network/RetrofitClient.kt).

1. Open `webauthndemo-kotlin` in Android Studio.
2. Open [`RetrofitClient.kt`](./app/src/main/java/com/lycorp/webauthn/sample/network/RetrofitClient.kt) file.
3. Replace the domain `"https://example.com"` with the server domain you want to test.

```kotlin
private val BASE_URL_FIDO2 = "https://example.com"  <-  replace here
```

### 6. Install and run the application on your android device:

1. From the top menu, select `File` > `Sync Project with Gradle Files`.
2. Connect your Android device to your computer and run the application.

## Additional Configuration
### 1. Create `keystore.jks` and set up `keystore.properties` for release

To sign your application for release, you need to create a `keystore.jks` file and set up a `keystore.properties` file.
This step is optional and only necessary if you are planning to release your application.

#### 1-1. Create `keystore.jks` file.
You can use Android Studio to create a new keystore file:

1. Open Android Studio.
2. Click on `Build` > `Generate Signed Bundle / APK`.
3. Click on `Create new...` to create a new keystore file.
4. Fill in the required fields and click `OK`.

Or, Use the `keytool` command to create a new keystore file. Open a terminal and run the following command:

```bash
keytool -genkeypair -v -keystore my-release-key.jks -alias my-key-alias -keyalg RSA -keysize 2048 -validity 10000
```

#### 1-2. Create `keystore.properties` file:
Create a new file named `keystore.properties` in the root directory of the project.
Add the following lines to the file:

```properties
storeFile=path_of_your_keystore.jks
storePassword=your_store_password
keyAlias=your_key_alias
keyPassword=your_key_password
```

Now, `build.gradle(app)` automatically reads the `keystore.properties` file to sign the application for release.

## Usage

The webauthndemo-kotlin is designed to demonstrate the functionality of the [webauthn-kotlin SDK](https://github.com/line/webauthn-kotlin.git) through a user-friendly interface. Below are the key features of the app and how to use them:

### SIGN UP and SIGN IN Buttons
- **SIGN UP**: Tapping this button initiates the registration process, allowing you to create a new user credential. This is essential for setting up a new account within the app.
- **SIGN IN**: This button is used for authentication. Once you have registered, you can use this button to log in using the credentials you have created.

### Account Management
- **SHOW ACCOUNT**: This option displays all the credentials that have been registered on the device. It is useful for viewing the details of all accounts created.
- **DELETE ACCOUNTS**: Use this button to remove all stored credentials from the device. This is helpful for clearing all data and resetting the app.

### LOG WINDOW
- The log window automatically appears when you engage in actions such as signing up or signing in. It can also be manually opened by clicking the **OPEN LOG WINDOW** button.
- **CLEAR**: This button clears all the content in the log window, allowing you to start fresh with a new log session.
- **CLOSE LOG WINDOW**: This button closes the log window. You can use this to hide the log details from the screen.
- The log window displays messages based on the operation results:
    - If an operation is successful, a success message will be shown.
    - If an operation fails, the log window will display the corresponding exception message, providing insight into what went wrong.

These features collectively showcase the capabilities of the WebAuthn Kotlin SDK, making it easier for developers and testers to understand and evaluate the implementation of FIDO2-based authentication in a real-world scenario.

## Screenshots

### Biometric Authenticator

<img src="./images/Biometric.gif" width="300" align="center" alt="biometric"/>

### Device Credential Authenticator

You can use it in the same way as Biometric if you select "Use Device Credential" on the first screen.

## License
Apache License 2.0. See [`LICENSE`](./LICENSE).

## Contact Information

We are dedicated to making our work open-source to assist with your specific needs. We are eager to learn how this library is being utilized and the issues it resolves for you. To communicate, we recommend the following approach:

*   For reporting bugs, proposing improvements, or asking questions about the library, please utilize the [**Issues**](https://github.com/line/webauthndemo-kotlin/issues) section of our GitHub repository. Your feedback is invaluable in helping us address your concerns more effectively and enhances the community's experience.

Please avoid sharing any sensitive or confidential information in the issues. If there is a need to discuss sensitive matters, please indicate so in your issue, and we will arrange a more secure communication method.
