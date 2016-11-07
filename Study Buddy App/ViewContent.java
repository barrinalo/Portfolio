package com.x10hosting.studybuddy.studybuddy;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ViewContent extends AppCompatActivity {
    ContentWrapper Content;
    SharedPreferences MyPreferences;
    TextView ContentTitle, ContentStatistics, ContentCreator, ContentFilters,
            ContentPermissions, ContentContributors, ContentDescription, ContentType, QuestionType;
    WebView ContentBody;
    ListView ChildContent;
    ChildContentManager ChildContentAdapter;
    Activity Self;
    String ContentBodyText = "";
    boolean SubscribedContent, JustView;
    int Override;
    static final int QUIZ_ACTIVITY = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_content);
        Self = this;
        MyPreferences = Self.getSharedPreferences(getString(R.string.SharedPreferencesFile),
                Context.MODE_PRIVATE);
        Intent ContentInfo = getIntent();
        Content = new ContentWrapper();
        Content.QuestionType = ContentInfo.getStringExtra("QuestionType");
        Content.ID = ContentInfo.getIntExtra("ID", 0);
        Override = ContentInfo.getIntExtra("Override", 0);
        Content.Title = ContentInfo.getStringExtra("Title");
        Content.Description = ContentInfo.getStringExtra("Description");
        Content.Permissions = ContentInfo.getStringExtra("Permissions");
        Content.ContentType = ContentInfo.getStringExtra("ContentType");
        Content.Content = ContentInfo.getStringExtra("Content");
        Content.Creator = ContentInfo.getStringExtra("Creator");
        Content.Filters = ContentInfo.getStringExtra("Filters");
        if (ContentInfo.hasExtra("SubscribedContent")) SubscribedContent = true;
        else SubscribedContent = false;
        if (ContentInfo.hasExtra("JustView")) JustView = true;
        else JustView = false;
        TabHost MyTabHost = (TabHost) findViewById(R.id.tabHost2);
        MyTabHost.setup();

        TabHost.TabSpec spec;

        //Subscribed Packages
        spec = MyTabHost.newTabSpec("Info");
        spec.setContent(R.id.ContentInfoTab);
        spec.setIndicator("Info");
        MyTabHost.addTab(spec);

        //Subscribed Packages
        spec = MyTabHost.newTabSpec("Content");
        spec.setContent(R.id.ContentBodyTab);
        spec.setIndicator("Content");
        MyTabHost.addTab(spec);

        //Subscribed Packages
        spec = MyTabHost.newTabSpec("ChildContent");
        spec.setContent(R.id.ChildContent);
        spec.setIndicator("Child Content");
        MyTabHost.addTab(spec);

        ContentTitle = (TextView) findViewById(R.id.ContentTitle);
        ContentTitle.setText(Content.Title);
        ContentCreator = (TextView) findViewById(R.id.ContentCreator);
        ContentCreator.setText(Content.Creator);
        ContentDescription = (TextView) findViewById(R.id.ContentDescription);
        ContentDescription.setText(Content.Description);
        ContentType = (TextView) findViewById(R.id.ContentType);
        ContentType.setText(Content.ContentType);
        QuestionType = (TextView) findViewById(R.id.QuestionType);
        QuestionType.setText(Content.QuestionType);
        ContentPermissions = (TextView) findViewById(R.id.ContentPermissions);
        ContentPermissions.setText(Content.Permissions);
        ContentFilters = (TextView) findViewById(R.id.ContentFilters);
        ContentFilters.setText(Content.Filters);

        ContentContributors = (TextView) findViewById(R.id.ContentContributors);
        ContentStatistics = (TextView) findViewById(R.id.ContentStatistics);
        ContentBody = (WebView) findViewById(R.id.ContentBody);
        ChildContent = (ListView) findViewById(R.id.ChildContent);

        WebSettings ws = ContentBody.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setBuiltInZoomControls(true);
        ContentBody.requestFocusFromTouch();
        ContentBody.setWebViewClient(new WebViewClient());
        ContentBody.setWebChromeClient(new WebChromeClient());

        MyPreferences = this.getSharedPreferences(getString(R.string.SharedPreferencesFile),
                Context.MODE_PRIVATE);
        String Username = MyPreferences.getString(getString(R.string.UsernameTag), "");
        String AccessToken = MyPreferences.getString(getString(R.string.AccessToken), "");
        String Url = getString(R.string.APIUrl);
        Url = Url.substring(0, Url.indexOf("main.php"));
        Url += "viewcontent.php";
        String PostData = "";
        PostData += "Username=" + Username;
        PostData += "&AccessToken=" + AccessToken;
        PostData += "&ContentID=" + Integer.toString(Content.ID);
        PostData += "&ContentType=" + Content.ContentType;
        if (ContentInfo.getIntExtra("Override", 0) != 0)
            PostData += "&Override=" + Integer.toString(ContentInfo.getIntExtra("Override", 0));
        try {
            ContentBody.postUrl(Url, PostData.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        //Set up Child Content
        if (Content.ContentType.equals("Note") || Content.ContentType.equals("Package")) {
            ChildContentAdapter = new ChildContentManager(Content.ContentType, MyPreferences.getString(getString(R.string.UsernameTag), ""),
                    MyPreferences.getString(getString(R.string.AccessToken), ""), getString(R.string.APIUrl), this, Content.ID, Override);
            ChildContentAdapter.SetTextView(ContentContributors);
            ChildContentAdapter.UpdateDataSource();
            ChildContent.setAdapter(ChildContentAdapter);
            ChildContent.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    ContentWrapper c = (ContentWrapper) adapterView.getItemAtPosition(i);
                    Intent ViewContent = new Intent(getApplicationContext(), ViewContent.class);
                    ViewContent.putExtra("Content", c.Content);
                    ViewContent.putExtra("ID", c.ID);
                    ViewContent.putExtra("Override", Override);
                    ViewContent.putExtra("Title", c.Title);
                    ViewContent.putExtra("Description", c.Description);
                    ViewContent.putExtra("Filters", c.Filters);
                    ViewContent.putExtra("Permissions", c.Permissions);
                    ViewContent.putExtra("ContentType", c.ContentType);
                    ViewContent.putExtra("QuestionType", c.QuestionType);
                    ViewContent.putExtra("Creator", c.Creator);
                    if (SubscribedContent)
                        ViewContent.putExtra("SubscribedContent", "SubscribedContent");
                    else ViewContent.putExtra("JustView", "JustView");
                    startActivity(ViewContent);
                }
            });
            UpdateStats();
        }

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        if(SubscribedContent) {
            if(Content.ContentType.equals("Note")) {
                inflater.inflate(R.menu.viewcontentsubscribedcontentnote_actionbar_menu, menu);
            }
            else if(Content.ContentType.equals("Package")) {
                inflater.inflate(R.menu.viewcontentsubscribedcontentpackage_actionbar_menu, menu);
            }
        }
        else if((Content.ContentType.equals("Note") || Content.ContentType.equals("Package")) && !JustView) {
                inflater.inflate(R.menu.viewcontentavailablecontent_actionbar_menu, menu);
        }
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i = new Intent(this, QuizActivity.class);
        switch(item.getItemId()){
            case R.id.action_subscribe:
                JSONObject SubscribeParams = new JSONObject();
                try {
                    SubscribeParams.put("Username", MyPreferences.getString(getString(R.string.UsernameTag),""));
                    SubscribeParams.put("AccessToken", MyPreferences.getString(getString(R.string.AccessToken),""));
                    SubscribeParams.put("ContentID", Content.ID);
                    SubscribeParams.put("Subscribe", true);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                JsonObjectRequest Subscribe = new JsonObjectRequest(Request.Method.POST, getString(R.string.APIUrl),
                        SubscribeParams, new Response.Listener<JSONObject>(){
                    @Override
                    public void onResponse(JSONObject response) {
                        if(response.has("SubscriptionResult")) {
                            try {
                                Intent i = new Intent();
                                i.putExtra("SubscriptionResult", response.getBoolean("SubscriptionResult"));
                                setResult(Activity.RESULT_OK, i);
                                finish();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }, new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });
                APIRequestHandler.getInstance(this).addToRequestQueue(Subscribe);
                return true;
            case R.id.action_unsubscribe:
                if(Content.ID == Override) {
                    JSONObject UnsubscribeParams = new JSONObject();
                    try {
                        UnsubscribeParams.put("Username", MyPreferences.getString(getString(R.string.UsernameTag), ""));
                        UnsubscribeParams.put("AccessToken", MyPreferences.getString(getString(R.string.AccessToken), ""));
                        UnsubscribeParams.put("ContentID", Content.ID);
                        UnsubscribeParams.put("Unsubscribe", true);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    JsonObjectRequest Unsubscribe = new JsonObjectRequest(Request.Method.POST, getString(R.string.APIUrl),
                            UnsubscribeParams, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            if (response.has("UnsubscriptionResult")) {
                                try {
                                    Intent i = new Intent();
                                    i.putExtra("UnsubscriptionResult", response.getBoolean("UnsubscriptionResult"));
                                    setResult(Activity.RESULT_OK, i);
                                    finish();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                        }
                    });
                    APIRequestHandler.getInstance(this).addToRequestQueue(Unsubscribe);
                }
                return true;
            case R.id.action_quiz:
                i.putExtra("ID", Content.ID);
                i.putExtra("Title", Content.Title);
                i.putExtra("Description", Content.Description);
                i.putExtra("Filters", Content.Filters);
                i.putExtra("Permissions", Content.Permissions);
                i.putExtra("ContentType", Content.ContentType);
                i.putExtra("Content", Content.Content);
                i.putExtra("Creator", Content.Creator);
                i.putExtra("QuestionType", Content.QuestionType);
                i.putExtra("Override", Override);
                i.putExtra("IsTest", false);
                startActivityForResult(i,QUIZ_ACTIVITY);
                return true;
            case R.id.action_test:
                i.putExtra("ID", Content.ID);
                i.putExtra("Title", Content.Title);
                i.putExtra("Description", Content.Description);
                i.putExtra("Filters", Content.Filters);
                i.putExtra("Permissions", Content.Permissions);
                i.putExtra("ContentType", Content.ContentType);
                i.putExtra("Content", Content.Content);
                i.putExtra("Creator", Content.Creator);
                i.putExtra("QuestionType", Content.QuestionType);
                i.putExtra("Override", Override);
                i.putExtra("IsTest", true);
                startActivityForResult(i,QUIZ_ACTIVITY);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == QUIZ_ACTIVITY && resultCode == RESULT_OK) {
            UpdateStats();
        }
    }
    protected void UpdateStats() {
        JSONObject GetStatsParams = new JSONObject();
        try {
            GetStatsParams.put("GetStats", Content.ContentType);
            GetStatsParams.put("Username", MyPreferences.getString(getString(R.string.UsernameTag),""));
            GetStatsParams.put("AccessToken", MyPreferences.getString(getString(R.string.AccessToken),""));
            GetStatsParams.put("Override", Override);
            GetStatsParams.put("ContentID", Content.ID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest GetStats = new JsonObjectRequest(Request.Method.POST, getString(R.string.APIUrl),
                GetStatsParams, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if(response.has("Stats")) {
                    String data = null;
                    try {
                        data = response.getString("Stats");
                        if(!data.equals("")) {
                            JSONObject Stats = new JSONObject(data);
                            String Result = "Attempted " + Integer.toString(Stats.getInt("AttemptedQuestions"));
                            Result += " of " + Integer.toString(Stats.getInt("UniqueQuestions")) + " available questions\n";
                            Result += Integer.toString(Stats.getInt("NumberOfAttempts")) + " questions answered ";
                            Result += "with average score " + String.format("%.2f", Stats.getDouble("AverageScore")* 100.0);
                            Result += " and average Time Taken " + String.format("%.2f", Stats.getDouble("AverageTimeTaken"));
                            Result += "s";
                            ContentStatistics.setText(Result);
                            JSONArray ContributorList = Stats.getJSONArray("Contributors");
                            String ContributorText = "";
                            for(int i = 0; i < ContributorList.length(); i++) ContributorText += ContributorList.getString(i) + ", ";
                            if(ContributorList.length() > 0) ContributorText = ContributorText.substring(0, ContributorText.length()-2);
                            ContentContributors.setText(Stats.getString(ContributorText));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        APIRequestHandler.getInstance(this).addToRequestQueue(GetStats);
    }
    public String GetQuestionText(JSONArray QuestionOptions, String QuestionType) {
        String result = "";
        if(QuestionType.equals("Table")) {
            result += "<table border='1' style='border-collapse: collapse'>";
            ArrayList<String> Aspects = new ArrayList<String>();
            ArrayList<String> Topics = new ArrayList<String>();
            for(int i = 0; i < QuestionOptions.length(); i++){
                try {
                    JSONObject option = QuestionOptions.getJSONObject(i);
                    String curaspect = option.getString("Aspect");
                    String curtopic = option.getString("Topic");
                    if(Aspects.indexOf(curaspect) == -1) Aspects.add(curaspect);
                    if(Topics.indexOf(curtopic) == -1) Topics.add(curtopic);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            result += "<tr><th>Topic</th>";
            for(int i = 0; i < Aspects.size(); i++) result += "<th>" + Aspects.get(i) + "</th>";
            result += "</tr>";
            for(int i = 0; i < Topics.size(); i++) {
                result += "<tr><td>" + Topics.get(i) + "</td>";
                for(int j = 0; j < Aspects.size(); j++) {
                    for(int k = 0; k < QuestionOptions.length(); k++) {
                        try {
                            JSONObject option = QuestionOptions.getJSONObject(k);
                            String curaspect = option.getString("Aspect");
                            String curtopic = option.getString("Topic");
                            String curtableentry = option.getString("TableEntry");
                            if(curaspect.equals(Aspects.get(j)) && curtopic.equals(Topics.get(i))) {
                                result += "<td>" + curtableentry + "</td>";
                                break;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
                result += "</tr>";
            }
            result += "</table>";
        }
        else if(QuestionType.equals("Manual")) {

        }
        else if(QuestionType.equals("Graph")) {

        }
        else if(QuestionType.equals("Logical")) {

        }
        return result;
    }
}
