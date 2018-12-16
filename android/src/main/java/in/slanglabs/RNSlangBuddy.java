
package in.slanglabs;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.slanglabs.slang.internal.util.SLog;
import com.slanglabs.slang.internal.util.SlangUIUtil;

import java.util.ArrayList;

import in.slanglabs.platform.application.ISlangApplicationStateListener;
import in.slanglabs.platform.application.SlangApplication;
import in.slanglabs.platform.application.SlangApplicationUninitializedException;
import in.slanglabs.platform.application.SlangIntentDescriptor;
import in.slanglabs.platform.application.SlangLocaleException;
import in.slanglabs.platform.application.actions.ISlangResolvedIntentAction;
import in.slanglabs.platform.session.SlangEntity;
import in.slanglabs.platform.session.SlangIntent;
import in.slanglabs.platform.session.SlangResolvedIntent;
import in.slanglabs.platform.session.SlangSession;
import in.slanglabs.platform.ui.SlangUI;

public class RNSlangBuddy extends ReactContextBaseJavaModule {

    private static final String TAG = "RNSlangBuddy";
    private final ReactApplicationContext reactContext;
    private SlangResolvedIntent mCurrentIntent;
    private SlangEntity mCurrentEntity;
    private SlangEntity mUnresolvedEntity;
    private SlangEntity mResolvedEntity;
    private boolean mInitialized;
    private SlangSession mCurrentSession;

    public RNSlangBuddy(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "SlangBuddy";
    }

    /**
     * Initialize Slang
     * @param appKey
     * @param authKey
     * @param successCallback
     * @param errorCallback
     */
    @ReactMethod
    public void initialize(
        String appKey,
        String authKey,
        final Callback successCallback,
        final Callback errorCallback
    ) {
        // Initialize slang here
        final Context appContext = reactContext.getApplicationContext();

        try {
            SlangApplication.initialize(
                appContext,
                appKey,
                authKey,
                SlangApplication.getSupportedLocales(),
                SlangApplication.LOCALE_ENGLISH_IN,
                new ISlangApplicationStateListener() {
                    @Override
                    public void onInitialized() {
                        mInitialized = true;
                        Toast.makeText(
                            appContext,
                            "Slang initialized",
                            Toast.LENGTH_LONG
                        );
                        registerHandlers();
                        if (successCallback != null) {
                            successCallback.invoke();
                        }
                    }

                    @Override
                    public void onInitializationFailed(FailureReason failureReason) {
                        mInitialized = true;
                        Toast.makeText(
                            appContext,
                            "Could not initialize slang!",
                            Toast.LENGTH_LONG
                        );
                        if (errorCallback != null) {
                            errorCallback.invoke(failureReason.toString());
                        }
                    }
                }
            );
        } catch (SlangLocaleException e) {
        }
    }

    /**
     * Inform Slang about the value of the given entity
     * @param entity
     * @param value
     */
    @ReactMethod
    public void setEntity(String entity, String value) {
        // First try and resolve using the currently active unresolved entity object
        if (mUnresolvedEntity != null && mUnresolvedEntity.getName().equals(entity)) {
            mUnresolvedEntity.resolve(value);
        } else if (mResolvedEntity != null && mResolvedEntity.getName().equals(entity)) {
            mResolvedEntity.resolve(value);
        } else if (mCurrentIntent != null && mCurrentIntent.getEntity(entity) != null) {
            mCurrentIntent.getEntity(entity).resolve(value);
        }
    }

    /**
     * Inform Slang to continue the lifecyle
     */
    @ReactMethod
    public void continueSession() {
        if (mCurrentSession != null) {
            mCurrentSession.success();
        }
    }

    /**
     * Inform Slang to abort the lifecycle
     */
    @ReactMethod
    public void failSession() {
        if (mCurrentSession != null) {
            mCurrentSession.failure();
        }
    }

    public void registerHandlers() {
        try {
            for (SlangIntentDescriptor intent : SlangApplication.getIntentDescriptors()) {
                registerHandler(intent.getName());
            }
        } catch (Exception e) {
            // handle the exception
            Toast.makeText(
                reactContext.getApplicationContext(),
                "Error registering handler - " + e.getLocalizedMessage(),
                Toast.LENGTH_LONG
            );
        }
    }

    public void registerHandler(
        String intent
    ) {
        try {
            SlangApplication.getIntentDescriptor(intent).setResolutionAction(new ISlangResolvedIntentAction() {
                @Override
                public SlangSession.Status onIntentResolutionBegin(SlangResolvedIntent slangResolvedIntent, SlangSession slangSession) {
                    setCurrentIntent(slangResolvedIntent);
                    setCurrentSession(slangSession);
                    WritableMap params = getMapFromIntentAndEnity(
                        "intentResolutionBegin",
                        slangResolvedIntent,
                        null,
                        null
                    );
                    sendEvent(slangResolvedIntent.getName(), params);
                    return slangSession.suspend();
                }

                @Override
                public SlangSession.Status onEntityUnresolved(final SlangEntity slangEntity, SlangSession slangSession) {
                    Log.d("SlangRegisterHandler", "onEntityUnresolved called - " + slangEntity.getName());

                    setCurrentUnresolvedEntity(slangEntity);
                    WritableMap params = getMapFromIntentAndEnity(
                        "unresolvedEntity",
                        slangEntity.getParent(),
                        slangEntity,
                        null
                    );
                    sendEvent(slangEntity.getParent().getName(), params);
                    return slangSession.suspend();
                }

                @Override
                public SlangSession.Status onEntityResolved(final SlangEntity slangEntity, SlangSession slangSession) {
                    Log.d("SlangRegisterHandler", "onEntityResolved called - " + slangEntity.getName());
                    /* TODO:T366 The suspend and success semantics does not seem to work well
                       for this callback. So until that is fixed, we will not be able to handle
                       this method inside RN

                    setCurrentResolvedEntity(slangEntity);
                    WritableMap params = getMapFromIntentAndEnity(
                        "resolvedEntity",
                        slangEntity.getParent(),
                        slangEntity,
                        null
                    );
                    sendEvent(slangEntity.getParent().getName(), params);
                    */
                    return slangSession.success();
                }

                @Override
                public SlangSession.Status action(final SlangResolvedIntent slangResolvedIntent, SlangSession slangSession) {
                    WritableMap params = getMapFromIntentAndEnity(
                        "action",
                        slangResolvedIntent,
                        null,
                        null
                    );
                    sendEvent(slangResolvedIntent.getName(), params);
                    return slangSession.suspend();
                }

                @Override
                public SlangSession.Status onIntentResolutionEnd(SlangResolvedIntent slangResolvedIntent, SlangSession slangSession) {
                    WritableMap params = getMapFromIntentAndEnity(
                        "intentResolutionEnd",
                        slangResolvedIntent,
                        null,
                        null
                    );
                    sendEvent(slangResolvedIntent.getName(), params);
                    return slangSession.suspend();
                }
            });
        } catch (SlangApplicationUninitializedException e) {
            e.printStackTrace();
        }
    }

    private WritableMap getMapFromIntentAndEnity(
        String eventType,
        SlangResolvedIntent intent,
        SlangEntity unresolvedEntity,
        SlangEntity resolvedEntity
    ) {
        WritableMap params = Arguments.createMap();

        params.putString("eventType", eventType);
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
                if (entity.getPrompt() != null) {
                    entityMap.putString("prompt_affirmative", entity.getPrompt().getAffirmative());
                    entityMap.putString("prompt_negative", entity.getPrompt().getNegative());
                }

                entitiesMap.putMap(entity.getName(), entityMap);
            }

            params.putMap("entities", entitiesMap);
        }

        if (resolvedEntity != null) {
            WritableMap entityMap = Arguments.createMap();

            entityMap.putString("name", resolvedEntity.getName());
            entityMap.putString("value", resolvedEntity.getValue());
            if (resolvedEntity.getPrompt() != null) {
                entityMap.putString("prompt_affirmative", resolvedEntity.getPrompt().getAffirmative());
                entityMap.putString("prompt_negative", resolvedEntity.getPrompt().getNegative());
            }

            params.putMap("resolvedEntity", entityMap);
        }

        if (unresolvedEntity != null) {
            WritableMap entityMap = Arguments.createMap();

            entityMap.putString("name", unresolvedEntity.getName());
            entityMap.putString("value", unresolvedEntity.getValue());
            if (unresolvedEntity.getPrompt() != null) {
                params.putString("prompt_affirmative", unresolvedEntity.getPrompt().getAffirmative());
                params.putString("prompt_negative", unresolvedEntity.getPrompt().getNegative());
            }


            params.putMap("unresolvedEntity", entityMap);
        }

        return params;
    }

    private void setCurrentSession(SlangSession session) {
        mCurrentSession = session;
    }

    private void setCurrentIntent(SlangResolvedIntent intent) {
        mCurrentIntent = intent;
    }

    private void setCurrentUnresolvedEntity(SlangEntity entity) {
        mUnresolvedEntity = entity;
    }

    private void setCurrentResolvedEntity(SlangEntity entity) {
        mResolvedEntity = entity;
    }

    private void sendEvent(String event, WritableMap params) {
        this.reactContext
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
            .emit(event, params);
        // TODO: Make this method sync.
    }
}
