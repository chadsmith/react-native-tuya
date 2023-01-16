# react-native-tuya

This is a fork of [tuya/tuya-home-sdk-react-native](https://github.com/tuya/tuya-home-sdk-react-native) combined with some inspiration from [Volst/react-native-tuya](https://github.com/Volst/react-native-tuya).

Some features are not implemented, feel free to send a PR for those missing features. Unfortunately there is no demo yet

## Feature Overview

Tuya Smart APP SDK provides the interface package for the communication with hardware and Tuya Cloud to accelerate the application development process, including the following features:

Hardware functions (network configuration, control, status reporting, regular tasks, groups, firmware upgrades, sharing)
Account system (phone number, email registration, login, password reset and other general account functions)
Tuya Cloud HTTP API interface package

## Getting started

```
npm install react-native-tuya
```

This library contains native code which is automatically linked in React Native >= 0.59. For iOS, run `cd ios && pod install`.

## Installation

In the Tuya development environment create a new app and make sure you have an "App key", "App secret" and "Secure image". [Read how to do this](https://tuyainc.github.io/tuyasmart_home_ios_sdk_doc/en/resource/Preparation.html).

### iOS

Put the secure image into the root path of your project as [explained here](https://tuyainc.github.io/tuyasmart_home_ios_sdk_doc/en/resource/Preparation.html).

In `ios/AppDelegate.m`, add the following import;

```obj-c
#import <TuyaSmartHomeKit/TuyaSmartKit.h>
```

Then, under the `roootView.backgroundColor` line in the same file, add this:

```obj-c
  #ifdef DEBUG
    [[TuyaSmartSDK sharedInstance] setDebugMode:YES];
  #endif

  [[TuyaSmartSDK sharedInstance] startWithAppKey:@"xxx" secretKey:@"xxx"];
```

Now replace the `xxx` with your app key and secret key.

### Android

Assuming you already have created an app in the Tuya development environment (otherwise follow the iOS steps before this), follow [these steps](https://tuyainc.github.io/tuyasmart_home_android_sdk_doc/en/resource/Integrated.html#3-integrated-security-image). You should now have an app key, app secret and security image for Android. Make sure the security image is put in `android/src/main/assets/t_s.bmp`.

Open your `AndroidManifest.xml` and put the following **in the `<application>` tag**:

```xml
<meta-data
  android:name="TUYA_SMART_APPKEY"
  android:value="xxx" />
<meta-data
  android:name="TUYA_SMART_SECRET"
  android:value="xxx" />
```

Replace the `xxx` with your app key and secret key.

Now open `MainApplication.java` and add the following import to the top:

```java
import com.tuya.smart.rnsdk.core.TuyaCoreModule;
```

Change the `onCreate` function to look like this:

```java
@Override
public void onCreate() {
  super.onCreate();
  SoLoader.init(this, /* native exopackage */ false);
  initializeFlipper(this); // Remove this line if you don't want Flipper enabled
  TuyaCoreModule.initWithoutOptions();
}
```

Now you can try to build, but you'll probably run into an error saying that it can't choose between `libc++_shared` or something. One fix for this (don't know if it's the best fix) is to open `android/app/build.gradle` and add this;

```
android {
    ...
    packagingOptions {
        pickFirst '**/armeabi-v7a/libc++_shared.so'
        pickFirst '**/x86/libc++_shared.so'
        pickFirst '**/arm64-v8a/libc++_shared.so'
        pickFirst '**/x86_64/libc++_shared.so'
        pickFirst '**/x86/libjsc.so'
        pickFirst '**/armeabi-v7a/libjsc.so'
    }
}
```

## Usage

Now you can actually use the methods in this package. Unfortunately I don't have time to document them all, so it is advised to read the source code, but here's a start.

To login with an existing account:

```js
import { TuyaUserApi } from 'react-native-tuya';

await TuyaUserApi.loginWithEmail({
  countryCode: '+1',
  email: 'you@example.com',
  password: 'testtest'
});
```

To register a new account you first need to validate the email address. And then actually register using the code in the email.

```js
import { TuyaUserApi } from 'react-native-tuya';

await TuyaUserApi.getRegisterEmailValidateCode({
  countryCode: '+1',
  email: 'you@example.com'
});

...

await TuyaUserApi.registerAccountWithEmail({
  countryCode: '+1',
  email: 'you@example.com',
  password: 'testtest',
  validateCode: 'xxxxxx'
})
```

To get the currently logged in user:

```js
import { TuyaUserApi } from 'react-native-tuya';

const user = await TuyaUserApi.getCurrentUser();
```
