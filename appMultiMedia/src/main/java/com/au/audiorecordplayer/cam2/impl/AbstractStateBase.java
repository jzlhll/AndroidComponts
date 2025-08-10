package com.au.audiorecordplayer.cam2.impl;

import android.graphics.ImageFormat;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.util.Size;
import android.view.Surface;

import com.au.audiorecordplayer.util.CamLog;
import com.au.audiorecordplayer.util.MyLog;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * 抽象类，用于描述，不同的camera session状态
 * 这个类的子类都将是处于camera open之后的状态（StateDied除外）
 */
public abstract class AbstractStateBase {
    /**
     * 监听创建session的状态变化
     */
    public interface IStateBaseCallback {
    }

    protected MyCamManager cameraManager;

    protected IStateBaseCallback mStateBaseCb;
    protected CaptureRequest.Builder previewBuilder;

    /**
     * 对于在创建会话的时候，必须将所有用到的surface(包含take picture)都贴入createSession里面
     */
    protected List<Surface> addTargetSurfaces;

    protected AbstractStateBase(MyCamManager mgr) {
        cameraManager = mgr;
        step0_createSurfaces();
    }

    /**
     * 在类初始化的时候被调用。你不应该调用它，只需要实现它。
     * <p>
     * 在camera open之后，session创建之前
     * 根据不同的state，组合不同的surface
     */
    protected abstract void step0_createSurfaces();

    /**
     * 根据不同的state，贴入不同的surface
     * <p>
     * 你不应该调用它，只需要实现它
     */
    private void step2_addTargets() {
        if (previewBuilder != null) {
            for (Surface surface : addTargetSurfaces) {
                previewBuilder.addTarget(surface);
            }
        }
    }

    public void closeSession() {
        if (cameraManager != null) {
            if (cameraManager.getCamSession() != null) {
                cameraManager.getCamSession().close();
                cameraManager.setCamSession(null);
            } else {
                CamLog.d("no camera cam session");
            }
        }
        if (addTargetSurfaces != null) {
            addTargetSurfaces.clear();
        }
        addTargetSurfaces = null;
    }

    /**
     * 不同的session下有不同的模式
     * 子类可以根据需要覆写该方法。
     */
    protected int step1_getTemplateType() {
        return CameraDevice.TEMPLATE_PREVIEW;
    }

    /**
     * 该方法用于camera opened以后，创建preview、picture和record等的会话
     * 且session只有一个
     */
    public boolean createSession(IStateBaseCallback cb) {
        mStateBaseCb = cb;
        try {
            previewBuilder = Objects.requireNonNull(cameraManager.getCameraDevice()).createCaptureRequest(step1_getTemplateType());
            step2_addTargets();
        } catch (Exception e) {
            MyLog.ex(e);
            return false;
        }
        return true;
    }

    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }
    }

    protected final Size setSize(int wishWidth, int wishHeight) {
        StreamConfigurationMap map = cameraManager.getCamCharacters().get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        Size[] sizes = null;
        if (map != null) {
            sizes = map.getOutputSizes(ImageFormat.JPEG);
        }
        Size needSize = null;

        if (sizes != null) {
            for (Size size : sizes) {
                if (needSize == null) {
                    needSize = size;
                }

                if (size.getHeight() == wishHeight || size.getHeight() == wishWidth) { //TODO 这里是随便写写的。你可以采用google的compare class
                    needSize = size;
                    break;
                }
            }
        }

        CamLog.d("after wish size " + needSize);

        if (needSize == null) {
            throw new RuntimeException("No need Camera Size!");
        }

        cameraManager.setPreviewSize(needSize.getWidth(), needSize.getHeight());
        return needSize;
    }
}
