
package com.getwala.aws.pinpoint;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.pinpoint.PinpointConfiguration;
import com.amazonaws.mobileconnectors.pinpoint.PinpointManager;
import com.amazonaws.mobileconnectors.pinpoint.analytics.AnalyticsEvent;
import com.amazonaws.mobileconnectors.pinpoint.analytics.monetization.CustomMonetizationEventBuilder;
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

    private String[] requiredAttributesForMonetization = {"currency", "itemPrice", "productId", "quantity"};

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

                // add attributes and metrics
                if (attributes != null) {
                    ReadableMapKeySetIterator attributeIterator = attributes.keySetIterator();
                    while (attributeIterator.hasNextKey()) {
                        String key = attributeIterator.nextKey();
                        analyticsEvent = analyticsEvent.withAttribute(key, attributes.getString(key));
                    }
                }

                if (metrics != null) {
                    ReadableMapKeySetIterator metricsIterator = metrics.keySetIterator();
                    while (metricsIterator.hasNextKey()) {
                        String key = metricsIterator.nextKey();
                        analyticsEvent = analyticsEvent.withMetric(key, metrics.getDouble(key));
                    }
                }

                mPinpointManager.getAnalyticsClient().recordEvent(analyticsEvent);
                mPinpointManager.getAnalyticsClient().submitEvents();
                promise.resolve(true);
            }
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