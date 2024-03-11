package com.au.jobstudy.pictures;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

public class MeMoryBitmap {
    public static MeMoryBitmap mInstance=null;
    private MeMoryBitmap() {}

    public static MeMoryBitmap getInstance() {
        if(mInstance==null){
            mInstance=new MeMoryBitmap();
        }
        return mInstance;
    }

    /**
     * 解决内存泄漏问题
     * @param
     */
    public void recycleImageView(ImageView ivPhotoshow) {
        if(ivPhotoshow==null){return;}
        if(ivPhotoshow instanceof ImageView){
            Drawable drawable=ivPhotoshow.getDrawable();
            //instanceof 是 Java 的保留关键字，它的作用是测试它左边的对象是否是它右边的类的实例
            if(drawable instanceof BitmapDrawable){
                Bitmap bmp=((BitmapDrawable) drawable).getBitmap();
                if(bmp!=null&&!bmp.isRecycled()){
                    ivPhotoshow.setImageBitmap(null);
                    bmp.recycle();

                }
            }
        }
    }

    /**
     * 动态获取图片的缩放值
     *
     * @param options
     *            BitmapFactory.Options
     * @param reqWidth
     *            设定的Img控件宽度
     * @param reqHeight
     *            设定的Img控件高度
     * @return inSampleSize
     */
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight)
    {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth)
        {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth)
            {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
}
