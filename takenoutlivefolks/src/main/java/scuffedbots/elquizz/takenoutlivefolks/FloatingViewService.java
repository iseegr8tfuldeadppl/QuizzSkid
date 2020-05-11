package scuffedbots.elquizz.takenoutlivefolks;

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
import android.graphics.drawable.Drawable;
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
import scuffedbots.elquizz.takenoutlivefolks.Debugging.TopExceptionHandler;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.O;


public class FloatingViewService extends Service{


    protected static final String NEW_QUESTION_INTENT = "scuffedbots.elquizz.takenoutlivefolks.NEW_QUESTION";
    private WindowManager.LayoutParams LayoutParams;
    private WindowManager mWindowManager;
    private Context context;
    private View FloatingView;
    private boolean addedview = false, running = true;
    private Intent intentnig;
    private String[] answers = {null, null, null, null};
    private WebView browser;
    private TextView question, timestamps;
    private List<TextView> answerdisplays = new ArrayList<>(), countdisplays = new ArrayList<>(), countdisplays2 = new ArrayList<>();
    private int[] its_background = {0,0,0,0}; // 0 for green, 1 for red, 2 for white
    private int[] its_background_google = {0,0,0,0}; // 0 for green, 1 for red, 2 for white


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
                    String[] prefixes = {  " wikipedia ", ""};
                    for(String prefix:prefixes){
                        Document document = Jsoup.connect(question_link + prefix).get();

                        treat_this_html(document.body().text(), true);

                        List<String> links = get_all_links_from_this_google_search(document);

                        for(String link:links) {
                            if(intentnig!=null)
                                other_page_scraper(link);
                            else
                                return;
                        }
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
        Elements linkHolders = document.select(getString(R.string.div));
        for(Element linkHolder:linkHolders){
            if(linkHolder.className().equals(getString(R.string.g))){
                String link = linkHolder.selectFirst(getString(R.string.a)).absUrl(getString(R.string.href));
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
            treat_this_html(document.body().text(), false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean not_a_link_we_dont_want(String link) {
        return !(link.contains(getString(R.string.youtube)) || link.contains(getString(R.string.dailymotion)) || link.contains(getString(R.string.facebook))
                || link.contains(getString(R.string.facebook2)));
    }

    private void start_countdown() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                running = true;
                int start_second = Integer.parseInt((new Date()).toString().split(" ")[3].split(":")[2]);
                int span;
                while(running && intentnig!=null){
                    wait_1_second();
                    span = calculate_span(start_second);

                    if(span>=10) running = false;
                    else trigger_update_timestamp(span, 10);

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

    private void trigger_update_timestamp(int span, int limit) {
        Message msg = new Message();
        Bundle b = new Bundle();
        b.putString(getString(R.string.timestamp), (limit-span) + getString(R.string.s));
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

        // you need to save context from here
        context = this;

        // debugging tool
        Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        createNotification();
        createWindowManager();
        createLayoutParams();
        createFloatingViews();
        createBroadcastReceiver();
        setOnClickListeners();
        /*DONT DELETE ME*/page_load_ensurance();
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

    private void treat_this_html(String html, boolean google_true_other_side_false) {
        remove_unwanted_characters_from_answers();
        remove_alif_wel_lam_from_answers();
        int[] counts = count_occurances_of_the_answers_in_google_by_splitting_their_words_for_more_accuracy(html);

        if(intentnig==null)
            return;

        display_occurances(counts, google_true_other_side_false);
    }

    private void color_most_common(int most_common, boolean google_true_other_side_false) {
        Message msg = new Message();
        Bundle b = new Bundle();
        b.putInt(getString(R.string.most_common), most_common);
        b.putBoolean("google_true_other_side_false", google_true_other_side_false);
        msg.setData(b);
        color_most_common.sendMessage(msg);
    }

    private void color_least_common(int least_common, boolean google_true_other_side_false) {
        Message msg = new Message();
        Bundle b = new Bundle();
        b.putInt(getString(R.string.least_common), least_common);
        b.putBoolean("google_true_other_side_false", google_true_other_side_false);
        msg.setData(b);
        color_least_common.sendMessage(msg);
    }

    private void remove_unwanted_characters_from_answers() {
        for(int i=0; i<answers.length; i++){
            answers[i] = answers[i].replace("-", " ");
        }
    }

    private void display_occurances(int[] counts, boolean google_true_other_side_false) {
        int index = -1;
        for(int count:counts){
            index ++;
            Message msg = new Message();
            Bundle b = new Bundle();
            b.putInt(getString(R.string.occurrences), count);
            b.putInt(getString(R.string.answer), index);
            b.putBoolean("google_true_other_side_false", google_true_other_side_false);
            msg.setData(b);
            update_count.sendMessage(msg);
        }
    }

    private void launch_main() {
        if(!addedview) {
            mWindowManager.addView(FloatingView, LayoutParams);
            addedview = true;
        }

        String questiono = getBundleExtras(intentnig);
        question.setText(questiono);


        // display answers
        for(int i=0; i<4; i++){
            answerdisplays.get(i).setText(answers[i]);
        }

        try{
            if(questiono.contains(" ليس ") || questiono.contains(" لم ") || questiono.contains(" لا "))
                print("NEGATIVE QUESTION!");
        } catch(Exception ignored){}

        questiono = getString(R.string.google_search_prefix) + questiono;

        // add the answers into the search
        /*for(int i=0; i<4; i++){
            questiono += " " + answers[i];
        }*/

        try{
            double test = Double.parseDouble(answers[0]);
            for(TextView answerdisplay:answerdisplays)
                answerdisplay.setGravity(Gravity.END);
            print("NUMBER ANSWERS");
        } catch(Exception ignored){
            try{
                for(TextView answerdisplay:answerdisplays)
                    answerdisplay.setGravity(Gravity.START);
            } catch(Exception ignored2){}
        }

        extra_scraper(questiono);
        browser.loadUrl(questiono);
    }


    private void log(Object log) {
        Log.i("HH", String.valueOf(log));
    }

    private int[] count_occurances_of_the_answers_in_google_by_splitting_their_words_for_more_accuracy(String html) {
        int[] counts = {0, 0, 0, 0};
        for(int i=0; i<answers.length; i++){
            String[] words_of_this_answer = answers[i].split(" ");
            for(String word:words_of_this_answer){
                if(word.length()>1)
                    counts[i] += count_its_occurances(html, word);
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
            String save = answers[i];
            try{
                answers[i] = "";
                for(String string: answersplitbyspaces){
                    if(string.length()>2)
                        if(String.valueOf(string.charAt(0)).equals(getString(R.string.alif)) && String.valueOf(string.charAt(1)).equals(getString(R.string.lam))){
                            string = string.substring(2);
                        }
                    answers[i] += string + " ";
                }
                if(String.valueOf(answers[i].charAt(answers[i].length()-1)).equals(" "))
                    answers[i] = answers[i].substring(0, answers[i].length()-1);
            } catch(Exception ignored){
                answers[i] = save;
            }
        }
    }

    private void print(Object log) {
        Message msg = new Message();
        Bundle b = new Bundle();
        b.putString(getString(R.string.log), String.valueOf(log));
        msg.setData(b);
        print.sendMessage(msg);
    }

    private String getBundleExtras(Intent intent) {
        Bundle b = intent.getExtras();
        if (b != null) {
            answers[0] = b.getString(getString(R.string.answer1));
            answers[1] = b.getString(getString(R.string.answer2));
            answers[2] = b.getString(getString(R.string.answer3));
            answers[3] = b.getString(getString(R.string.answer4));
            try{
                answers[0] = answers[0].replace("-", " ").replace("  ", " ");
                answers[1] = answers[1].replace("-", " ").replace("  ", " ");
                answers[2] = answers[2].replace("-", " ").replace("  ", " ");
                answers[3] = answers[3].replace("-", " ").replace("  ", " ");
            } catch(Exception ignored){}
            return b.getString(getString(R.string.question));
        }
        return "";
    }

    private void setOnClickListeners() {
        countdisplays.add((TextView) FloatingView.findViewById(R.id.acount));
        countdisplays.add((TextView) FloatingView.findViewById(R.id.bcount));
        countdisplays.add((TextView) FloatingView.findViewById(R.id.ccount));
        countdisplays.add((TextView) FloatingView.findViewById(R.id.dcount));
        countdisplays2.add((TextView) FloatingView.findViewById(R.id.acount2));
        countdisplays2.add((TextView) FloatingView.findViewById(R.id.bcount2));
        countdisplays2.add((TextView) FloatingView.findViewById(R.id.ccount2));
        countdisplays2.add((TextView) FloatingView.findViewById(R.id.dcount2));
        answerdisplays.add((TextView) FloatingView.findViewById(R.id.a));
        answerdisplays.add((TextView) FloatingView.findViewById(R.id.b));
        answerdisplays.add((TextView) FloatingView.findViewById(R.id.c));
        answerdisplays.add((TextView) FloatingView.findViewById(R.id.d));
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
        if(addedview){
            mWindowManager.removeView(FloatingView);
            addedview = false;
        }
        clear();

    }

    private void clear() {
        intentnig = null;

        timestamps.setText(getString(R.string.ten));

        int black = getDatColor(R.color.black);
        for(int i=0; i<4; i++){
            its_background[i] = 2;
            its_background_google[i] = 2;

            countdisplays.get(i).setTextColor(black);
            setDrawable(countdisplays.get(i), R.drawable.white);
            countdisplays.get(i).setText(getString(R.string._0));
            countdisplays2.get(i).setTextColor(black);
            setDrawable(countdisplays2.get(i), R.drawable.white);
            countdisplays2.get(i).setText(getString(R.string._0));
        }


    }

    private int getDatColor(int black) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
            return getColor(black);
        else
            return getResources().getColor(black);
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
        String NOTIFICATION_CHANNEL_ID = getString(R.string.channel_id);
        String channelName = getString(R.string.channel_name);
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
                .setContentTitle(getString(R.string.notification_title));

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
                    if(browser.canGoBack()){
                        browser.goBack();
                    } else {
                        hide_main_page();
                    }
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
                        clear();
                        intentnig = intent;
                        start_countdown();
                        launch_main();
                    }
                }
            }
        }
    };

    private Handler update_count = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            int answer = msg.getData().getInt(getString(R.string.answer));
            int occurances = msg.getData().getInt(getString(R.string.occurrences));
            boolean google_true_other_side_false = msg.getData().getBoolean("google_true_other_side_false");
            if(intentnig!=null){
                if (google_true_other_side_false) {
                    String count = countdisplays2.get(answer).getText().toString();
                    countdisplays2.get(answer).setText(String.valueOf(occurances+Integer.parseInt(count)));

                    int[] total_for_each_answer = get_total2_for_each_answer();
                    int largest = get_largest(total_for_each_answer);
                    int tiniest = get_tiniest(total_for_each_answer);

                    color_most_common(largest, true);
                    color_least_common(tiniest, true);
                } else {
                    String count = countdisplays.get(answer).getText().toString();
                    countdisplays.get(answer).setText(String.valueOf(occurances+Integer.parseInt(count)));

                    int[] total_for_each_answer = get_total_for_each_answer();
                    int largest = get_largest(total_for_each_answer);
                    int tiniest = get_tiniest(total_for_each_answer);

                    color_most_common(largest, false);
                    color_least_common(tiniest, false);
                }

            } else {
                hide_main_page();
            }
            return true; }});

    private int get_tiniest(int[] total_for_each_answer) {
        int tiniest = 0;
        for(int i=1; i<total_for_each_answer.length; i++){
            if(total_for_each_answer[i]<=total_for_each_answer[tiniest]){
                tiniest = i;
            }
        }
        return tiniest;
    }

    private int get_largest(int[] total_for_each_answer) {
        int largest = 0;
        for(int i=1; i<total_for_each_answer.length; i++){
            if(total_for_each_answer[i]>=total_for_each_answer[largest]){
                largest = i;
            }
        }
        return largest;
    }

    private int[] get_total_for_each_answer() {
        int[] bruh = {0,0,0,0};
        int index = -1;
        for(TextView countdisplay:countdisplays){
            index ++;
            String text = countdisplay.getText().toString();
            if(text.contains(getString(R.string.kaws2))){
                bruh[index] = Integer.parseInt(text.split(getString(R.string.kaws))[0]);
            } else {
                bruh[index] = Integer.parseInt(text);
            }
        }
        return bruh;
    }

    private int[] get_total2_for_each_answer() {
        int[] bruh = {0,0,0,0};
        int index = -1;
        for(TextView countdisplay:countdisplays2){
            index ++;
            bruh[index] = Integer.parseInt(countdisplay.getText().toString());
        }
        return bruh;
    }

    private Handler color_most_common = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            boolean google_true_other_side_false = msg.getData().getBoolean("google_true_other_side_false");
            int most_common = msg.getData().getInt(getString(R.string.most_common));
            if(intentnig!=null){
                if(google_true_other_side_false){
                    its_background_google[most_common] = 0;
                    countdisplays2.get(most_common).setTextColor(getDatColor(R.color.black));
                    setDrawable(countdisplays2.get(most_common), R.drawable.green);
                } else {
                    its_background[most_common] = 0;
                    countdisplays.get(most_common).setTextColor(getDatColor(R.color.black));
                    setDrawable(countdisplays.get(most_common), R.drawable.green);
                }

                reset_the_other_greens(most_common, google_true_other_side_false);
            } else {
                hide_main_page();
            }
            return true; }});


    private void setDrawable(TextView textView, int green) {
        if (SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            textView.setBackground(getDatDrawable(green));
        else
            textView.setBackgroundDrawable(getDatDrawable(green));
    }

    private Drawable getDatDrawable(int green) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
            return getDrawable(green);
        else
            return getResources().getDrawable(green);
    }

    private Handler color_least_common = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            int least_common = msg.getData().getInt(getString(R.string.least_common));
            boolean google_true_other_side_false = msg.getData().getBoolean("google_true_other_side_false");
            if(intentnig!=null){
                if(google_true_other_side_false){
                    its_background_google[least_common] = 1;

                    countdisplays2.get(least_common).setTextColor(getDatColor(R.color.white));
                    setDrawable(countdisplays2.get(least_common), R.drawable.red);
                } else {
                    its_background[least_common] = 1;

                    countdisplays.get(least_common).setTextColor(getDatColor(R.color.white));
                    setDrawable(countdisplays.get(least_common), R.drawable.red);
                }

                reset_the_other_reds(least_common, google_true_other_side_false);
                try{
                    if (google_true_other_side_false) {
                        for(int i=0; i<4; i++){
                            if(Integer.parseInt(countdisplays2.get(i).getText().toString())==0){
                                its_background_google[i] = 1;

                                countdisplays2.get(i).setTextColor(getDatColor(R.color.white));
                                setDrawable(countdisplays2.get(i), R.drawable.red);
                            }
                        }
                    } else {
                        for(int i=0; i<4; i++){
                            if(Integer.parseInt(countdisplays.get(i).getText().toString())==0){
                                its_background[i] = 1;

                                countdisplays.get(i).setTextColor(getDatColor(R.color.white));
                                setDrawable(countdisplays.get(i), R.drawable.red);
                            }
                        }
                    }
                } catch(Exception ignored){};
            } else {
                clear();
            }
            return true; }});

    private void reset_the_other_greens(int most_common, boolean google_true_other_side_false) {
        for(int i=0; i<4; i++){
            if(i!=most_common){
                if(google_true_other_side_false){
                    if(its_background_google[i]==0){
                        its_background_google[i] = 2;

                        countdisplays2.get(i).setTextColor(getDatColor(R.color.black));
                        setDrawable(countdisplays2.get(i), R.drawable.white);
                    }
                } else {
                    if(its_background[i]==0){
                        its_background[i] = 2;

                        countdisplays.get(i).setTextColor(getDatColor(R.color.black));
                        setDrawable(countdisplays.get(i), R.drawable.white);
                    }
                }
            }
        }
    }

    private void reset_the_other_reds(int least_common, boolean google_true_other_side_false) {
        for(int i=0; i<4; i++){
            if(i!=least_common){
                if (google_true_other_side_false) {
                    if(its_background_google[i]==1){
                        its_background_google[i] = 2;

                        countdisplays2.get(i).setTextColor(getDatColor(R.color.black));
                        setDrawable(countdisplays2.get(i), R.drawable.white);
                    }
                } else {
                    if(its_background[i]==1){

                        its_background[i] = 2;
                    /*answerdisplays.get(i).setTextColor(getDatColor(R.color.black));
                    setDrawable(answerdisplays.get(i), R.drawable.white);*/

                        countdisplays.get(i).setTextColor(getDatColor(R.color.black));
                        setDrawable(countdisplays.get(i), R.drawable.white);
                    }
                }
            }
        }
    }

    // this is for logging
    /*private void log(Object log){
        Log.i("HH", String.valueOf(log));
    }
    private void logAll() {
        log("addedview " + addedview);
        log("running " + running);
        log("intentnig " + intentnig);
        log("question " + question.getText().toString());
        log("answers[0] " + answers[0]);
        log("answers[1] " + answers[1]);
        log("answers[2] " + answers[2]);
        log("answers[3] " + answers[3]);
        log("acount " + countdisplays.get(0).getText().toString());
        log("bcount " + countdisplays.get(1).getText().toString());
        log("ccount " + countdisplays.get(2).getText().toString());
        log("dcount " + countdisplays.get(3).getText().toString());

    }*/

    private Handler update_timestamp = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            String timestamp = msg.getData().getString(getString(R.string.timestamp));
            update_timestamp(timestamp);
            return true; }});

    private Handler print = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            String log = msg.getData().getString(getString(R.string.log));
            Toast.makeText(context, log, Toast.LENGTH_LONG).show();
            return true; }});

}