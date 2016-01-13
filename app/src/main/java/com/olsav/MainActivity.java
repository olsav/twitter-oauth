package com.olsav;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import twitter4j.Paging;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class MainActivity extends Activity {
    private Twitter mTwitter;
    private RequestToken mRequestToken;

    private ListView list;

    java.util.List statuses = null;
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        Button getTweetsButton = (Button)findViewById(R.id.getFeedBtn);
        getTweetsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "Loading feed..", Toast.LENGTH_SHORT).show();
                try {
                    if (mTwitter == null) {
                        ConfigurationBuilder confbuilder = new ConfigurationBuilder();
                        Configuration conf = confbuilder
                                .setOAuthConsumerKey(Const.CONSUMER_KEY)
                                .setOAuthConsumerSecret(Const.CONSUMER_SECRET)
                                .build();
                        mTwitter = new TwitterFactory(conf).getInstance();
                    }

                    SharedPreferences pref = getSharedPreferences(Const.PREF_NAME, MODE_PRIVATE);
                    String accessToken = pref.getString(Const.PREF_KEY_ACCESS_TOKEN, null);
                    String accessTokenSecret = pref.getString(Const.PREF_KEY_ACCESS_TOKEN_SECRET, null);

                    if (accessToken == null || accessTokenSecret == null) {
                        Toast.makeText(MainActivity.this, "Not logged in yet", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    mTwitter.setOAuthAccessToken(new AccessToken(accessToken, accessTokenSecret));

                    EditText screenNameText = (EditText) findViewById(R.id.status);
                    String screenName = screenNameText.getText().toString();
                    Paging paging = new Paging(1, 20);
                    statuses = mTwitter.getUserTimeline(screenName, paging);

                } catch (TwitterException e) {
                    Toast.makeText(getApplicationContext(), "Fail: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.d("CGN", "Error: " + e.getMessage());
                }

                if (statuses != null) {
                    list=(ListView)findViewById(R.id.feedList);
                    FeedListAdapter adapter = new FeedListAdapter(MainActivity.this, statuses);
                    list.setAdapter(adapter);
                }
            }
        });

        switchToLogin();
    }

    private void switchToLogin() {
        ConfigurationBuilder confbuilder = new ConfigurationBuilder();
        Configuration conf = confbuilder
                .setOAuthConsumerKey(Const.CONSUMER_KEY)
                .setOAuthConsumerSecret(Const.CONSUMER_SECRET)
                .build();
        mTwitter = new TwitterFactory(conf).getInstance();
        mTwitter.setOAuthAccessToken(null);
        try {
            mRequestToken = mTwitter.getOAuthRequestToken(Const.CALLBACK_URL);
            Intent intent = new Intent(MainActivity.this, TwitterLogin.class);
            intent.putExtra(Const.IEXTRA_AUTH_URL, mRequestToken.getAuthorizationURL());
            startActivityForResult(intent, 0);
        } catch (TwitterException e) {
            e.printStackTrace();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                AccessToken accessToken = null;
                try {
                    String oauthVerifier = intent.getExtras().getString(Const.IEXTRA_OAUTH_VERIFIER);
                    accessToken = mTwitter.getOAuthAccessToken(mRequestToken, oauthVerifier);
                    SharedPreferences pref = getSharedPreferences(Const.PREF_NAME, MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString(Const.PREF_KEY_ACCESS_TOKEN, accessToken.getToken());
                    editor.putString(Const.PREF_KEY_ACCESS_TOKEN_SECRET, accessToken.getTokenSecret());
                    editor.commit();

                    Toast.makeText(this, "Logged in", Toast.LENGTH_SHORT).show();
                } catch(TwitterException e) {
                    e.printStackTrace();
                }
            } else if (resultCode == RESULT_CANCELED) {
                Log.w("CGN", "Twitter auth canceled.");
            }
        }
    }
}
