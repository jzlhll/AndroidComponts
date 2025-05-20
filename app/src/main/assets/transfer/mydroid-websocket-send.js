(function() {
    //250MB就认为是小文件。
    const SMALL_FILE_DEFINE_SIZE = 250 * 1024 * 1024;

    let mFileSaver = null;
    const uuidDecoder = new TextDecoder();

    async function handleChunk(blob) {
        const arrayBuffer = await blob.arrayBuffer();
        const dataView = new DataView(arrayBuffer);
        // 假设前 4 字节为 UUID 长度
        const uuidLen = 32; //dataView.getUint32(0, false);

        const uuid = uuidDecoder.decode(arrayBuffer.slice(0, uuidLen));
        // 后续为分片信息
        const index = dataView.getUint32(32, false);
        const total = dataView.getUint32(36, false);
        const offset = dataView.getBigUint64(40, false);
        const dataSize = dataView.getUint32(48, false);
        const data = blob.slice(52, 52 + dataSize);
        console.log(`handle Chunk ${uuid} index:${index}/${total} offset:${offset} dataSize:${dataSize}`);

        await mFileSaver?.handleChunk(uuid, index, dataSize, data);
    }

    class AbsFileSaver {
        constructor() {
        }
        
        async onStart(uuid, totalFileSize, totalChunks) {

        }

        async onStop(uuid, fileName, totalFileSize, totalChunks) {

        }

        async handleChunk(uuid, index, dataSize, data) {

        }
    }

    class IndexedDBFileSaver extends AbsFileSaver {
        constructor() {
            super();
        }

        initLocalForage() {
            if(isInitLocalForage) return;
            isInitLocalForage = true;

            // 配置 LocalForage
            localforage.config({
                driver: localforage.INDEXEDDB, // 使用 IndexedDB 存储引擎
                name: 'myDroid', // 数据库名称
                version: 1.0, // 数据库版本
                storeName: 'merge_file' // 存储表名称
            });
        }

        async endChunks(totalIndexs) {
            const chunks = [];
            
            // 按顺序获取所有分片
            for (let i = 0; i < totalIndexs; i++) {
                const chunk = await localforage.getItem(`${uuid}_${i}`);
                if (!chunk) throw new Error(`Missing chunk ${i} for file ${uuid}`);
                chunks.push(chunk);
            }

            // 合并分片为Blob对象
            const blob = new Blob(chunks);
            const url = URL.createObjectURL(blob);
            
            // 创建下载链接
            const a = document.createElement('a');
            a.href = url;
            a.download = uuid; // 设置下载文件名
            document.body.appendChild(a);
            a.click();
            
            // 清理资源
            setTimeout(() => {
                document.body.removeChild(a);
                URL.revokeObjectURL(url);
            }, 100);

            // 删除临时分片
            for (let i = 0; i < totalIndexs; i++) {
                await localforage.removeItem(`${uuid}_${i}`);
            }
        }

        async onStop() {
            // 存储分片数据
            //await localforage.setItem(`${uuid}_${index}`, data);
        }
    }

    class SmallFileSaver extends AbsFileSaver {
        constructor() {
            super();
        }

        chunkMap = new Map(); // 用于跟踪文件分片状态

        async onStart(uuid, totalFileSize, totalChunks) {
            if (this.chunkMap.has(uuid)) {
                this.chunkMap.delete(uuid);
            }
            this.chunkMap.set(uuid, new Map());
        }

        async handleChunk(uuid, index, dataSize, data) {
            const chunks = this.chunkMap.get(uuid);
            const a = {};
            a.index = index;
            a.dataSize = dataSize;
            a.data = data;
            chunks.set(index, a);
        }

        checkCompletionOnce(uuid, totalChunks) {
            const chunks = this.chunkMap.get(uuid);
            if (!chunks) return false;

            let i = 0;
            while (i < totalChunks) {
                if (!chunks.get(i++)) {
                    return false;
                }
            }
            return true;
        }

        async onStop(uuid, fileName, totalFileSize, totalChunks) {
            const chunks = this.chunkMap.get(uuid);
            if (!chunks) return;
            let checkCount = 5;
            while (checkCount-- >= 0) {
                const isCompleted = this.checkCompletionOnce(uuid, totalChunks);
                if (isCompleted) {
                    break;
                }
                await delay(500);
            }
            
            if (checkCount > 0) {
                console.log(`${nowTimeStr()} on Stop all is good.`);
                try {
                    // 合并分片
                    const sortedChunks = Array.from(chunks.values())
                        .sort((a, b) => a.index - b.index)
                        .map(c => c.data);
                    
                    const mergedBlob = new Blob(sortedChunks, { type: 'application/octet-stream' });

                    // 创建下载链接
                    const downloadLink = document.createElement('a');
                    const objectUrl = URL.createObjectURL(mergedBlob);
                    
                    // 现代浏览器下载方案
                    downloadLink.href = objectUrl;
                    downloadLink.download = fileName;
                    document.body.appendChild(downloadLink);
                    downloadLink.click();

                    // 立即清理资源
                    requestAnimationFrame(() => {
                        document.body.removeChild(downloadLink);
                        URL.revokeObjectURL(objectUrl);
                    });

                    console.log(`文件 ${fileName} 下载完成`);
                } catch (error) {
                    console.error('合并下载失败:', error);
                }
                
                this.chunkMap.delete(uuid);
            } else {
                console.log(`${nowTimeStr()} on Stop is bad.`);
                this.chunkMap.delete(uuid);
            }
        }
    }

    window.parseMessage = async function(eventData) {
        if (eventData instanceof Blob) {
            handleChunk(eventData);
            return true;
        } else {
            const jsonData = JSON.parse(eventData);
            const data = jsonData.data;
            const api = jsonData.api;
            const msg = jsonData.msg;

            if (api == API_SEND_SMALL_FILE_CHUNK) {
                if (data.action == "start") {
                    console.log(api, msg, data);
                    if (!mFileSaver) {
                        mFileSaver = new SmallFileSaver();
                    }
                    mFileSaver.onStart(data.uriUuid, data.totalFileSize, data.totalChunks);
                } else if (data.action == "end") {
                    console.log(api, msg, data);
                    let fileName = data.fileName;
                    if (fileName == "") {
                        fileName = data.uriUuid;
                    }
                    mFileSaver?.onStop(data.uriUuid, fileName, data.totalFileSize, data.totalChunks);
                }
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