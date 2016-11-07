package com.x10hosting.studybuddy.studybuddy;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by david on 15/9/16.
 */
public class ChildContentManager extends BaseAdapter {
    private String ContentType, Username, AccessToken, Url;
    private int ContentID, Override;
    private JSONArray ContentList;
    private Activity ParentActivity;
    private ArrayList<ContentWrapper> DataSource;
    private LayoutInflater Inflater;
    private ChildContentManager Instance;
    private TextView ContributorText;

    public ChildContentManager(String ContentType, String Username, String AccessToken, String Url, Activity ParentActivity, int ContentID, int Override) {
        this.ContentType = ContentType;
        this.Username = Username;
        this.AccessToken = AccessToken;
        this.Url = Url;
        this.ParentActivity = ParentActivity;
        this.Inflater = (LayoutInflater) ParentActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.Override = Override;
        this.ContentID = ContentID;
        Instance = this;
        DataSource = new ArrayList<ContentWrapper>();
    }
    public void SetTextView(TextView t) {
        ContributorText = t;
    }
    public String GetQuestionType(int id) {
        for(int i = 0; i < DataSource.size(); i++) {
            if(DataSource.get(i).ID == id) return DataSource.get(i).QuestionType;
        }
        return "";
    }
    public void UpdateDataSource() {
        JSONObject PostData = new JSONObject();
        try {
            PostData.put("GetChildContent", ContentType);
            PostData.put("Username", Username);
            PostData.put("AccessToken", AccessToken);
            PostData.put("ContentID", ContentID);
            PostData.put("Override", Override);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest UpdateRequest = new JsonObjectRequest(Request.Method.POST, Url, PostData, new Response.Listener<JSONObject>(){
            @Override
            public void onResponse(JSONObject response) {
                if(response.has("ChildContent")) {
                    try {
                        ContentList = response.getJSONArray("ChildContent");
                        DataSource.clear();
                        for(int i = 0; i < ContentList.length(); i++) {
                            ContentWrapper c = new ContentWrapper();
                            JSONObject entry = ContentList.getJSONObject(i);
                            c.Content = entry.getString("Content");
                            c.ContentType = entry.getString("ContentType");
                            c.ID = entry.getInt("ID");
                            c.Creator = entry.getString("Creator");
                            c.Description = entry.getString("Description");
                            c.Filters = entry.getString("Filters");
                            c.Permissions = entry.getString("Permissions");
                            c.Title = entry.getString("Title");
                            c.QuestionType = entry.getString("QuestionType");
                            DataSource.add(c);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Instance.notifyDataSetChanged();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }

        });
        APIRequestHandler.getInstance(ParentActivity.getApplicationContext()).addToRequestQueue(UpdateRequest);
    }
    @Override
    public int getCount() {
        return DataSource.size();
    }

    @Override
    public Object getItem(int i) {
        return DataSource.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup parent) {
        View rowview = Inflater.inflate(R.layout.titledescriptionentry, parent, false);
        TextView Title = (TextView) rowview.findViewById(R.id.TitleDescriptionTitle);
        TextView Description = (TextView) rowview.findViewById(R.id.TitleDescriptionDescription);
        ContentWrapper content = (ContentWrapper) getItem(i);
        Title.setText(Integer.toString(i + 1) + ") " + content.Title + " - " + content.Creator);
        Description.setText(content.Description);
        return rowview;
    }
}
