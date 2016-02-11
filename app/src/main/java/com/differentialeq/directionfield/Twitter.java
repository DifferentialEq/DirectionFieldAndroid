package com.differentialeq.directionfield;

import android.content.Intent;
import android.media.session.MediaSessionManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.core.internal.TwitterApi;
import com.twitter.sdk.android.core.internal.TwitterApiConstants;
import com.twitter.sdk.android.core.internal.TwitterCollection;
import com.twitter.sdk.android.core.models.Media;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.CollectionService;
import com.twitter.sdk.android.core.services.MediaService;
import com.twitter.sdk.android.core.services.StatusesService;
import com.twitter.sdk.android.tweetcomposer.Card;
import com.twitter.sdk.android.tweetcomposer.ComposerActivity;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;
import com.twitter.sdk.android.tweetcomposer.internal.CardCreate;
import com.twitter.sdk.android.tweetui.TweetView;

import java.io.File;

import retrofit.mime.TypedFile;

public class Twitter extends AppCompatActivity {
    private TwitterLoginButton loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(android.R.style.Theme_Holo);
        setContentView(R.layout.activity_twitter);
        Bundle ImageInfo = getIntent().getExtras();
        final Uri imageUri = Uri.parse(ImageInfo.getString("imageUri"));
        final String MatrixName = ImageInfo.getString("MatrixName");
        final RelativeLayout TwitterLayout = (RelativeLayout)findViewById(R.id.TwitterLayout);

        loginButton = (TwitterLoginButton) findViewById(R.id.login_button);
        loginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                // Do something with result, which provides a TwitterSession for making API calls
                final TwitterSession session = TwitterCore.getInstance().getSessionManager().getActiveSession();
                TwitterApiClient client = TwitterCore.getInstance().getApiClient(session);
                MediaService mservice = client.getMediaService();
                final StatusesService statusesService = client.getStatusesService();
                TypedFile typedFile = new TypedFile("application/octet-stream", new File(imageUri.getPath()));
                mservice.upload(typedFile, null, null, new Callback<Media>() {
                    @Override
                    public void success(Result<Media> result) {
                        statusesService.update("Matrix: " + MatrixName, null, false, 0.0, 0.0, null, true, false, result.data.mediaIdString, new Callback<Tweet>() {
                            @Override
                            public void success(Result<Tweet> result) {
                                // we posted a tweet!!!!
                                // Now we can show the tweet result
                                TwitterLayout.removeView(loginButton);
                                TwitterLayout.addView(new TweetView(Twitter.this, result.data));

                            }

                            @Override
                            public void failure(TwitterException e) {
                                Log.i("ZZZ", "Failed to send tweet");
                            }
                        });
                    }

                    @Override
                    public void failure(TwitterException e) {
                        Log.i("ZZZ", "Failed to send media");
                    }
                });

            }

            @Override
            public void failure(TwitterException exception) {
                // Do something on failure
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result to the login button.
        loginButton.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        final Intent intent = new Intent(this, SelectMatrixOrNew.class);
        startActivity(intent);
    }
}
