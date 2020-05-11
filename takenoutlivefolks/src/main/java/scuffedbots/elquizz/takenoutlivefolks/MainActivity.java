package scuffedbots.elquizz.takenoutlivefolks;

import androidx.appcompat.app.AppCompatActivity;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import scuffedbots.elquizz.takenoutlivefolks.Debugging.TopExceptionHandler;


public class MainActivity extends AppCompatActivity {

    private int OVERLAY_PERMISSION = 1365;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        if (!is_huawei())
            findViewById(R.id.overridehuaweipermission).setVisibility(View.GONE);

    }

    private boolean is_huawei() {
        return "huawei".equalsIgnoreCase(android.os.Build.MANUFACTURER);
    }

    // To check if service is enabled
    private boolean isAccessibilitySettingsOn() {
        int accessibilityEnabled = 0;
        final String service = getPackageName() + "/" + MyAccessibilityService.class.getCanonicalName();
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException ignored) {
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(
                    getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();

                    if (accessibilityService.equalsIgnoreCase(service)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
    private void protected_apps_request() {
        try{
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(getString(R.string.huaweisource), getString(R.string.huaweiactivity)));
            startActivity(intent);
        }
        catch(Exception ignored){}
    }

    private void launch(){
        Intent intent22 = new Intent(getApplicationContext(), FloatingViewService.class);
        startService(intent22);
    }

    private void permission() {
        if (isAccessibilitySettingsOn()){
            if(Build.VERSION.SDK_INT>=23){
                if(!Settings.canDrawOverlays(MainActivity.this)){
                    startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()))
                            , OVERLAY_PERMISSION);
                } else {
                    launch();
                }
            } else {
                launch();
            }
        } else {
            startActivityForResult(new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS), 0);
        }
    }

    public void overridebatteryClicked(View view) {
        protected_apps_request();
    }

    @Override
    protected void onResume() {
        super.onResume();
        permission();
    }
}
