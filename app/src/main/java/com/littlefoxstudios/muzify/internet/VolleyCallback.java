package com.littlefoxstudios.muzify.internet;

import org.json.JSONObject;

public interface VolleyCallback {
    void onSuccess(JSONObject result);
    void onError(String result);
}
