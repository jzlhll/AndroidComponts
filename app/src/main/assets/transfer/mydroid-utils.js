(function() {
    // 辅助函数：格式化文件大小
    window.myDroidFormatSize = function(bytes) {
        const units = ['B', 'KB', 'MB', 'GB'];
        let size = bytes;
        let unitIndex = 0;

        if (size < 1024) {
            return `${size}B`;
        }

        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        return `${size.toFixed(1)}${units[unitIndex]}`;
    };

    //辅助函数：遍历打印对象
    window.myDroidTraverse = function(obj) {
        for (const key in obj) {
            if (typeof obj[key] === 'object') {
                console.log(`${key}--`);
                myDroidTraverse(obj[key]);
            } else {
                console.log(`${key}: ${obj[key]}`);
            }
        }
    };

    window.isValidString = function(value) {
        return typeof value === 'string' && value.length > 0;
    };

    window.generateUUID = function() {
        return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
            var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
            return v.toString(16);
        });
    };

    window.delay = function(ms) {
        return new Promise(resolve => setTimeout(resolve, ms));
    }

    // 辅助函数，用于格式化数字为两位或三位数（前面补0）
    function padTo2Digits(num) {
        return num.toString().padStart(2, '0');
    }
    
    function padTo3Digits(num) {
        return num.toString().padStart(3, '0');
    }

    window.nowTimeStr = function() {
        // 获取当前时间的毫秒数
        const timestamp = Date.now();
        
        // 创建一个Date对象
        const date = new Date(timestamp);
        
        // 获取时分秒毫秒
        const hr = date.getHours(); // 小时
        const m = date.getMinutes(); // 分钟
        const s = date.getSeconds(); // 秒
        const ms = date.getMilliseconds(); // 毫秒
        
        // 格式化输出
        return `${padTo2Digits(hr)}:${padTo2Digits(m)}:${padTo2Digits(s)}.${padTo3Digits(ms)}`;
    }

    window.LoopController = class{
        constructor() {
            this.intervalId = null;
            this.isLooping = false;
        }

        start(callback, interval = 300) {
            if (this.isLooping) return;
            
            this.isLooping = true;
            if (!this.intervalId) {
            this.intervalId = setInterval(() => {
                callback();
            }, interval);
            }
        }

        stop() {
            this.isLooping = false;
            if (this.intervalId) {
            clearInterval(this.intervalId);
            this.intervalId = null;
            }
        }
    }
})();