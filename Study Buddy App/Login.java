package com.x10hosting.studybuddy.studybuddy;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONException;
import org.json.JSONObject;

public class Login extends AppCompatActivity {
    private Context ApplicationContext;
    private SharedPreferences MyPreferences;
    private SharedPreferences.Editor PreferencesWriter;
    private ContentManager AvailableNotes, AvailablePackages, SubscribedNotes, SubscribedPackages;
    private int CurrentLayoutID;
    public static final int VIEW_CONTENT = 1;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ApplicationContext = getApplicationContext();
        MyPreferences = ApplicationContext.getSharedPreferences(getString(R.string.SharedPreferencesFile),
                Context.MODE_PRIVATE);
        String StoredUsername = MyPreferences.getString(getString(R.string.UsernameTag), "");
        String StoredPassword = MyPreferences.getString(getString(R.string.PasswordTag), "");
        String StoredAccessToken = MyPreferences.getString(getString(R.string.AccessToken),"");
        if (StoredUsername.equals("") || StoredPassword.equals("")) {
            ChangeView(R.layout.activity_login);
        } else {
            if(!StoredAccessToken.equals("")) VerifyAccessToken(StoredUsername, StoredAccessToken);
            else GetAccessToken(StoredUsername, StoredPassword);
        }
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    public void Login(View v) {
        TextView LoginFeedback = (TextView) findViewById(R.id.LoginFeedback);
        LoginFeedback.setText("Attempting to login...");
        PreferencesWriter = MyPreferences.edit();
        EditText LoginUsername = (EditText) findViewById(R.id.LoginUsername);
        EditText LoginPassword = (EditText) findViewById(R.id.LoginPassword);
        PreferencesWriter.putString(getString(R.string.UsernameTag), LoginUsername.getText().toString());
        PreferencesWriter.putString(getString(R.string.PasswordTag), LoginPassword.getText().toString());
        PreferencesWriter.commit();
        SubscribedNotes = null;
        SubscribedPackages = null;
        AvailableNotes = null;
        AvailablePackages = null;
        GetAccessToken(LoginUsername.getText().toString(), LoginPassword.getText().toString());
    }
    protected void VerifyAccessToken(String Username, String AccessToken) {
        JSONObject Details = new JSONObject();
        try {
            Details.put(getString(R.string.UsernameTag), Username);
            Details.put(getString(R.string.AccessToken), AccessToken);
            Details.put("VerifyToken", true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest VerifyDetails = new JsonObjectRequest(
                Request.Method.POST, getString(R.string.APIUrl), Details,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if(response.has("TokenValid")) {
                            ChangeView(R.layout.manage_subscriptions);
                        }
                        else {
                            String StoredUsername = MyPreferences.getString(getString(R.string.UsernameTag), "");
                            String StoredPassword = MyPreferences.getString(getString(R.string.PasswordTag), "");
                            GetAccessToken(StoredUsername, StoredPassword);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ChangeView(R.layout.activity_login);
                TextView LoginFeedback = (TextView) findViewById(R.id.LoginFeedback);
                LoginFeedback.setText("Sorry, unable to login possibly due to lack of internet connection");
                EditText LoginPassword = (EditText) findViewById(R.id.LoginPassword);
                LoginPassword.setText(MyPreferences.getString(getString(R.string.PasswordTag),""));
            }
        });
        APIRequestHandler.getInstance(this).addToRequestQueue(VerifyDetails);
    }
    protected void GetAccessToken(String Username, String Password) {
        JSONObject Details = new JSONObject();
        try {
            Details.put(getString(R.string.UsernameTag), Username);
            Details.put(getString(R.string.PasswordTag), Password);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest VerifyDetails = new JsonObjectRequest(
                Request.Method.POST, getString(R.string.APIUrl), Details,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if(response.has(getString(R.string.AccessToken))) {
                            PreferencesWriter = MyPreferences.edit();
                            try {
                                PreferencesWriter.putString(getString(R.string.AccessToken),
                                        response.getString(getString(R.string.AccessToken)));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            PreferencesWriter.commit();
                            ChangeView(R.layout.manage_subscriptions);
                        }
                        else {
                            ChangeView(R.layout.activity_login);
                            TextView LoginFeedback = (TextView) findViewById(R.id.LoginFeedback);
                            LoginFeedback.setText("Sorry, unable to login. Please check your username and password");
                        }
                    }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ChangeView(R.layout.activity_login);
                TextView LoginFeedback = (TextView) findViewById(R.id.LoginFeedback);
                LoginFeedback.setText("Sorry, unable to login possibly due to lack of internet connection");
                EditText LoginPassword = (EditText) findViewById(R.id.LoginPassword);
                LoginPassword.setText(MyPreferences.getString(getString(R.string.PasswordTag),""));
            }
        });
        APIRequestHandler.getInstance(this).addToRequestQueue(VerifyDetails);
    }
    public void GoToWebsite(View v) {
        Intent ViewWebsite = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.WebsiteUrl) + "/#SignUp"));
        startActivity(ViewWebsite);
    }
    public void GetPrev(View v) {
        switch(v.getId()) {
            case R.id.subscribedNotePrev:
                if(!SubscribedNotes.GetPrev()) Toast.makeText(this, "Already showing earliest entries",Toast.LENGTH_LONG).show();
                break;
            case R.id.subscribedPackagePrev:
                if(!SubscribedPackages.GetPrev()) Toast.makeText(this, "Already showing earliest entries",Toast.LENGTH_LONG).show();
                break;
            case R.id.availableNotePrev:
                if(!AvailableNotes.GetPrev()) Toast.makeText(this, "Already showing earliest entries",Toast.LENGTH_LONG).show();
                break;
            case R.id.availablePackagePrev:
                if(!AvailablePackages.GetPrev()) Toast.makeText(this, "Already showing earliest entries",Toast.LENGTH_LONG).show();
                break;
            default:
                break;
        }
    }
    public void GetNext(View v) {
        switch(v.getId()) {
            case R.id.subscribedNoteNext:
                if(!SubscribedNotes.GetNext()) Toast.makeText(this, "Already showing last entries",Toast.LENGTH_LONG).show();
                break;
            case R.id.subscribedPackageNext:
                if(!SubscribedPackages.GetNext()) Toast.makeText(this, "Already showing last entries",Toast.LENGTH_LONG).show();
                break;
            case R.id.availableNoteNext:
                if(!AvailableNotes.GetNext()) Toast.makeText(this, "Already showing last entries",Toast.LENGTH_LONG).show();
                break;
            case R.id.availablePackageNext:
                if(!AvailablePackages.GetNext()) Toast.makeText(this, "Already showing last entries",Toast.LENGTH_LONG).show();
                break;
            default:
                break;
        }

    }
    protected void ChangeView(int ViewId) {
        CurrentLayoutID = ViewId;
        if(ViewId == R.layout.activity_login) {
            setContentView(ViewId);
            Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
            setSupportActionBar(myToolbar);
            String StoredUsername = MyPreferences.getString(getString(R.string.UsernameTag), "");
            EditText LoginUsername = (EditText) findViewById(R.id.LoginUsername);
            LoginUsername.setText(StoredUsername);
        }
        else if(ViewId == R.layout.manage_subscriptions) {
            setContentView(ViewId);
            Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
            setSupportActionBar(myToolbar);
            TabHost MyTabHost = (TabHost) findViewById(R.id.tabHost);
            MyTabHost.setup();

            TabHost.TabSpec spec;

            //Subscribed Packages
            spec = MyTabHost.newTabSpec("Subscribed Packages");
            spec.setContent(R.id.subscribedPackageLayout);
            spec.setIndicator("Subscr. Packages");
            MyTabHost.addTab(spec);

            //Subscribed Packages
            spec = MyTabHost.newTabSpec("Subscribed Notes");
            spec.setContent(R.id.subscribedNoteLayout);
            spec.setIndicator("Subscr. Notes");
            MyTabHost.addTab(spec);

            //Subscribed Packages
            spec = MyTabHost.newTabSpec("Available Packages");
            spec.setContent(R.id.availablePackageLayout);
            spec.setIndicator("Avail. Packages");
            MyTabHost.addTab(spec);

            //Subscribed Packages
            spec = MyTabHost.newTabSpec("Available Notes");
            spec.setContent(R.id.availableNoteLayout);
            spec.setIndicator("Avail. Notes");
            MyTabHost.addTab(spec);

            String AccessToken = MyPreferences.getString(getString(R.string.AccessToken), "");
            String Username = MyPreferences.getString(getString(R.string.UsernameTag), "");
            String APIurl = getString(R.string.APIUrl);
            if(SubscribedNotes == null) {
                SubscribedNotes = new ContentManager("Note", Username, AccessToken,
                        APIurl, 25, this, "subscribed");
                SubscribedNotes.UpdateDataSource();
            }
            if(SubscribedPackages == null) {
                SubscribedPackages = new ContentManager("Package", Username, AccessToken,
                        APIurl, 25, this, "subscribed");
                SubscribedPackages.UpdateDataSource();
            }
            if(AvailableNotes == null) {
                AvailableNotes = new ContentManager("Note", Username, AccessToken,
                        APIurl, 25, this, "available");
                AvailableNotes.UpdateDataSource();
            }
            if(AvailablePackages == null) {
                AvailablePackages = new ContentManager("Package", Username, AccessToken,
                        APIurl, 25, this, "available");
                AvailablePackages.UpdateDataSource();
            }
            ListView SubscribedNotesListView = (ListView) findViewById(R.id.subscribedNoteListView);
            SubscribedNotesListView.setAdapter(SubscribedNotes);
            SubscribedNotesListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    ContentWrapper c = (ContentWrapper) adapterView.getItemAtPosition(i);
                    Intent ViewContent = new Intent(getApplicationContext(), ViewContent.class);
                    ViewContent.putExtra("Content", c.Content);
                    ViewContent.putExtra("ID", c.ID);
                    ViewContent.putExtra("Override", c.ID);
                    ViewContent.putExtra("Title",c.Title);
                    ViewContent.putExtra("Description", c.Description);
                    ViewContent.putExtra("Filters", c.Filters);
                    ViewContent.putExtra("Permissions", c.Permissions);
                    ViewContent.putExtra("ContentType", c.ContentType);
                    ViewContent.putExtra("QuestionType", c.QuestionType);
                    ViewContent.putExtra("Creator", c.Creator);
                    ViewContent.putExtra("SubscribedContent", "SubscribedContent");
                    startActivityForResult(ViewContent, VIEW_CONTENT);
                }
            });
            ListView SubscribedPackagesListView = (ListView) findViewById(R.id.subscribedPackageListView);
            SubscribedPackagesListView.setAdapter(SubscribedPackages);
            SubscribedPackagesListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    ContentWrapper c = (ContentWrapper) adapterView.getItemAtPosition(i);
                    Intent ViewContent = new Intent(getApplicationContext(), ViewContent.class);
                    ViewContent.putExtra("Content", c.Content);
                    ViewContent.putExtra("ID", c.ID);
                    ViewContent.putExtra("Override", c.ID);
                    ViewContent.putExtra("Title",c.Title);
                    ViewContent.putExtra("Description", c.Description);
                    ViewContent.putExtra("Filters", c.Filters);
                    ViewContent.putExtra("Permissions", c.Permissions);
                    ViewContent.putExtra("ContentType", c.ContentType);
                    ViewContent.putExtra("QuestionType", c.QuestionType);
                    ViewContent.putExtra("Creator", c.Creator);
                    ViewContent.putExtra("SubscribedContent", "SubscribedContent");
                    startActivityForResult(ViewContent, VIEW_CONTENT);
                }
            });
            ListView AvailableNotesListView = (ListView) findViewById(R.id.availableNoteListView);
            AvailableNotesListView.setAdapter(AvailableNotes);
            AvailableNotesListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    ContentWrapper c = (ContentWrapper) adapterView.getItemAtPosition(i);
                    Intent ViewContent = new Intent(getApplicationContext(), ViewContent.class);
                    ViewContent.putExtra("Content", c.Content);
                    ViewContent.putExtra("ID", c.ID);
                    ViewContent.putExtra("Override", c.ID);
                    ViewContent.putExtra("Title",c.Title);
                    ViewContent.putExtra("Description", c.Description);
                    ViewContent.putExtra("Filters", c.Filters);
                    ViewContent.putExtra("Permissions", c.Permissions);
                    ViewContent.putExtra("ContentType", c.ContentType);
                    ViewContent.putExtra("QuestionType", c.QuestionType);
                    ViewContent.putExtra("Creator", c.Creator);
                    startActivityForResult(ViewContent, VIEW_CONTENT);
                }
            });
            ListView AvailablePackagesListView = (ListView) findViewById(R.id.availablePackageListView);
            AvailablePackagesListView.setAdapter(AvailablePackages);
            AvailablePackagesListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    ContentWrapper c = (ContentWrapper) adapterView.getItemAtPosition(i);
                    Intent ViewContent = new Intent(getApplicationContext(), ViewContent.class);
                    ViewContent.putExtra("Content", c.Content);
                    ViewContent.putExtra("ID", c.ID);
                    ViewContent.putExtra("Override", c.ID);
                    ViewContent.putExtra("Title",c.Title);
                    ViewContent.putExtra("Description", c.Description);
                    ViewContent.putExtra("Filters", c.Filters);
                    ViewContent.putExtra("Permissions", c.Permissions);
                    ViewContent.putExtra("ContentType", c.ContentType);
                    ViewContent.putExtra("QuestionType", c.QuestionType);
                    ViewContent.putExtra("Creator", c.Creator);
                    startActivityForResult(ViewContent, VIEW_CONTENT);
                }
            });
            SubscribedNotes.notifyDataSetChanged();
            SubscribedPackages.notifyDataSetChanged();
            AvailableNotes.notifyDataSetChanged();
            AvailablePackages.notifyDataSetChanged();

            EditText SubscribedNotesTitleFilter = (EditText) findViewById(R.id.subscribedNoteTitleFilter);
            SubscribedNotesTitleFilter.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                    SubscribedNotes.UpdateDataSource();
                }
            });
            EditText SubscribedNotesCreatorFilter = (EditText) findViewById(R.id.subscribedNoteCreatorFilter);
            SubscribedNotesCreatorFilter.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                    SubscribedNotes.UpdateDataSource();
                }
            });
            EditText SubscribedNotesFiltersFilter = (EditText) findViewById(R.id.subscribedNoteFiltersFilter);
            SubscribedNotesFiltersFilter.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                    SubscribedNotes.UpdateDataSource();
                }
            });
            EditText SubscribedPackagesTitleFilter = (EditText) findViewById(R.id.subscribedPackageTitleFilter);
            SubscribedPackagesTitleFilter.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                    SubscribedPackages.UpdateDataSource();
                }
            });
            EditText SubscribedPackagesCreatorFilter = (EditText) findViewById(R.id.subscribedPackageCreatorFilter);
            SubscribedPackagesCreatorFilter.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                    SubscribedPackages.UpdateDataSource();
                }
            });
            EditText SubscribedPackagesFiltersFilter = (EditText) findViewById(R.id.subscribedPackageFiltersFilter);
            SubscribedPackagesFiltersFilter.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                    SubscribedPackages.UpdateDataSource();
                }
            });
            EditText AvailableNotesTitleFilter = (EditText) findViewById(R.id.availableNoteTitleFilter);
            AvailableNotesTitleFilter.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                    AvailableNotes.UpdateDataSource();
                }
            });
            EditText AvailableNotesCreatorFilter = (EditText) findViewById(R.id.availableNoteCreatorFilter);
            AvailableNotesCreatorFilter.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                    AvailableNotes.UpdateDataSource();
                }
            });
            EditText AvailableNotesFiltersFilter = (EditText) findViewById(R.id.availableNoteFiltersFilter);
            AvailableNotesFiltersFilter.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                    AvailableNotes.UpdateDataSource();
                }
            });
            EditText AvailablePackagesTitleFilter = (EditText) findViewById(R.id.availablePackageTitleFilter);
            AvailablePackagesTitleFilter.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                    AvailablePackages.UpdateDataSource();
                }
            });
            EditText AvailablePackagesCreatorFilter = (EditText) findViewById(R.id.availablePackageCreatorFilter);
            AvailablePackagesCreatorFilter.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                    AvailablePackages.UpdateDataSource();
                }
            });
            EditText AvailablePackagesFiltersFilter = (EditText) findViewById(R.id.availablePackageFiltersFilter);
            AvailablePackagesFiltersFilter.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                    AvailablePackages.UpdateDataSource();
                }
            });
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == VIEW_CONTENT && resultCode == RESULT_OK) {
            if(data.hasExtra("SubscriptionResult")) {
                if(data.getBooleanExtra("SubscriptionResult",false)) {
                    Toast.makeText(this, "Subscribed", Toast.LENGTH_SHORT).show();
                    SubscribedNotes.UpdateDataSource();
                    SubscribedPackages.UpdateDataSource();
                }
                else {
                    Toast.makeText(this, "Unable to subscribe, probably because already subscribed to content", Toast.LENGTH_SHORT).show();
                }
            }
            else if(data.hasExtra("UnsubscriptionResult")) {
                if(data.getBooleanExtra("UnsubscriptionResult",false)) {
                    Toast.makeText(this, "Unsubscribed", Toast.LENGTH_SHORT).show();
                    SubscribedNotes.UpdateDataSource();
                    SubscribedPackages.UpdateDataSource();
                }
                else {
                    Toast.makeText(this, "Unable to unsubscribe", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(CurrentLayoutID != R.layout.activity_login) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.actionbar_menu, menu);
        }
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_logout:
                PreferencesWriter = MyPreferences.edit();
                PreferencesWriter.putString(getString(R.string.PasswordTag),"");
                PreferencesWriter.putString(getString(R.string.AccessToken),"");
                PreferencesWriter.commit();
                ChangeView(R.layout.activity_login);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Login Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.x10hosting.studybuddy.studybuddy/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Login Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.x10hosting.studybuddy.studybuddy/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}
