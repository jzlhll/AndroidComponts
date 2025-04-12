package com.au.module_android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.au.module_android.R;

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import androidx.annotation.NonNull;
import androidx.viewbinding.ViewBinding;

/**
 * ViewStub的Pro版本;
 * 参考https://blog.csdn.net/blankmargin/article/details/128158459 和ViewStub实现。
 *
 * <p>
 * 支持ViewStub本身的功能;
 * 可以动态添加单个View，不用再为单个View写一个xml布局
 * inflatedId的作用是避免ConstraintLayout的不传递不生效问题。
 * <p>
 * 用法eg:
 * <p>
 *     1. xml中设置replaceLayout（layout资源id）和inflatedId。然后viewStubPro.launch()。
 *     2. xml中设置replaceViewClass (class全名) 和 inflatedId 。然后viewStubPro.launch()。
 *          这种情况，需要保护replaceViewClass不被混淆名字和构造体。
 *     3. view = viewStubPro.setReplaceLayoutResource(R.layout.layout_any_layout).launch()。
 *     4. view = viewStubPro.setReplaceViewClass(class Or ClassFullName).launch()。
 *         这种情况，setReplaceViewClass(string)需要保护不被混淆名字和构造体。
 * <p>
 * 注意：暂时不支持<merge>标签
 */
public final class ViewStubPro extends View {
    private interface IReplace {  }

    private static final class ReplaceLayout implements IReplace {
        @LayoutRes
        private int mLayoutResource;
    }

    private static final class ReplaceView implements IReplace {
        private Class<? extends View> mViewClass;
    }

    private static final class ReplaceViewBinding<T extends ViewBinding> implements IReplace {
        private T mViewBinding;
    }

    private static final String TAG = "ViewStubPro";

    private final Context mContext;
    private WeakReference<View> mInflatedViewRef;

    private IReplace mReplace;

    /**
     * 从布局或者手动设定inflatedId。
     */
    @IdRes
    public int inflatedId;

    /**
     * 从外部设置进来；或者xml中。
     * 注意
     * 1. 如果是代码的话，自行转换了dp以后传入。即通过context.resource.getDimen(R.dimen.xx)进来
     * 2. 必须在未实例化Stub之前设置
     *
     */
    public int allCornerSize;

    //圆角矩形
    private ViewOutlineProvider mCustomViewOutlineProvider;

    private ViewOutlineProvider getCustomViewOutlineProvider() {
        if (mCustomViewOutlineProvider != null) {
            return mCustomViewOutlineProvider;
        }
        mCustomViewOutlineProvider = new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), allCornerSize);
            }
        };
        return mCustomViewOutlineProvider;
    }

    public ViewStubPro(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ViewStubPro(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ViewStubPro);

        //从xml初始化
        int layout = a.getResourceId(R.styleable.ViewStubPro_replaceLayout, 0);
        if (layout > 0) {
            setReplaceLayoutResource(layout);
        } else {
            String viewClass = a.getString(R.styleable.ViewStubPro_replaceViewClass);
            if (viewClass != null && !viewClass.isBlank()) {
                setReplaceViewClass(viewClass);
            }
        }

        inflatedId = a.getResourceId(R.styleable.ViewStubPro_inflatedId, NO_ID);

        allCornerSize = (int) a.getDimension(R.styleable.ViewStubPro_inflatedCornerSize, 0);

        a.recycle();

        setVisibility(GONE);
        setWillNotDraw(true);
    }

    /**
     * 代码中设定为layout模式。设置布局id模式。
     */
    public ViewStubPro setReplaceLayoutResource(@LayoutRes int layoutResource) {
        ReplaceLayout r = new ReplaceLayout();
        r.mLayoutResource = layoutResource;
        mReplace = r;
        return this;
    }

    /**
     * 代码中设定为layout模式。设置为单View模式。传入Class我将帮你new出来。
     */
    public ViewStubPro setReplaceViewClass(Class<? extends View> replaceView) {
        ReplaceView v = new ReplaceView();
        v.mViewClass = replaceView;
        mReplace = v;
        return this;
    }

    public <T extends ViewBinding> ViewStubPro setReplaceViewBinding(@NonNull T viewBinding) {
        ReplaceViewBinding<T> vb = new ReplaceViewBinding<>();
        vb.mViewBinding = viewBinding;
        mReplace = vb;
        return this;
    }

    /**
     * 代码中设定为layout模式。设置为单View模式。传入Class我将帮你new出来。
     */
    public ViewStubPro setReplaceViewClass(String replaceView) {
        ReplaceView v = new ReplaceView();
        try {
            v.mViewClass = (Class<? extends View>) Class.forName(replaceView);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        mReplace = v;
        return this;
    }

    public View launch() {
        if (mReplace == null) {
            Log.e(TAG, "launch: ");
            return null;
        }
        if (mIsLaunched) {
            Log.e(TAG, "sendView: The rocket is disposable and cannot be fired repeatedly");
            return mInflatedViewRef.get();
        }

        View view = null;
        if (mReplace instanceof ReplaceLayout) {
            view = inflateViewNoAdd((ViewGroup) getParent(), ((ReplaceLayout) mReplace).mLayoutResource);
        } else if (mReplace instanceof ReplaceView) {
            /*以下调用带参的、私有构造函数*/
            try {
                Constructor<? extends View> c = ((ReplaceView) mReplace).mViewClass.getDeclaredConstructor(Context.class);
                view = (View) c.newInstance(mContext);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
                e.printStackTrace();
            }
        } else if (mReplace instanceof ReplaceViewBinding<?>) {
            view = ((ReplaceViewBinding) mReplace).mViewBinding.getRoot();
            if (view.getParent() != null) {
                throw new IllegalStateException();
            }
        }

        if (view != null) {
            launch(view);
            return view;
        }

        return null;
    }

    private void launch(View view) {
        final ViewGroup parent = (ViewGroup) getParent();
        final int index = parent.indexOfChild(this);
        parent.removeViewInLayout(this);

        if (parent instanceof ConstraintLayout) {
            //如果parentView是约束布局，把自身id设置给运送的View
            inflatedId = this.getId();
            view.setId(inflatedId);
        }
        //如果View没有id,就给view生成一个id
        if (view.getId() == View.NO_ID) {
            inflatedId = View.generateViewId();
            view.setId(inflatedId);
        }

        final ViewGroup.LayoutParams layoutParams = getLayoutParams();
        if (layoutParams != null) {
            parent.addView(view, index, layoutParams);
        } else {
            parent.addView(view, index);
        }

        if (allCornerSize > 0) {
            //圆角矩形
            view.setOutlineProvider(getCustomViewOutlineProvider());
            view.setClipToOutline(true);
        }

        mIsLaunched = true;
        mInflatedViewRef = new WeakReference<>(view);
    }

    private View inflateViewNoAdd(ViewGroup parent, int layoutRes) {
        final LayoutInflater factory = LayoutInflater.from(mContext);
        final View view = factory.inflate(layoutRes, parent, false);

        if (layoutRes != NO_ID) {
            view.setId(layoutRes);
        }
        return view;
    }

    @Override
    public void setVisibility(int visibility) {
        // 当真正的布局文件被加载之后
        if (mInflatedViewRef != null) {
            // 获取到当前的View
            View view = mInflatedViewRef.get();
            if (view != null) {
                //操纵当前View的可见行
                view.setVisibility(visibility);
            } else {
                throw new IllegalStateException("setVisibility called on un-referenced view");
            }
        } else {
            //没有调用inflate的话，会设置可见性
            super.setVisibility(visibility);
            //当 当前设置可见性为 VISIBLE或者INVISIBLE的时候，会调用inflate方法。
            if (visibility == VISIBLE || visibility == INVISIBLE) {
                launch();
            }
        }
    }

    @Override
    public int getVisibility() {
        if (mInflatedViewRef != null) {
            View view = mInflatedViewRef.get();
            if (view != null) {
                return view.getVisibility();
            }
        }
        return View.GONE;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(0, 0);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(null);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        //do nothing.
    }

    private boolean mIsLaunched = false;

    public boolean isLaunched() {
        return mIsLaunched;
    }
}