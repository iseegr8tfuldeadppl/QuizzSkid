package scuffedbots.quizzter;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.webkit.ValueCallback;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationCompat.Builder;

import java.util.ArrayList;
import java.util.List;
import scuffedbots.quizzter.Debugging.TopExceptionHandler;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.O;


public class FloatingViewService extends Service{

    private WindowManager.LayoutParams LayoutParams;
    /*private WindowManager.LayoutParams LayoutParams2;*/
    private WindowManager mWindowManager;
    private Context context;
    private TextView close;
    private View FloatingView/*, FloatingView2*/;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    protected static final String SHOW_LAYOUT_INTENT = "scuffedbots.quizzter.SHOWLAYOUT";
    private void createBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(SHOW_LAYOUT_INTENT);
        registerReceiver(receiver, filter);
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            assert action != null;
            if(action.equals(SHOW_LAYOUT_INTENT)) {
                /*if(addedview){
                    addedview = false;
                    intentnig = null;
                }*/
                if(intentnig==null){
                    intentnig = intent;
                    launch_main();
                    /*print("ready");*/
                }
            }
        }
    };

    private boolean addedview = false;
    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());
        createNotification();
        createWindowManager();
        createLayoutParams();
        /*createLayoutParams2();*/
        createFloatingViews();
        /*createFloatingViews2();*/
        createBroadcastReceiver();

        setOnClickListeners();
        main_browser_work();
        /*setOnClickListeners2();*/

        /*if(!addedview) {
            mWindowManager.addView(FloatingView, LayoutParams);
            *//*mWindowManager.removeView(FloatingView2);*//*
            addedview = true;
        }
        once = true;
        browser.loadUrl("https://www.google.com/search?q=" + "degenerate");*/
    }

    private void main_browser_work() {
        browser.getSettings().setJavaScriptEnabled(true);
        WebViewClient webViewClient = new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(browser, url);
                if(once)
                    get_html_of_page_and_treat_it();

            }
        };
        /*webViewClient.shouldOverrideUrlLoading(browser, );*/
        browser.setWebViewClient(webViewClient);
    }

    private void get_html_of_page_and_treat_it() {
        browser.post(new Runnable() {
            @Override
            public void run() {
                browser.evaluateJavascript("(function(){return window.document.body.outerHTML})();", new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String html) {
                        if(questione!=null){
                            if(once){
                                once = false;
                                remove_alif_wel_lam_from_answers();
                                int[] counts = count_occurances_of_the_answers_in_google_by_splitting_their_words_for_more_accuracy(html);

                                if(all_zero(counts)){
                                    print(0);
                                } else {
                                    int most_or_least_common = find_most_common_or_least_common(counts);
                                    for(int i=0; i<counts.length; i++){
                                        Log.i("HH", "answer " + answers[i] + " has occured " + counts[i] + " times");
                                    }
                                    List<Occurance> occurancehandler = new ArrayList<>();
                                    boolean unique = if_other_answers_appear_the_same_amount_then_add_them_into_array(occurancehandler, counts, most_or_least_common);

                                    if(unique){
                                        print(most_or_least_common+1);
                                    }

                                    for(Occurance occurance:occurancehandler){
                                        if(occurance.answer==0){
                                            String donut = answers[0] + " occured " + occurance.occurances;
                                            a.setText(donut);
                                        } else if(occurance.answer==1){
                                            String donut = answers[1] + " occured " + occurance.occurances;
                                            b.setText(donut);
                                        } else if(occurance.answer==2){
                                            String donut = answers[2] + " occured " + occurance.occurances;
                                            c.setText(donut);
                                        } else if(occurance.answer==3){
                                            String donut = answers[3] + " occured " + occurance.occurances;
                                            d.setText(donut);
                                        }
                                    }



                                }
                            }
                        }

                    }
                });
            }
        });
    }

    /*private void setOnClickListeners2() {
        Button mostcommon = FloatingView2.findViewById(R.id.mostcommon);
        Button leastcommon = FloatingView2.findViewById(R.id.leastcommon);

        mostcommon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                most_common_true_least_common_false = true;
                launch_main();
            }
        });
        leastcommon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                most_common_true_least_common_false = false;
                launch_main();
            }
        });
    }*/

    private void launch_main() {
        if(!addedview) {
            mWindowManager.addView(FloatingView, LayoutParams);
            /*mWindowManager.removeView(FloatingView2);*/
            addedview = true;
        }

        questione = getBundleExtras(intentnig);
        question.setText(questione);
        a.setText(answers[0]);
        b.setText(answers[1]);
        c.setText(answers[2]);
        d.setText(answers[3]);

        once = true;
        browser.loadUrl("https://www.google.com/search?q=" + questione);
    }

    private String questione;
    private int find_most_common_or_least_common(int[] counts) {
        int most_or_least_common = 0;
        if(most_common_true_least_common_false){
            for(int i=0; i<counts.length; i++){
                if(counts[i]>=counts[most_or_least_common]){
                    most_or_least_common = i;
                }
            }
        } else {
            for(int i=0; i<counts.length; i++){
                if(counts[i]<=counts[most_or_least_common]){
                    most_or_least_common = i;
                }
            }
        }
        return most_or_least_common;
    }

    public class Occurance {
        public int answer;
        public int occurances;
    }
    private boolean most_common_true_least_common_false = true;
    private boolean if_other_answers_appear_the_same_amount_then_add_them_into_array(List<Occurance> occurancehandler, int[] counts, int most_common) {

        // is another answer occuring the exact same amount
        /*boolean unique = true;
        for(int i=0; i<counts.length; i++){
            if(i!=most_common){
                if(counts[i]==counts[most_common]){
                    unique = false;
                }
            }
        }*/

        // are all others zero or all others absolutely inoccurant, if so then unique
        boolean unique = true;
        if(most_common_true_least_common_false){
            for(int i=0; i<counts.length; i++){
                if(i!=most_common){
                    if(counts[i]!=0){
                        unique = false;
                        break;
                    }
                }
            }
        } else {
            for(int i=0; i<counts.length; i++){
                if(i!=most_common){
                    if(counts[i]==0){
                        unique = false;
                        break;
                    }
                }
            }
        }

        for(int i=0; i<counts.length; i++){
            if(counts[i]!=0){
                Occurance occurance = new Occurance();
                occurance.answer = i;
                occurance.occurances = counts[i];
                occurancehandler.add(occurance);
            }
        }
        return unique;
    }

    private int[] count_occurances_of_the_answers_in_google_by_splitting_their_words_for_more_accuracy(String html) {
        int[] counts = {0, 0, 0, 0};
        for(int i=0; i<answers.length; i++){
            String[] answersplit = answers[i].split(" ");
            for(String string:answersplit){
                if(!string.equals("")){
                    int lastIndex = 0;
                    while(lastIndex != -1){

                        lastIndex = html.indexOf(string,lastIndex);

                        if(lastIndex != -1){
                            counts[i]++;
                            lastIndex += string.length();
                        }
                    }
                }
            }
        }
        return counts;
    }

    private void remove_alif_wel_lam_from_answers() {
        // TODO remove alif wel lam from answers words
        for(int i=0; i<answers.length; i++){
            String[] answersplitbyspaces = answers[i].split(" ");
            answers[i] = "";
            for(String string: answersplitbyspaces){
                if(String.valueOf(string.charAt(0)).equals("ا") && String.valueOf(string.charAt(1)).equals("ل")){
                    string = string.substring(2);
                }
                answers[i] += string + " ";
            }
            answers[i] = answers[i].substring(0, answers[i].length()-1);
        }
    }

    private boolean all_zero(int[] counts) {
        boolean yes = true;
        for(int i=1; i<counts.length; i++){
            yes &= counts[0]-counts[i]==0;
        }
        return yes;
    }

    private void print(Object i) {
        Toast.makeText(context, String.valueOf(i), Toast.LENGTH_LONG).show();
    }

    private Intent intentnig;
    private boolean once = true;
    private String getBundleExtras(Intent intent) {
        Bundle b = intent.getExtras();
        if (b != null) {
            /*display.setText(b.getString("question"));*/
            answers[0] = b.getString("answer1");
            answers[1] = b.getString("answer2");
            answers[2] = b.getString("answer3");
            answers[3] = b.getString("answer4");
            return b.getString("question");
        }
        return "nigga";
    }

    private String[] answers = {null, null, null, null};
    private WebView browser;
    private TextView a, b, c, d, question;
    private Button recheck;
    private RelativeLayout browserholder;
    private void setOnClickListeners() {
        a = FloatingView.findViewById(R.id.a);
        b = FloatingView.findViewById(R.id.b);
        c = FloatingView.findViewById(R.id.c);
        d = FloatingView.findViewById(R.id.d);
        question = FloatingView.findViewById(R.id.question);
        recheck = FloatingView.findViewById(R.id.recheck);
        close = FloatingView.findViewById(R.id.close);
        browser = FloatingView.findViewById(R.id.browser);
        browserholder = FloatingView.findViewById(R.id.browserholder);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hide_main_page();
            }
        });
        recheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                once = true;
                Log.i("HH", browser.getUrl());
                get_html_of_page_and_treat_it();
            }
        });
    }

    private void hide_main_page() {
        intentnig = null;
        mWindowManager.removeView(FloatingView);
        addedview = false;
    }

    private void createNotification() {
        if (SDK_INT >= O) {
            createOwnNotificationChannel();
        } else {
            startForeground(2, setup_notification_skipping_channel_creation(new NotificationCompat.Builder(context)));
        }
    }

    @RequiresApi(api = O)
    private void createOwnNotificationChannel() {
        String NOTIFICATION_CHANNEL_ID = "scuffedbots.quizzter";
        String channelName = "QuizzterChannel";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        Builder builder = new Builder(context, NOTIFICATION_CHANNEL_ID);
        manager.createNotificationChannel(chan);

        Notification notification = setup_notification_skipping_channel_creation(builder);

        startForeground(3, notification);
    }

    private Notification setup_notification_skipping_channel_creation(Builder builder) {

        builder = builder.setOngoing(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Quizzter is on");

        if (SDK_INT >= Build.VERSION_CODES.N) {
            builder.setPriority(NotificationManager.IMPORTANCE_HIGH);
        }
        if (SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setCategory(Notification.CATEGORY_SERVICE);
        }
        return builder.build();
    }

    private void createFloatingViews() {

        FrameLayout wrapper = new FrameLayout(context) {
            @Override
            public boolean dispatchKeyEvent(KeyEvent event) {
                if (event.getKeyCode()==KeyEvent.KEYCODE_BACK) {
                    // handle the back button code;
                    hide_main_page();
                    return true;
                } else if(event.getKeyCode()==KeyEvent.KEYCODE_HOME){
                    hide_main_page();
                    return true;
                }
                return super.dispatchKeyEvent(event);
            }

        };

        FloatingView = LayoutInflater.from(context).inflate(R.layout.bubble_main, wrapper);
    }
   /* private void createFloatingViews2() {
        FloatingView2 = LayoutInflater.from(context).inflate(R.layout.bubble_side, null);
    }*/

    private void createLayoutParams() {
        int layoutFlag = createLayoutFlag();

        WindowManager.LayoutParams createdLayoutParams;
        createdLayoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutFlag, 0,
                PixelFormat.TRANSLUCENT);
        createdLayoutParams.gravity = Gravity.TOP | Gravity.START;
        createdLayoutParams.x = 0;
        createdLayoutParams.y = 0;
        this.LayoutParams = createdLayoutParams;
    }

    /*private void createLayoutParams2() {
        int layoutFlag = createLayoutFlag();

        WindowManager.LayoutParams createdLayoutParams;
        if(SDK_INT >= O){
            createdLayoutParams = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSPARENT);
        } else {
            createdLayoutParams = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSPARENT);
        }
        createdLayoutParams.gravity = Gravity.TOP | Gravity.START;
        createdLayoutParams.x = 0;
        createdLayoutParams.y = 0;
        this.LayoutParams2 = createdLayoutParams;
    }*/

    private int createLayoutFlag() {
        if (SDK_INT >= O) {
            return WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            return WindowManager.LayoutParams.TYPE_PHONE;
        }
    }

    private void createWindowManager() {
        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
    }

}