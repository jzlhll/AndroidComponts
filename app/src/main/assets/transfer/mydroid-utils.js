(function() {
    // 辅助函数：格式化文件大小
    window.myDroidFormatSize = function(bytes) {
        const units = ['B', 'KB', 'MB', 'GB'];
        let size = bytes;
        let unitIndex = 0;

        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        return `${size.toFixed(2)} ${units[unitIndex]}`;
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
})();