(function() {
    // 前端代码（需运行在安全上下文 HTTPS 或 localhost）
    let fileStream; // 文件写入流
    let fileHandle; // 文件句柄

    // 存储不同文件的写入流（处理多个文件并发）
    const activeFiles = new Map();

    // 处理分片
    async function handleChunk(chunk) {
        // 解析元数据和二进制内容（根据实际传输协议）
        const arrayBuffer = await chunk.arrayBuffer();
        const { uuid, index, total, offset, data } = parseChunk(arrayBuffer);

        // 如果是第一个分片，请求用户选择保存位置
        if (index === 0 && !activeFiles.has(uuid)) {
          fileHandle = await window.showSaveFilePicker({
            suggestedName: `${uuid}.bin` // 可自定义文件名
          });
          fileStream = await fileHandle.createWritable();
          activeFiles.set(uuid, { fileStream, fileHandle });
        }

        // 获取当前文件的流
        const { fileStream } = activeFiles.get(uuid);

        // 定位到分片应写入的位置（关键！）
        await fileStream.seek(offset);

        // 直接写入磁盘
        await fileStream.write(data);

        // 如果是最后一个分片，关闭流
        if (index === total - 1) {
            await fileStream.close();
            activeFiles.delete(uuid);
            console.log("文件保存完成！");
        }
    }

    async function saveHugeFile(file, fileSize, chunkSize) { //  1024 * 1024 * 10 10MB/块


        const writer = fileStream.getWriter();
        let offset = 0;

        while (offset < file.size) {
            const chunk = file.slice(offset, offset + chunkSize);
            const buffer = await chunk.arrayBuffer();
            await writer.write(new Uint8Array(buffer));
            offset += chunkSize;
        }

        await writer.close(); // 完成写入
    }

    function parseChunk(arrayBuffer) {
        const dataView = new DataView(arrayBuffer);
        // 假设前 4 字节为 UUID 长度
        const uuidLen = 32; //dataView.getUint32(0, false);

        let startIndex = 0;
        const uuidDecoder = new TextDecoder();
        const uuid = uuidDecoder.decode(arrayBuffer.slice(startIndex, startIndex + uuidLen));
        startIndex = startIndex + uuidLen;
        // 后续为分片信息
        const index = dataView.getUint32(startIndex, false);
        startIndex = startIndex + 4;
        const total = dataView.getUint32(startIndex, false);
        startIndex = startIndex + 4;
        const offset = dataView.getUint32(startIndex, false);
        startIndex = startIndex + 4;
        const dataSize = dataView.getUint32(startIndex, false);
        startIndex = startIndex + 4;
        const data = arrayBuffer.slice(startIndex, startIndex + dataSize);
        console.log(`parseChunk ${uuid} index:${index}/${total} offset:${offset} dataSize:${dataSize}`);
        return { uuid, index, total, offset, data };
    }

    window.parseMessage = function(eventData) {
        if (eventData instanceof Blob || eventData instanceof ArrayBuffer) {
            console.log("receiver blob");
            // 手动创建 File 对象
            const manualFile = new File(["Hello, World!"], "hello.png", {
              type: "*/*",
              lastModified: Date.now()
            });
               const fileStream = StreamSaver.createWriteStream(file.name, {
                        size: file.size // 可选，提供文件大小以显示进度
                    });
            handleChunk(manualFile, eventData);
            return true;
        } else {
            const jsonData = JSON.parse(eventData);
            const data = jsonData.data;
            const api = jsonData.api;
            const msg = jsonData.msg;

            if (api == API_SEND_FILE_CHUNK) {
                console.log(api, "chunk bytes ", msg);
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