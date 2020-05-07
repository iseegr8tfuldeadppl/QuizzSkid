package scuffedbots.quizzter;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import java.lang.reflect.AccessibleObject;

import scuffedbots.quizzter.Debugging.TopExceptionHandler;

import static scuffedbots.quizzter.FloatingViewService.SHOW_LAYOUT_INTENT;

public class MainActivity extends AppCompatActivity {

    private int OVERLAY_PERMISSION = 1365;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        permission();
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
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()) );
                startActivityForResult(intent, OVERLAY_PERMISSION);
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
        Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
        startActivityForResult(intent, 0);
    }
}
