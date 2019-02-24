
import { NativeModules, DeviceEventEmitter } from 'react-native';

const { SlangBuddy } = NativeModules;

// RN wrapper for SlangBuddy. All the SlangBuddy integrations exposed over the native bridge are available here. 
class Slang {

  // Initializes Slang
  static initialize(buddyId, apiKey, config, callback) {
    SlangBuddy.initialize(buddyId, apiKey, config, callback);
  }

  // Listen to SlangIntentAction
  static setIntentActionListener(actionListener) {
    DeviceEventEmitter.addListener('slang_action', function(event) {
      actionListener(event);
    });
  }

  // Notify Slang of successful action
  static notifyActionCompleted(success) {
    SlangBuddy.notifyActionCompleted(success);
  }
}

export default Slang;