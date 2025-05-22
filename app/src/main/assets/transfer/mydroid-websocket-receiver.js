(function() {
    window.parseMessage = function(eventData) {
        const jsonData = JSON.parse(eventData);
        const data = jsonData.data;
        const api = jsonData.api;
        const msg = jsonData.msg;
        if (api == API_WS_LEFT_SPACE) {
            htmlUpdateLeftSpace(loc["remaining_phone_space"] + data.leftSpace);
            return true;
        } else if (api == API_WS_CLIENT_INIT_CALLBACK) {
            window.debugReceiver = data.debugReceiver;
            return true;
        }
        return false;
    }
})();