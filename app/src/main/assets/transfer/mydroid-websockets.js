(function() {
    let wsCntCount = 0;
    let wsCntInterval = 1000;
    let wsCntIntervalId = null;

    window.WS = null;

    const API_SEND_FILE_LIST = "s_sendFileList";
    const API_LEFT_SPACE = "s_leftSpace";
    const API_CLIENT_INIT_CALLBACK = "s_clientInitBack";
    const API_SEND_FILE_CHUNK = "s_sendFileChunk";
    const API_SEND_FILE_START_NOT_EXIST = "s_sendFileNotExist";

    const API_WS_INIT = "c_wsInit";
    window.API_REQUEST_FILE = "c_requestFile";

    function stopWebSocketInterval() {
        if (wsCntIntervalId) {
            console.log('停止start WebSocket ConnectInterval');
            clearInterval(wsCntIntervalId);
            wsCntIntervalId = null;
        }
    }

    function setupSocketFileList(ipPort) {
        try{
            // 创建 WebSocket 连接 (ws:// 或 wss://)
            const socket = new WebSocket(`ws://${ipPort}/ws-test`);
            // 监听连接打开事件
            socket.onopen = (event) => {
                console.log('WebSocket 连接已建立');
                // 发送初始数据
                const n = generateUUID();
                const wsName = n.substring(0, 8);
                const wsInit = {};
                wsInit[API_WS_INIT] = wsName;
                socket.send(JSON.stringify(wsInit));
            };
    
            // 接收消息
            socket.onmessage = (event) => {
                //通过每次接收leftSpace来当做ping/pong
                const jsonData = JSON.parse(event.data);
                const data = jsonData.data;
                const api = jsonData.api;
                const msg = jsonData.msg;
                console.log("apiAndMsg ", api, msg);
                console.log("json ", jsonData);
                parseMessage(api, msg, data);
            };
    
            // 错误处理
            socket.onerror = (error) => {
                console.error('WebSocket 错误 todo 占据关闭:', error);
            };
    
            // 连接关闭
            socket.onclose = (event) => {
                console.log('WebSocket 连接关闭 todo 占据关闭:', event.code, event.reason);
                //if (!event.wasClean) {
                    // 非正常关闭时尝试重连
                    //reconnect();
                //}
                commonHtmlConnectError();
            };

            WS = socket;
            return true;
        } catch(e) {
            console.error("create websocket error", e);
            commonHtmlConnectError();
        }
        
        return false;
    }

    function parseMessage(api, msg, data) {
        if (api == API_LEFT_SPACE) {
            if(htmlUpdateLeftSpace) htmlUpdateLeftSpace("Fast局域网传输工具\n手机剩余空间：" + data.leftSpace);
        } if (api == API_CLIENT_INIT_CALLBACK) {
            htmlUpdateIpClient(data.myDroidMode, data.clientName);
        } else if (api == API_SEND_FILE_LIST) {
            console.log("data url result Infos " + data.urlRealInfoHtmlList);
            onUrlRealInfoHtmlListReceiver(data.urlRealInfoHtmlList);
        } else if (api == API_SEND_FILE_START_NOT_EXIST) {
            if (onStartDownloadError) {
                onStartDownloadError(msg, data.uriUuid);
            }
        } 
        
        else {
            // 处理数据（JSON 示例）
            console.log("on messagae other: " + data);
            try {
                //handleMessage(jsonData);
            } catch (e) {
                console.error('WebSocket 消息解析失败', e);
            }
        }
    }

    async function startWebSocket() {
        wsCntInterval = 5 * 1000;
        wsCntCount++;
        console.log(`start WebSocket 循环执行次数: ${wsCntCount}`);
        let response = null;
        let shouldStopInterval = false;
        try {
            response = await fetch('/read-websocket-ip-port', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
            });
        } catch(e) {
            console.error("read websocket ip port error!", e);
            commonHtmlConnectError();
            shouldStopInterval = true;
        }

        let isSuc = false;
        if (response && response.ok) {
            let ipPortJson = null;
            try {
                ipPortJson = await response.json();
            } catch(e) {}
            const ip = ipPortJson?.data?.ip;
            const port = ipPortJson?.data?.port;
            if (isValidString(ip) && port) {
                isSuc = setupSocketFileList(ip + ":" + port);
            }
        }

        if (isSuc || shouldStopInterval) {
            stopWebSocketInterval();
        }
    }

    // 启动定时器
    window.startWsConnect = function startWsConnect() {
        // 然后设置定时器
        wsCntIntervalId = setInterval(startWebSocket, wsCntInterval);
    }

    function onUrlRealInfoHtmlListReceiver(urlRealInfoHtmlList) {
        if(sendHtmlDisplayFileList) sendHtmlDisplayFileList(urlRealInfoHtmlList);
    };
})();