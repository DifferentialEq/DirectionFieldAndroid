package com.differentialeq.directionfield;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.RelativeLayout;
import android.view.ViewGroup.LayoutParams;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;

import io.fabric.sdk.android.Fabric;
import java.util.ArrayList;

public class welcomeScreen extends AppCompatActivity {

    private DBManager DBmgr;
    Handler handleTextChange = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            TextView mainText = (TextView)findViewById(R.id.WelcomeText);
            mainText.setText(msg.getData().getString("status"));
        }
    };

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));
        Fabric.with(this, new TweetComposer(), new Twitter(authConfig));
        final Intent i = new Intent(this, SelectMatrixOrNew.class); // Move to the bacon activity!
        DBManager DBmgr = new DBManager(this);
        ArrayList<String> matrixNames = DBmgr.getMatrixNames();
        setTheme(android.R.style.Theme_Holo);
        setContentView(R.layout.activity_welcome_screen);

        if (matrixNames.size() != 0){
            // When it's a repeat visit Let's make a little runnable saying we're loading and meanwhile load the other activity
            // Though, generally this is too fast to be seen so it looks like we are jumping to the selection screen.
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    Message waitMsg = new Message();
                    Bundle contentMsg = new Bundle();
                    contentMsg.putString("status", "DirectionField\nLoading....");
                    waitMsg.setData(contentMsg);
                    handleTextChange.sendMessage(waitMsg);

                }
            };
            Thread loading = new Thread(r); // calls the run method on the r Runnable object
            loading.start();
            startActivity(i);
        } else {
            DBManager MatrixDB = new DBManager(this); // Don't do anything with it though... just creating the first association with it
            // Let's display something on the screen when it's the first time
            RelativeLayout.LayoutParams layoutRel = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            layoutRel.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            layoutRel.addRule(RelativeLayout.CENTER_HORIZONTAL);
            final Button clickToContinue = new Button(this);
            clickToContinue.setText("Click to load");
            RelativeLayout welcomeLayout = (RelativeLayout)findViewById(R.id.welcomeLayout);
            welcomeLayout.addView(clickToContinue, layoutRel); // insert the button into the view and position it according to the rules
            clickToContinue.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(i);
                }
            });

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_welcome_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /*Debug purpose only, reference: http://stackoverflow.com/a/14213035  */
    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
