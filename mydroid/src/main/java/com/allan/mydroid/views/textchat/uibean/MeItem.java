package com.allan.mydroid.views.textchat.uibean;

import com.allan.mydroid.beans.WSChatMessageBean;

public class MeItem extends AbsItem{
    public MeItem() {
        super(VIEW_TYPE_ME);
    }

    public WSChatMessageBean message;
}