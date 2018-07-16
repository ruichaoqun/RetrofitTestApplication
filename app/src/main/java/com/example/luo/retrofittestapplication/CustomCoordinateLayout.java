package com.example.luo.retrofittestapplication;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TabLayout;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChild2;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.OverScroller;
import android.widget.Toast;

public class CustomCoordinateLayout extends ViewGroup implements NestedScrollingParent{
    private static final int INVALID_POINTER = -1;

    private View headView;
    private View bottomView;
    private int screenWidth, screenHeight;

    private int mHeaderHeight;//头部高度
    private int mCurrentTargetOffsetTop;//头部当前偏移值
    private int mOriginalTargetOffsetTop;//头部初始偏移值
    private int mLastTargetOffsetTop;//上次滑动头部初始偏移值
    private int mTotalDownOffsetTop;//最大下拉偏移值
    private int mTotalUpOffsetTop;//最大上拉偏移值


    private int mActivePointId;
    //即将开始拖动
    private boolean mIsBeingDragged;
    private float mInitialDownY;//初始Y值
    private float mInitialMotionY;//初始移动值
    private boolean mReturningToStart = false;

    private int mTotalUncosumed;
    private boolean mNestScrollInProgress;

    private OverScroller mScroller;

    //最小滑动距离
    private int mTouchSlop;
    private int mMinimumVelocity;
    private int mMaximumVelocity;


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
        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        screenWidth = Utility.getScreenWidth(context);
        screenHeight = Utility.getScreenHeight(context);

        mHeaderHeight = (int) (screenWidth * 1.5f);
        mLastTargetOffsetTop = mCurrentTargetOffsetTop = mOriginalTargetOffsetTop = screenWidth - Utility.dp2px(getContext(), 20) - mHeaderHeight;
        mTotalDownOffsetTop = -mOriginalTargetOffsetTop;
        mTotalUpOffsetTop = Utility.dp2px(context, 50) - mHeaderHeight;
        headView.setPadding(0, -mOriginalTargetOffsetTop, 0, 0);

        mScroller = new OverScroller(getContext());
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
        headView.measure(MeasureSpec.makeMeasureSpec(screenWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec((int) mHeaderHeight, MeasureSpec.EXACTLY));
        bottomView.measure(MeasureSpec.makeMeasureSpec(screenWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(screenHeight, MeasureSpec.EXACTLY));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        headView.layout(0, mCurrentTargetOffsetTop, getMeasuredWidth(), mCurrentTargetOffsetTop + mHeaderHeight);
        bottomView.layout(0, mCurrentTargetOffsetTop + mHeaderHeight, getMeasuredWidth(), mCurrentTargetOffsetTop + mHeaderHeight + screenHeight);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        int pointIndex;

        if (!isEnabled() || mReturningToStart || mNestScrollInProgress) {
            return false;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
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

        if (!isEnabled() || mReturningToStart || mNestScrollInProgress) {
            return false;
        }
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
                    final float overscrollTop = y - mInitialMotionY;
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
                    finishSpinner();
                }
                mActivePointId = INVALID_POINTER;
                return false;
            }
        }
        return true;
    }

    private void finishSpinner() {
        if (mCurrentTargetOffsetTop > mOriginalTargetOffsetTop) {
            animateHeightToOriginal();
        } else {
            mLastTargetOffsetTop = mCurrentTargetOffsetTop;
        }
    }

    private void animateHeightToOriginal() {
        mReturningToStart = true;
        ValueAnimator animator = ValueAnimator.ofInt(mCurrentTargetOffsetTop, mOriginalTargetOffsetTop).setDuration(400);
        animator.setInterpolator(new DecelerateInterpolator(2));
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int offsetTop = (int) animation.getAnimatedValue();
                int offset = offsetTop - mCurrentTargetOffsetTop;
                setTargetOffsetTopAndBottom(offset);
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mLastTargetOffsetTop = mCurrentTargetOffsetTop;
                mReturningToStart = false;
            }
        });
        animator.start();
    }

    private void moveSpinner(float overscrollTop) {
        //适用于阻尼效果
        if (mLastTargetOffsetTop + overscrollTop > mOriginalTargetOffsetTop) {
            float fixScrollTop = (mLastTargetOffsetTop + overscrollTop - mOriginalTargetOffsetTop) / 2;
            float originalDragPercent = fixScrollTop / mTotalDownOffsetTop;

            float fixPercent = Math.min(1, originalDragPercent);
            float trueDragDistance = (float) (1 - Math.pow(1 - fixPercent, 3)) * mTotalDownOffsetTop;
            int offset = (int) (trueDragDistance - (mCurrentTargetOffsetTop - mOriginalTargetOffsetTop));
            setTargetOffsetTopAndBottom(offset);
        } else {
            float fixScrollTop = Math.max(mLastTargetOffsetTop + overscrollTop, mTotalUpOffsetTop);
            int offset = (int) (fixScrollTop - mCurrentTargetOffsetTop);
            setTargetOffsetTopAndBottom(offset);
        }

    }

    private void setTargetOffsetTopAndBottom(int offset) {
        ViewCompat.offsetTopAndBottom(headView, offset);
        ViewCompat.offsetTopAndBottom(bottomView, offset);
        mCurrentTargetOffsetTop += offset;
        headView.setPadding(0, -mCurrentTargetOffsetTop, 0, 0);
    }

    private void startDragging(float y) {
        final float yDiff = y - mInitialDownY;
        if (Math.abs(yDiff) > mTouchSlop && !mIsBeingDragged) {
            if (yDiff > 0) {
                mInitialMotionY = mInitialDownY + mTouchSlop;
            } else {
                mInitialMotionY = mInitialDownY - mTouchSlop;
            }
            mIsBeingDragged = true;
        }
    }

    // NestedScrollingParent
    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return isEnabled() && !mReturningToStart && (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }


    @Override
    public void onNestedScrollAccepted(View child, View target, int axes) {
        super.onNestedScrollAccepted(child, target, axes);
        mTotalUncosumed = 0;
        mNestScrollInProgress = true;
        mLastTargetOffsetTop = mCurrentTargetOffsetTop;
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        scrollBy(0, dyUnconsumed);
        super.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
        if (dyUnconsumed < 0) {
            mTotalUncosumed += Math.abs(dyUnconsumed);
            moveSpinner(mTotalUncosumed);
        }
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        if (dy > 0 && mCurrentTargetOffsetTop > mTotalUpOffsetTop) {
            mTotalUncosumed -= dy;
            if (mTotalUncosumed < mTotalUpOffsetTop - mLastTargetOffsetTop) {
                consumed[1] = mTotalUncosumed + dy - (mTotalUpOffsetTop - mLastTargetOffsetTop);
            } else {
                consumed[1] = dy;
            }
            moveSpinner(mTotalUncosumed);
        }
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        if (!consumed) {
            flingWithNestedDispatch((int) velocityY);
            return true;
        }
        return false;
    }

    private void flingWithNestedDispatch(int velocityY) {
        final int scrollY = getScrollY();
        final boolean canFling = (scrollY > 0 || velocityY > 0)
                && (scrollY < getScrollRange() || velocityY < 0);
        if(canFling){
            fling(velocityY);
        }
    }

    int getScrollRange() {
        int scrollRange = 0;
        if (getChildCount() > 0) {
            View child = getChildAt(0);
            scrollRange = Math.max(0,
                    child.getHeight() - (getHeight() - getPaddingBottom() - getPaddingTop()));
        }
        return scrollRange;
    }

    public void fling(int velocityY) {
        if (getChildCount() > 0) {
            mScroller.fling(getScrollX(), getScrollY(), // start
                    0, velocityY, // velocities
                    0, 0, // x
                    Integer.MIN_VALUE, Integer.MAX_VALUE, // y
                    0, 0); // overscroll
            //mLastScrollerY = getScrollY();
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        return super.onNestedPreFling(target, velocityX, velocityY);
    }

    @Override
    public void onStopNestedScroll(View child) {
        super.onStopNestedScroll(child);
        mNestScrollInProgress = false;
        finishSpinner();
        mTotalUncosumed = 0;
    }
}
