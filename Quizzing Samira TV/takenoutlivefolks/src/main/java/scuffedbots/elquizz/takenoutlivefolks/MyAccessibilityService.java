package scuffedbots.elquizz.takenoutlivefolks;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.Objects;


public class MyAccessibilityService extends AccessibilityService {

    private String[] question_data = {null, null, null, null, null};
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // TODO: incase we need to make it work for huawei p9 lite
        //new_method = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N;

        //Log.i("HH", "ayoo");
        new_method(getRootInActiveWindow());
    }

    private boolean competition_detected = false;
    private void new_method(AccessibilityNodeInfo root) {
        //Log.i("HH", "dude " + root);
        if (root == null)
            return;

        //Log.i("HH", "we getting updates");
        try{
            Log.i("HH", "competition_detected " + competition_detected);
            if(!competition_detected){

                // test element for now we using the top winners page temporarily, i'll add another comment once i move to the final one
                /*print_tree(root
                        .getChild(0)
                        .getChild(0)
                        .getChild(0)
                        .getChild(0)
                        .getChild(0)
                        .getChild(0)
                        .getChild(0)
                        .getChild(2), 0);*/
                // test element for now we using the top winners page temporarily, i'll add another comment once i move to the final one
                // TEST ELEMENT:
                String timestamp = root
                        .getChild(0)
                        .getChild(0)
                        .getChild(0)
                        .getChild(0)
                        .getChild(0)
                        .getChild(0)
                        .getChild(0)

                        .getChild(2)
                        .getContentDescription()
                        .toString();
                // OFFICIAL ELEMENT

                boolean testing = false;
                // (testing && timestamp.equals("0")) || (testing && timestamp.equals("1")) ||
                if(timestamp.equals("0") || timestamp.equals("1") || timestamp.equals("2") || timestamp.equals("3") // we avoided comparing with 0 and 1 because there is another ranking top winners page that has the number 1 which may conflict with this page, plus we don't even care if it is equal to 1 anyways we will detect the page well over that
                        || timestamp.equals("4") || timestamp.equals("5") || timestamp.equals("6") || timestamp.equals("7")
                        || timestamp.equals("8") || timestamp.equals("9") || timestamp.equals("10")
                        || timestamp.equals("12")){

                    Log.i("HH", "timestamp " + timestamp);
                    competition_detected = true; // if it didn't crash then yep we detected the counter

                    // TODO: ur indicator goes here
                    // then set competition_detected to true

                    //Log.i("HH", "ayooo found the number: " + timestamp);
                    //new_treat_question(root);
                    //if((testing && timestamp.equals("0")) || (testing && timestamp.equals("1"))){
                    try {
                        question_data[0] = root
                                .getChild(0)
                                .getChild(0)
                                .getChild(0)
                                .getChild(0)
                                .getChild(0)
                                .getChild(0)
                                .getChild(0)

                                .getChild(3)
                                .getContentDescription()
                                .toString();
                        question_data[1] = root
                                .getChild(0)
                                .getChild(0)
                                .getChild(0)
                                .getChild(0)
                                .getChild(0)
                                .getChild(0)
                                .getChild(0)

                                .getChild(4)
                                .getContentDescription()
                                .toString();
                        question_data[2] = root
                                .getChild(0)
                                .getChild(0)
                                .getChild(0)
                                .getChild(0)
                                .getChild(0)
                                .getChild(0)
                                .getChild(0)

                                .getChild(5)
                                .getContentDescription()
                                .toString();
                        question_data[3] = root
                                .getChild(0)
                                .getChild(0)
                                .getChild(0)
                                .getChild(0)
                                .getChild(0)
                                .getChild(0)
                                .getChild(0)

                                .getChild(6)
                                .getContentDescription()
                                .toString();
                        question_data[4] = root
                                .getChild(0)
                                .getChild(0)
                                .getChild(0)
                                .getChild(0)
                                .getChild(0)
                                .getChild(0)
                                .getChild(0)

                                .getChild(7)
                                .getContentDescription()
                                .toString();

                        // weird if statement ik just read on
                        // TESTING PAGE: i'm using the top winners page as a tester, but one of the elements that have the same position as the question is the number 2 thing so just go into test mode if we detected that
                        if(Objects.equals(question_data[0], "2")){
                            question_data[0] = "الدوبارة اكلة شعبية معروفة في أي ولاية ؟";
                            question_data[1] = "البليدة";
                            question_data[2] = "سكيكدة";
                            question_data[3] = "بسكرة";
                            question_data[4] = "البيض";
                        }
                    } catch(Exception e){
                        e.printStackTrace();
                        Log.i("HH", "error inside: " + e.toString());
                        question_data[0] = "الدوبارة اكلة شعبية معروفة في أي ولاية ؟";
                        question_data[1] = "البليدة";
                        question_data[2] = "سكيكدة";
                        question_data[3] = "بسكرة";
                        question_data[4] = "البيض";
                    }
                    send_newquestion();
                }
            }
        } catch(Exception e){
            competition_detected = false;

            Log.i("HH", "found error : " + e);
            e.printStackTrace();
        }
    }

    private void new_treat_question(AccessibilityNodeInfo root) {

        root = root.getChild(0)
                    .getChild(0)
                    .getChild(0)
                    .getChild(0)
                    .getChild(0)
                    .getChild(1)
                    .getChild(0) // the guy
                    .getChild(1)

                    .getChild(0)

                    .getChild(1) // the guy
                    .getChild(0); // the guy

        CharSequence[] samples = {null, null, null, null, null};
        samples[0] = root.getChild(3).getText();
        samples[1] = root.getChild(4).getChild(0).getChild(0).getText();
        samples[2] = root.getChild(5).getChild(0).getChild(0).getText();
        samples[3] = root.getChild(6).getChild(0).getChild(0).getText();
        samples[4] = root.getChild(7).getChild(0).getChild(0).getText();

        if(samples[0]!=null && samples[1]!=null && samples[2]!=null && samples[3]!=null && samples[4]!=null){

            if(question_data[0]!=null){
                if(!question_data[0].equals(samples[0].toString())){
                    apply_new_question_stuff(samples);
                    send_newquestion();
                }
            } else {
                apply_new_question_stuff(samples);
                send_newquestion();
            }

        } else {
            /*log("stuff isn't fully in yet");*/
        }

    }

    private void apply_new_question_stuff(CharSequence[] samples) {
        question_data[0] = samples[0].toString();
        question_data[1] = samples[1].toString();
        question_data[2] = samples[2].toString();
        question_data[3] = samples[3].toString();
        question_data[4] = samples[4].toString();
    }

    // TODO Finding a certain element
    //private boolean stop = false;
    private void print_tree(AccessibilityNodeInfo nodeInfo, int depth) {
        if (nodeInfo == null) return;

        String logString = "";

        for (int i = 0; i < depth; ++i) {
            logString += " ";
        }

        //if(!stop){
            logString += "Text: " + nodeInfo.getText() + " " + " Content-Description: " + nodeInfo.getContentDescription();

            log(logString);
        //}
        //else
        //    return;

        try{
            //if(nodeInfo.getText().toString().contains("12s"))
            //    stop = true;
        } catch(Exception ignored){}

        for (int i = 0; i < nodeInfo.getChildCount(); ++i) {
            //if(!stop)

            //print_tree(nodeInfo.getChild(i), depth + 1);
            //else
            //    return;
        }
    }

    private void log(Object log) {
        Log.i("HH", String.valueOf(log));
    }

    // TODO OLD METHOD
    //private boolean start_recording_data = false;
    //private int index = 0;

    /*private void old_method(AccessibilityEvent event) {
        if (is_a_valid_textview(event.getSource())){
            String text = gettext(event.getSource());
            if(!is_anew_question(text)) {
                if(start_recording_data)
                    treat_newquestion(text);
            }
        }
    }*/

    private String gettext(AccessibilityNodeInfo source) {
        return source.getText().toString();
    }

    /*private boolean is_anew_question(String text) {
        if(text.equals(getString(R.string.twelve))){
            start_recording_data = true;
            index = 0;
            return true;
        }
        return false;
    }*/

    /*private void treat_newquestion(String text) {
        if (!(text.equals("أ") || text.equals("ب") || text.equals("ت") || text.equals("ث")
                || text.equals("A") || text.equals("B") || text.equals("C") || text.equals("D"))) {
            question_data[index] = text;
            if (index == 4) {
                start_recording_data = false;
                index = 0;
                send_newquestion();
            }
            index++;
        }
    }*/

    /*private boolean is_a_valid_textview(AccessibilityNodeInfo source) {
        //int level = source.getChildCount();
        return source.getClassName().equals(getString(R.string.textview_package)) && source.getText()!=null && !source.getText().toString().isEmpty();
    }*/

    private void send_newquestion() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Intent intent2 = new Intent(FloatingViewService.NEW_QUESTION_INTENT);
                Bundle b = new Bundle();
                b.putString(getString(R.string.question), question_data[0].replace(getString(R.string.slash), " "));
                b.putString(getString(R.string.answer1), question_data[1]);
                b.putString(getString(R.string.answer2), question_data[2]);
                b.putString(getString(R.string.answer3), question_data[3]);
                b.putString(getString(R.string.answer4), question_data[4]);
                intent2.putExtras(b);
                getApplicationContext().sendBroadcast(intent2);
                question_data = new String[]{null, null, null, null, null};
                competition_detected = false;

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
        // We are keeping the timeout to 0 as we don’t need any delay or to pause our accessibility events

        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
                | AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
                | AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            info.eventTypes |= AccessibilityEvent.TYPE_WINDOWS_CHANGED;
        }

        info.packageNames = new String[]{getString(R.string.target_package)};
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC; // YOU HAVE TO INCLUDE IT IN SERVICECONFIG.XML FOR IT TO WORK
        //info.feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK; // YOU HAVE TO INCLUDE IT IN SERVICECONFIG.XML FOR IT TO WORK

        info.flags = AccessibilityServiceInfo.DEFAULT
                | AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            info.flags |= AccessibilityServiceInfo.FLAG_REQUEST_ENHANCED_WEB_ACCESSIBILITY
                    | AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            info.flags |= AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;
        }

        info.notificationTimeout = 100; // 500 worked, 100 worked too, 1000 might be slower, also in the first time i made this code it was set to zero wtf

        this.setServiceInfo(info);
    }
}