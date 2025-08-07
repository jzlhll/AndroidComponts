package com.au.audiorecordplayer.player.mediaplayer;

import android.media.MediaPlayer;
import android.os.HandlerThread;
import android.os.Message;

import com.au.audiorecordplayer.util.MyLog;
import com.au.audiorecordplayer.util.WeakHandler;

import java.io.IOException;

/**
 * MediaPlayer的状态就比较严谨了，这里只做简单的状态管理。
 * 如果过于随意点击则会报错。
 */
public class MyMediaPlayer implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, WeakHandler.Callback {
    private MediaPlayer mMediaPlayer;
    private St mCurrentSt = St.NOT_INIT;

    private static final int MSG_UPDATE_VISIBLE = 2;
    private static final int MSG_UPDATE_POS = 1;
    private static final int MSG_UPDATE_POS_INIT = 0;

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_UPDATE_POS:
                if (mPositionCallback != null && mMediaPlayer != null) {
                    mPositionCallback.onUpdate(mMediaPlayer.getCurrentPosition());
                }
                mWeakHandler.sendMessageDelayed(mWeakHandler.obtainMessage(MSG_UPDATE_POS), 500);
                break;
            case MSG_UPDATE_POS_INIT:
                if (mPositionCallback != null && mMediaPlayer != null) {
                    mPositionCallback.onInit(mMediaPlayer.getDuration());
                }
                mWeakHandler.sendMessageDelayed(mWeakHandler.obtainMessage(MSG_UPDATE_POS), 500);
                break;
            case MSG_UPDATE_VISIBLE:
                boolean visible = (boolean) msg.obj;
                if (mPositionCallback != null) {
                    mPositionCallback.onProgressVisible(visible);
                }
                break;
        }
    }

    private enum St {
        NOT_INIT,
        INIT,
        PLAYING,
        PAUSED,
    }

    private WeakHandler mWeakHandler;
    private PositionCallback mPositionCallback;
    public void setPositionCallback(PositionCallback p) {
        mPositionCallback = p;
    }

    public MyMediaPlayer() {
    }

    public void start(String filePath) {
        if (mCurrentSt == St.PAUSED || mCurrentSt == St.PLAYING) {
            MyLog.d("errr!!");
            return;
        }

        if (St.NOT_INIT == mCurrentSt) {
            mMediaPlayer = new MediaPlayer();
        }

        if (mWeakHandler == null) {
            HandlerThread t = new HandlerThread("handle thread");
            t.start();
            mWeakHandler = new WeakHandler(this);
        }

        //1. 第一种方案
        //setDataSource (String path)
        //setDataSource (FileDescriptor fd)
        //setDataSource (Context context, Uri uri)
        //setDataSource (FileDescriptor fd, long offset, long length)
        try {
            mMediaPlayer.setDataSource(filePath);
        } catch (IOException e) {
            MyLog.ex(e);
            throw new RuntimeException("错误的初始化mediaplayer");
        }
        //2. 第二种方案
        //TODO mMediaPlayer = MediaPlayer.create(context, R.raw.soundeffect_paopao);
        //下面的选择可以打开一个
        //mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        //TODO mMediaPlayer.setAudioStreamType();
        mCurrentSt = St.INIT;
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.prepareAsync();
    }

    public void pause() {
        if (mCurrentSt == St.PLAYING) {
            mMediaPlayer.pause();
            mCurrentSt = St.PAUSED;
        }
    }

    public void resume() {
        if (mCurrentSt == St.PAUSED) {
            mMediaPlayer.start();
            mCurrentSt = St.PLAYING;
        }
    }

    public void stop() {
        if (mCurrentSt == St.NOT_INIT) {
            return;
        }
        mMediaPlayer.stop();
        mMediaPlayer.reset();
        mCurrentSt = St.INIT;
    }

    private void release() {
        mMediaPlayer.release();
        mCurrentSt = St.NOT_INIT;
        mMediaPlayer = null;
    }

    public void close() {
        if (mWeakHandler != null) {
            mWeakHandler.removeCallbacksAndMessages(null);
        }
    }

    public void seekTo(int msec) {
        if (mCurrentSt == St.PLAYING || mCurrentSt == St.PAUSED) {
            mMediaPlayer.seekTo(msec);
        }
    }

    //setOnPreparedListener(this)实现的方法
    @Override
    public void onPrepared(MediaPlayer mp) {
        mMediaPlayer.start();
        mCurrentSt = St.PLAYING;
        mWeakHandler.sendMessage(mWeakHandler.obtainMessage(MSG_UPDATE_VISIBLE, true));
        mWeakHandler.sendEmptyMessage(MSG_UPDATE_POS_INIT);
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        MyLog.d("onComplete!!!");
        mMediaPlayer.stop();
        mCurrentSt = St.INIT;
        mWeakHandler.sendMessage(mWeakHandler.obtainMessage(MSG_UPDATE_VISIBLE, false));
        release();
    }

    public interface PositionCallback {
        void onInit(int totalMsec);
        void onUpdate(int curMsec);
        void onProgressVisible(boolean visible);
    }
}
