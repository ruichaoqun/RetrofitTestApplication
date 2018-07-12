package com.example.luo.retrofittestapplication;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class CustomCoordinateLayout extends ViewGroup{
    private View headView;
    private View bottomView;
    private int mOriginalHeaderHeight;


    public CustomCoordinateLayout(Context context) {
        this(context,null);
    }

    public CustomCoordinateLayout(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public CustomCoordinateLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        headView = LayoutInflater.from(context).inflate(R.layout.layout_header,this,false);
        bottomView = LayoutInflater.from(context).inflate(R.layout.layout_bottom,this,false);
        addView(headView);
        addView(bottomView);
        ViewGroup.LayoutParams params = headView.getLayoutParams();
        mOriginalHeaderHeight = params.height;

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
        bottomView.measure(MeasureSpec.makeMeasureSpec(getMeasuredWidth() - getPaddingLeft() - getPaddingRight(), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(getMeasuredHeight() - getPaddingBottom() - getPaddingTop(), MeasureSpec.EXACTLY));

        ViewGroup.LayoutParams params = headView.getLayoutParams();

        headView.measure(MeasureSpec.makeMeasureSpec(getMeasuredWidth() - getPaddingLeft() - getPaddingRight(), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(params.height, MeasureSpec.EXACTLY));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        headView.layout(getPaddingLeft(),0,getMeasuredWidth() - getPaddingRight(),mOriginalHeaderHeight);
        bottomView.layout(getPaddingLeft(),mOriginalHeaderHeight,getMeasuredWidth() - getPaddingRight(),bottomView.getMeasuredHeight());
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return super.onStartNestedScroll(child, target, nestedScrollAxes);
    }
}
