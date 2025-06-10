(function() {
    let wsCntCount = 0;
    let wsCntInterval = 1000;
    let wsCntIntervalId = null;

    let wsHeartBeatIntervalId = null;

    window.WS = null;

    window.API_WS_SEND_FILE_LIST = "s_sendFileList";
    window.API_WS_LEFT_SPACE = "s_leftSpace";
    window.API_WS_CLIENT_INIT_CALLBACK = "s_clientInitBack";
    window.API_WS_SEND_FILE_CHUNK = "s_sendFileChunk";
    window.API_WS_SEND_SMALL_FILE_CHUNK = "s_sendSmallFileChunk";
    window.API_WS_SEND_FILE_NOT_EXIST = "s_sendFileNotExist";

    window.API_WS_INIT = "c_wsInit";
    window.API_WS_REQUEST_FILE = "c_requestFile";
    window.API_WS_FILE_DOWNLOAD_COMPLETE = "c_downloadFileComplete";
    window.API_WS_PING = "c_ping";

    window.HEARTBEAT_INTERVAL = 12000;

    const heartbeatFrame = JSON.stringify({
                api: window.API_WS_PING
        });

    function stopWebSocketInterval() {
        if (wsCntIntervalId) {
            console.log('停止start WebSocket ConnectInterval');
            clearInterval(wsCntIntervalId);
            wsCntIntervalId = null;
        }
    }

    function stopHeartbeatInterval() {
        if (wsHeartBeatIntervalId) {
            console.log('停止 HeartBeat Interval');
            clearInterval(wsHeartBeatIntervalId);
            wsHeartBeatIntervalId = null;
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
                wsInit.api = API_WS_INIT;
                wsInit.wsName = wsName;
                socket.send(JSON.stringify(wsInit));
            };
    
            // 接收消息
            socket.onmessage = (event) => {
                console.log(`${nowTimeStr()} on message`, event.data);
                const result = parseMessage(event.data);
                if (!result) {
                    // 处理数据（JSON 示例）
                    console.log("on msg other! ");
                    try {
                        //handleMessage(jsonData);
                    } catch (e) {
                        console.error('WebSocket 消息解析失败', e);
                    }
                }
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

        if (isSuc) {
            wsHeartBeatIntervalId = setInterval(() => {
                    WS?.send(heartbeatFrame);
            }, window.HEARTBEAT_INTERVAL);
        }
    }

    // 启动定时器
    window.startWsConnect = function startWsConnect() {
        // 然后设置定时器
        wsCntIntervalId = setInterval(startWebSocket, wsCntInterval);
    }
})();