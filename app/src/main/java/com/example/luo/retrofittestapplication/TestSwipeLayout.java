package com.example.luo.retrofittestapplication;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.widget.CircularProgressDrawable;
import android.support.v7.widget.TintTypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.RelativeLayout;

public class TestSwipeLayout extends ViewGroup {
    private static final float DECELERATE_INTERPOLATOR_DEFAULT_FACTOR = 2.0F;

    private int mHeaderHeight;//头部高度
    private View mHeader;//头部View
    private View mContainer;//内容View

    //最小滑动距离
    private int touchSlop;

    //头部view的top
    private int mCurrentTargetOffsetTop;
    private DecelerateInterpolator mInterpolator;
    private int mMediumAnimationDuration;
    private LayoutInflater mLayoutInflater;
    private boolean mReturningToStart;//是否正在返回起始位置

    public TestSwipeLayout(Context context) {
        this(context,null);
    }

    public TestSwipeLayout(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public TestSwipeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mLayoutInflater = LayoutInflater.from(context);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        mMediumAnimationDuration = getResources().getInteger(android.R.integer.config_mediumAnimTime);

        mInterpolator = new DecelerateInterpolator(DECELERATE_INTERPOLATOR_DEFAULT_FACTOR);
        TypedArray a = context.obtainStyledAttributes(attrs,R.styleable.TestSwipeLayout);
        inflateHeaderView(a.getResourceId(R.styleable.TestSwipeLayout_headLayout,R.layout.classics_header));
        a.recycle();
    }

    private void inflateHeaderView(int resourceId) {
        View view = mLayoutInflater.inflate(resourceId,this,false);
        mHeader = view;
        addView(view);
        ViewGroup.LayoutParams params = mHeader.getLayoutParams();
        mHeaderHeight = params.height;
        mCurrentTargetOffsetTop = - mHeaderHeight;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mContainer == null) {
            ensureTarget();
        }
        if (mContainer == null) {
            return;
        }

        mContainer.measure(MeasureSpec.makeMeasureSpec(getMeasuredWidth() - getPaddingLeft() - getPaddingRight(), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(getMeasuredHeight() - getPaddingBottom() - getPaddingTop(), MeasureSpec.EXACTLY));

        ViewGroup.LayoutParams params = mHeader.getLayoutParams();

        mHeader.measure(MeasureSpec.makeMeasureSpec(getMeasuredWidth() - getPaddingLeft() - getPaddingRight(), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(params.height, MeasureSpec.EXACTLY));

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int width = getMeasuredWidth();
        final int height = getMeasuredHeight();

        if (getChildCount() == 0) {
            return;
        }

        if (mContainer == null) {
            ensureTarget();
        }

        if (mContainer == null) {
            return;
        }

        final View child = mContainer;
        final int childLeft = getPaddingLeft();
        final int childTop = getPaddingTop();
        final int childWidth = width - getPaddingLeft() - getPaddingRight();
        final int childHeight = height - getPaddingTop() - getPaddingBottom();
        mContainer.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);
        int headerWidth = mHeader.getMeasuredWidth();
        int headerHeight = mHeader.getMeasuredHeight();
        mHeader.layout(width/2-headerWidth/2,mCurrentTargetOffsetTop,width/2+headerWidth/2,mCurrentTargetOffsetTop+headerHeight);
    }

    public void ensureTarget() {
        if (mContainer == null) {
            for (int i = 0; i < getChildCount(); i++) {
                if (mHeader != getChildAt(i)) {
                    mContainer = getChildAt(i);
                }
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        ensureTarget();

        final int action = ev.getAction();
        int pointIndex;
        if(mReturningToStart && action == MotionEvent.ACTION_DOWN){
            mReturningToStart = false;
        }

        if(!isEnabled() || mReturningToStart ){
            return false;
        }

        switch (action){
            case MotionEvent.ACTION_DOWN:

                break;
        }
        return false;
    }
}
