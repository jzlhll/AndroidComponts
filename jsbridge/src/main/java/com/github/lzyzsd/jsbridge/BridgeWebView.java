package com.github.lzyzsd.jsbridge;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

@SuppressLint("SetJavaScriptEnabled")
public class BridgeWebView extends WebView implements WebViewJavascriptBridge {
	public static final String toLoadJs = "WebViewJavascriptBridge.js";

	final BridgeObject bridgeObject;

	public BridgeWebView(Context context) { //必须保留三个构造，不能this。会导致无法弹起键盘
		super(context);
		bridgeObject = new BridgeObject(this);
		init();
	}

	public BridgeWebView(Context context, AttributeSet attrs) {//必须保留三个构造，不能this。会导致无法弹起键盘
		super(context, attrs);
		bridgeObject = new BridgeObject(this);
		init();
	}

	public BridgeWebView(Context context, AttributeSet attrs, int defStyleAttr) {//必须保留三个构造，不能this。会导致无法弹起键盘
		super(context, attrs, defStyleAttr);
		bridgeObject = new BridgeObject(this);
		init();
	}


	private void init() {
		this.setVerticalScrollBarEnabled(false);
		this.setHorizontalScrollBarEnabled(false);
		WebSettings setting = getSettings();
		setting.setJavaScriptEnabled(true);
		setting.setAllowFileAccess(true);
		setting.setAllowContentAccess(true);
		setting.setDatabaseEnabled(true);
		setting.setDomStorageEnabled(true);
		setOverScrollMode(OVER_SCROLL_NEVER);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
		this.setWebViewClient(generateBridgeWebViewClient());
		addJavascriptInterface(bridgeObject, "Android");
	}

    protected BridgeWebViewClient generateBridgeWebViewClient() {
        return new BridgeWebViewClient(this);
    }

    @Override
    public void send(String data) {
        send(data, null);
    }

    @Override
    public void send(String data, CallBackFunction responseCallback) {
        doSend(null, data, responseCallback);
    }

	private void doSend(String handlerName, String data, @Nullable CallBackFunction responseCallback) {
		if (responseCallback == null) {
			bridgeObject.webViewCallJs(handlerName, data);
		} else {
			bridgeObject.webViewCallJsWithResponse(handlerName, data, responseCallback);
		}
	}

    /**
     * call javascript registered handler
     *
     * @param handlerName
     * @param data
     * @param callBack
     */
    public void callHandler(String handlerName, String data, CallBackFunction callBack) {
        doSend(handlerName, data, callBack);
    }

	////////////////////////////////////////////////////////////////////
	////////////////
	////////////////////////////////////////////////////////////////////

	private final Map<String, BridgeHandler> messageHandlers = new HashMap<>();

	BridgeHandler findHandler(@Nullable String handleName) {
		if (handleName != null && messageHandlers.containsKey(handleName)) {
			BridgeHandler handler = messageHandlers.get(handleName);
			if (handler != null) {
				return handler;
			}
		}
		return defaultHandler;
	}

	private BridgeHandler defaultHandler = new DefaultHandler();
	/**
	 *
	 * @param handler
	 *            default handler,handle messages send by js without assigned handler name,
	 *            if js message has handler name, it will be handled by named handlers registered by native
	 */
	public void setDefaultHandler(BridgeHandler handler) {
		this.defaultHandler = handler;
	}

	/**
	 * register handler,so that javascript can call it
	 */
	public void registerHandler(String handlerName, BridgeHandler handler) {
		if (handler != null) {
			messageHandlers.put(handlerName, handler);
		}
	}
}
