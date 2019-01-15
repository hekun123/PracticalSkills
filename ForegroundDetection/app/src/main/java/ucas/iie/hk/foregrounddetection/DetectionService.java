package ucas.iie.hk.foregrounddetection;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

/**
 * Created by hk on 2018/12/10.
 */

public class DetectionService extends AccessibilityService {
    final static String TAG = "DetectionService";
    public static String foregroundPackageName;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: we start service");
        return START_STICKY;
    }

    public static boolean isFore(String packageName){
        return foregroundPackageName.equals(packageName);
    }

    /**
     * 重载辅助功能事件回调函数，对窗口状态变化事件进行处理
     *
     * @param event
     */
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            /*
             * 如果 与 DetectionService 相同进程，直接比较 foregroundPackageName 的值即可
             * 如果在不同进程，可以利用 Intent 或 bind service 进行通信
             */
            foregroundPackageName = event.getPackageName().toString();
            Log.d(TAG, "onAccessibilityEvent: " + foregroundPackageName);
        }
    }

    @Override
    public void onInterrupt() {
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
    }

}
