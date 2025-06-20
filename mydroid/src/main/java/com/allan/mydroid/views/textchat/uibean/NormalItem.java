package com.allan.mydroid.views.textchat.uibean;

import com.allan.mydroid.beans.WSChatMessageBean;

public class NormalItem extends AbsItem{

    public NormalItem(boolean isMe) {
        super(isMe? VIEW_TYPE_ME :VIEW_TYPE_OTHER);
    }

    public boolean isMe() {
        return viewType == VIEW_TYPE_ME;
    }

    public WSChatMessageBean message;
}