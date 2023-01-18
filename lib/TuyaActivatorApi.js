const ActivatorNativeApi = require('react-native').NativeModules.TuyaActivatorModule

const TuyaActivatorApi = {
  getCurrentSSID() {  
    return ActivatorNativeApi.getCurrentSSID()
  },
  initActivator(params) {  
    return ActivatorNativeApi.initActivator(params)
  },
  initMultiModeActivator(params) {  
    return ActivatorNativeApi.initMultiModeActivator(params)
  },
  stop() {
     ActivatorNativeApi.stop()
  },
  startBluetoothScan() {
    return ActivatorNativeApi.startBluetoothScan()
  },
  stopBluetoothScan() {
    ActivatorNativeApi.stopBluetoothScan()
  },
  newGwSubDevActivator(params) {
    return ActivatorNativeApi.newGwSubDevActivator(params)
  },
  newGwActivator(params) {
    return ActivatorNativeApi.newGwActivator(params)
  },
}

module.exports = TuyaActivatorApi
