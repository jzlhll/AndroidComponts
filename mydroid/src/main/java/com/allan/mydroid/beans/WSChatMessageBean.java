package com.allan.mydroid.beans;

import static com.allan.mydroid.beans.WSApisKt.API_WS_TEXT_CHAT_MSG;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.UUID;

@Keep
public class WSChatMessageBean {
    public WSChatMessageBean(@NonNull Sender sender, @NonNull Content content, @NonNull String status) {
        this.sender = sender;
        this.content = content;
        this.messageId = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
        this.status = status;
    }

    public static class Sender {
        //public String userId;
        public String name;
        //public String avatar;
        public int color;
        public boolean isServer;

        /**
         * androidApp, iosApp, html
         */
        public String platform;

        @NonNull
        @Override
        public String toString() {
            return "Sender=" + name + "(" + (isServer ? "S" : "C") + ")";
        }
    }

    public static class Content {
        public Content(@NonNull String text, @Nullable UriRealInfoHtml file) {
            this.text = text;
            this.file = file;
        }
        @NonNull
        public final String text;
        @Nullable
        public UriRealInfoHtml file;

        @NonNull
        @Override
        public String toString() {
            if (file == null) {
                return "Content=" + text;
            }
            return "Content=" + text + ", " + file;
        }
    }

    public final String messageId;
    public long timestamp;

    @NonNull
    public final String api = API_WS_TEXT_CHAT_MSG;

    @NonNull
    public final Sender sender;

    @NonNull
    public final Content content;

    /**
     * 可选范围 sending 发送中, delivered 服务器发送给到客户端显示的状态, sendOverTime 发送超时
     */
    @NonNull
    public String status;

    public void setStatusToSending() {
        status = "sending";
    }

    public void setStatusToDelivered() {
        status = "delivered";
    }

    public void setStatusToSendOverTime() {
        status = "sendOverTime";
    }

    //public String[] readReceipt;
    //public String replyTo;
    //public boolean encrypted;

    @NonNull
    @Override
    public String toString() {
        return  sender +
                ", content=" + content +
                ", status='" + status + '\'' +
                '}';
    }
}
