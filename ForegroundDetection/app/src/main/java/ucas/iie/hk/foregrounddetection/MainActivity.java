package ucas.iie.hk.foregrounddetection;

import android.content.Intent;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import static ucas.iie.hk.foregrounddetection.AccessibilityUtil.isAccessibilitySettingsOn;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AssessibilityFunction();
    }

    private void  AssessibilityFunction() {
        // 判断辅助功能是否开启
        if (!isAccessibilitySettingsOn(getApplicationContext())) {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        }
    }
}
