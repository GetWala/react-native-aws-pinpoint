
package com.getwala.aws.pinpoint;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.pinpoint.PinpointConfiguration;
import com.amazonaws.mobileconnectors.pinpoint.PinpointManager;
import com.amazonaws.mobileconnectors.pinpoint.analytics.AnalyticsEvent;
import com.amazonaws.mobileconnectors.pinpoint.analytics.monetization.GooglePlayMonetizationEventBuilder;
import com.amazonaws.regions.Regions;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;

public class ReactNativeAwsPinpointModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;
    private static PinpointManager mPinpointManager;

    public ReactNativeAwsPinpointModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }


    @Override
    public String getName() {
        return "ReactNativeAwsPinpoint";
    }

    @ReactMethod
    public void initialize(String appId, String identityPoolId, String region, Promise promise) {
        try {
            Regions regionValue = Regions.fromName(region);

            CognitoCachingCredentialsProvider cognitoCachingCredentialsProvider =
                    new CognitoCachingCredentialsProvider(reactContext.getApplicationContext(), identityPoolId, regionValue);

            PinpointConfiguration config =
                    new PinpointConfiguration(reactContext.getApplicationContext(), appId, regionValue, cognitoCachingCredentialsProvider);

            mPinpointManager = new PinpointManager(config); // can take a few seconds

            promise.resolve(true);
        } catch (AmazonClientException ace) {
            promise.reject(ace);
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
     * Record a montetization event
     */
    @ReactMethod
    public void recordMonetizationEvent(ReadableMap event, Promise promise) {
        if (mPinpointManager != null) {
            final AnalyticsEvent analyticsEvent =
                    GooglePlayMonetizationEventBuilder.create(mPinpointManager.getAnalyticsClient())
                            .withCurrency(event.getString("currency"))
                            .withItemPrice(event.getDouble("itemPrice"))
                            .withProductId(event.getString("productId"))
                            .withQuantity(event.getDouble("quantity"))
                            .withTransactionId(event.getString("transactionId"))
                            .build();
            mPinpointManager.getAnalyticsClient().recordEvent(analyticsEvent);
            promise.resolve(true);
        } else {
            promise.reject(new Exception("ReactNativeAwsPinpointModule should be initialized first"));
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

            if (attributes != null) {
                ReadableMapKeySetIterator attributeIterator = attributes.keySetIterator();
                while (attributeIterator.hasNextKey()) {
                    String key = attributeIterator.nextKey();
                    event = event.withAttribute(key, attributes.getString(key));
                }
            }

            if (metrics != null) {
                ReadableMapKeySetIterator metricsIterator = metrics.keySetIterator();
                while (metricsIterator.hasNextKey()) {
                    String key = metricsIterator.nextKey();
                    event = event.withMetric(key, metrics.getDouble(key));
                }
            }

            mPinpointManager.getAnalyticsClient().recordEvent(event);
            promise.resolve(true);
        } else {
            promise.reject(new Exception("ReactNativeAwsPinpointModule should be initialized first"));
        }
    }
}