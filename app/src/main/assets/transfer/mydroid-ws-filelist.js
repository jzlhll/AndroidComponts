//这个js，确保运行在websockets.js的生命里面
(function() {
     window.onUrlRealInfoHtmlListReceiver = function(urlRealInfoHtmlList) {
        console.log("receiver urlRealInfos");
        console.log(urlRealInfoHtmlList);
        displayFileList(urlRealInfoHtmlList);
     };

     window.startDownload = async function(uriUuid) {
        try {
            const downUrl = `/download?uriUuid=${uriUuid}`;
            console.log("startDownload: ", downUrl);
            const response = await fetch(downUrl);
            console.log("startDownload response: ", response);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            // 将响应流转换为 Blob
            const blob = await response.blob();

            // 创建临时 URL 并触发下载
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = 'large_file.zip'; // 设置下载文件名
            document.body.appendChild(a);
            a.click();

            // 清理资源
            window.URL.revokeObjectURL(url);
            document.body.removeChild(a);
        } catch (error) {
            console.error('下载失败:', error);
            alert('下载失败，请检查控制台日志');
        }
    };
})();