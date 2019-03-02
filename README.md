# react-native-slang

Slang integration for React Native Apps built for Android.

## Getting started

`$ npm install react-native-slang --save`

### Automatic installation

`$ react-native link react-native-slang`

### Manual installation

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.reactlibrary.RNSlangPackage;` to the imports at the top of the file
  - Add `new RNSlangPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-slang'
  	project(':react-native-slang').projectDir = file('../node_modules/react-native-slang/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      implementation project(':react-native-slang')
  	```


## Usage

```javascript
import { Slang } from 'react-native-slang';

// Initialize slang
Slang.initialize(
      "<your buddy id>", 
      "<your api key>", 
      { 
        "locale": "LOCALE_ENGLISH_IN", // Other possible values: LOCALE_HINDI_IN, LOCALE_ENGLISH_US
        "position": "CENTER_BOTTOM", // Other possible values: LEFT_TOP, CENTER_TOP, RIGHT_TOP, CENTER, LEFT_BOTTOM, RIGHT_BOTTOM etc. 
      }, 
      () => { console.log("Slang initialized successfully") });

// Listen to slang intent actions. 
Slang.setIntentActionListener((action) => {
  console.log(action);
  Slang.notifyActionCompleted(true);
});
```

Go to https://docs.slanglabs.in/slang/ to know more about configuration. 
  
