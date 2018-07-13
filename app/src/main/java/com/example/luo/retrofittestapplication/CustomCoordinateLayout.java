package com.example.luo.retrofittestapplication;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

public class CustomCoordinateLayout extends ViewGroup {
    private static final int INVALID_POINTER = -1;

    private View headView;
    private View bottomView;
    private int mOriginalHeaderHeight;
    private int screenWidth, screenHeight;
    private int mCurrentHeaderHeight;
    private int mMaxHeaderHeight;

    //最小滑动距离
    private int touchSlop;
    private int mActivePointId;
    //即将开始拖动
    private boolean mIsBeingDragged;
    private float mInitialDownY;//初始Y值
    private float mInitialMotionY;//初始移动值


    public CustomCoordinateLayout(Context context) {
        this(context, null);
    }

    public CustomCoordinateLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomCoordinateLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        headView = LayoutInflater.from(context).inflate(R.layout.layout_header, this, false);
        bottomView = LayoutInflater.from(context).inflate(R.layout.layout_bottom, this, false);
        addView(headView);
        addView(bottomView);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        screenWidth = Utility.getScreenWidth(context);
        screenHeight = Utility.getScreenHeight(context);
        mOriginalHeaderHeight = mCurrentHeaderHeight = screenWidth - Utility.dp2px(getContext(), 20);
        mMaxHeaderHeight = (int) (mOriginalHeaderHeight * 1.5);
    }

    public View getHeadView() {
        return headView;
    }

    public View getBottomView() {
        return bottomView;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        headView.measure(MeasureSpec.makeMeasureSpec(screenWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(mCurrentHeaderHeight, MeasureSpec.EXACTLY));
        bottomView.measure(MeasureSpec.makeMeasureSpec(screenWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(screenHeight, MeasureSpec.EXACTLY));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        headView.layout(0, 0, getMeasuredWidth(), mCurrentHeaderHeight);
        bottomView.layout(0, mCurrentHeaderHeight, getMeasuredWidth(), mCurrentHeaderHeight + screenHeight);
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return super.onStartNestedScroll(child, target, nestedScrollAxes);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        int pointIndex;

        if (!isEnabled()) {
            return false;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                //setTargetOffsetTopAndBottom(mOriginalOffsetTop - mHeader.getTop());
                mActivePointId = ev.getPointerId(0);
                mIsBeingDragged = false;
                pointIndex = ev.findPointerIndex(mActivePointId);
                if (pointIndex < 0) {
                    return false;
                }
                mInitialDownY = ev.getY(pointIndex);
                break;
            case MotionEvent.ACTION_MOVE:
                if (mActivePointId == INVALID_POINTER) {
                    return false;
                }

                pointIndex = ev.findPointerIndex(mActivePointId);
                if (pointIndex < 0) {
                    return false;
                }
                final float y = ev.getY(pointIndex);
                startDragging(y);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIsBeingDragged = false;
                mActivePointId = INVALID_POINTER;
                break;
        }
        return mIsBeingDragged;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getActionMasked();
        int pointerIndex = -1;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mActivePointId = event.getPointerId(0);
                mIsBeingDragged = false;
                break;
            case MotionEvent.ACTION_MOVE: {
                pointerIndex = event.findPointerIndex(mActivePointId);
                if (pointerIndex < 0) {
                    return false;
                }
                final float y = event.getY(pointerIndex);
                startDragging(y);
                if (mIsBeingDragged) {
                    final float overscrollTop = (y - mInitialMotionY) * 0.5f;
                    moveSpinner(overscrollTop);
                }

                break;
            }
            case MotionEvent.ACTION_UP: {
                pointerIndex = event.findPointerIndex(mActivePointId);
                if (pointerIndex < 0) {
                    return false;
                }
                if (mIsBeingDragged) {
                    mIsBeingDragged = false;
                    //finishSpinner();
                }
                mActivePointId = INVALID_POINTER;
                return false;
            }
        }
        return true;
    }

    private void moveSpinner(float overscrollTop) {
        //下拉距离和最大下拉距离百分比
        if (overscrollTop > 0) {
            float originalDragPercent = overscrollTop / (mMaxHeaderHeight - mOriginalHeaderHeight);

            float fixPercent = Math.min(1, originalDragPercent);
            float dragDistance = (float) (1 - Math.pow(1 - fixPercent, 2)) * (mMaxHeaderHeight - mOriginalHeaderHeight);
            ViewGroup.LayoutParams params = headView.getLayoutParams();
            Log.w("AAA", "dragDistance-->" + dragDistance);
            mCurrentHeaderHeight = (int) (mOriginalHeaderHeight + dragDistance);


            params.height = mCurrentHeaderHeight;
            headView.setLayoutParams(params);

        }
//        float originalDragPercent = overscrollTop / mTotalDragDistance;
//        //大于1修正
//        float dragPercent = Math.min(1, Math.abs(originalDragPercent));
//        float trueDragDistance = (float) (1 - Math.pow(1 - dragPercent, 2)) * mTotalDragDistance;
//        int offset = (int) (trueDragDistance - (-mOriginalOffsetTop + mCurrentTargetOffsetTop));
//        int mPreTargetOffsetTop = mCurrentTargetOffsetTop;
//        setTargetOffsetTopAndBottom(offset);
//        if (mPreTargetOffsetTop * mCurrentTargetOffsetTop <= 0) {
//            if (mCurrentTargetOffsetTop >= 0) {
//                mTitle.setText("可以了");
//                animateArrow(true);
//            } else {
//                mTitle.setText("使劲拉！");
//                animateArrow(false);
//            }
//        }
    }


    private void startDragging(float y) {
        final float yDiff = y - mInitialDownY;
        if (yDiff > touchSlop && !mIsBeingDragged) {
            mInitialMotionY = mInitialDownY + touchSlop;
            mIsBeingDragged = true;
        }
    }
}
