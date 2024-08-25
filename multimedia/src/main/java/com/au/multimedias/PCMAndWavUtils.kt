package com.au.multimedias

import android.media.AudioFormat
import android.media.AudioRecord
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.RandomAccessFile


/**
 * 将pcm音频文件转换为wav音频文件
 */
class PCMAndWavUtil(
            /**
             * 采样率
             */
            private val mSampleRate:Int,
            /**
             * 声道数
             */
            private val mChannel:Int,
            /**
             * 采样率
             */
            private val encodingFmt:Int) {

    private var encodingBit: Byte = 0

    init {
        when (encodingFmt) {
            AudioFormat.ENCODING_PCM_8BIT -> encodingBit = 8
            AudioFormat.ENCODING_PCM_16BIT -> encodingBit = 16
        }
    }

    /**
     * 添加wav头；尚缺他的length
     * 这是首先预制一个头，等录制完成后，endPcmHeader
     */
    @Throws(IOException::class)
    fun addPcmHeader(file: RandomAccessFile) {
        val longSampleRate = mSampleRate.toLong()
        val channels = if (mChannel == AudioFormat.CHANNEL_IN_MONO) 1 else 2
        val byteRate = (encodingBit * mSampleRate * channels / 8).toLong()

        val totalAudioLen: Long = 0 //暂时值为空
        val totalDataLen = totalAudioLen + 36
        writeWaveFileHeader(
            file, totalAudioLen, totalDataLen,
            longSampleRate, channels, byteRate, encodingBit
        )
    }

    /**
     * 结束一个文件PCm的Wav文件
     * @param file
     * @param totalAudioLen
     * @throws IOException
     */
    @Throws(IOException::class)
    fun endPcmHeader(file: RandomAccessFile, totalAudioLen: Long) {
        val totalDataLen = totalAudioLen + 36
        Log.d("PCMAndWavUtils", "endPcmHeader totalAudioLen:$totalAudioLen totalData: $totalDataLen")
        val head4_7 = byteArrayOf(
            (totalDataLen and 0xff).toByte(),
            (totalDataLen shr 8 and 0xff).toByte(),
            (totalDataLen shr 16 and 0xff).toByte(),
            (totalDataLen shr 24 and 0xff).toByte()
        )
        file.seek(4)
        file.write(head4_7, 0, 4)
        val head40_43 = byteArrayOf(
            (totalAudioLen and 0xff).toByte(),
            (totalAudioLen shr 8 and 0xff).toByte(),
            (totalAudioLen shr 16 and 0xff).toByte(),
            (totalAudioLen shr 24 and 0xff).toByte()
        )
        file.seek(40)
        file.write(head40_43, 0, 4)
    }

    /**
     * resume的时候，追加header。TODO 目前没有检测是否文件参数与之前相同。比如buffsize等。
     *
     * fileSize = totalDataLen + 8
     * fileSize = (totalAudioLen + 36) + 8
     */
    @Throws(IOException::class)
    fun appendOldPcmHeader(file: RandomAccessFile, oldFileSize: Long, newTotalAudioDataLen: Long) {
        val oldAudioDataLen = oldFileSize - 8 - 36
        Log.d("PCMAndWavUtils", "append end pcm oldAudioDataLen $oldAudioDataLen")
        val totalAudioLen = newTotalAudioDataLen + oldAudioDataLen
        Log.d("PCMAndWavUtils", "append end pcm totalAudioLen $totalAudioLen")
        val totalDataLen = totalAudioLen + 36
        Log.d("PCMAndWavUtils", "append end pcm totalDataLen $totalDataLen")
        val head4_7 = byteArrayOf(
            (totalDataLen and 0xff).toByte(),
            (totalDataLen shr 8 and 0xff).toByte(),
            (totalDataLen shr 16 and 0xff).toByte(),
            (totalDataLen shr 24 and 0xff).toByte()
        )
        file.seek(4)
        file.write(head4_7, 0, 4)
        val head40_43 = byteArrayOf(
            (totalAudioLen and 0xff).toByte(),
            (totalAudioLen shr 8 and 0xff).toByte(),
            (totalAudioLen shr 16 and 0xff).toByte(),
            (totalAudioLen shr 24 and 0xff).toByte()
        )
        file.seek(40)
        file.write(head40_43, 0, 4)
    }

    /**
     * pcm文件转wav文件
     *
     * @param inFilename 源文件路径
     * @param outFilename 目标文件路径
     */
    fun pcmToWav(inFilename: String?, outFilename: String?) {
        var totalAudioLen: Long
        var totalDataLen: Long
        val longSampleRate = mSampleRate.toLong()
        val channels = if (mChannel == AudioFormat.CHANNEL_IN_MONO) 1 else 2
        val byteRate = (encodingBit * mSampleRate * channels / 8).toLong()
        val mBufferSize = AudioRecord.getMinBufferSize(mSampleRate, mChannel, encodingFmt)
        if (mBufferSize < 0) {
            Log.d("PCMAndWavUtils", "错误！！" + mBufferSize + "mSampleRate " + mSampleRate + " mChannel " + mChannel + " encodingFmt " + encodingFmt)
            return
        }
        val data = ByteArray(mBufferSize)
        try {
            FileInputStream(inFilename).use { fis ->
                FileOutputStream(outFilename).use { out ->
                    totalAudioLen = fis.channel.size()
                    totalDataLen = totalAudioLen + 36
                    writeWaveFileHeader(
                        out, totalAudioLen, totalDataLen,
                        longSampleRate, channels, byteRate, encodingBit
                    )
                    while (fis.read(data) != -1) {
                        out.write(data)
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    companion object {
        /**
         * 加入wav文件头
         */
        @Throws(IOException::class)
        private fun writeWaveFileHeader(
            file: Any, totalAudioLen: Long, totalDataLen: Long,
            longSampleRate: Long, channels: Int, byteRate: Long, encodingBit: Byte
        ) {
            val header = ByteArray(44)
            // https://www.cnblogs.com/ranson7zop/p/7657874.html 此篇文章界面了多种wav的格式；我们只研究PCM的格式的wav重建Header
            // RIFF/WAVE header
            header[0] = 'R'.code.toByte()
            header[1] = 'I'.code.toByte()
            header[2] = 'F'.code.toByte()
            header[3] = 'F'.code.toByte()
            header[4] = (totalDataLen and 0xff).toByte() //fileLength = totalDataLen + 8
            header[5] = (totalDataLen shr 8 and 0xff).toByte()
            header[6] = (totalDataLen shr 16 and 0xff).toByte()
            header[7] = (totalDataLen shr 24 and 0xff).toByte()
            //WAVE
            header[8] = 'W'.code.toByte()
            header[9] = 'A'.code.toByte()
            header[10] = 'V'.code.toByte()
            header[11] = 'E'.code.toByte()
            // 'fmt ' chunk
            header[12] = 'f'.code.toByte()
            header[13] = 'm'.code.toByte()
            header[14] = 't'.code.toByte()
            header[15] = ' '.code.toByte()
            // 4 bytes: size of 'fmt ' chunk
            header[16] = 16 //可以是 16、 18 、20、40 等; 1(0x0001)	PCM/非压缩格式 16; 2(0x0002	Microsoft ADPCM	18; 49(0x0031)	GSM 6.10	20
            header[17] = 0
            header[18] = 0
            header[19] = 0
            // format = 1
            header[20] = 1 //1表示pcm
            header[21] = 0
            // channel number
            header[22] = channels.toByte()
            header[23] = 0
            //采样频率
            header[24] = (longSampleRate and 0xff).toByte()
            header[25] = (longSampleRate shr 8 and 0xff).toByte()
            header[26] = (longSampleRate shr 16 and 0xff).toByte()
            header[27] = (longSampleRate shr 24 and 0xff).toByte()
            //数据传输速率 声道数×采样频率×每样本的数据位数/8 : channels * sampleRate * encodingFmt / 8
            header[28] = (byteRate and 0xff).toByte()
            header[29] = (byteRate shr 8 and 0xff).toByte()
            header[30] = (byteRate shr 16 and 0xff).toByte()
            header[31] = (byteRate shr 24 and 0xff).toByte()
            // block align 数据块对齐单位 声道数×位数 / 8
            header[32] = (2 * encodingBit / 8).toByte()
            header[33] = 0
            // bits per sample
            header[34] = encodingBit
            header[35] = 0
            //data
            header[36] = 'd'.code.toByte()
            header[37] = 'a'.code.toByte()
            header[38] = 't'.code.toByte()
            header[39] = 'a'.code.toByte()
            header[40] = (totalAudioLen and 0xff).toByte()
            header[41] = (totalAudioLen shr 8 and 0xff).toByte()
            header[42] = (totalAudioLen shr 16 and 0xff).toByte()
            header[43] = (totalAudioLen shr 24 and 0xff).toByte()
            if (file is FileOutputStream) {
                file.write(header, 0, 44)
            } else if (file is RandomAccessFile) {
                file.write(header, 0, 44)
            }
        }

        fun getInfo(filePath: String): PcmInfo? {
            return getInfo(File(filePath))
        }

        fun getInfo(file: File): PcmInfo? {
            try {
                FileInputStream(file).use { fileInputStream ->
                    val bytes = ByteArray(44)
                    if (fileInputStream.read(bytes, 0, 44) == -1) {
                        return null
                    }
                    val sampleRate =
                        (bytes[24].toInt() and 0xff) +
                        (bytes[25].toInt() and 0xff) * 16 * 16 +
                        (bytes[26].toInt() and 0xff) * 16 * 16 * 16 * 16 +
                        (bytes[27].toInt() and 0xff) * 16 * 16 * 16 * 16 * 16 * 16
                    val channelConfig =
                        if (bytes[22].toInt() == 1) AudioFormat.CHANNEL_IN_MONO else AudioFormat.CHANNEL_IN_STEREO
                    //TODO 目前暂时支持这两种
                    var encodingFmt = -1
                    when (bytes[34].toInt()) {
                        8 -> encodingFmt = AudioFormat.ENCODING_PCM_8BIT
                        16 -> encodingFmt = AudioFormat.ENCODING_PCM_16BIT
                    }
                    return if (encodingFmt == -1) {
                        null //TODO 目前暂时支持这两种，其实其他的比如float也有比较少，可以查spec
                    } else PcmInfo(null, sampleRate, channelConfig, encodingFmt)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return null
        }
    }
}

