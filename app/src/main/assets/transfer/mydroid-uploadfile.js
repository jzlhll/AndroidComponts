(function() {
    // 上传分片
    async function uploadChunk(formData) {
        const response = await fetch('/upload-chunk', {
            method: 'POST',
            body: formData,
        });
        if (!response.ok) throw new Error('上传失败E01 ' + (await response.text()));
        return await response.json();
    }

    // 通知服务器合并分片
    async function mergeChunks(md5, fileName, totalChunks) {
        const response = await fetch('/merge-chunks', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ md5, fileName, totalChunks }),
        });
        if (!response.ok) throw new Error('上传失败E02 ' + (await response.text()));
        return await response.json();
    }

    window.startUploadFile = async function startUploadFile(file, md5, onProgress = () => {}) {
        if (!file) {
            throw new Error("没有选择文件！");
        }
    
        const fileName = file.name;
    
        changeProgressVisible(true);
        // 64k 600kb/s 256k 2MB/s 512k 2.8~3MB/s 1M 6MB/s 分块越少，越快。
        const CHUNK_SIZE = 1024 * 1024;
        const fileSize = file.size;
        const chunks = Math.ceil(fileSize / CHUNK_SIZE); // 计算分片数量
        let uploadedChunks = 0; // 已上传的分片数量
        let sendBytes = 0;
        const startTime = Date.now();
        let deltaTime = 0;
        let sendSpeedStr = "-- KB/s";

        for (let i = 0; i < chunks; i++) {
            const start = i * CHUNK_SIZE;
            const end = Math.min(fileSize, start + CHUNK_SIZE);

            sendBytes += (end - start);
            const chunk = file.slice(start, end); // 切割分片

            const formData = new FormData();
            formData.append('fileName', fileName);
            formData.append('chunk', chunk);
            formData.append('chunkIndex', i + 1);
            formData.append('totalChunks', chunks);
            formData.append('md5', md5);

            const ans = await uploadChunk(formData); // 上传分片
            console.log(`chunkResponse: code=${ans.code} msg=${ans.msg}`);
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