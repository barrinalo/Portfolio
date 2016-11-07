package com.x10hosting.studybuddy.studybuddy;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by david on 14/9/16.
 */
public class ContentManager extends BaseAdapter {
    private String ContentType, Username, AccessToken, Url, LayoutType;
    private JSONArray ContentList;
    private int Offset, Limit;
    private Activity ParentActivity;
    private ArrayList<ContentWrapper> DataSource;
    private LayoutInflater Inflater;
    private ContentManager Instance;
    private int RowCount;

    public ContentManager(String ContentType, String Username, String AccessToken, String Url, int Limit, Activity ParentActivity, String LayoutType) {
        this.ContentType = ContentType;
        this.Username = Username;
        this.AccessToken = AccessToken;
        this.Url = Url;
        this.Limit = Limit;
        this.Offset = 0;
        this.ParentActivity = ParentActivity;
        this.LayoutType = LayoutType;
        this.Inflater = (LayoutInflater) ParentActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Instance = this;
        DataSource = new ArrayList<ContentWrapper>();
    }
    public boolean GetNext() {
        if((Offset + Limit) <= RowCount) {
            Offset += Limit;
            UpdateDataSource();
            return true;
        }
        else return false;
    }
    public boolean GetPrev() {
        if((Offset - Limit) >= 0) {
            Offset -= Limit;
            UpdateDataSource();
            return true;
        }
        else return false;
    }
    public void UpdateDataSource() {
        JSONObject PostData = new JSONObject();
        EditText CreatorFilter = (EditText) ParentActivity.findViewById(ParentActivity.getResources().getIdentifier(LayoutType + ContentType + "CreatorFilter","id", ParentActivity.getPackageName()));
        EditText TitleFilter = (EditText) ParentActivity.findViewById(ParentActivity.getResources().getIdentifier(LayoutType + ContentType + "TitleFilter","id", ParentActivity.getPackageName()));
        EditText FiltersFilter = (EditText) ParentActivity.findViewById(ParentActivity.getResources().getIdentifier(LayoutType + ContentType + "FiltersFilter","id", ParentActivity.getPackageName()));
        try {
            if(LayoutType.equals("available")) PostData.put("GetReadableContent", ContentType);
            else if(LayoutType.equals("subscribed")) PostData.put("GetSubscriptions", ContentType);
            PostData.put("Username", Username);
            PostData.put("Offset", Offset);
            PostData.put("AccessToken", AccessToken);
            PostData.put("Limit", Limit);
            PostData.put("Creator", CreatorFilter.getText().toString());
            PostData.put("TitleFilter", TitleFilter.getText().toString());
            PostData.put("FiltersFilter", FiltersFilter.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest UpdateRequest = new JsonObjectRequest(Request.Method.POST, Url, PostData, new Response.Listener<JSONObject>(){
            @Override
            public void onResponse(JSONObject response) {
                if(response.has("Content")) {
                    try {
                        JSONObject ContentResponse = new JSONObject(response.getString("Content"));
                        ContentList = ContentResponse.getJSONArray("data");
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
                        RowCount = ContentResponse.getInt("RowCount");
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
        Title.setText(content.Title + " - " + content.Creator);
        Description.setText(content.Description);
        return rowview;
    }
}
