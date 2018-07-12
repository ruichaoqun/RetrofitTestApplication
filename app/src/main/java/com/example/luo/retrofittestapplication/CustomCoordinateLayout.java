package com.example.luo.retrofittestapplication;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class CustomCoordinateLayout extends ViewGroup{

    public CustomCoordinateLayout(Context context) {
        this(context,null);
    }

    public CustomCoordinateLayout(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public CustomCoordinateLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return super.onStartNestedScroll(child, target, nestedScrollAxes);
    }
}
