(function() {
    let wsCntCount = 0;
    let wsCntInterval = 1000;
    let wsCntIntervalId = null;

    let mRandom8bitName = null;
    window.getRandomWSName = function() {
        if (mRandom8bitName) {
            return mRandom8bitName;
        }
        const n = generateUUID();
        mRandom8bitName = n.substring(0, 6);
        return mRandom8bitName;
    }

    // 停止定时器
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
                const wsName = getRandomWSName();
                const wsInit = { "wsInit": wsName };
                socket.send(JSON.stringify(wsInit));
            };
    
            // 接收消息
            socket.onmessage = (event) => {
                //通过每次接收leftSpace来当做ping/pong
                console.log("on messagae: " + event.data);
                const jsonData = JSON.parse(event.data);
                const data = jsonData.data;
                if (data.leftSpace) {
                    myHtmlUpdateSubtitle("Fast局域网传输工具\n手机剩余空间：" + data.leftSpace);
                } if (data.myDroidMode) {
                    const ip = data.ip
                    myHtmlUpdateBigTitle(data.myDroidMode, ip + "@" + data.clientName);
                } else {
                    // 处理数据（JSON 示例）
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
                myHtmlShowConnectError();
            };

            return true;
        } catch(e) {
            console.error("create websocket error", e);
            myHtmlShowConnectError();
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
            myHtmlShowConnectError();
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
})();