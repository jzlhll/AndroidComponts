// IIFE（传统非模块化环境）
(function() {
    window.streamBasedHashWorker = async function streamBasedHashWorker(file, startTs) {
        return new Promise((resolve, reject) => {
            const worker = new Worker('worker-mydroid-md5.js');
            
            worker.onmessage = (e) => {
                const t = e.data.type;
                if (t === 'HASH_RESULT') {
                    resolve(e.data.hash);
                    worker.terminate(); // 计算完成后销毁Worker
                }
            };
            
            worker.onerror = (e) => {
                reject(`Worker error: ${e.message}`);
                worker.terminate();
            };
            const stream = file.stream();
            const reader = stream.getReader();

            const processChunk = async () => {
                // 分块发送数据到Worker 创建可读流并分块传输
                while (true) {
                    const { done, value } = await reader.read();
                    if (done) {
                        console.log(file.name + " finish.");
                        worker.postMessage({ type: 'FINISH' });
                        reader.releaseLock();
                        break;
                    } else if (startTs != window.lastStartTsFlagArr.startTs) {
                        console.log(file.name + " aborted.");
                        await reader.cancel();
                        reader.releaseLock();
                        worker.terminate(); // 直接终止 Worker
                        reject('aborted');
                        break;
                    } else {
                        worker.postMessage({ 
                            type: 'DATA_CHUNK', 
                            chunk: value.buffer 
                        }, [value.buffer]);
                    }
                }
            };
            
            processChunk();
        });
    };
  })();