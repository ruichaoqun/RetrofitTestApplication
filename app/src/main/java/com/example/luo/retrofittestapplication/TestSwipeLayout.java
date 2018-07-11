package com.example.luo.retrofittestapplication;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.CircularProgressDrawable;
import android.support.v7.widget.TintTypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.Transformation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class TestSwipeLayout extends ViewGroup {
    private static final float DECELERATE_INTERPOLATOR_DEFAULT_FACTOR = 2.0F;

    private static final int ANIMATE_TO_START_DURATION = 200;

    private static final int INVALID_POINTER = -1;
    private static final float DRAG_RATE = .5f;

    private int mHeaderHeight;//头部高度
    private View mHeader;//头部View
    private View mContainer;//内容View
    private ImageView mArrow;
    private TextView mTitle;
    private TextView mDate;

    //最小滑动距离
    private int touchSlop;

    //头部view的top
    private int mCurrentTargetOffsetTop;
    private int mOriginalOffsetTop;
    private int mTotalDragDistance;
    private DecelerateInterpolator mInterpolator;
    private int mMediumAnimationDuration;
    private LayoutInflater mLayoutInflater;
    private boolean mReturningToStart;//是否正在返回起始位置
    private int mActivePointId;
    //即将开始拖动
    private boolean mIsBeingDragged;
    private float mInitialDownY;//初始Y值
    private float mInitialMotionY;//初始移动值
    private boolean mRefreshing = false;
    private boolean mNotify;
    private OnRefreshListener mRefreshListener;

    private final ValueAnimator mAnimatorToStartPosition = ValueAnimator.ofFloat(0, 1).setDuration(ANIMATE_TO_START_DURATION);
    private final ValueAnimator mAnimatorToRefreshingPosition = ValueAnimator.ofFloat(0,1).setDuration(ANIMATE_TO_START_DURATION);
    private ObjectAnimator mRefreshingAnimator = ObjectAnimator.ofFloat(mArrow,"scaleX",1,2).setDuration(350);


    public TestSwipeLayout(Context context) {
        this(context, null);
    }

    public TestSwipeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TestSwipeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mLayoutInflater = LayoutInflater.from(context);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        mMediumAnimationDuration = getResources().getInteger(android.R.integer.config_mediumAnimTime);

        mInterpolator = new DecelerateInterpolator(DECELERATE_INTERPOLATOR_DEFAULT_FACTOR);
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TestSwipeLayout);
        inflateHeaderView(a.getResourceId(R.styleable.TestSwipeLayout_headLayout, R.layout.classics_header));
        a.recycle();

        mAnimatorToStartPosition.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float currentOffset = mOriginalOffsetTop - (mOriginalOffsetTop - mFrom) * (1 - animation.getAnimatedFraction());
                setTargetOffsetTopAndBottom((int) (currentOffset - mCurrentTargetOffsetTop));
            }
        });
        mAnimatorToStartPosition.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                reset();
            }
        });

        mAnimatorToRefreshingPosition.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float currentOffset = mFrom * (1 - animation.getAnimatedFraction());
                setTargetOffsetTopAndBottom((int) (currentOffset - mCurrentTargetOffsetTop));
            }
        });
        mAnimatorToRefreshingPosition.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mArrow.setImageResource(R.drawable.ic_refreshing);
                ObjectAnimator animator = ObjectAnimator.ofFloat(mArrow,"rotation",0,360).setDuration(900);
                animator.setInterpolator(new LinearInterpolator());
                animator.setRepeatCount(-1);
                animator.start();
                if(mNotify){
                    if(mRefreshListener != null){
                        mRefreshListener.onRefresh();
                    }
                }
            }
        });

        mRefreshingAnimator.setRepeatCount(100);
    }

    private void reset() {
        mArrow.clearAnimation();
        mArrow.setImageResource(R.drawable.ic_arrow_bottom);
        setTargetOffsetTopAndBottom(mOriginalOffsetTop - mCurrentTargetOffsetTop);
    }

    private void inflateHeaderView(int resourceId) {
        View view = mLayoutInflater.inflate(resourceId, this, false);
        mHeader = view;
        mArrow = mHeader.findViewById(R.id.iv_progress);
        mTitle = mHeader.findViewById(R.id.tv_title);
        mDate = mHeader.findViewById(R.id.tv_date);
        addView(view);
        ViewGroup.LayoutParams params = mHeader.getLayoutParams();
        mHeaderHeight = params.height;
        mOriginalOffsetTop = mCurrentTargetOffsetTop = -mHeaderHeight;
        mTotalDragDistance = Math.abs(mHeaderHeight) * 2;//最大下拉距离是头部高度的2倍
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
        mHeader.layout(width / 2 - headerWidth / 2, mCurrentTargetOffsetTop, width / 2 + headerWidth / 2, mCurrentTargetOffsetTop + headerHeight);
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
        if (mReturningToStart && action == MotionEvent.ACTION_DOWN) {
            mReturningToStart = false;
        }

        if (!isEnabled() || mReturningToStart || mRefreshing) {
            return false;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                setTargetOffsetTopAndBottom(mOriginalOffsetTop - mHeader.getTop());
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
    public void requestDisallowInterceptTouchEvent(boolean b) {
        // if this is a List < L or another view that doesn't support nested
        // scrolling, ignore this request so that the vertical scroll event
        // isn't stolen
        if ((android.os.Build.VERSION.SDK_INT < 21 && mContainer instanceof AbsListView)
                || (mContainer != null && !ViewCompat.isNestedScrollingEnabled(mContainer))) {
            // Nope.
        } else {
            super.requestDisallowInterceptTouchEvent(b);
        }
    }

    //NestedScrollingParent


    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return isEnabled() && !mReturningToStart && !mRefreshing
                && (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int axes) {
        super.onNestedScrollAccepted(child, target, axes);
    }

    private void setTargetOffsetTopAndBottom(int offset) {
        mHeader.bringToFront();
        //ViewCompat.offsetTopAndBottom(mHeader, offset);
        float mPreTargetOffsetTop = mCurrentTargetOffsetTop;
        mCurrentTargetOffsetTop += offset;
    }

    private void startDragging(float y) {
        final float yDiff = y - mInitialDownY;
        if (yDiff > touchSlop && !mIsBeingDragged) {
            mInitialMotionY = mInitialDownY + touchSlop;
            mIsBeingDragged = true;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getActionMasked();
        int pointerIndex = -1;

        if (mReturningToStart && action == MotionEvent.ACTION_DOWN) {
            mReturningToStart = false;
        }

        if (!isEnabled() || mReturningToStart || mRefreshing) {
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
                    final float overscrollTop = (y - mInitialMotionY) * DRAG_RATE;
                    if (overscrollTop > 0) {
                        moveSpinner(overscrollTop);
                    } else {
                        return false;
                    }
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
        if (mCurrentTargetOffsetTop > 0) {
            setRefreshing(true, true);
        } else {
            mRefreshing = false;
            animateOffsetToStartPosition(mCurrentTargetOffsetTop);
        }
    }

    private void animateOffsetToStartPosition(int currentTargetOffsetTop) {
        mFrom = currentTargetOffsetTop;
        mAnimatorToStartPosition.start();
    }

    private void animateOffsetToRefreshingPosition(int currentTargetOffsetTop){
        mFrom = currentTargetOffsetTop;
        mAnimatorToRefreshingPosition.start();
    }


    private float mFrom;

    private void moveSpinner(float overscrollTop) {
        //下拉距离和最大下拉距离百分比
        float originalDragPercent = overscrollTop / mTotalDragDistance;
        //大于1修正
        float dragPercent = Math.min(1, Math.abs(originalDragPercent));
        float trueDragDistance = (float) (1 - Math.pow(1 - originalDragPercent, 3)) * mTotalDragDistance;
        int offset = (int) (trueDragDistance - (-mOriginalOffsetTop + mCurrentTargetOffsetTop));
        int mPreTargetOffsetTop = mCurrentTargetOffsetTop;
        setTargetOffsetTopAndBottom(offset);
        if (mPreTargetOffsetTop * mCurrentTargetOffsetTop <= 0) {
            if (mCurrentTargetOffsetTop >= 0) {
                animateArrow(true);
            } else {
                animateArrow(false);
            }
        }
    }

    private void animateArrow(boolean isDown) {
        if (isDown) {
            mArrow.animate().rotation(180).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                }
            }).start();
        } else {
            mArrow.animate().rotation(0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                }
            }).start();
        }
    }

    private void setRefreshing(boolean refreshing, boolean notify) {
        if (mRefreshing != refreshing) {
            mNotify = notify;
            ensureTarget();
            mRefreshing = refreshing;
            if (mRefreshing) {
                animateOffsetToRefreshingPosition(mCurrentTargetOffsetTop);
            } else {
                animateOffsetToStartPosition(mCurrentTargetOffsetTop);
            }
        }
    }


    public interface OnRefreshListener {
        void onRefresh();
    }

    public void setRefreshing(boolean refreshing){
        setRefreshing(refreshing,false);
    }

    public void setRefreshListener(OnRefreshListener refreshListener) {
        mRefreshListener = refreshListener;
    }
}
