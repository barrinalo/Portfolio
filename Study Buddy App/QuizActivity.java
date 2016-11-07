package com.x10hosting.studybuddy.studybuddy;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.io.UnsupportedEncodingException;

public class QuizActivity extends AppCompatActivity {
    ContentWrapper ParentContent;
    SharedPreferences MyPreferences;
    WebView Display;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);
        ParentContent = new ContentWrapper();
        Intent ParentIntent = getIntent();
        ParentContent.ID = ParentIntent.getIntExtra("ID",0);
        ParentContent.Title = ParentIntent.getStringExtra("Title");
        ParentContent.Description = ParentIntent.getStringExtra("Description");
        ParentContent.Content = ParentIntent.getStringExtra("Content");
        ParentContent.ContentType = ParentIntent.getStringExtra("ContentType");
        ParentContent.QuestionType = ParentIntent.getStringExtra("QuestionType");
        ParentContent.Permissions = ParentIntent.getStringExtra("Permissions");
        ParentContent.Filters = ParentIntent.getStringExtra("Filters");
        ParentContent.Creator = ParentIntent.getStringExtra("Creator");

        MyPreferences = this.getSharedPreferences(getString(R.string.SharedPreferencesFile),
                Context.MODE_PRIVATE);
        Display = (WebView) findViewById(R.id.QuizDisplay);
        WebSettings ws = Display.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setBuiltInZoomControls(true);
        String Username = MyPreferences.getString(getString(R.string.UsernameTag),"");
        String AccessToken = MyPreferences.getString(getString(R.string.AccessToken),"");
        String Url = getString(R.string.APIUrl);
        Url = Url.substring(0,Url.indexOf("main.php"));
        Url += "quiz.php";
        String PostData = "";
        PostData += "Username=" + Username;
        PostData += "&AccessToken=" + AccessToken;
        PostData += "&ContentID=" + Integer.toString(ParentContent.ID);
        PostData += "&ContentType=" + ParentContent.ContentType;
        if(ParentIntent.getBooleanExtra("IsTest",false)) PostData += "&IsTest=true";
        if(ParentIntent.getIntExtra("Override",0) != 0) PostData += "&Override=" + Integer.toString(ParentIntent.getIntExtra("Override",0));
        try {
            Display.postUrl(Url, PostData.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        Intent i = new Intent();
        setResult(Activity.RESULT_OK, i);
        finish();
    }
}
