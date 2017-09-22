
package com.getwala.aws.pinpoint;

import android.util.Log;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.pinpoint.PinpointCallback;
import com.amazonaws.mobileconnectors.pinpoint.PinpointConfiguration;
import com.amazonaws.mobileconnectors.pinpoint.PinpointManager;
import com.amazonaws.mobileconnectors.pinpoint.analytics.AnalyticsEvent;
import com.amazonaws.mobileconnectors.pinpoint.analytics.monetization.CustomMonetizationEventBuilder;
import com.amazonaws.regions.Regions;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.UnexpectedNativeTypeException;

public class ReactNativeAwsPinpointModule extends ReactContextBaseJavaModule implements LifecycleEventListener {

    private final String appId;
    private final String identityPoolId;
    private final String region;

    private final ReactApplicationContext reactContext;
    private static PinpointManager mPinpointManager;

    private boolean sessionIsStarted = false;
    private boolean initializing = false;

    private String[] requiredAttributesForMonetization = {"currency", "itemPrice", "productId", "quantity"};

    public ReactNativeAwsPinpointModule(ReactApplicationContext reactContext, String appId, String identityPoolId, String region) {
        super(reactContext);
        this.reactContext = reactContext;

        this.appId = appId;
        this.identityPoolId = identityPoolId;
        this.region = region;

        reactContext.addLifecycleEventListener(this);
    }


    @Override
    public String getName() {
        return "ReactNativeAwsPinpoint";
    }


    private void initializeWithSettings(PinpointCallback callback) {
        try {
            initializing = true;
            Regions regionValue = Regions.fromName(region);

            CognitoCachingCredentialsProvider cognitoCachingCredentialsProvider =
                    new CognitoCachingCredentialsProvider(reactContext.getApplicationContext(), identityPoolId, regionValue);

            PinpointConfiguration config =
                    new PinpointConfiguration(reactContext.getApplicationContext(), appId, regionValue, cognitoCachingCredentialsProvider)
                    .withInitCompletionCallback(callback);

            mPinpointManager = new PinpointManager(config); // can take a few seconds
        } catch (AmazonClientException ace) {
            Log.e("RNAwsPinpointModule", "Something went wrong with initializing the module." + ace.toString());
            initializing = false;
        }
    }

    @ReactMethod
    public void pauseSession(Promise promise) {
        if (mPinpointManager != null) {
            mPinpointManager.getSessionClient().pauseSession();
            mPinpointManager.getAnalyticsClient().submitEvents();
            promise.resolve(true);
        } else {
            promise.reject(new Exception("ReactNativeAwsPinpointModule should be initialized first"));
        }
    }

    @ReactMethod
    public void resumeSession(Promise promise) {
        if (mPinpointManager != null) {
            mPinpointManager.getSessionClient().resumeSession();
            promise.resolve(true);
        } else {
            promise.reject(new Exception("ReactNativeAwsPinpointModule should be initialized first"));
        }
    }

    @ReactMethod
    public void submitEvents(Promise promise) {
        if (mPinpointManager != null) {
            mPinpointManager.getAnalyticsClient().submitEvents();
            promise.resolve(true);
        } else {
            promise.reject(new Exception("ReactNativeAwsPinpointModule should be initialized first"));
        }
    }

    /**
     * Record a montetization event, immediately submits events
     *
     * @param eventDetails requires currency (String), itemPrice (Double), productId (String) and quantity (Double)
     */
    @ReactMethod
    public void recordMonetizationEvent(
            ReadableMap eventDetails,
            ReadableMap attributes,
            ReadableMap metrics,
            Promise promise
    ) {
        if (mPinpointManager != null) {

            // validate input
            boolean missingRequiredKey = false;
            for (String s : requiredAttributesForMonetization) {
                if (!eventDetails.hasKey(s)) {
                    missingRequiredKey = true;
                }
            }

            if (missingRequiredKey) {
                promise.reject(new Exception("ReactNativeAwsPinpointModule.recordMonetizationEvent requires currency, itemPrice, productId and quantity"));
            } else {

                CustomMonetizationEventBuilder builder = CustomMonetizationEventBuilder
                        .create(mPinpointManager.getAnalyticsClient())
                        .withStore("Virtual")
                        .withCurrency(eventDetails.getString("currency"))
                        .withItemPrice(eventDetails.getDouble("itemPrice"))
                        .withProductId(eventDetails.getString("productId"))
                        .withQuantity(eventDetails.getDouble("quantity"));

                if (eventDetails.hasKey("transactionId"))
                    builder.withTransactionId(eventDetails.getString("transactionId"));

                AnalyticsEvent analyticsEvent = builder.build();

                setAttributes(analyticsEvent, attributes);
                setMetrics(analyticsEvent, metrics);

                mPinpointManager.getAnalyticsClient().recordEvent(analyticsEvent);
                promise.resolve(true);
            }
        } else {
            promise.reject(new Exception("ReactNativeAwsPinpointModule should be initialized first"));
        }
    }

    private void setMetrics(AnalyticsEvent analyticsEvent, ReadableMap metrics) {
        if (metrics != null && analyticsEvent != null) {
            ReadableMapKeySetIterator metricsIterator = metrics.keySetIterator();
            while (metricsIterator.hasNextKey()) {
                String key = metricsIterator.nextKey();
                Double value = 0d;
                try {
                    value = metrics.getDouble(key);
                } catch (UnexpectedNativeTypeException ex) {
                    Log.d("RNAwsPinpointModule", "UnexpectedNativeTypeException when attempting to getDouble with key: " + key);
                    continue;
                }
                analyticsEvent = analyticsEvent.withMetric(key, value);
            }
        }
    }

    private void setAttributes(AnalyticsEvent analyticsEvent, ReadableMap attributes) {
        if (attributes != null && analyticsEvent != null) {
            ReadableMapKeySetIterator attributeIterator = attributes.keySetIterator();
            while (attributeIterator.hasNextKey()) {
                String key = attributeIterator.nextKey();
                String value = null;
                try {
                    value = attributes.getString(key);
                } catch (UnexpectedNativeTypeException ex) {
                    Log.d("RNAwsPinpointModule", "UnexpectedNativeTypeException when attempting to getString with key: " + key);
                    continue;
                }
                analyticsEvent = analyticsEvent.withAttribute(key, value);
            }
        }
    }

    /**
     * Record a custom event
     */
    @ReactMethod
    public void recordCustomEvent(
            String eventType,
            ReadableMap attributes,
            ReadableMap metrics,
            Promise promise
    ) {
        if (mPinpointManager != null) {
            AnalyticsEvent event = mPinpointManager.getAnalyticsClient().createEvent(eventType);

            setAttributes(event, attributes);
            setMetrics(event, metrics);

            mPinpointManager.getAnalyticsClient().recordEvent(event);
            promise.resolve(true);
        } else {
            promise.reject(new Exception("ReactNativeAwsPinpointModule should be initialized first"));
        }
    }

    private void startSession(){
        if (mPinpointManager == null) {
            if (initializing) return;
            this.initializeWithSettings(new PinpointCallback<PinpointManager>() {
                @Override
                public void onComplete(PinpointManager manager) {
                    manager.getSessionClient().startSession();
                    sessionIsStarted = true;
                }
            });
        } else {
            if (sessionIsStarted) return;
            mPinpointManager.getSessionClient().startSession();
            sessionIsStarted = true;
        }
    }

    @Override
    public void onHostResume() {
       startSession();
    }

    @Override
    public void onHostPause() {
        if (mPinpointManager != null && sessionIsStarted) {
            mPinpointManager.getSessionClient().stopSession();
            mPinpointManager.getAnalyticsClient().submitEvents();
            sessionIsStarted = false;
        }
    }

    @Override
    public void onHostDestroy() {
        if (mPinpointManager != null && sessionIsStarted) {
            mPinpointManager.getSessionClient().stopSession();
            mPinpointManager.getAnalyticsClient().submitEvents();
            sessionIsStarted = false;
        }
    }
}