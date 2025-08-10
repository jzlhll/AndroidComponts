package com.au.audiorecordplayer.cam2.impl.states;

import android.util.Size;
import com.au.audiorecordplayer.cam2.impl.AbstractStateBase;
import com.au.audiorecordplayer.cam2.impl.MyCamManager;

import java.util.ArrayList;

public class StatePreview extends AbstractStateBase {
    public interface IStatePreviewCallback extends IStateBaseCallback{
        void onPreviewSucceeded();
        void onPreviewFailed();
    }

    Size mNeedSize = null;

    public StatePreview(MyCamManager cd) {
        super(cd);
    }

    @Override
    protected void step0_createSurfaces() {
        addTargetSurfaces = new ArrayList<>();
        mNeedSize = setSize(1920, 1080); //这里故意设置一些不同的分辨率,让不同的模式下有不同
        //其实我们可能希望拍照和预览和录像都保持preview size的不变。则这里设置好即可。
        addTargetSurfaces.add(cameraManager.getRealViewSurface());
    }
}

