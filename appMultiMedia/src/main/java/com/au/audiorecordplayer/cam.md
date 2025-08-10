### Camera1

涉及的类和主要函数作用：

#### Camera

 Camera.open(id)打开摄像头；可以设置方向；设置params比如图像大小，预览大小，自动对焦等；

startPreview() 进行预览。

takePicture() 进行拍照。

#### SurfaceView/TextureView

SurfaceView: 提供画布，用于预览camera。监听显示变化,surfaceCreated, surfaceChanged, surfaceDestroyed;

将surface传入到camera中：camera.setPreviewDisplay(surfaceView.holder) /setPreviewSurface(surface)

另外一个选择是TextureView做显示，能够提供给多的View的属性，比如旋转缩放平移等。但是到了android7，SurfaceView解决了位置移动的黑边撕裂问题。性能也大幅优化，推荐使用SurfaceView。

代码区别就是把surfaceTexture包裹成Surface传递给MediaRecorder。surface texture传递给其实在camera的JNI层，仍然转换成了Surface。殊途同归。

#### MediaRecorder

录像前需要camera.unlock()

将camera设置给MediaRecorder；

设置setAudioSource音频源，setVideoSource视频源；

setProfile设置质量；

setOutputFile设置录制文件；

此时将surface传递给mediaRecorder.setPreviewDisplay(mHolder.surface) ,目的就是让现在的view交给mediaRecorder一边录制一边渲染；

最后prepare()/start()就开始录制了。

- 自动循环录制：setNextOutputFile 设置切换自动最大文件的下一个文件(配合setMaxDuration和setMaxFileSize，和MediaRecorder.OnInfoListener)；

### Camera2

涉及的类和函数主要作用：

### android.media.Image

直接图像缓冲区对象。直接访问编解码器、相机的原始像素缓冲区。

基本上都是通过ImageReader.acquireLatestImage()得到并必须close()释放。

ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();byte[] bytes = new byte[buffer.remaining()];通过这2个代码得到一个可以保存成图像文件的数据。

 ##### Planes 多平面结构

| 图像格式                  | 平面数 | 平面索引                           | 数据内容         |
| :------------------------ | :----- | :--------------------------------- | :--------------- |
| `ImageFormat.JPEG`        | 1      | Plane[0]                           | 完整JPEG压缩数据 |
| `ImageFormat.YUV_420_888` | 3      | Plane[0]=Y, Plane[1]=U, Plane[2]=V | YUV分量数据      |
| `ImageFormat.RAW_SENSOR`  | 1      | Plane[0]                           | 原始传感器数据   |

```
ByteBuffer buffer = planes[0].getBuffer();
int pixelStride = planes[0].getPixelStride(); // 像素步长
int rowStride   = planes[0].getRowStride();   // 行步长
```

根据ImageReader设置的不同的参数，



https://juejin.cn/post/7354922285092847668
https://blog.csdn.net/ItJavawfc/article/details/146088044

usbcamera和camera1

https://www.nxrte.com/jishu/50169.html 架构讲的好camerax



https://www.nxrte.com/jishu/yinshipin/60247.html 音视频进程学习

https://www.nxrte.com/tag/camerax camerax

### 官方文档
https://developer.android.google.cn/media/camera/get-started-with-camera?hl=zh-cn
https://developer.android.google.cn/media/camera/camera-intents?hl=zh-cn

https://developer.android.google.cn/media/camera/camera2/capture-sessions-requests?hl=hr

https://developers.google.cn/codelabs/camerax-getting-started?hl=zh_cn#1

#### 不集成camera库

* Intent的方式

  ```kotlin
  val REQUEST_IMAGE_CAPTURE = 1
   
  private fun dispatchTakePictureIntent() {
      val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
      try {
          startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
      } catch (e: ActivityNotFoundException) {
          // display error state to the user
      }
  }
  ```

  ```kotlin
  val REQUEST_VIDEO_CAPTURE = 1
   
  private fun dispatchTakeVideoIntent() {
      Intent(MediaStore.ACTION_VIDEO_CAPTURE).also { takeVideoIntent ->
          takeVideoIntent.resolveActivity(packageManager)?.also {
              startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE)
          } ?: run {
            //display error state to the user
          }
      }
  }
  ```

* ActivityResultContracts&ActivityResultCallback

  ```kotlin
  ActivityResultContracts.TakePicture()
  ActivityResultContracts.CaptureVideo()
  ```

#### CameraX

支持预览，图片分析(可以无缝访问缓冲区的图片传递给算法分析)，拍照和拍视频。

##### CameraX架构



#### Extensions API

焦外成像，HDR（高动态范围），夜间模式，脸部修复等。





#### ML 二维码





#### 个人心得

首先，代码使用层面架构。

Camera1：camera+SurfaceView/TextureView+MediaRecorder。使用简单，功能单一。

Camera2：基于各种回调，复杂，功能强大。

CameraX：基于camera2开发，简化原来复杂的代码，同时兼容市面上98%的设备，配合扩展库能做更多的图像分析，二维码识别等。



其次，framework架构上：

Camera1：

Camera2：底层Hal3



最后我最了什么：

1. 多路开关研究，在camera—service端有限制打开；
2. Adas算法库植入额外copy buffer；
3. MediaRecorder循环录制；
4. MTK HAL层虚拟上屏；
5. 配合framework做降频降温处理；
6. AIDL管理视频文件分享；wifi局域网通信；IOS开发；
7. mp4断电保护。

