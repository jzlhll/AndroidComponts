package com.allan.nongyaofloat

import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.view.LayoutInflater
import android.view.View

import android.view.WindowManager

class FloatWindow(private val context:Context) {
//    private val windowManager: WindowManager
    private val view: View? = null
    private var density = 0f //屏幕密度

//    init {
//        val displayMetrics = context.resources.displayMetrics
//        density = displayMetrics.density
//        val screenWidth = displayMetrics.widthPixels
//        val screenHeight = displayMetrics.heightPixels
//
//        windowManager = context.getSystemService(WINDOW_SERVICE) as WindowManager
//        view = LayoutInflater.from(context).inflate(R.layout.itemlayout,null);
//        //3.WindowManager布局参数
//        WindowManager.LayoutParams layoutParams=new WindowManager.LayoutParams();
//
//        //判断版本   设置类型为悬浮窗使其可覆盖所有应用且不受限于当前Activity
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
//        } else {
//            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
//        }
//        layoutParams.format = PixelFormat.RGBA_8888;//设置颜色存储方式
//        //设置不可触碰（触碰后传递给下一层）| 不用获焦点（使周围空白处操作不受影响）
//        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
//        layoutParams.gravity = Gravity.LEFT | Gravity.TOP;//起始位置（重力）
//        //单位px 即100dp转成px +0.5f是为了四舍五入时不出0     虚拟像素(dp)*屏幕密度(density)=实际像素(px)
//        layoutParams.width=(int) (100*density+0.5f);
//        layoutParams.height=(int)(100*density+0.5f);
//        layoutParams.x=(int) ((screenWidth-100*density)/2);
//        layoutParams.y=(int) ((screenHeight-100*density)/2);
//
//        //添加View
//        windowManager.addView(view,layoutParams);
//
//        //为view添加监听(用于拖拽)
//        view.setOnTouchListener(new View.OnTouchListener() {
//            private int x,y;
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//                switch (motionEvent.getAction()) {
//                    case MotionEvent.ACTION_DOWN:
//                    x = (int) motionEvent.getRawX();
//                    y = (int)motionEvent.getRawY();
//                    break;
//                    case MotionEvent.ACTION_MOVE:
//                    int nowX = (int) motionEvent.getRawX();
//                    int nowY = (int) motionEvent.getRawY();
//                    int movedX = nowX - x;
//                    int movedY = nowY - y;
//                    x = nowX;
//                    y = nowY;
//                    layoutParams.x = layoutParams.x + movedX;
//                    layoutParams.y = layoutParams.y + movedY;
//
//                    // ***更新悬浮窗控件布局***
//                    windowManager.updateViewLayout(view, layoutParams);
//                    break;
//                    default:
//                    break;
//                }
//                return true;//返回了ture，则代表touch事件已处理用户的该次行为，不需要其他事件处理器进行处理，就不再将事件进行传递。如果返回了false，则代表未处理，需要将事件传递出去。
//            }
//        });
//    }
}