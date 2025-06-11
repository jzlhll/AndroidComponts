package com.allan.mydroid.views.textchat.uibean;

import com.allan.mydroid.beans.WSChatMessageBean;

public class OtherItem extends AbsItem{
    public OtherItem() {
        super(VIEW_TYPE_OTHER);
    }

    public WSChatMessageBean message;
}