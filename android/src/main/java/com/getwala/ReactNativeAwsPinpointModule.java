
package com.getwala;


import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.pinpoint.PinpointConfiguration;
import com.amazonaws.mobileconnectors.pinpoint.PinpointManager;
import com.amazonaws.mobileconnectors.pinpoint.analytics.AnalyticsEvent;
import com.amazonaws.mobileconnectors.pinpoint.analytics.monetization.GooglePlayMonetizationEventBuilder;
import com.amazonaws.regions.Regions;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.WritableMap;

public class ReactNativeAwsPinpointModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;
    private final PinpointManager mPinpointManager;

    public ReactNativeAwsPinpointModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;

        CognitoCachingCredentialsProvider cognitoCachingCredentialsProvider =
                new CognitoCachingCredentialsProvider(reactContext.getApplicationContext(), "IDENTITY_POOL_ID", Regions.US_EAST_1);

        PinpointConfiguration config =
                new PinpointConfiguration(reactContext.getApplicationContext(), "APP_ID", Regions.US_EAST_1, cognitoCachingCredentialsProvider);

        mPinpointManager = new PinpointManager(config); // can take a few seconds
    }

    @Override
    public String getName() {
        return "ReactNativeAwsPinpoint";
    }


    /**
     * Record a monetization event
     *
     * @param {String}  COST
     * @param {String}  PRODUCT_ID
     * @param {Promise} Promise
     */
    @ReactMethod
    public void generateMonetizationEvent(
            String cost,
            String productId,
            Promise promise
    ) {

        final AnalyticsEvent event = GooglePlayMonetizationEventBuilder.create(mPinpointManager.getAnalyticsClient())
                .withFormattedItemPrice(cost)
                .withQuantity(1.0)
                .withTransactionId(productId)
                .withProductId(productId).build();

        mPinpointManager.getAnalyticsClient().recordEvent(event);
        mPinpointManager.getAnalyticsClient().submitEvents();

        WritableMap map = Arguments.createMap();
        map.putString("result", "ok");
        promise.resolve(map);
    }

    /**
     * Record a custom event
     *
     * @param {String}      eventType
     * @param {ReadableMap} attributes
     * @param {ReadableMap} metrics
     */
    @ReactMethod
    public void recordEvent(
        String eventType,
        ReadableMap attributes,
        ReadableMap metrics
    ) {
        AnalyticsEvent event = mPinpointManager.getAnalyticsClient().createEvent(eventType);

        ReadableMapKeySetIterator attributeIterator = attributes.keySetIterator();
        while(attributeIterator.hasNextKey()) {
            String key = attributeIterator.nextKey();
            event = event.withAttribute(key, attributes.getString(key));
        }

        ReadableMapKeySetIterator metricsIterator = metrics.keySetIterator();
        while(metricsIterator.hasNextKey()) {
            String key = metricsIterator.nextKey();
            event = event.withMetric(key, metrics.getDouble(key));
        }

        mPinpointManager.getAnalyticsClient().recordEvent(event);
        mPinpointManager.getAnalyticsClient().submitEvents();
    }
}