package com.example.moaaz.spodervis;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;


public class nlp extends AsyncTask<String, Void, Void>
{
    String value = "";
    ChattingActivity main;
    public nlp(ChattingActivity main) {
        this.main = main;
    }



    protected void onPostExecute(String result) {
        Log.i("Resutls: ", result);
        // something with data retrieved from server in doInBackground
    }

    @Override
    protected Void doInBackground(String... params) {

        String url = "https://api.wit.ai/message";
        String key = "YK3SCFERCBGOWKW74UYFWSX3PRTJNZDI";

        String param1 = "20141022";
        String param2 = params[0];
        String charset = "UTF-8";

        String query = null;
        try {
            query = String.format("v=%s&q=%s",
                    URLEncoder.encode(param1, charset),
                    URLEncoder.encode(param2, charset));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        URLConnection connection = null;
        try {
            connection = new URL(url + "?" + query).openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        connection.setRequestProperty ("Authorization", "Bearer "+key);
        connection.setRequestProperty("Accept-Charset", charset);
        InputStream response = null;
        try {
            response = connection.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        BufferedReader streamReader = null;
        try {
            streamReader = new BufferedReader(new InputStreamReader(response, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        StringBuilder responseStrBuilder = new StringBuilder();

        String inputStr;
        try {
            while ((inputStr = streamReader.readLine()) != null)
                responseStrBuilder.append(inputStr);
        } catch (IOException e) {
            e.printStackTrace();
        }
        JSONObject json = null;
        try {
            json = new JSONObject(responseStrBuilder.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONArray arr = null;
        try {
            arr = json.getJSONArray("outcomes");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JSONObject entities = null;
        try {
            entities = (JSONObject) ((JSONObject)arr.get(0)).get("entities");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i("Response: ", json.toString());

        if (entities.keys().hasNext())
        {
            try {
                JSONObject values = (JSONObject) entities.getJSONArray(entities.keys().next()).get(0);
                double confidence = Double.parseDouble(values.get("confidence").toString());
                if (confidence > 0.6)
                    this.value = entities.keys().next();
                else
                    this.value = "null";
                Log.i("Entity: ", this.value);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        else
        {
            this.value = "null";
            Log.i("Entity: ", "I can't understand");
        }

        return null;
    }
}

