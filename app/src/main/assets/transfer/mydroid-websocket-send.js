(function() {
    window.parseMessage = function(eventData) {
        if (eventData instanceof Blob || eventData instanceof ArrayBuffer) {
            return true;
        } else {
            const jsonData = JSON.parse(eventData);
            const data = jsonData.data;
            const api = jsonData.api;
            const msg = jsonData.msg;

            if (api == API_SEND_FILE_CHUNK) {
                console.log("chunk bytes ", api, msg);
                return true;
            } else if (api == API_CLIENT_INIT_CALLBACK) {
                htmlUpdateIpClient(data.myDroidMode, data.clientName);
                return true;
            } else if (api == API_SEND_FILE_LIST) {
                htmlShowFileList(data.urlRealInfoHtmlList);
                return true;
            } else if (api == API_SEND_FILE_START_NOT_EXIST) {
                onStartDownErr(msg, data.uriUuid);
                return true;
            }
        }

        return false;
    }
})();