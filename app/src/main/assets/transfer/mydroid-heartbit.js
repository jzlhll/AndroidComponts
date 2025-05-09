(function() {
    let heartBitCount = 0;
    const heartBitInterval = 2 * 60 * 1000;
    let heartBitIntervalId = null;

    // 停止定时器
    function stopHeartbeat() {
        console.log('停止心跳 ' + heartBitIntervalId);
        if (heartBitIntervalId) {
            clearInterval(heartBitIntervalId);
            heartBitIntervalId = null;
        }
    }

    async function checkLeftSpace() {
        console.log(`循环执行次数: ${heartBitCount++}`);
        let isSuc = false;
        try {
           const response = await fetch('/read-left-space', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
            });

            if (response && response.ok) {
                const json = await response.text();
                updateSubtitle("Fast局域网传输工具\n手机剩余空间：" + json);
                isSuc = true;
            }
        } catch(e) {
            console.error("发生错误:", e);
        }
        if (!isSuc) {
            showConnectionError();
            stopHeartbeat();
        }
    }

    // 启动定时器
    window.startHeartbeat = function startHeartbeat() {
        // 先立即执行一次
        checkLeftSpace();
        // 然后设置定时器
        heartBitIntervalId = setInterval(checkLeftSpace, heartBitInterval);
    }
})();