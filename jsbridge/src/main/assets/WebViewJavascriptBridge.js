//notation: js file can only use this kind of comments
//since comments will cause error when use in webview.loadurl,
//comments will be remove by java use regexp
(function() {
    if (window.WebViewJavascriptBridge) {
        return;
    }

    var messagingIframe;
    var sendMessageQueue = [];
    var receiveMessageQueue = [];
    var messageHandlers = {};

    var CUSTOM_PROTOCOL_SCHEME = 'yy';
    var QUEUE_HAS_MESSAGE = '__QUEUE_MESSAGE__/';

    var responseCallbacks = {};
    var uniqueId = 1;

    var responseMessagesPageMap = {};
    var sendMessagesPageMap = {};

    function _createQueueReadyIframe(doc) {
        messagingIframe = doc.createElement('iframe');
        messagingIframe.style.display = 'none';
        doc.documentElement.appendChild(messagingIframe);
    }

    function isAndroid() {
        var ua = navigator.userAgent.toLowerCase();
        var isA = ua.indexOf("android") > -1;
        if (isA) {
            return true;
        }
        return false;
    }

    function isIphone() {
        var ua = navigator.userAgent.toLowerCase();
        var isIph = ua.indexOf("iphone") > -1;
        if (isIph) {
            return true;
        }
        return false;
    }

    function logd(s) {
        _doSend({
                handlerName: 'logd',
                data: s
            });
    }
    function loge(s) {
        _doSend({
                handlerName: 'loge',
                data: s
            });
    }
    function logw(s) {
        _doSend({
                handlerName: 'logw',
                data: s
            });
    }

    //set default messageHandler
    function init(messageHandler) {
        if (WebViewJavascriptBridge._messageHandler) {
            //throw new Error('WebViewJavascriptBridge.init called twice');
            return;
        }
        WebViewJavascriptBridge._messageHandler = messageHandler;
        var receivedMessages = receiveMessageQueue;
        receiveMessageQueue = null;
        for (var i = 0; i < receivedMessages.length; i++) {
            _dispatchMessageFromNative(receivedMessages[i]);
        }
    }

    function send(data, responseCallback) {
        _doSend({
            data: data
        }, responseCallback);
    }

    function registerHandler(handlerName, handler) {
        messageHandlers[handlerName] = handler;
    }

    function callHandler(handlerName, data, responseCallback) {
        _doSend({
            handlerName: handlerName,
            data: data
        }, responseCallback);
    }

    //sendMessage add message, 触发native处理 sendMessage
    function _doSend(message, responseCallback) {
        if (responseCallback) {
            var callbackId = 'cb_' + (uniqueId++) + '_' + new Date().getTime();
            responseCallbacks[callbackId] = responseCallback;
            message.callbackId = callbackId;
        }

        sendMessageQueue.push(message);
        messagingIframe.src = CUSTOM_PROTOCOL_SCHEME + '://' + QUEUE_HAS_MESSAGE;
    }

    // 提供给native调用,该函数作用:获取sendMessageQueue返回给native,由于android不能直接获取返回的内容,所以使用url shouldOverrideUrlLoading 的方式返回内容
    function _fetchQueue() {
        var messageQueueString = JSON.stringify(sendMessageQueue);
        sendMessageQueue = [];
        //add by hq
        if (isIphone()) {
            return messageQueueString;
            //android can't read directly the return data, so we can reload iframe src to communicate with java
        } else if (isAndroid()) {
            messagingIframe.src = CUSTOM_PROTOCOL_SCHEME + '://return/_fetchQueue/' + encodeURIComponent(messageQueueString);
        }
    }

    function _dispatchMessageFromNativeResponse(responseId, responseData) {
        var responseCallback = responseCallbacks[responseId];
        if (!responseCallback) {
            return;
        }
        responseCallback(responseData);
        delete responseCallbacks[responseId];
    }

    function _dispatchMessageFromNativeSend(callbackId, handlerName, data) {
        //直接发送
        var responseCallback;
        if (callbackId) {
            var callbackResponseId = callbackId;
            responseCallback = function(responseData) {
                _doSend({
                    responseId: callbackResponseId,
                    responseData: responseData
                });
            };
        }

        var handler = WebViewJavascriptBridge._messageHandler;
        if (handlerName) {
            handler = messageHandlers[handlerName];
        }
        //查找指定handler
        try {
            handler(data, responseCallback);
        } catch (exception) {
            if (typeof console != 'undefined') {
                console.log("WebViewJavascriptBridge: WARNING: javascript handler threw.", message, exception);
            }
        }
    }

    //提供给native使用,
    function _dispatchMessageFromNative(messageJSON) {
        setTimeout(function() {
            var message = JSON.parse(messageJSON);
            //java call finished, now need to call js callback function
            if (message.responseId) {
                var rid = message.responseId;
                //追加处理分页
                if (message.pageTotal) {
                    logd("_dispatchMessageFromNative responseId pageTotal " + message.pageIndex + "/" + message.pageTotal);
                    if (message.pageIndex == 1) {
                        responseMessagesPageMap[rid] = message.responseData;
                    } else if (message.pageIndex < message.pageTotal) {
                        responseMessagesPageMap[rid] = responseMessagesPageMap[rid] + message.responseData;
                    } else {
                        _dispatchMessageFromNativeResponse(rid, responseMessagesPageMap[rid] + message.responseData);
                        delete responseMessagesPageMap[rid];
                    }
                } else {
                    logw("_dispatchMessageFromNative responseId no page");
                    _dispatchMessageFromNativeResponse(rid, message.responseData);
                }
            } else {
                var cid = message.callbackId;
                //追加处理分页
                if (message.pageTotal) {
                    logw("_dispatchMessageFromNative send pageTotal " + message.pageIndex + "/" + message.pageTotal);
                    if (message.pageIndex == 1) {
                        sendMessagesPageMap[cid] = message.data;
                    } else if (message.pageIndex < message.pageTotal) {
                        sendMessagesPageMap[cid] = sendMessagesPageMap[cid] + message.data;
                    } else {
                        _dispatchMessageFromNativeSend(cid, message.handlerName, sendMessagesPageMap[cid] + message.data);
                        delete sendMessagesPageMap[cid];
                    }
                } else {
                    logw("_dispatchMessageFromNative send no page");
                    _dispatchMessageFromNativeSend(cid, message.handlerName, message.data);
                }
            }
        });
    }

    //提供给native调用,receiveMessageQueue 在会在页面加载完后赋值为null,所以
    function _handleMessageFromNative(messageJSON) {
        if (receiveMessageQueue) {
            receiveMessageQueue.push(messageJSON);
        } else {
            _dispatchMessageFromNative(messageJSON);
        }
    }

    var WebViewJavascriptBridge = window.WebViewJavascriptBridge = {
        init: init,
        send: send,
        logd: logd,
        loge: loge,
        logw: logw,
        registerHandler: registerHandler,
        callHandler: callHandler,
        _fetchQueue: _fetchQueue,
        _handleMessageFromNative: _handleMessageFromNative
    };

    var doc = document;
    _createQueueReadyIframe(doc);
    var readyEvent = doc.createEvent('Events');
    readyEvent.initEvent('WebViewJavascriptBridgeReady');
    readyEvent.bridge = WebViewJavascriptBridge;
    doc.dispatchEvent(readyEvent);
})();