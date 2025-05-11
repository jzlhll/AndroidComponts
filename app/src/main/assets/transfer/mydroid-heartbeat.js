(function() {
    let heartBeatCount = 0;
    const heatBeatInterval = 2 * 60 * 1000;
    let heatBeatIntervalId = null;

    // 停止定时器
    function stopHeartbeat() {
        console.log('停止心跳 ' + heatBeatIntervalId);
        if (heatBeatIntervalId) {
            clearInterval(heatBeatIntervalId);
            heatBeatIntervalId = null;
        }
    }

    async function checkLeftSpace() {
        console.log(`checkLeftSpace count: ${heartBeatCount++}`);
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
            }
        } catch(e) {
            console.error("发生错误:", e);
            showConnectionError();
            stopHeartbeat();
        }
    }

    // 启动定时器
    window.startHeartbeat = function startHeartbeat() {
        stopHeartbeat();
        // 先立即执行一次
        checkLeftSpace();
        // 然后设置定时器
        console.log(`start Heart beat check.`);
        heatBeatIntervalId = setInterval(checkLeftSpace, heatBeatInterval);
    }
})();