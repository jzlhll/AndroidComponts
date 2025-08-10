package com.au.audiorecordplayer.cam2.impl.picture;

import android.media.Image;

import com.au.audiorecordplayer.util.CamLog;
import com.au.audiorecordplayer.util.MyLog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * 这里给出的是直接处理Jpeg的保存方式。只获取plane[0]即可。
 * 如果是YUV数据，则需要特殊处理。
 */
public class JpegImageSaverRunnable implements Runnable{

    /**
     * The JPEG image
     */
    private final Image mImage;
    /**
     * The file we save the image into.
     */
    private final File mFile;

    public JpegImageSaverRunnable(Image image, File file) {
        mImage = image;
        mFile = file;
    }

    @Override
    public void run() {
        ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        CamLog.d("Image save buffers " + bytes.length);
        try (FileOutputStream output = new FileOutputStream(mFile)) {
            output.write(bytes);
            CamLog.d("Image save buffers File " + mFile);
        } catch (IOException e) {
            MyLog.ex(e);
        } finally {
            mImage.close();
        }
    }

    /**
     // 获取YUV图像
     Image image = ...;
     Image.Plane[] planes = image.getPlanes();

     // 1. 提取Y分量（亮度）
     ByteBuffer yBuffer = planes[0].getBuffer();
     byte[] yBytes = new byte[yBuffer.remaining()];
     yBuffer.get(yBytes);

     // 2. 提取UV分量（色度）
     ByteBuffer uBuffer = planes[1].getBuffer();
     byte[] uBytes = new byte[uBuffer.remaining()];

     // 3. 合并为完整YUV数据
     ByteBuffer totalBuffer = ByteBuffer.allocate(yBytes.length + uBytes.length + planes[2].getBuffer().remaining());
     totalBuffer.put(yBytes)
     .put(uBytes)
     .put(planes[2].getBuffer());

     // 4. 写入文件（实际使用需考虑YUV存储格式）
     Files.write(new File("capture.yuv").toPath(), totalBuffer.array());




     // 有效数据宽度可能小于rowStride
     int effectiveWidth = image.getWidth();
     int yRowStride = planes[0].getRowStride();
     byte[] realYData = new byte[effectiveWidth * image.getHeight()];

     for (int row = 0; row < image.getHeight(); row++) {
     yBuffer.position(row * yRowStride);
     yBuffer.get(realYData, row * effectiveWidth, effectiveWidth);
     }
     */
}
