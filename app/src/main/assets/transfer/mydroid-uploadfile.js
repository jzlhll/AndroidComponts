(function() {
    // 上传分片
    async function uploadChunk(formData) {
        const response = await fetch('/upload-chunk', {
            method: 'POST',
            body: formData,
        });
        if (!response.ok) throw new Error('上传失败');
    }

    // 通知服务器合并分片
    async function mergeChunks(fileHash, fileName, totalChunks) {
        const response = await fetch('/merge-chunks', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ fileHash, fileName, totalChunks }),
        });
        if (!response.ok) throw new Error('合并失败');
    }

    window.startUploadFile = async function startUploadFile(file, callback) {
        if (!file) {
            callback('请先选择文件', 'error');
            return;
        }
    
        const fileName = file.name;
    
        changeProgressVisible(true);
    
        const CHUNK_SIZE = 64 * 1024;
        const totalChunks = Math.ceil(file.size / CHUNK_SIZE);
    
        const fileSize = file.size;
        const chunks = Math.ceil(fileSize / CHUNK_SIZE); // 计算分片数量
    
        try {
            let uploadedChunks = 0; // 已上传的分片数量
            for (let i = 0; i < chunks; i++) {
                const start = i * CHUNK_SIZE;
                const end = Math.min(fileSize, start + CHUNK_SIZE);
                const chunk = file.slice(start, end); // 切割分片
    
                const formData = new FormData();
                formData.append('fileName', fileName);
                formData.append('chunk', chunk);
                formData.append('chunkIndex', i + 1);
                formData.append('totalChunks', chunks);
                formData.append('md5', md5);
    
                try {
                    const ans = await uploadChunk(formData); // 上传分片
                    console.log(`each response ${ans}`);
                    uploadedChunks++;
                    console.log(`分片 ${i + 1}/${chunks} 上传成功`);
                } catch (error) {
                    console.error(`分片 ${i + 1}/${chunks} 上传失败`, error);
                    return;
                }
            }
    
            console.log('所有分片上传完成，通知服务器合并文件');
            const mergeAns = await mergeChunks(md5, file.name, chunks); // 通知服务器合并分片
            console.log('mergeAns ' + mergeAns);
            callback('文件上传成功！', 'success');
            resetUI();
        } catch (err) {
            callback(`上传失败: ${err.message}`, 'error');
        }
    };
  })();