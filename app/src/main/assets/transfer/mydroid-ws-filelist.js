//这个js，确保运行在websockets.js的生命里面
(function() {
     window.onUrlRealInfoHtmlListReceiver = function(urlRealInfoHtmlList) {
        console.log("receiver urlRealInfos");
        console.log(urlRealInfoHtmlList);
        displayFileList(urlRealInfoHtmlList);
     }
})();