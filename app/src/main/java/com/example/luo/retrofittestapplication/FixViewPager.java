package com.example.luo.retrofittestapplication;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

/**
 * <p>Description.</p>
 *
 * <b>Maintenance History</b>:
 * <table>
 * 		<tr>
 * 			<th>Date</th>
 * 			<th>Developer</th>
 * 			<th>Target</th>
 * 			<th>Content</th>
 * 		</tr>
 * 		<tr>
 * 			<td>2018-07-14 21:56</td>
 * 			<td>rcq</td>
 * 			<td>All</td>
 *			<td>Created.</td>
 * 		</tr>
 * </table>
 */
public class FixViewPager extends ViewPager {
    public FixViewPager(@NonNull Context context) {
        super(context);
        touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    public FixViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    private float mInitialDownY, mInitialDownX;
    private int touchSlop;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mInitialDownX = ev.getX();
                mInitialDownY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float x = ev.getX();
                float y = ev.getY();
                //if(Math.abs(x - mInitialDownX) > touchSlop || Math.abs(y - mInitialDownY) > touchSlop){
                    if (Math.abs(x - mInitialDownX) < Math.abs(y - mInitialDownY)) {
                        requestDisallowInterceptTouchEvent(true);
                   // }
                }
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        Log.w("AAA", "onNestedPreScroll");
        super.onNestedPreScroll(target, dx, dy, consumed);
    }
}
