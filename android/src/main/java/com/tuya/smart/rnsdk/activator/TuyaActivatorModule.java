package com.tuya.smart.rnsdk.activator;

import android.util.Log;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.tuya.smart.android.ble.api.ScanDeviceBean;
import com.tuya.smart.android.ble.api.ScanType;
import com.tuya.smart.android.ble.api.TyBleScanResponse;
import com.tuya.smart.android.common.utils.WiFiUtil;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.builder.ActivatorBuilder;
import com.tuya.smart.home.sdk.builder.TuyaGwActivatorBuilder;
import com.tuya.smart.home.sdk.builder.TuyaGwSubDevActivatorBuilder;
import com.tuya.smart.rnsdk.utils.JsonUtils;
import com.tuya.smart.rnsdk.utils.ReactParamsCheck;
import com.tuya.smart.rnsdk.utils.TuyaReactUtils;
import com.tuya.smart.sdk.api.IMultiModeActivator;
import com.tuya.smart.sdk.api.IMultiModeActivatorListener;
import com.tuya.smart.sdk.api.ITuyaActivator;
import com.tuya.smart.sdk.api.ITuyaActivatorGetToken;
import com.tuya.smart.sdk.api.ITuyaSmartActivatorListener;
import com.tuya.smart.sdk.bean.DeviceBean;
import com.tuya.smart.sdk.bean.MultiModeActivatorBean;
import com.tuya.smart.sdk.enums.ActivatorModelEnum;

import java.util.Arrays;

import javax.annotation.Nonnull;

import static com.tuya.smart.rnsdk.utils.Constant.DEVID;
import static com.tuya.smart.rnsdk.utils.Constant.HOMEID;
import static com.tuya.smart.rnsdk.utils.Constant.PASSWORD;
import static com.tuya.smart.rnsdk.utils.Constant.SSID;
import static com.tuya.smart.rnsdk.utils.Constant.TIME;
import static com.tuya.smart.rnsdk.utils.Constant.TYPE;
import static com.tuya.smart.rnsdk.utils.Constant.coverDTL;

public class TuyaActivatorModule extends ReactContextBaseJavaModule {
    public TuyaActivatorModule(@Nonnull ReactApplicationContext reactContext) {
        super(reactContext);
    }

    private ITuyaActivator mITuyaActivator;
    private ITuyaActivator iTuyaActivator;
    private IMultiModeActivator iMultiModeActivator;
    private String uuid;

    @Nonnull
    @Override
    public String getName() {
        return "TuyaActivatorModule";
    }


    @ReactMethod
    public void getCurrentSSID(Promise promise) {
        promise.resolve(WiFiUtil.getCurrentSSID(getReactApplicationContext()));
    }

    @ReactMethod
    public void startBluetoothScan(final Promise promise) {
        TuyaHomeSdk.getBleOperator().startLeScan(60000, ScanType.SINGLE, new TyBleScanResponse() {
            @Override
            public void onResult(ScanDeviceBean bean) {
                promise.resolve(TuyaReactUtils.parseToWritableMap(bean));
            }
        });
    }

    @ReactMethod
    public void stopBluetoothScan() {
        TuyaHomeSdk.getBleOperator().stopLeScan();
    }


    @ReactMethod
    public void initMultiModeActivator(final ReadableMap params, final Promise promise) {
        if (ReactParamsCheck.checkParams(Arrays.asList(HOMEID, SSID, PASSWORD, TIME, TYPE), params)) {
            TuyaHomeSdk.getActivatorInstance().getActivatorToken(coverDTL(params.getDouble(HOMEID)), new ITuyaActivatorGetToken() {
                @Override
                public void onSuccess(String token) {
                    stop();
                    MultiModeActivatorBean multiModeActivatorBean = new MultiModeActivatorBean();
                    multiModeActivatorBean.ssid = params.getString(SSID);
                    multiModeActivatorBean.pwd = params.getString(PASSWORD);
                    uuid = params.getString("uuid");
                    multiModeActivatorBean.uuid = uuid;
                    multiModeActivatorBean.deviceType = params.getInt("deviceType");
                    multiModeActivatorBean.mac = params.getString("mac");
                    multiModeActivatorBean.address = params.getString("address");
                    multiModeActivatorBean.homeId = coverDTL(params.getDouble(HOMEID));
                    multiModeActivatorBean.token = token;
                    multiModeActivatorBean.timeout = 180000;
                    multiModeActivatorBean.phase1Timeout = 60000;


                    iMultiModeActivator = TuyaHomeSdk.getActivator().newMultiModeActivator();
                    iMultiModeActivator.startActivator(multiModeActivatorBean, new IMultiModeActivatorListener() {
                        @Override
                        public void onSuccess(DeviceBean deviceBean) {
                            promise.resolve(TuyaReactUtils.parseToWritableMap(deviceBean));
                        }

                        @Override
                        public void onFailure(int code, String msg, Object handle) {
                            promise.reject(String.valueOf(code), msg);
                        }
                    });
                }

                @Override
                public void onFailure(String errorCode, String errorMsg) {
                    promise.reject(errorCode, errorMsg);
                }
            });
        }
    }

    @ReactMethod
    public void initActivator(final ReadableMap params, final Promise promise) {
        if (ReactParamsCheck.checkParams(Arrays.asList(HOMEID, SSID, PASSWORD, TIME, TYPE), params)) {
            TuyaHomeSdk.getActivatorInstance().getActivatorToken(coverDTL(params.getDouble(HOMEID)), new ITuyaActivatorGetToken() {
                @Override
                public void onSuccess(String token) {
                    stop();
                    ActivatorBuilder activatorBuilder = new ActivatorBuilder()
                            .setSsid(params.getString(SSID))
                            .setContext(getReactApplicationContext().getApplicationContext())
                            .setPassword(params.getString(PASSWORD))
                            .setActivatorModel(ActivatorModelEnum.valueOf(params.getString(TYPE)))
                            .setTimeOut(params.getInt(TIME))
                            .setToken(token).setListener(new ITuyaSmartActivatorListener() {
                                @Override
                                public void onError(String errorCode, String errorMsg) {
                                    promise.reject(errorCode, errorMsg);
                                }

                                @Override
                                public void onActiveSuccess(DeviceBean devResp) {
                                    promise.resolve(TuyaReactUtils.parseToWritableMap(devResp));
                                }

                                @Override
                                public void onStep(String step, Object data) {
                                    promise.resolve(JsonUtils.toString(data));
                                }
                            });
                    if(ActivatorModelEnum.valueOf(params.getString(TYPE))==ActivatorModelEnum.TY_AP){
                        mITuyaActivator = TuyaHomeSdk.getActivatorInstance().newActivator(activatorBuilder);
                    }else{
                        mITuyaActivator = TuyaHomeSdk.getActivatorInstance().newMultiActivator(activatorBuilder);
                    }
                    mITuyaActivator.start();
                }

                @Override
                public void onFailure(String errorCode, String errorMsg) {
                    promise.reject(errorCode, errorMsg);
                }
            });
        }
    }

    @ReactMethod
    public void stop() {
        if (mITuyaActivator != null) {
            mITuyaActivator.stop();
            mITuyaActivator.onDestroy();
            mITuyaActivator = null;
        }
        if (iTuyaActivator != null) {
            iTuyaActivator.stop();
            iTuyaActivator.onDestroy();
            iTuyaActivator = null;
        }
        if (iMultiModeActivator != null && uuid != null) {
            iMultiModeActivator.stopActivator(uuid);
            iMultiModeActivator = null;
        }
    }


    @ReactMethod
    public void newGwSubDevActivator(final ReadableMap params, final Promise promise) {
        if (ReactParamsCheck.checkParams(Arrays.asList(DEVID, TIME), params)) {
            stop();
            TuyaGwSubDevActivatorBuilder tuyaGwSubDevActivatorBuilder = new TuyaGwSubDevActivatorBuilder().setDevId(params.getString(DEVID)).setTimeOut(params.getInt(TIME)).setListener(new ITuyaSmartActivatorListener() {
                @Override
                public void onError(String errorCode, String errorMsg) {
                    promise.reject(errorCode, errorMsg);
                }

                @Override
                public void onActiveSuccess(DeviceBean devResp) {
                    promise.resolve(TuyaReactUtils.parseToWritableMap(devResp));
                }

                @Override
                public void onStep(String step, Object data) {
                    promise.resolve(JsonUtils.toString(data));
                }
            });
            iTuyaActivator = TuyaHomeSdk.getActivatorInstance().newGwSubDevActivator(tuyaGwSubDevActivatorBuilder);
            iTuyaActivator.start();
        }
    }

    @ReactMethod
    public void newGwActivator(final ReadableMap params, final Promise promise) {
        if (ReactParamsCheck.checkParams(Arrays.asList(HOMEID, TIME), params)) {
            TuyaHomeSdk.getActivatorInstance().getActivatorToken(coverDTL(params.getDouble(HOMEID)), new ITuyaActivatorGetToken() {
                @Override
                public void onSuccess(String token) {
                    stop();
                    ITuyaSmartActivatorListener iTuyaSmartActivatorListener = new ITuyaSmartActivatorListener() {
                        @Override
                        public void onError(String errorCode, String errorMsg) {
                            promise.reject(errorCode, errorMsg);
                        }

                        @Override
                        public void onActiveSuccess(DeviceBean deviceBean) {
                            promise.resolve(TuyaReactUtils.parseToWritableMap(deviceBean));
                        }

                        @Override
                        public void onStep(String step, Object data) {
                            promise.resolve(JsonUtils.toString(data));
                        }
                    };
                    iTuyaActivator = TuyaHomeSdk.getActivatorInstance().newGwActivator(new TuyaGwActivatorBuilder()
                            .setToken(token)
                            .setTimeOut(params.getInt(TIME))
                            .setContext(getReactApplicationContext())
                            .setListener(iTuyaSmartActivatorListener));
                    iTuyaActivator.start();
                }

                @Override
                public void onFailure(String errorCode, String errorMsg) {
                    promise.reject(errorCode, errorMsg);
                }
            });

        }
    }
}
