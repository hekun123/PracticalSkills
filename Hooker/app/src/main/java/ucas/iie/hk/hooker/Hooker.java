package ucas.iie.hk.hooker;

import android.bluetooth.BluetoothAdapter;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraManager;
import android.icu.util.Calendar;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioRecord;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.CellLocation;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

//Hooker everything on system
/*
    trace应用获取用户设备信息接口
    信息:
        IMEI、IMSI、SIM、网络类型、phone number
        定位、基站信息、
        WiFi、IP Address、MAC、android_id、
        content、短信、file、calendar、
        录音、打开摄像头、打开蓝牙
 */
public class Hooker implements IXposedHookLoadPackage {
    public void printMethodStack(String packagename) {
        Throwable ex = new Throwable();
        /**
         * Throwable的getStackTrace()可以返回当前线程的虚拟机栈信息，返回
         * 数组的第一个元素是栈顶元素，最后一个元素是栈底元素。
         */
        StackTraceElement[] stackElements = ex.getStackTrace();
        System.out.println(stackElements.length);
        StringBuilder sb = new StringBuilder();
        sb.append("invoker: ");
        sb.append(packagename);sb.append(".");
        for (int i = stackElements.length -1; i > 1; i--) {
            sb.append(stackElements[i].getMethodName());
            sb.append(".");
        }
        sb.delete(sb.length()-1, sb.length());

//        for (StackTraceElement stackTraceElement : stackElements) {
//            sb.insert(0, "."+stackTraceElement.getMethodName());
//
//        }
        XposedBridge.log(sb.toString());
    }
//    对xposed框架进行改造，增加接口函数，返回调用栈的信息。
//    如果知道了调用栈的信息，就可以记录应用中哪些模块调用了这些信息。
//    在此基础上，以此来推断隐私信息泄露情况
//
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        final String packageName = loadPackageParam.packageName;

        //获取手机状态
        XposedHelpers.findAndHookMethod(
                TelephonyManager.class,
                "getCallState",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log("HK :"+ packageName + "[" + param + "]: TelephonyManager.getCallState");
                        //printMethodStack(packageName);
                        super.afterHookedMethod(param);
                    }
                });
        //IMEI信息
        XposedHelpers.findAndHookMethod(
                TelephonyManager.class,
                "getDeviceId",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        //printMethodStack(packageName);
                        super.beforeHookedMethod(param);
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log("HK :"+ packageName + "[" + param + "]: TelephonyManager.getDeviceId");
                        super.afterHookedMethod(param);
                    }
                });
        XposedHelpers.findAndHookMethod(
                TelephonyManager.class,
                "getImei",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        //printMethodStack(packageName);
                        super.beforeHookedMethod(param);
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log("HK :"+ packageName + "[" + param + "]: TelephonyManager.getImei");
                        super.afterHookedMethod(param);
                    }
                });
        //IMSI
        XposedHelpers.findAndHookMethod(
                TelephonyManager.class,
                "getSubscriberId",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log("HK :"+ packageName + "[" + param + "]: TelephonyManager.getSubscriberId");
                        //printMethodStack(packageName);
                        super.afterHookedMethod(param);
                    }
                });

        //SIM
        XposedHelpers.findAndHookMethod(
                TelephonyManager.class,
                "getSimSerialNumber",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log("HK :"+ packageName + "[" + param + "]: TelephonyManager.getSimSerialNumber");
                        //printMethodStack(packageName);
                        super.afterHookedMethod(param);
                    }
                });
        //网络类型
        XposedHelpers.findAndHookMethod(
                TelephonyManager.class,
                "getDataNetworkType",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log("HK :"+ packageName + "[" + param + "]: TelephonyManager.getDataNetworkType");
                        //printMethodStack(packageName);
                        super.afterHookedMethod(param);
                    }
                });
        //phone number for line 1
        XposedHelpers.findAndHookMethod(
                TelephonyManager.class,
                "getLine1Number",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log("HK :"+ packageName + "[" + param + "]: TelephonyManager.getLine1Number");
                        //printMethodStack(packageName);
                        super.afterHookedMethod(param);
                    }
                });

        //定位
        XposedHelpers.findAndHookMethod(
                Location.class,
                "getLatitude",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log("HK :"+ packageName + "[" + param + "]: Location.getLatitude");
                        //printMethodStack(packageName);
                        super.afterHookedMethod(param);
                    }
                });
        XposedHelpers.findAndHookMethod(
                Location.class,
                "getLongitude",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log("HK :"+ packageName + "[" + param + "]: Location.getLongitude");
                        //printMethodStack(packageName);
                        super.afterHookedMethod(param);
                    }
                });
        XposedBridge.hookAllMethods(
                LocationManager.class,
                "getLastKnownLocation",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log("HK :"+ packageName + "[" + param + "]: LocationManager.getLastKnownLocation");
                        //printMethodStack(packageName);
                        super.afterHookedMethod(param);
                    }
                });
        XposedBridge.hookAllMethods(
                LocationManager.class,
                "requestLocationUpdates",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log("HK :"+ packageName + "[" + param + "]: LocationManager.requestLocationUpdates");
                        //printMethodStack(packageName);
                        super.afterHookedMethod(param);
                    }
                });

        //基站信息
        XposedHelpers.findAndHookMethod(
                "android.telephony.TelephonyManager",
                loadPackageParam.classLoader,
                "getCellLocation",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log("HK :"+ packageName + "[" + param + "]: TelephonyManager.getCellLocation");
                        //printMethodStack(packageName);
                        super.afterHookedMethod(param);
                    }
                });

        XposedHelpers.findAndHookMethod(
                "android.telephony.PhoneStateListener",
                loadPackageParam.classLoader,
                "onCellLocationChanged",
                CellLocation.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log("HK :"+ packageName + "[" + param + "]: PhoneStateListener.onCellLocationChanged");
                        //printMethodStack(packageName);
                        super.afterHookedMethod(param);
                    }
                });

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            XposedHelpers.findAndHookMethod(
                    "android.telephony.TelephonyManager",
                    loadPackageParam.classLoader,
                    "getNeighboringCellInfo",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            XposedBridge.log("HK :"+ packageName + "[" + param + "]: TelephonyManager.getNeighboringCellInfo");
                            //printMethodStack(packageName);
                            super.afterHookedMethod(param);
                        }
                    });
        }

        //WiFi
        XposedHelpers.findAndHookMethod(
                WifiManager.class,
                "getScanResults",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log("HK :"+ packageName + "[" + param + "]: WifiInfo.getScanResults");
                        //printMethodStack(packageName);
                        super.afterHookedMethod(param);
                    }
                });
        //IP Address
        XposedHelpers.findAndHookMethod(
                WifiInfo.class,
                "getIpAddress",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log("HK :"+ packageName + "[" + param + "]: WifiInfo.getIpAddress");
                        //printMethodStack(packageName);
                        super.afterHookedMethod(param);
                    }
                });
        //MAC
        XposedHelpers.findAndHookMethod(
                WifiInfo.class,
                "getMacAddress",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log("HK :"+ packageName + "[" + param + "]: WifiInfo.getMacAddress");
                        //printMethodStack(packageName);
                        super.afterHookedMethod(param);
                        Sensor a;
                    }
                });

        //content//短信//file//calendar//通话记录
        XposedBridge.hookAllMethods(
                ContentResolver.class,
                "query",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log("HK :"+ packageName + "[" + param + "]: ContentResolver.query( 0:"
                                + param.args[0].toString() + ", 1: " + param.args[1] +
                                "2 :" + param.args[2] + "3: " + param.args[3] + ")");
                        //printMethodStack(packageName);
                        super.afterHookedMethod(param);
                    }
                });
        XposedBridge.hookAllMethods(
                AsyncQueryHandler.class,
                "startQuery",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log("HK :"+ packageName + "[" + param + "]: AsyncQueryHandler.startQuery( 0:"
                                + param.args[0].toString() + ", 1: " + param.args[1] + ", 2 :" + param.args[2]
                                + ", 3: " + param.args[3] + ", 4: " + param.args[4] + ", 5: " + param.args[5] + ")");
                        //printMethodStack(packageName);
                        super.afterHookedMethod(param);
                    }
                });
            //发送短信
        XposedBridge.hookAllMethods(
                SmsManager.class,
                "sendTextMessage",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log("HK :"+ packageName + "[" + param + "]: SmsManager.sendTextMessage("
                                + param.args[0].toString() + ")");
                        //printMethodStack(packageName);
                        super.afterHookedMethod(param);
                    }
                });
        XposedBridge.hookAllMethods(
                SmsManager.class,
                "SendDataMessage",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log("HK :"+ packageName + "[" + param + "]: SmsManager.SendDataMessage("
                                + param.args[0].toString() + ")");
                        //printMethodStack(packageName);
                        super.afterHookedMethod(param);
                    }
                });

        //录音
        XposedHelpers.findAndHookMethod(
                AudioRecord.class,
                "startRecording",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log("HK :"+ packageName + "[" + param + "]: AudioRecord startRecording");
                        //printMethodStack(packageName);
                        super.afterHookedMethod(param);
                    }
                });
        //打开摄像头
        XposedBridge.hookAllMethods(
                CameraManager.class,
                "openCamera",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log("HK :"+ packageName + "[" + param + "]: CameraManager.openCamera( 0:"
                                + param.args[0].toString() + ", 1: " + param.args[1] + ", 2 :" + param.args[2] + ")");
                        //printMethodStack(packageName);
                        super.afterHookedMethod(param);
                    }
                });
        XposedBridge.hookAllMethods(
                Camera.class,
                "open",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log("HK :"+ packageName + "[" + param + "]: CameraManager.openCamera");
                        //printMethodStack(packageName);
                        super.afterHookedMethod(param);
                    }
                });
        XposedBridge.hookAllMethods(
                Camera.class,
                "takePicture",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log("HK :"+ packageName + "[" + param + "]: CameraManager.takePicture");
                        //printMethodStack(packageName);
                        super.afterHookedMethod(param);
                    }
                });
        //打开蓝牙
        XposedHelpers.findAndHookMethod(
                BluetoothAdapter.class,
                "enable",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log("HK :"+ packageName + "[" + param + "]: BluetoothAdapter enable");
                        //printMethodStack(packageName);
                        super.afterHookedMethod(param);
                    }
                });
        XposedHelpers.findAndHookMethod(
                SensorManager.class,
                "getSensors",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log("HK :"+ packageName + "[" + param + "]: SensorManager getSensors");
                        //printMethodStack(packageName);
                        super.afterHookedMethod(param);
                    }
                });
        //calendar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            XposedHelpers.findAndHookMethod(
                    Calendar.class,
                    "getInstance",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            XposedBridge.log("HK :"+ packageName + "[" + param + "]: SensorManager getSensors");
                            //printMethodStack(packageName);
                            super.afterHookedMethod(param);
                        }
                    });
        }

        //是否有申请权限
//        XposedHelpers.findAndHookMethod(
//                ActivityCompat.class,
//                "shouldShowRequestPermissionRationale",
//                new XC_MethodHook() {
//                    @Override
//                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                        XposedBridge.log("HK [" + param + "]: ActivityCompat shouldShowRequestPermissionRationale");
//                        printMethodStack(packageName);
//                        super.afterHookedMethod(param);
//                    }
//                });
//        XposedHelpers.findAndHookMethod(
//                ContextCompat.class,
//                "checkSelfPermission",
//                new XC_MethodHook() {
//                    @Override
//                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                        XposedBridge.log("HK [" + param + "]: ContextCompat checkSelfPermission");
//                        printMethodStack(packageName);
//                        super.afterHookedMethod(param);
//                    }
//                });
    }
}