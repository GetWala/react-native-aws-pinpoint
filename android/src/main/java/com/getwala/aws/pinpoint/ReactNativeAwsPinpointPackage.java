
package com.getwala.aws.pinpoint;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.facebook.react.*;
import com.facebook.react.BuildConfig;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;
import com.facebook.react.bridge.JavaScriptModule;

public class ReactNativeAwsPinpointPackage implements ReactPackage {

    private final String appId;
    private final String identityPoolId;
    private final String region;

    public ReactNativeAwsPinpointPackage(String appId, String identityPoolId, String region) {
        this.appId = appId;
        this.identityPoolId = identityPoolId;
        this.region = region;
    }

    @Override
    public List<NativeModule> createNativeModules(ReactApplicationContext reactContext) {
      return Arrays.<NativeModule>asList(new ReactNativeAwsPinpointModule(reactContext, appId, identityPoolId, region));
    }

    public List<Class<? extends JavaScriptModule>> createJSModules() {
      return Collections.emptyList();
    }

    @Override
    public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {
      return Collections.emptyList();
    }
}