# react-native-slang

## Getting started

`$ npm install react-native-slang --save`

### Mostly automatic installation

`$ react-native link react-native-slang`

### Manual installation

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.reactlibrary.RNSlangPackage;` to the imports at the top of the file
  - Add `new RNSlangPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-slang'
  	project(':react-native-slang').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-slang/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-slang')
  	```

#### iOS (not supported currently)

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-slang` and add `RNSlang.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNSlang.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

#### Windows (not supported currently)
[Read it! :D](https://github.com/ReactWindows/react-native)

1. In Visual Studio add the `RNSlang.sln` in `node_modules/react-native-slang/windows/RNSlang.sln` folder to their solution, reference from their app.
2. Open up your `MainPage.cs` app
  - Add `using Slang.RNSlang;` to the usings at the top of the file
  - Add `new RNSlangPackage()` to the `List<IReactPackage>` returned by the `Packages` method


## Usage
```javascript
import {NativeModules} from 'react-native';

const {SlangBuddy} = NativeModules;

// TODO: What to do with the module?
SlangBuddy;
```
  
