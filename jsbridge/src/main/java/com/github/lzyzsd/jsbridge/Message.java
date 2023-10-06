package com.github.lzyzsd.jsbridge;

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

    private String callbackId; //callbackId
    private String responseId; //responseId
    private String responseData; //responseData
    private String data; //data of message
    private String handlerName; //name of handler

    private String pageMask;

    private Integer pageTotal = null;

    private Integer pageIndex = null;

    private final static String CALLBACK_ID_STR = "callbackId";
    private final static String RESPONSE_ID_STR = "responseId";
    private final static String PAGE_MASK_STR = "pageMask";
    private final static String PAGE_INDEX_STR = "pageIndex";
    private final static String PAGE_TOTAL_STR = "pageTotal";
    private final static String RESPONSE_DATA_STR = "responseData";
    private final static String DATA_STR = "data";
    private final static String HANDLER_NAME_STR = "handlerName";

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

    public Integer getPageIndex() {
        return pageIndex;
    }
    public void setPageIndex(Integer pageIndex) {
        this.pageIndex = pageIndex;
    }

    public String getPageMask() {
        return pageMask;
    }
    public void setPageMask(String pageMask) {
        this.pageMask = pageMask;
    }

    public Integer getPageTotal() {
        return pageTotal;
    }
    public void setPageTotal(Integer pageTotal) {
        this.pageTotal = pageTotal;
    }

    public String toJson() {
        JSONObject jsonObject= new JSONObject();
        try {
            jsonObject.put(CALLBACK_ID_STR, getCallbackId());
            jsonObject.put(DATA_STR, getData());
            jsonObject.put(HANDLER_NAME_STR, getHandlerName());
            jsonObject.put(RESPONSE_DATA_STR, getResponseData());
            jsonObject.put(RESPONSE_ID_STR, getResponseId());

            if (getPageTotal() != null) {
                jsonObject.put(PAGE_TOTAL_STR, getPageTotal());
            }

            if (getPageMask() != null) {
                jsonObject.put(PAGE_MASK_STR, getPageMask());
            }

            if (getPageIndex() != null) {
                jsonObject.put(PAGE_INDEX_STR, getPageIndex());
            }
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Message toObject(JSONObject jsonObject) throws JSONException {
        Message m =  new Message();
        m.setHandlerName(jsonObject.has(HANDLER_NAME_STR) ? jsonObject.getString(HANDLER_NAME_STR):null);
        m.setCallbackId(jsonObject.has(CALLBACK_ID_STR) ? jsonObject.getString(CALLBACK_ID_STR):null);
        m.setResponseData(jsonObject.has(RESPONSE_DATA_STR) ? jsonObject.getString(RESPONSE_DATA_STR):null);
        m.setResponseId(jsonObject.has(RESPONSE_ID_STR) ? jsonObject.getString(RESPONSE_ID_STR):null);
        m.setData(jsonObject.has(DATA_STR) ? jsonObject.getString(DATA_STR):null);

        m.setPageMask(jsonObject.has(PAGE_MASK_STR) ? jsonObject.getString(PAGE_MASK_STR) : null);
        m.setPageIndex(jsonObject.has(PAGE_INDEX_STR) ? jsonObject.getInt(PAGE_INDEX_STR) : null);
        m.setPageTotal(jsonObject.has(PAGE_TOTAL_STR) ? jsonObject.getInt(PAGE_TOTAL_STR) : null);
        return m;
    }

    public static List<Message> toArrayList(String jsonStr){
        List<Message> list = new ArrayList<Message>();
        try {
            JSONArray jsonArray = new JSONArray(jsonStr);
            for(int i = 0; i < jsonArray.length(); i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Message m = toObject(jsonObject);
                list.add(m);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }
}
