package com.au.audiorecordplayer.player.audiotracker;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import com.au.audiorecordplayer.recorder.PCMAndWavUtil;
import com.au.audiorecordplayer.recorder.PcmInfo;
import com.au.audiorecordplayer.util.MyLog;
import com.au.module_android.utils.ThreadPoolUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;

/**
 MODE_STREAM：在这种模式下，通过write一次次把音频数据写到AudioTrack中。
 这和平时通过write系统调用往文件中写数据类似，
 但这种工作方式每次都需要把数据从用户提供的Buffer中拷贝到AudioTrack内部的Buffer中，
 这在一定程度上会使引起延时。为解决这一问题，AudioTrack就引入了第二种模式。


 MODE_STATIC：这种模式下，在play之前只需要把所有数据通过一次write
 调用传递到AudioTrack中的内部缓冲区，后续就不必再传递数据了。
 这种模式适用于像铃声这种内存占用量较小，延时要求较高的文件。
 但它也有一个缺点，就是一次write的数据不能太多，否则系统无法分配足够的内存来存储全部数据。
 */
public class MyAudioTracker {
    private byte[] audioData;
    private AudioTrack audioTrack;

    private final Object StreamModObject = new Object();

    private int mStreamModSt = STREAM_MOD_STOPPED;

    private static final int STREAM_MOD_STOPPED = -1;
    private static final int STREAM_MOD_PLAYING = 0;
    private static final int STREAM_MOD_PAUSED = 1;

    //TODO 人为设置一个超过这个大小的就走Stream模式. 真实的应该是音效的大小。建议小于20k
    private static final long LENGTH_OF_STREAM = 150*1000;

    private void playInStatic(Context context, final String filePath, Runnable notPcmFileCallback) {
        final SoftReference<Context> sf = new SoftReference<>(context);
        ThreadPoolUtils.getThreadPollProxy().execute(() -> {
            MyLog.d("loading....");
            if (audioTrack != null && audioTrack.getState() != AudioTrack.STATE_UNINITIALIZED) {
                release();
            }

            if (sf.get() == null) {
                return;
            }

            try (ByteArrayOutputStream out = new ByteArrayOutputStream(12*1024);
                 InputStream in = new FileInputStream(filePath)) {
                for (int b; (b = in.read()) != -1;) {
                    out.write(b);
                }

                if (sf.get() == null) {
                    return;
                }

                audioData = out.toByteArray();

                MyLog.d("audioData length " + audioData.length);
                PcmInfo pcm = PCMAndWavUtil.getInfo(filePath);
                if (pcm == null) {
                    notPcmFileCallback.run();
                    return;
                }

                var is8bit = pcm.getEncodingFmt() == AudioFormat.ENCODING_PCM_8BIT;

                audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, pcm.getSampleRate(),
                        pcm.getOutChannelConfig(), is8bit ? AudioFormat.ENCODING_PCM_8BIT : AudioFormat.ENCODING_PCM_16BIT,
                        audioData.length, AudioTrack.MODE_STATIC);
                audioTrack.write(audioData, 0, audioData.length);
                audioTrack.play();

            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                //
            }
        });
    }

    private void playInStream(Context context, final String filePath, Runnable notPcmFileCallback) {
        final SoftReference<Context> sf = new SoftReference<>(context);
        ThreadPoolUtils.getThreadPollProxy().execute(() -> {
            MyLog.d("loading....");
            if (audioTrack != null && audioTrack.getState() != AudioTrack.STATE_UNINITIALIZED) {
                release();
            }

            if (sf.get() == null) {
                return;
            }
            PcmInfo pcm = PCMAndWavUtil.getInfo(filePath);
            if (pcm == null) {
                notPcmFileCallback.run();
                return;
            }

            MyLog.d("loading...." + pcm);

            var is8bit = pcm.getEncodingFmt() == AudioFormat.ENCODING_PCM_8BIT;
            final int minBufferSize = AudioTrack.getMinBufferSize(pcm.getSampleRate(), pcm.getOutChannelConfig(),
                    is8bit ? AudioFormat.ENCODING_PCM_8BIT : AudioFormat.ENCODING_PCM_16BIT);

            audioTrack = new AudioTrack(
                    new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build(),
                    new AudioFormat.Builder().setSampleRate(pcm.getSampleRate())
                            .setChannelMask(pcm.getOutChannelConfig())
                            .setEncoding(is8bit ? AudioFormat.ENCODING_PCM_8BIT : AudioFormat.ENCODING_PCM_16BIT)
                            .build(),
                    minBufferSize,
                    AudioTrack.MODE_STREAM,
                    AudioManager.AUDIO_SESSION_ID_GENERATE);
            audioTrack.play();
            File f = new File(filePath);
            final long fileTotalLength = f.length();
            long currentTotalLength = 0;
            try (InputStream in = new FileInputStream(f)) {
                byte[] tempBuffer = new byte[minBufferSize];
                mStreamModSt = STREAM_MOD_PLAYING;
                while (in.available() > 0) {
                    synchronized (StreamModObject) {
                        while(STREAM_MOD_PAUSED == mStreamModSt) {
                            StreamModObject.wait();
                        }
                    }

                    if (STREAM_MOD_STOPPED == mStreamModSt) {
                        break;
                    }

                    int readCount = in.read(tempBuffer);
                    if (readCount == AudioTrack.ERROR_INVALID_OPERATION ||
                            readCount == AudioTrack.ERROR_BAD_VALUE) {
                        continue;
                    }
                    currentTotalLength += readCount;
                    MyLog.d("playing stream mode " + currentTotalLength + "/" + fileTotalLength);
                    if (readCount != 0 && readCount != -1) {
                        audioTrack.write(tempBuffer, 0, readCount);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                //
            }
        });
    }

    public int play(Context context, final String filePath, Runnable notPcmFileCallback) {
        if (audioTrack != null && audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
            MyLog.d("状态不对");
            return -1;
        }

        File file = new File(filePath);
        boolean isStatic = false;
        if (file.exists()) {
            MyLog.d("file length " + file.length());
            if (file.length() > LENGTH_OF_STREAM) {
                MyLog.d("走Stream Mod~~");
                playInStream(context, filePath, notPcmFileCallback);
            } else {
                MyLog.d("走Static Mod！");
                isStatic = true;
                playInStatic(context, filePath, notPcmFileCallback);
            }
        }

        return isStatic ? 1 : 0;
    }

    public void pause() {
        int playSt = audioTrack.getPlayState();
        MyLog.d("pause click!" + playSt);
        if (playSt == AudioTrack.PLAYSTATE_PLAYING && mStreamModSt == STREAM_MOD_PLAYING) {
            audioTrack.pause();
            audioTrack.flush();
            mStreamModSt = STREAM_MOD_PAUSED;
        }
    }

    public void resume() {
        int playSt = audioTrack.getPlayState();
        MyLog.d("resume click!" + playSt);
        if (playSt == AudioTrack.PLAYSTATE_PAUSED) {
            audioTrack.play();
            synchronized (StreamModObject) {
                mStreamModSt = STREAM_MOD_PLAYING;
                StreamModObject.notify();
            }
        }
    }

    public void stop() {
        int playSt = audioTrack.getPlayState();
        MyLog.d("stop click!" + playSt);
        if (playSt == AudioTrack.PLAYSTATE_PAUSED || playSt == AudioTrack.PLAYSTATE_PLAYING) {
            audioTrack.stop();
            synchronized (StreamModObject) {
                mStreamModSt = STREAM_MOD_STOPPED;
                StreamModObject.notify();
            }
        }
    }

    public void release() {
        if (audioTrack != null) {
            if (audioTrack.getState() != AudioTrack.STATE_UNINITIALIZED) {
                audioTrack.stop();
                audioTrack.flush();
                audioTrack.release();
            }
            audioData = null;
        }
    }
}
