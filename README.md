
# react-native-aws-pinpoint

## Getting started

`$ npm install react-native-aws-pinpoint --save`

### Mostly automatic installation

`$ react-native link react-native-aws-pinpoint`

### Manual installation


#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-aws-pinpoint` and add `ReactNativeAwsPinpoint.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libReactNativeAwsPinpoint.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.getwala.ReactNativeAwsPinpointPackage;` to the imports at the top of the file
  - Add `new ReactNativeAwsPinpointPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-aws-pinpoint'
  	project(':react-native-aws-pinpoint').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-aws-pinpoint/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-aws-pinpoint')
  	```

#### Windows
[Read it! :D](https://github.com/ReactWindows/react-native)

1. In Visual Studio add the `ReactNativeAwsPinpoint.sln` in `node_modules/react-native-aws-pinpoint/windows/ReactNativeAwsPinpoint.sln` folder to their solution, reference from their app.
2. Open up your `MainPage.cs` app
  - Add `using com.getwala.ReactNativeAwsPinpoint;` to the usings at the top of the file
  - Add `new ReactNativeAwsPinpointPackage()` to the `List<IReactPackage>` returned by the `Packages` method


## Usage
```javascript
import ReactNativeAwsPinpoint from 'react-native-aws-pinpoint';

// TODO: What to do with the module?
ReactNativeAwsPinpoint;
```
  