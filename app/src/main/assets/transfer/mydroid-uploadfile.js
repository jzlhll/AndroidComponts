(function() {
    // 上传分片
    async function uploadChunk(formData) {
        let response = null;
        try {
            response = await fetch('/upload-chunk', {
                    method: 'POST',
                    body: formData,
                });
        } catch(e) {}
        if (!response) throw new Error('上传失败E01: 可能手机端不在线。');
        if (!response.ok) throw new Error('上传失败E02 ' + (await response.text()));
        return await response.json();
    }

    // 通知服务器合并分片
    async function mergeChunks(md5, fileName, totalChunks) {
        let response = null;
        try {
            response = await fetch('/merge-chunks', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ md5, fileName, totalChunks }),
            });
        } catch(e) {}
        if (!response) throw new Error('上传失败E03: 可能手机端不在线。');
        if (!response.ok) throw new Error('上传失败E04 ' + (await response.text()));
        return await response.json();
    }
    
    function getChunkSize(fileSize) {
        const MB = 1024 * 1024;
        if (fileSize <= 10 * MB) {
            return MB / 2;
        } else if (fileSize <= 100 * MB) {
            return 1 * MB;
        } else if (fileSize <= 500 * MB) {
            return 2 * MB;
        } else if (fileSize <= 2 * 1024 * MB) {
            return 5 * MB;
        } else {
            return 8 * MB;
        }
    }

    window.startUploadFile = async function startUploadFile(startTs, file, md5, onProgress = () => {}) {
        if (!file) {
            throw new Error("没有选择文件！");
        }
    
        const fileName = file.name;
        changeProgressVisible(true);
        // 64k 600kb/s 256k 2MB/s 512k 2.8~3MB/s 1M 6MB/s 分块越大，越快。
        const fileSize = file.size;
        const chunkSize = getChunkSize(fileSize);
        console.log("选择的chunkSize是" + chunkSize);

        const chunks = Math.ceil(fileSize / chunkSize); // 计算分片数量
        let uploadedChunks = 0; // 已上传的分片数量
        let sendBytes = 0;
        const startTime = Date.now();
        let deltaTime = 0;
        let sendSpeedStr = "-- KB/s";

        for (let i = 0; i < chunks; i++) {
            //放在这里可以让最后一包如果完成的话，就接着完成。
            if (startTs != window.lastStartTsFlagArr.startUploadTime) {
                throw new Error("已被取消!");
            }

            const start = i * chunkSize;
            const end = Math.min(fileSize, start + chunkSize);

            sendBytes += (end - start);
            const chunk = file.slice(start, end); // 切割分片

            const formData = new FormData();
            formData.append('fileName', fileName);
            formData.append('chunk', chunk);
            formData.append('chunkIndex', i + 1);
            formData.append('totalChunks', chunks);
            formData.append('md5', md5);

            const ans = await uploadChunk(formData); // 上传分片
            //console.log(`chunkResponse: code=${ans.code} msg=${ans.msg}`);
            if (ans.code != 0) {
                throw new Error(`${ans.msg}`);
            }
            uploadedChunks++;
            deltaTime = Date.now() - startTime;
            if (deltaTime > 0) {
                const speedBps = (sendBytes / deltaTime) * 1000;
                if (speedBps >= 1024 * 1024) {
                    sendSpeedStr = `${(speedBps / (1024 * 1024)).toFixed(2)} MB/s`;
                } else {
                    sendSpeedStr = `${(speedBps / 1024).toFixed(2)} KB/s`;
                }
            }
            onProgress(uploadedChunks, chunks, sendSpeedStr, "uploadChunk");
        }

        console.log('所有分片上传完成，通知服务器合并文件');
        onProgress(chunks, chunks, "", "mergeChunksStart");
        const mergeAns = await mergeChunks(md5, file.name, chunks); // 通知服务器合并分片
        if (mergeAns.code != 0) {
            throw new Error(`${mergeAns.msg}`);
        }
        console.log(`mergeAllChunksReponse: code=${mergeAns.code} msg=${mergeAns.msg}`);
        return mergeAns.msg;
    };
  })();