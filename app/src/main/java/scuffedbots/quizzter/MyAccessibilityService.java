package scuffedbots.quizzter;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class MyAccessibilityService extends AccessibilityService {

    private boolean start_recording_data = false;
    private String[] question_data = {null, null, null, null, null};
    private int skipper = 0;
    private int index = 0;


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        AccessibilityNodeInfo source = event.getSource();
        if (source == null)
            return;

        if (is_a_valid_textview(source)){
            String text = gettext(source);
            if(!is_anew_question(text)) {
                if(start_recording_data)
                    treat_newquestion(text);
            }
        }

    }

    private String gettext(AccessibilityNodeInfo source) {
        return source.getText().toString();
    }

    private boolean is_anew_question(String text) {
        if(text.equals("12s")){
            start_recording_data = true;
            index = 0;
            return true;
        }
        return false;
    }

    private void treat_newquestion(String text) {
        skipper++;
        if (skipper % 2 == 1) {
            question_data[index] = text;
            if (index == 4) {
                skipper = 0;
                start_recording_data = false;
                index = 0;
                send_newquestion();
            }
            index++;
        }
    }

    private boolean is_a_valid_textview(AccessibilityNodeInfo source) {
        /*Log.i("HH", event.toString());*/
        /*Log.i("HH", source.toString());*/
        /*int level = source.getChildCount();*/
        return source.getClassName().equals("android.widget.TextView") && source.getText()!=null && !source.getText().toString().isEmpty();
    }

    private void send_newquestion() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Intent intent2 = new Intent(FloatingViewService.NEW_QUESTION_INTENT);
                Bundle b = new Bundle();
                b.putString("question", question_data[0]);
                b.putString("answer1", question_data[1]);
                b.putString("answer2", question_data[2]);
                b.putString("answer3", question_data[3]);
                b.putString("answer4", question_data[4]);
                intent2.putExtras(b);
                getApplicationContext().sendBroadcast(intent2);

                question_data = new String[]{null, null, null, null, null};
            }
        }).start();
    }

    @Override public void onInterrupt() {
    }

    AccessibilityServiceInfo info = new AccessibilityServiceInfo();
    @Override public void onServiceConnected() {
        // Set the type of events that this service wants to listen to. Others won't be passed to this service.
        // We are only considering windows state changed event.
        // If you only want this service to work with specific applications, set their package names here. Otherwise, when the service is activated, it will listen to events from all applications.
        // Set the type of feedback your service will provide. We are setting it to GENERIC.
        // Default services are invoked only if no package-specific ones are present for the type of AccessibilityEvent generated.
        // This is a general-purpose service, so we will set some flags
        /*| AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS*//*| AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS*//*| AccessibilityServiceInfo.FLAG_REQUEST_ENHANCED_WEB_ACCESSIBILITY*//*| AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS*/;
        // We are keeping the timeout to 0 as we don’t need any delay or to pause our accessibility events
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
                | AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            info.eventTypes |= AccessibilityEvent.TYPE_WINDOWS_CHANGED;
        }
        info.packageNames = new String[]{"com.elquiz.app.prod"};
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.flags = AccessibilityServiceInfo.DEFAULT;
        info.notificationTimeout = 0;
        this.setServiceInfo(info);
    }
}