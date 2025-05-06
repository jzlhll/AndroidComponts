// 引入SparkMD5库（需确保该库在Worker作用域可用）
self.importScripts('spark-md5.min.js');

let spark = new self.SparkMD5.ArrayBuffer();

self.onmessage = function(e) {
  switch (e.data.type) {
    case 'DATA_CHUNK':
      spark.append(e.data.chunk); // 增量计算哈希
      break;
      
    case 'FINISH':
      const hash = spark.end(); // 获取最终结果
      self.postMessage({ 
        type: 'HASH_RESULT', 
        hash: hash 
      });
      spark.destroy(); // 清理内存
      break;
  }
};
