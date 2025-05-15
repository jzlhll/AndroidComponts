//这个js，确保运行在websockets.js的生命里面
(function() {
     window.onUrlRealInfoHtmlListReceiver = function(urlRealInfoHtmlList) {
        console.log("receiver urlRealInfos");
        console.log(urlRealInfoHtmlList);
        displayFileList(urlRealInfoHtmlList);
     };

     window.startDownload = async function(uriUuid) {
        try {
            const params = new URLSearchParams({
              uriUuid: uriUuid,
              ipRandomName: window.myIpRandomName
            });
            const downUrl = `/download?${params.toString()}`;
            console.log("startDownload: ", downUrl);
            const response = await fetch(downUrl);
            console.log("startDownload response: ", response);
            if (!response.ok) {
                throw new Error(response);
            }
        } catch (error) {
            console.error('下载失败:', error);
            alert('下载失败，请检查控制台日志');
        }
    };
})();