
package in.slanglabs;

import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.Locale;

import in.slanglabs.platform.SlangBuddy;
import in.slanglabs.platform.SlangBuddyOptions;
import in.slanglabs.platform.SlangEntity;
import in.slanglabs.platform.SlangIntent;
import in.slanglabs.platform.SlangLocale;
import in.slanglabs.platform.SlangSession;
import in.slanglabs.platform.action.SlangAction;
import in.slanglabs.platform.action.SlangIntentAction;
import in.slanglabs.platform.ui.SlangBuiltinUI;

public class RNSlangBuddy extends ReactContextBaseJavaModule {

    private static final String TAG = "RNSlangBuddy";

    // Config constants
    private static final String CONFIG_LOCALE = "locale";
    private static final String CONFIG_POSITION = "position";

    private SlangSession mCurrentSession;
    private ReadableMap mConfigOptions;

    public RNSlangBuddy(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "SlangBuddy";
    }

    /**
     * Initialize Slang
     * @param buddyId
     * @param apiKey
     * @param configOptions
     * @param buddyListener
     */
    @ReactMethod
    public void initialize(
        String buddyId,
        String apiKey,
        ReadableMap configOptions,
        final Callback buddyListener) {

        // Config options
        mConfigOptions = configOptions;

        // Initialize SlangBuddy
        try {
            SlangBuddyOptions options = new SlangBuddyOptions.Builder()
                    .setContext(getReactApplicationContext().getApplicationContext())
                    .setBuddyId(buddyId)
                    .setAPIKey(apiKey)
                    .setListener(new RNSlangBuddyListener(buddyListener, getSlangUiPosition()))
                    .setIntentAction(new RNSlangAction(getReactApplicationContext()))
                    .setRequestedLocales(SlangLocale.getSupportedLocales())
                    .setDefaultLocale(RNSlangLocaleMap.getSlangLocale(getStringConfig(CONFIG_LOCALE)))
                    .build();
            SlangBuddy.initialize(options);
            Log.d(TAG, "initialize: called: buddyId:" + buddyId + " apiKey:" + apiKey);
        } catch (SlangBuddyOptions.InvalidOptionException e) {
            Log.d(TAG, "initialize: InvalidOptionException");
            e.printStackTrace();
        } catch (SlangBuddy.InsufficientPrivilegeException e) {
            Log.d(TAG, "initialize: InsufficientPrivilegeException");
            e.printStackTrace();
        }
    }

    /**
     *  Returns string configuration for the key passed in {@link #initialize(String, String, ReadableMap, Callback)} method
     *
     * @param key
     *          Configuration key
     * @returns
     *          Value corresponding to {@param key}
     */
    private String getStringConfig(String key) {
        if (mConfigOptions == null) {
            return null;
        }
        return mConfigOptions.getString(key);
    }

    private SlangBuiltinUI.SlangUIPosition getSlangUiPosition() {
        SlangBuiltinUI.SlangUIPosition position = null;
        String configPosition = getStringConfig(CONFIG_POSITION);
        if (position != null) {
            return SlangBuiltinUI.SlangUIPosition.valueOf(configPosition);
        } else {
            return SlangBuiltinUI.SlangUIPosition.CENTER_BOTTOM;
        }
    }

    /**
     * Notify Slang of a action's success/failure status for Slang to show completion statement
     *
     * @param isActionResolutionSuccess
     */
    @ReactMethod
    public void notifyActionCompleted(boolean isActionResolutionSuccess) {
        if (mCurrentSession != null) {
            mCurrentSession.notifyActionCompleted(isActionResolutionSuccess ? SlangAction.Status.SUCCESS : SlangAction.Status.FAILURE);
        }
    }

    private static class RNSlangBuddyListener implements SlangBuddy.Listener {

        private Callback mBuddyListener;
        private SlangBuiltinUI.SlangUIPosition mPosition;

        public RNSlangBuddyListener(Callback rnBuddyListener, SlangBuiltinUI.SlangUIPosition position) {
            mBuddyListener = rnBuddyListener;
            mPosition = position;
        }

        @Override
        public void onInitialized() {
            Log.d(TAG, "onInitialized: Success");
            try {
                SlangBuddy.getBuiltinUI().setPosition(mPosition);
            } catch (SlangBuddy.UninitializedUsageException e) {
                e.printStackTrace();
                // TODO: Emit failure events to notify the bridge
            }
            mBuddyListener.invoke();
        }

        @Override
        public void onInitializationFailed(SlangBuddy.InitializationError initializationError) {
            Log.d(TAG, "onInitializationFailed: Failed");
        }

        @Override
        public void onLocaleChanged(Locale locale) {
            // TODO Emit an event to let the bridge know
        }

        @Override
        public void onLocaleChangeFailed(Locale locale, SlangBuddy.LocaleChangeError localeChangeError) {
            // TODO Emit an event to let the bridge know
        }
    }

    private class RNSlangAction implements SlangIntentAction {

        private final ReactApplicationContext mReactContext;

        private RNSlangAction(ReactApplicationContext mReactContext) {
            this.mReactContext = mReactContext;
        }

        @Override
        public Status action(SlangIntent slangIntent, SlangSession slangSession) {
            mCurrentSession = slangSession;
            WritableMap map = getMapFromIntentAndEntity(slangIntent);
            sendEvent(map);
            return Status.SUCCESS;
        }

        // TODO: Revisit and separate out the data transformation to separate class as this shouldn't change in subsequent versions
        private WritableMap getMapFromIntentAndEntity(SlangIntent intent) {
            WritableMap params = Arguments.createMap();
            if (intent != null) {
                WritableMap intentMap = Arguments.createMap();

                intentMap.putString("name", intent.getName());
                intentMap.putString("userUtterance", intent.getUserUtterance());
                intentMap.putString("completionStatement_affirmative", intent.getCompletionStatement().getAffirmative());
                intentMap.putString("completionStatement_negative", intent.getCompletionStatement().getNegative());

                params.putMap("intent", intentMap);

                // Get all the entities for this intent
                WritableMap entitiesMap = Arguments.createMap();
                for (SlangEntity entity : intent.getEntities()) {
                    WritableMap entityMap = Arguments.createMap();
                    entityMap.putString("name", entity.getName());
                    entityMap.putString("value", entity.getValue());
                    entityMap.putString("type", entity.getType().getName());
                    if (entity.getPrompt() != null) {
                        entityMap.putString("prompt_affirmative", entity.getPrompt().getAffirmative());
                        entityMap.putString("prompt_negative", entity.getPrompt().getNegative());
                    }

                    entitiesMap.putMap(entity.getName(), entityMap);
                }

                params.putMap("entities", entitiesMap);
            }

            return params;
        }

        private void sendEvent(WritableMap params) {
            mReactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit("slang_action", params);
        }
    }
}
