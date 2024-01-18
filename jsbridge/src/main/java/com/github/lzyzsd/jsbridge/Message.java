package com.github.lzyzsd.jsbridge;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * data of bridge
 * @author haoqing
 *
 */
public class Message {
    private Message() {}

    private String callbackId; //callbackId
    private String responseId; //responseId
    private String responseData; //responseData
    private String data; //data of message
    private String handlerName; //name of handler

    public Integer pageTotal = null; //总共的页数.使用Integer来可以当空使用。则与之前无异。
    public Integer pageIndex = null; //当前页数。使用Integer来可以当空使用。则与之前无异。

    private static final int MAX_RESPONSE_MESSAGE_SIZE = 1024 * 1024; //最大其实只允许2MB。
    private static final int MAX_SEND_MESSAGE_SIZE = 800 * 1024; //最大其实只允许2MB。

    private static Message createSendDirectly(String callbackId, String data, String handlerName) {
        Message m = new Message();
        if (!TextUtils.isEmpty(callbackId)) {
            m.setCallbackId(callbackId);
        }
        if (!TextUtils.isEmpty(handlerName)) {
            m.setHandlerName(handlerName);
        }
        if (!TextUtils.isEmpty(data)) {
            m.setData(data);
        }
        return m;
    }

    private static Message createSendPage(String callbackId, String data, String handlerName, int pageIndex, int pageTotal) {
        Message m = new Message();
        if (!TextUtils.isEmpty(callbackId)) {
            m.setCallbackId(callbackId);
        }
        if (!TextUtils.isEmpty(handlerName)) {
            m.setHandlerName(handlerName);
        }
        if (!TextUtils.isEmpty(data)) {
            m.setData(data);
        }
        m.pageTotal = pageTotal;
        m.pageIndex = pageIndex;
        return m;
    }

    public static Message[] createSend(String callbackId, String data, String handlerName) {
        int maxSize = MAX_SEND_MESSAGE_SIZE;
        int len = data == null ? 0 : data.length();
        int total = len / MAX_SEND_MESSAGE_SIZE;
        if (len % maxSize > 0) {
            total++;
        }

        //Log.d(BridgeUtil.TAG, "createSend: " + len);
        if (total > 1) {
            //Log.d(BridgeUtil.TAG, "createSend page " + total + ", " + callbackId);
            Message[] messages = new Message[total];
            int i = 1;
            int curSize = 0;
            for (; i < total ;i++) {
                messages[i - 1] = createSendPage(callbackId, data.substring(curSize, curSize + maxSize), handlerName, i, total);
                curSize = curSize + maxSize;
            }
            messages[i - 1] = createSendPage(callbackId, data.substring(curSize), handlerName, i, total);
            return messages;
        } else {
            //Log.d(BridgeUtil.TAG, "createSend no page, " + callbackId);
            return new Message[] {createSendDirectly(callbackId, data, handlerName)};
        }
    }

    private static Message createResponseDirectly(String responseId, String responseData) {
        Message responseMsg = new Message();
        responseMsg.setResponseId(responseId);
        responseMsg.setResponseData(responseData);
        return responseMsg;
    }

    private static Message createResponsePage(String responseId, String responseData, int pageIndex, int pageTotal) {
        Message responseMsg = new Message();
        responseMsg.setResponseId(responseId);
        responseMsg.setResponseData(responseData);
        responseMsg.pageTotal = pageTotal;
        responseMsg.pageIndex = pageIndex;
        return responseMsg;
    }

    public static Message[] createResponse(String data, String responseId) {
        int maxSize = MAX_RESPONSE_MESSAGE_SIZE;
        int len = data == null ? 0 : data.length();
        //Log.d(BridgeUtil.TAG, "createResponse: " + len);
        int total = len / maxSize;
        if (len % maxSize > 0) {
            total++;
        }
        if (total > 1) {
            //Log.d(BridgeUtil.TAG, "createResponse page " + total + ", " + responseId);
            Message[] messages = new Message[total];
            int i = 1;
            int curSize = 0;
            for (; i < total ;i++) {
                messages[i - 1] = createResponsePage(responseId, data.substring(curSize, curSize + maxSize), i, total);
                curSize = curSize + maxSize;
            }
            messages[i - 1] = createResponsePage(responseId, data.substring(curSize), i, total);
            return messages;
        } else {
            //Log.d(BridgeUtil.TAG, "createResponse no page, " + responseId);
            return new Message[] {createResponseDirectly(responseId, data)};
        }
    }

    private final static String CALLBACK_ID_STR = "callbackId";
    private final static String RESPONSE_ID_STR = "responseId";
    private final static String RESPONSE_DATA_STR = "responseData";
    private final static String DATA_STR = "data";
    private final static String HANDLER_NAME_STR = "handlerName";

    private final static String PAGE_TOTAL_STR = "pageTotal";
    private final static String PAGE_INDEX_STR = "pageIndex";

    public String getResponseId() {
        return responseId;
    }
    public void setResponseId(String responseId) {
        this.responseId = responseId;
    }
    public String getResponseData() {
        return responseData;
    }
    public void setResponseData(String responseData) {
        this.responseData = responseData;
    }
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
        JSONObject jsonObject= new JSONObject();
        try {
            jsonObject.put(CALLBACK_ID_STR, getCallbackId());
            jsonObject.put(DATA_STR, getData());
            jsonObject.put(HANDLER_NAME_STR, getHandlerName());
            jsonObject.put(RESPONSE_DATA_STR, getResponseData());
            jsonObject.put(RESPONSE_ID_STR, getResponseId());

            if (pageTotal != null) {
                jsonObject.put(PAGE_TOTAL_STR, pageTotal);
            }
            if (pageIndex != null) {
                jsonObject.put(PAGE_INDEX_STR, pageIndex);
            }
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Message> toArrayList(String jsonStr) {
        List<Message> list = new ArrayList<Message>();
        try {
            JSONArray jsonArray = new JSONArray(jsonStr);
            for(int i = 0; i < jsonArray.length(); i++){
                Message m = new Message();
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                m.setHandlerName(jsonObject.has(HANDLER_NAME_STR) ? jsonObject.getString(HANDLER_NAME_STR):null);
                m.setCallbackId(jsonObject.has(CALLBACK_ID_STR) ? jsonObject.getString(CALLBACK_ID_STR):null);
                m.setResponseData(jsonObject.has(RESPONSE_DATA_STR) ? jsonObject.getString(RESPONSE_DATA_STR):null);
                m.setResponseId(jsonObject.has(RESPONSE_ID_STR) ? jsonObject.getString(RESPONSE_ID_STR):null);
                m.setData(jsonObject.has(DATA_STR) ? jsonObject.getString(DATA_STR):null);
                if (jsonObject.has(PAGE_INDEX_STR)) {
                    m.pageIndex = jsonObject.getInt(PAGE_INDEX_STR);
                }
                if (jsonObject.has(PAGE_TOTAL_STR)) {
                    m.pageTotal = jsonObject.getInt(PAGE_TOTAL_STR);
                }
                list.add(m);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }
}
