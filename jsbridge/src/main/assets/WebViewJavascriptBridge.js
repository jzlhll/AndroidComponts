//notation: js file can only use this kind of comments
//since comments will cause error when use in webview.loadurl,
//comments will be remove by java use regexp
(function() {
    if (window.WebViewJavascriptBridge) {
        return;
    }

    var receiveMessageQueue = [];
    var messageHandlers = {};

    var responseCallbacks = {};
    var uniqueId = 1;

    var emptyResponseFunction = function(data) {};

    //set default messageHandler
    function init(messageHandler) {
        console.log("js bridge init call");
        if (WebViewJavascriptBridge._messageHandler || WebViewJavascriptBridge.isInit) {
            //throw new Error('WebViewJavascriptBridge.init called twice');
            return;
        }
        WebViewJavascriptBridge._messageHandler = messageHandler;
        WebViewJavascriptBridge.isInit = true;

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
            var id = 'jsId_' + (uniqueId++) + '_' + new Date().getTime();
            responseCallbacks[id] = responseCallback;
            message.id = id;
        }
        Android.jsCall(JSON.stringify(message));
    }

    //不需要参数responseCode，因为我们直接调用了该方法。
    function _doSendResponse(message) {
        Android.jsResponse(JSON.stringify(message));
    }

    //提供给native使用,
    function _dispatchMessageFromNative(messageJSON) {
        setTimeout(function() {
            var message = JSON.parse(messageJSON);
            var msgId = message.id;
            var responseCallback;
            //java call finished, now need to call js callback function

            //=2表示，是js已经向native发送过指令。现在call回来
            if (message.responseCode == 2) {
                responseCallback = responseCallbacks[msgId];
                if (!responseCallback) {
                    return;
                }
                responseCallback(message.data);
                delete responseCallbacks[msgId];
            } else {
                //查找注册的handler
                var handler = WebViewJavascriptBridge._messageHandler;
                if (message.handlerName) {
                    handler = messageHandlers[message.handlerName];
                }

                if (msgId) {
                    //native主动调用上来，存在id，则需要response给native
                    responseCallback = function(data) {
                        _doSendResponse({
                            id: msgId,
                            data: data,
                        });
                    };
                } else {
                    //native主动调上来，不存在id。则给一个空函数
                    responseCallback = emptyResponseFunction;
                }

                try {
                    handler(message.data, responseCallback);
                } catch (exception) {
                    if (typeof console != 'undefined') {
                        console.log("WebViewJavascriptBridge: WARNING: javascript handler threw.", message, exception);
                    }
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
        registerHandler: registerHandler,
        callHandler: callHandler,
        _handleMessageFromNative: _handleMessageFromNative
    };

    var doc = document;
    var readyEvent = doc.createEvent('Events');
    readyEvent.initEvent('WebViewJavascriptBridgeReady');
    readyEvent.bridge = WebViewJavascriptBridge;
    doc.dispatchEvent(readyEvent);
    console.log("js bridge init end.");
})();