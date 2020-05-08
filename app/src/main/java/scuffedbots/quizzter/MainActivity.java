package scuffedbots.quizzter;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import scuffedbots.quizzter.Debugging.TopExceptionHandler;


public class MainActivity extends AppCompatActivity {

    private int OVERLAY_PERMISSION = 1365;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        if (!is_huawei())
            findViewById(R.id.overridehuaweipermission).setVisibility(View.GONE);

        permission();
    }

    private boolean is_huawei() {
        return "huawei".equalsIgnoreCase(android.os.Build.MANUFACTURER);
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
        Intent intent2 = new Intent(this, MyAccessibilityService.class);
        startService(intent2);
    }

    private void permission() {
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OVERLAY_PERMISSION) {
            launch();
        }
    }


    public void openaccessibilitysettingsClicked(View view) {
        startActivityForResult(new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS), 0);
    }

    public void overridebatteryClicked(View view) {
        protected_apps_request();
    }
}
