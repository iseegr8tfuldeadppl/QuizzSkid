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
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationCompat.Builder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import scuffedbots.quizzter.Debugging.TopExceptionHandler;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.O;


public class FloatingViewService extends Service{

    protected static final String NEW_QUESTION_INTENT = "scuffedbots.quizzter.NEW_QUESTION";
    private WindowManager.LayoutParams LayoutParams;
    private WindowManager mWindowManager;
    private Context context;
    private View FloatingView;
    private boolean addedview = false,
            running = true,
            most_common_true_least_common_false = true;
    private Intent intentnig;
    private String[] answers = {null, null, null, null};
    private WebView browser;
    private TextView a, b, c, d,
                     acount, bcount, ccount, dcount,
                     question, timestamps;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(NEW_QUESTION_INTENT);
        registerReceiver(receiver, filter);
    }

    @Override
    public void onDestroy() {
        running = false;
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    private void extra_scraper(final String question_link) {
        //Connect to website
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Document document = Jsoup.connect(question_link).get();

                    treat_this_html(document.body().text());

                    List<String> links = get_all_links_from_this_google_search(document);

                    for(String link:links) {
                        if(intentnig!=null)
                            other_page_scraper(link);
                        else
                            break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private List<String> get_all_links_from_this_google_search(Document document) {
        List<String> links = new ArrayList<>();
        //Get the logo source of the website
        Elements linkHolders = document.select("div");
        for(Element linkHolder:linkHolders){
            if(linkHolder.className().equals("g")){
                String link = linkHolder.selectFirst("a").absUrl("href");
                if(not_a_link_we_dont_want(link)){
                    links.add(link);
                }
            }
        }
        return links;
    }

    private void other_page_scraper(String link) {
        try {
            Document document = Jsoup.connect(link).get();
            treat_this_html(document.body().text());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean not_a_link_we_dont_want(String link) {
        return !(link.contains("youtube.") || link.contains("dailymotion.") || link.contains("facebook.")
                || link.contains("fb.com"));
    }

    private void start_countdown() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                running = true;
                int start_second = Integer.parseInt((new Date()).toString().split(" ")[3].split(":")[2]);
                int span;
                while(running){
                    wait_1_second();
                    span = calculate_span(start_second);
                    if(span>=11) running = false;
                    else trigger_update_timestamp(span);
                }
            }
        }).start();
    }

    private int calculate_span(int start_second) {
        int now_second = Integer.parseInt((new Date()).toString().split(" ")[3].split(":")[2]);
        int span = now_second - start_second;
        if(span<0) span += 60;
        return span;
    }

    private void trigger_update_timestamp(int span) {
        Message msg = new Message();
        Bundle b = new Bundle();
        b.putString("timestamp", (11-span) + "s");
        msg.setData(b);
        update_timestamp.sendMessage(msg);
    }

    private void wait_1_second() {
        long futuretime = System.currentTimeMillis() + 1000;

        while (System.currentTimeMillis() < futuretime && running) {
            synchronized (this) {
                try {
                    wait(futuretime - System.currentTimeMillis());
                } catch(Exception ignored){}
            }
        }
    }

    private void update_timestamp(String timestamp) {
        timestamps.setText(timestamp);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());
        createNotification();
        createWindowManager();
        createLayoutParams();
        createFloatingViews();
        createBroadcastReceiver();
        setOnClickListeners();
        /*DONT DELETE ME*/page_load_ensurance();
        /*tests();*/
    }

    private void tests() {
        answers[0] = "blue";
        answers[1] = "red";
        answers[2] = "green";
        answers[3] = "yellow";
        mWindowManager.addView(FloatingView, LayoutParams);
        browser.loadUrl("https://www.google.com/search?q=what is the color of the sun?");
        extra_scraper("https://www.google.com/search?q=what is the color of the sun?");
    }

    private void page_load_ensurance() {
        browser.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(browser, url);
                Log.i("HH", "finished loading, don't remove this webviewclient he allows pages to finish loading");

            }
        });
    }


    private void treat_this_html(String html) {
        remove_unwanted_characters_from_answers();
        remove_alif_wel_lam_from_answers();
        int[] counts = count_occurances_of_the_answers_in_google_by_splitting_their_words_for_more_accuracy(html);

        if(!all_zero(counts)){
            int most_or_least_common = find_most_common_or_least_common(counts);
            List<Occurance> occurancehandler = new ArrayList<>();
            boolean unique = if_other_answers_appear_the_same_amount_then_add_them_into_array(occurancehandler, counts, most_or_least_common);

            if(unique && intentnig!=null)
                print(most_or_least_common+1);

            display_occurances(occurancehandler);
        }
    }

    private void remove_unwanted_characters_from_answers() {
        for(int i=0; i<answers.length; i++){
            answers[i] = answers[i].replace("-", " ");
        }
    }

    private void display_occurances(List<Occurance> occurancehandler) {
        for(Occurance occurance:occurancehandler){
            Message msg = new Message();
            Bundle b = new Bundle();
            b.putInt("answer", occurance.answer);
            b.putInt("occurances", occurance.occurances);
            msg.setData(b);
            update_count.sendMessage(msg);
        }
    }

    private String launch_main() {
        if(!addedview) {
            mWindowManager.addView(FloatingView, LayoutParams);
            addedview = true;
        }

        String questiono = getBundleExtras(intentnig);
        question.setText(questiono);
        a.setText(answers[0]);
        b.setText(answers[1]);
        c.setText(answers[2]);
        d.setText(answers[3]);

        questiono = "https://www.google.com/search?q=" + questiono;
        extra_scraper(questiono);
        browser.loadUrl(questiono);
        return questiono;
    }

    private int find_most_common_or_least_common(int[] counts) {
        if(most_common_true_least_common_false){
            return find_most_common(counts);
        } else {
            return find_least_common(counts);
        }
    }

    private int find_least_common(int[] counts) {
        int most_or_least_common = 0;
        for(int i=0; i<counts.length; i++){
            if(counts[i]<=counts[most_or_least_common]){
                most_or_least_common = i;
            }
        }
        return most_or_least_common;
    }

    private int find_most_common(int[] counts) {
        int most_or_least_common = 0;
        for(int i=0; i<counts.length; i++){
            if(counts[i]>=counts[most_or_least_common]){
                most_or_least_common = i;
            }
        }
        return most_or_least_common;
    }

    private boolean if_other_answers_appear_the_same_amount_then_add_them_into_array(List<Occurance> occurancehandler, int[] counts, int most_common) {

        // are all others zero or all others absolutely inoccurant, if so then unique
        boolean unique;
        if(most_common_true_least_common_false){
            unique = is_it_the_only_non_zero(counts, most_common);
        } else {
            unique = is_it_the_only_zero(counts, most_common);
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

    private boolean is_it_the_only_zero(int[] counts, int most_common) {
        for(int i=0; i<counts.length; i++){
            if(i!=most_common){
                if(counts[i]==0){
                    return false;
                }
            }
        }
        return true;
    }

    private boolean is_it_the_only_non_zero(int[] counts, int most_common) {
        for(int i=0; i<counts.length; i++){
            if(i!=most_common){
                if(counts[i]!=0){
                    return false;
                }
            }
        }
        return true;
    }

    private int[] count_occurances_of_the_answers_in_google_by_splitting_their_words_for_more_accuracy(String html) {
        int[] counts = {0, 0, 0, 0};
        for(int i=0; i<answers.length; i++){
            String[] words_of_this_answer = answers[i].split(" ");
            for(String word:words_of_this_answer){
                if(!word.equals(""))
                    counts[i] = count_its_occurances(html, word);
            }
        }
        return counts;
    }

    private int count_its_occurances(String html, String string) {
        int count = 0;
        int lastIndex = 0;
        while(lastIndex != -1){
            lastIndex = html.indexOf(string,lastIndex);
            if(lastIndex != -1){
                count++;
                lastIndex += string.length();
            }
        }
        return count;
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

    private void print(Object log) {
        Message msg = new Message();
        Bundle b = new Bundle();
        b.putString("log", String.valueOf(log));
        msg.setData(b);
        print.sendMessage(msg);
    }

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

    private void setOnClickListeners() {
        acount = FloatingView.findViewById(R.id.acount);
        bcount = FloatingView.findViewById(R.id.bcount);
        ccount = FloatingView.findViewById(R.id.ccount);
        dcount = FloatingView.findViewById(R.id.dcount);
        a = FloatingView.findViewById(R.id.a);
        b = FloatingView.findViewById(R.id.b);
        c = FloatingView.findViewById(R.id.c);
        d = FloatingView.findViewById(R.id.d);
        question = FloatingView.findViewById(R.id.question);
        timestamps = FloatingView.findViewById(R.id.timestamps);
        browser = FloatingView.findViewById(R.id.browser);

        FloatingView.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hide_main_page();
            }
        });
    }

    private void hide_main_page() {
        intentnig = null;
        acount.setText("0");
        bcount.setText("0");
        ccount.setText("0");
        dcount.setText("0");
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

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action!=null){
                if(action.equals(NEW_QUESTION_INTENT)) {
                    if(intentnig==null){
                        intentnig = intent;
                        extra_scraper(launch_main());
                        start_countdown();
                    }
                }
            }
        }
    };

    private Handler update_count = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            int answer = msg.getData().getInt("answer");
            int occurances = msg.getData().getInt("occurances");
            if(intentnig!=null){
                if(answer==0){
                String count = acount.getText().toString();
                if(!count.contains("(")){
                    if(count.equals("0")){
                        acount.setText(String.valueOf(occurances));
                    } else {
                        String f = occurances + "(" + count + ")";
                        acount.setText(f);
                    }
                } else {
                    String[] splitted = count.split("\\(");
                    String f = (occurances+Integer.parseInt(splitted[0])) + "(" + splitted[1];
                    acount.setText(f);
                }
                } else if(answer==1){
                    String count = bcount.getText().toString();
                    if(!count.contains("(")){
                        if(count.equals("0")){
                            bcount.setText(String.valueOf(occurances));
                        } else {
                            String f = occurances + "(" + count + ")";
                            bcount.setText(f);
                        }
                    } else {
                        String[] splitted = count.split("\\(");
                        String f = (occurances+Integer.parseInt(splitted[0])) + "(" + splitted[1];
                        bcount.setText(f);
                    }
                } else if(answer==2){
                    String count = ccount.getText().toString();
                    if(!count.contains("(")){
                        if(count.equals("0")){
                            ccount.setText(String.valueOf(occurances));
                        } else {
                            String f = occurances + "(" + count + ")";
                            ccount.setText(f);
                        }
                    } else {
                        String[] splitted = count.split("\\(");
                        String f = (occurances+Integer.parseInt(splitted[0])) + "(" + splitted[1];
                        ccount.setText(f);
                    }
                } else if(answer==3){
                    String count = dcount.getText().toString();
                    if(!count.contains("(")){
                        if(count.equals("0")){
                            dcount.setText(String.valueOf(occurances));
                        } else {
                            String f = occurances + "(" + count + ")";
                            dcount.setText(f);
                        }
                    } else {
                        String[] splitted = count.split("\\(");
                        String f = (occurances+Integer.parseInt(splitted[0])) + "(" + splitted[1];
                        dcount.setText(f);
                    }
                }
            }
            return true; }});

    private Handler update_timestamp = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            String timestamp = msg.getData().getString("timestamp");
            update_timestamp(timestamp);
            return true; }});

    private Handler print = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            String log = msg.getData().getString("log");
            Toast.makeText(context, log, Toast.LENGTH_LONG).show();
            return true; }});

    public static class Occurance {
        int answer;
        int occurances;
    }
}