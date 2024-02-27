package com.github.lzyzsd.jsbridge;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

public class Message {
    private final static String ID_STR = "id";
    private final static String DATA_STR = "data";
    private final static String HANDLER_NAME_STR = "handlerName";
    private final static String RESPONSE_STR = "responseCode";

    private String id; //id
    private String data; //data of message
    private String handlerName; //name of handler

    //0 js->java或者java->js的调用
    //1 js调用java，但是该消息是用来response java的某次指令。其实我们不需要。因为BridgeObject中直接定义函数。
    //2 java调用js，但是该消息是用来response js的某条指令
    private int responseCode;

    public void setResponseCodeAsJavaResponseToJs() {
        responseCode = 2;
    }

//这是js中才会使用的所以这里不需要
//    public void setResponseCodeAsJsResponseToJava() {
//        responseCode = 1;
//    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    @Nullable
    public String toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(ID_STR, getId());
            jsonObject.put(DATA_STR, getData());
            jsonObject.put(HANDLER_NAME_STR, getHandlerName());
            jsonObject.put(RESPONSE_STR, responseCode);
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @NonNull
    public static Message toObject(String jsonStr) {
        Message m = new Message();
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            m.setHandlerName(jsonObject.has(HANDLER_NAME_STR) ? jsonObject.getString(HANDLER_NAME_STR) : null);
            m.setId(jsonObject.has(ID_STR) ? jsonObject.getString(ID_STR) : null);
            m.setData(jsonObject.has(DATA_STR) ? jsonObject.getString(DATA_STR) : null);
            m.responseCode = (jsonObject.has(RESPONSE_STR) ? jsonObject.getInt(RESPONSE_STR) : 0);
            return m;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return m;
    }

//    public static List<Message> toArrayList(String jsonStr) {
//        List<Message> list = new ArrayList<Message>();
//        try {
//            JSONArray jsonArray = new JSONArray(jsonStr);
//            for (int i = 0; i < jsonArray.length(); i++) {
//                Message m = new Message();
//                JSONObject jsonObject = jsonArray.getJSONObject(i);
//                m.setHandlerName(jsonObject.has(HANDLER_NAME_STR) ? jsonObject.getString(HANDLER_NAME_STR) : null);
//                m.setId(jsonObject.has(ID_STR) ? jsonObject.getString(ID_STR) : null);
//                m.setData(jsonObject.has(DATA_STR) ? jsonObject.getString(DATA_STR) : null);
//                list.add(m);
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        return list;
//    }
}
