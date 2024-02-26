package com.github.lzyzsd.jsbridge;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * data of bridge
 *
 * @author haoqing
 */
public class Message {

    private String callbackId; //callbackId
    private String data; //data of message
    private String handlerName; //name of handler

    private final static String CALLBACK_ID_STR = "callbackId";
    private final static String DATA_STR = "data";
    private final static String HANDLER_NAME_STR = "handlerName";

    public String getCallbackId() {
        return callbackId;
    }

    public void setCallbackId(String callbackId) {
        this.callbackId = callbackId;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getHandlerName() {
        return handlerName;
    }

    public void setHandlerName(String handlerName) {
        this.handlerName = handlerName;
    }

    public String toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(CALLBACK_ID_STR, getCallbackId());
            jsonObject.put(DATA_STR, getData());
            jsonObject.put(HANDLER_NAME_STR, getHandlerName());
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Message toObject(String jsonStr) {
        Message m = new Message();
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            m.setHandlerName(jsonObject.has(HANDLER_NAME_STR) ? jsonObject.getString(HANDLER_NAME_STR) : null);
            m.setCallbackId(jsonObject.has(CALLBACK_ID_STR) ? jsonObject.getString(CALLBACK_ID_STR) : null);
            m.setData(jsonObject.has(DATA_STR) ? jsonObject.getString(DATA_STR) : null);
            return m;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return m;
    }

    public static List<Message> toArrayList(String jsonStr) {
        List<Message> list = new ArrayList<Message>();
        try {
            JSONArray jsonArray = new JSONArray(jsonStr);
            for (int i = 0; i < jsonArray.length(); i++) {
                Message m = new Message();
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                m.setHandlerName(jsonObject.has(HANDLER_NAME_STR) ? jsonObject.getString(HANDLER_NAME_STR) : null);
                m.setCallbackId(jsonObject.has(CALLBACK_ID_STR) ? jsonObject.getString(CALLBACK_ID_STR) : null);
                m.setData(jsonObject.has(DATA_STR) ? jsonObject.getString(DATA_STR) : null);
                list.add(m);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }
}
