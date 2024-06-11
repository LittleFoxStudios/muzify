package com.littlefoxstudios.muzify.internet;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.Api;
import com.littlefoxstudios.muzify.Utilities;
import com.littlefoxstudios.muzify.accounts.YoutubeMusic;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ApiCaller {
    Context context;

    public ApiCaller(Context context){
        this.context = context;
    }

    public ApiCaller(){

    }

    public JsonObjectRequest doGet(String uri, String accessToken, final VolleyCallback callback)
    {
        Utilities.Loggers.postInfoLog("DO_GET_URI", uri+" __ "+accessToken);
        return new JsonObjectRequest(Request.Method.GET, uri, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Utilities.Loggers.postInfoLog("API_CALL_COUNTER", "Do get success");
                callback.onSuccess(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Utilities.Loggers.postInfoLog("API_CALL_COUNTER", "Do get fail");
                callback.onError(error.getMessage());
            }
        })
        {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", accessToken);
                return headers;
            }
        };
    }

    public JsonObjectRequest doPost(String uri, String accessToken, final VolleyCallback callback, JSONObject jsonReqBody)
    {
        return new JsonObjectRequest(Request.Method.POST, uri, jsonReqBody, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Utilities.Loggers.postInfoLog("API_CALL_COUNTER", "Do post success");
                callback.onSuccess(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Utilities.Loggers.postInfoLog("API_CALL_COUNTER", "Do post fail");
                callback.onError(error.getMessage());
            }
        })
        {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", accessToken);
                return headers;
            }

        };
    }

}
