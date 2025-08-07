package com.au.audiorecordplayer.recorder;

import android.media.AudioFormat;
import android.media.AudioRecord;

import androidx.annotation.NonNull;

import com.au.audiorecordplayer.util.MyLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * 将pcm音频文件转换为wav音频文件
 */
public class PCMAndWavUtil {
    /**
     * 采样率
     */
    private final int mSampleRate;
    /**
     * 声道数
     */
    private final int mChannel;

    private final byte encodingBit;

    private final int encodingFmt;
    /**
     * @param sampleRate sample rate、采样率
     * @param inChannel channel、声道
     */
    public PCMAndWavUtil(int sampleRate, int inChannel, int encodingFmt) {
        this.mSampleRate = sampleRate;
        this.mChannel = inChannel;
        this.encodingFmt = encodingFmt;

        encodingBit = switch (encodingFmt) {
            case AudioFormat.ENCODING_PCM_8BIT -> 8;
            case AudioFormat.ENCODING_PCM_16BIT -> 16;
            default -> 8;
        };
    }


    /**
     * 添加wav头；尚缺他的length
     * 这是首先预制一个头，等录制完成后，endPcmHeader
     */
    public void addPcmHeader(@NonNull RandomAccessFile file) throws IOException{
        long totalAudioLen;
        long totalDataLen;
        int channels = mChannel == AudioFormat.CHANNEL_IN_MONO ? 1 : 2;
        long byteRate = (long) encodingBit * mSampleRate * channels / 8;
        totalAudioLen = 0; //暂时值为空
        totalDataLen = totalAudioLen + 36;

        writeWaveFileHeader(file, totalAudioLen, totalDataLen,
                mSampleRate, channels, byteRate, encodingBit);
    }
    /**
     * 结束一个文件PCm的Wav文件
     */
    public void endPcmHeader(@NonNull RandomAccessFile file, long totalAudioLen) throws IOException {
        long totalDataLen = totalAudioLen + 36;
        MyLog.d("endPcmHeader totalAudioLen:" + totalAudioLen + " totalData: " + totalDataLen);
        byte[] head4_7 = new byte[] {
                (byte) (totalDataLen & 0xff),
                (byte) ((totalDataLen >> 8) & 0xff),
                (byte) ((totalDataLen >> 16) & 0xff),
                (byte) ((totalDataLen >> 24) & 0xff)
        };
        file.seek(4);
        file.write(head4_7, 0, 4);
        byte[] head40_43 = new byte[] {
                (byte) (totalAudioLen & 0xff),
                (byte) ((totalAudioLen >> 8) & 0xff),
                (byte) ((totalAudioLen >> 16) & 0xff),
                (byte) ((totalAudioLen >> 24) & 0xff)
        };
        file.seek(40);
        file.write(head40_43, 0, 4);
    }
    /**
     * resume的时候，追加header。TODO 目前没有检测是否文件参数与之前相同。比如buffsize等。
     *
     * fileSize = totalDataLen + 8
     * fileSize = (totalAudioLen + 36) + 8
     */
    public void appendOldPcmHeader(@NonNull RandomAccessFile file, long oldFileSize, long newTotalAudioDataLen) throws IOException {
        long oldAudioDataLen = oldFileSize - 8 - 36;
        MyLog.d("append end pcm oldAudioDataLen " + oldAudioDataLen);
        long totalAudioLen = newTotalAudioDataLen + oldAudioDataLen;
        MyLog.d("append end pcm totalAudioLen " + totalAudioLen);
        long totalDataLen = totalAudioLen + 36;
        MyLog.d("append end pcm totalDataLen " + totalDataLen);

        byte[] head4_7 = new byte[] {
                (byte) (totalDataLen & 0xff),
                (byte) ((totalDataLen >> 8) & 0xff),
                (byte) ((totalDataLen >> 16) & 0xff),
                (byte) ((totalDataLen >> 24) & 0xff)
        };
        file.seek(4);
        file.write(head4_7, 0, 4);
        byte[] head40_43 = new byte[] {
                (byte) (totalAudioLen & 0xff),
                (byte) ((totalAudioLen >> 8) & 0xff),
                (byte) ((totalAudioLen >> 16) & 0xff),
                (byte) ((totalAudioLen >> 24) & 0xff)
        };
        file.seek(40);
        file.write(head40_43, 0, 4);
    }

    /**
     * 加入wav文件头
     */
    private static void writeWaveFileHeader(@NonNull Object file, long totalAudioLen, long totalDataLen,
                                            long longSampleRate, int channels, long byteRate, byte encodingBit)
            throws IOException {
        byte[] header = new byte[44];
        // https://www.cnblogs.com/ranson7zop/p/7657874.html 此篇文章界面了多种wav的格式；我们只研究PCM的格式的wav重建Header
        // RIFF/WAVE header
        header[0] = 'R';
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff); //fileLength = totalDataLen + 8
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        //WAVE
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        // 'fmt ' chunk
        header[12] = 'f';
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        // 4 bytes: size of 'fmt ' chunk
        header[16] = 16; //可以是 16、 18 、20、40 等; 1(0x0001)	PCM/非压缩格式 16; 2(0x0002	Microsoft ADPCM	18; 49(0x0031)	GSM 6.10	20
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        // format = 1
        header[20] = 1; //1表示pcm
        header[21] = 0;
        // channel number
        header[22] = (byte) channels;
        header[23] = 0;
        //采样频率
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        //数据传输速率 声道数×采样频率×每样本的数据位数/8 : channels * sampleRate * encodingFmt / 8
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        // block align 数据块对齐单位 声道数×位数 / 8
        header[32] = (byte) (2 * encodingBit / 8);
        header[33] = 0;
        // bits per sample
        header[34] = encodingBit;
        header[35] = 0;
        //data
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
        if (file instanceof FileOutputStream) {
            ((FileOutputStream)file).write(header, 0, 44);
        } else if (file instanceof RandomAccessFile) {
            ((RandomAccessFile)file).write(header, 0, 44);
        }
    }

    /**
     * pcm文件转wav文件
     *
     * @param inFilename 源文件路径
     * @param outFilename 目标文件路径
     */
    public void pcmToWav(String inFilename, String outFilename) {
        long totalAudioLen;
        long totalDataLen;
        int channels = mChannel == AudioFormat.CHANNEL_IN_MONO ? 1 : 2;
        long byteRate = (long) encodingBit * mSampleRate * channels / 8;
        int mBufferSize = AudioRecord.getMinBufferSize(mSampleRate, mChannel, encodingFmt);
        if (mBufferSize < 0) {
            MyLog.e("错误！！" + mBufferSize + "mSampleRate " + mSampleRate + " mChannel " + mChannel + " encodingFmt " + encodingFmt);
            return;
        }
        byte[] data = new byte[mBufferSize];
        try (FileInputStream in = new FileInputStream(inFilename);
             FileOutputStream out = new FileOutputStream(outFilename)) {
            totalAudioLen = in.getChannel().size();
            totalDataLen = totalAudioLen + 36;

            writeWaveFileHeader(out, totalAudioLen, totalDataLen,
                    mSampleRate, channels, byteRate, encodingBit);
            while (in.read(data) != -1) {
                out.write(data);
            }
        } catch (IOException e) {
            com.au.audiorecordplayer.util.MyLog.ex(e);
        }
    }

    public static PcmInfo getInfo(@NonNull String filePath) {
        return getInfo(new File(filePath));
    }

    public static PcmInfo getInfo(@NonNull File file) {
        try(FileInputStream fileInputStream = new FileInputStream(file)) {
            byte[] bytes = new byte[44];
            if (fileInputStream.read(bytes, 0, 44) == -1) {
                return null;
            }
            int sampleRate = ((bytes[24] & 0xff)  + (bytes[25] &0xff) * 16*16 +
                    (bytes[26] &0xff)* 16*16* 16*16 + (bytes[27] &0xff)* 16*16* 16*16* 16*16);

            int channelConfig = bytes[22] == 1 ? AudioFormat.CHANNEL_IN_MONO : AudioFormat.CHANNEL_IN_STEREO;
            //TODO 目前暂时支持这两种
            int encodingFmt = -1;
            encodingFmt = switch (bytes[34]) {
                case 8 -> AudioFormat.ENCODING_PCM_8BIT;
                case 16 -> AudioFormat.ENCODING_PCM_16BIT;
                default -> encodingFmt;
            };

            if (encodingFmt == -1) {
                return null; //TODO 目前暂时支持这两种，其实其他的比如float也有比较少，可以查spec
            }

            return new PcmInfo(null, sampleRate, channelConfig, encodingFmt);
        } catch (IOException e) {
            com.au.audiorecordplayer.util.MyLog.ex(e);
        }

        return null;
    }
}
