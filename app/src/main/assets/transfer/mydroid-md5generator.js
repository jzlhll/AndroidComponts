// IIFE（传统非模块化环境）
(function() {
    // 计算文件唯一标识（MD5）
    window.streamBasedHashSmall = async function streamBasedHash(file) {
        const spark = new SparkMD5.ArrayBuffer();
        const stream = file.stream().getReader();
        
        while (true) {
            const { done, value } = await stream.read();
            if (done) break;
            spark.append(value.buffer);
        }
        
        return spark.end();
    };

    window.streamBasedHashBig = async function streamBasedHashWorker(file) {
        return new Promise((resolve, reject) => {
            const worker = new Worker('worker-mydroid-md5.js');
            
            // 创建可读流并分块传输
            const stream = file.stream();
            const reader = stream.getReader();
            
            worker.onmessage = (e) => {
                if (e.data.type === 'HASH_RESULT') {
                    resolve(e.data.hash);
                    worker.terminate(); // 计算完成后销毁Worker
                }
            };
            
            worker.onerror = (e) => {
                reject(new Error(`Worker error: ${e.message}`));
                worker.terminate();
            };
            
            // 分块发送数据到Worker
            const processChunk = async () => {
                const { done, value } = await reader.read();
                if (done) {
                    worker.postMessage({ type: 'FINISH' });
                    return;
                }
                worker.postMessage({ 
                    type: 'DATA_CHUNK', 
                    chunk: value.buffer 
                }, [value.buffer]); // 使用Transferable接口提升性能
                
                processChunk(); // 递归处理下一块
            };
            
            processChunk();
        });
    };
  })();