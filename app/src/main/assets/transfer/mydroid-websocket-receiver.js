(function() {
    window.parseMessage = function(eventData) {
        const jsonData = JSON.parse(eventData);
        const data = jsonData.data;
        const api = jsonData.api;
        const msg = jsonData.msg;
        if (api == API_LEFT_SPACE) {
            htmlUpdateLeftSpace("手机剩余空间：" + data.leftSpace);
            return true;
        }
        return false;
    }
})();