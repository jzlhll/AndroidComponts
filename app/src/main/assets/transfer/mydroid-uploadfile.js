(function() {
    const MERGE_CHUNKS = "/merge-chunks";
    const UPLOAD_CHUNK = "/upload-chunk";
    const ABORT_UPLOAD_CHUNKS = "/abort-upload-chunks";

    window.generateMd5Small = async function(file) {
        const spark = new SparkMD5.ArrayBuffer();
        const stream = file.stream().getReader();
        while (true) {
            const { done, value } = await stream.read();
            if (done) break;
            spark.append(value.buffer);
        }
        return spark.end();
    }

    // 上传分片
    async function uploadChunk(formData) {
        let response = null;
        try {
            response = await fetch(UPLOAD_CHUNK, {
                    method: 'POST',
                    body: formData,
                });
        } catch(e) {}
        if (!response) throw new Error(loc["error_upload_failure_e01"]);
        if (!response.ok) throw new Error(loc["error_upload_failure_e02"] + (await response.text()));
        return await response.json();
    }

    // 通知服务器合并分片
    async function mergeChunks(md5, fileName, totalChunks, lastModified) {
        let response = null;
        try {
            response = await fetch(MERGE_CHUNKS, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ md5, fileName, totalChunks, lastModified }),
            });
        } catch(e) {}
        if (!response) throw new Error(loc["error_upload_failure_e03"]);
        if (!response.ok) throw new Error(loc["error_upload_failure_e04"] + (await response.text()));
        return await response.json();
    }

    async function abortUploadChunk(md5, fileName) {
                let response = null;
        try {
            response = await fetch(ABORT_UPLOAD_CHUNKS, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ md5, fileName }),
            });
        } catch(e) {}
        if (!response) throw new Error(loc["error_upload_failure_e05"]);
        if (!response.ok) throw new Error(loc["error_upload_failure_e06"] + (await response.text()));
        return await response.json();
    }
    
    // 64k 600kb/s 256k 2MB/s 512k 2.8~3MB/s 1M 6MB/s 分块越大，越快。
    function getChunkSize(fileSize) {
        const MB = 1024 * 1024;
        if (fileSize <= 10 * MB) {
            return MB / 2;
        } else if (fileSize <= 100 * MB) {
            return 3 * MB;
        } else if (fileSize <= 500 * MB) {
            return 4 * MB;
        } else {
            return 5 * MB;
        }
    }

    window.startUploadFile = async function(startTs, file, md5, onProgress = () => {}) {
        if (!file) {
            throw new Error(loc["no_file_selected"]);
        }
    
        const fileName = file.name;
        const fileSize = file.size;
        const chunkSize = getChunkSize(fileSize);
        console.log("选择的chunkSize是" + chunkSize);

        const chunks = Math.ceil(fileSize / chunkSize); // 计算分片数量
        let uploadedChunks = 0; // 已上传的分片数量
        let sendBytes = 0;
        const startTime = Date.now();
        let deltaTime = 0;
        let sendSpeedStr = "-- KB/s";

        let isAbort = false;
        for (let i = 0; i < chunks; i++) {
            //放在这里可以让最后一包如果完成的话，就接着完成。
            if (startTs != window.lastStartTsFlagArr.startUploadTime) {
                await abortUploadChunk(md5, fileName);
                isAbort = true;
                break;
            }

            const start = i * chunkSize;
            const end = Math.min(fileSize, start + chunkSize);

            sendBytes += (end - start);
            const chunk = file.slice(start, end); // 切割分片

            const formData = new FormData();
            console.log(`send chunk ${fileName} ${md5}: ${i}/${chunks}`);
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

            if (window.debugReceiver == true) {
                await delay(1000);
            }
        }

        if (isAbort) {
            onProgress(0, 0, "", "abort");
            return "abort";
        } else {
            console.log('所有分片上传完成，通知服务器合并文件', file);
            onProgress(chunks, chunks, "", "mergeChunksStart");
            const mergeAns = await mergeChunks(md5, fileName, chunks, file.lastModified); // 通知服务器合并分片
            if (mergeAns.code != 0) {
                throw new Error(`${mergeAns.msg}`);
            }
            console.log(`mergeAllChunksResponse: code=${mergeAns.code} msg=${mergeAns.msg}`);
            onProgress(chunks, chunks, "", "complete");
            return mergeAns.msg;
        }
    };
  })();