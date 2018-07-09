package com.example.luo.retrofittestapplication;

import android.animation.Animator;
import android.view.animation.Animation;

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
 * 			<td>2018-07-09 11:49</td>
 * 			<td>rcq</td>
 * 			<td>All</td>
 *			<td>Created.</td>
 * 		</tr>
 * </table>
 */
public interface Header {

    void startRefreshAnimation(Animator animator);



    void clearRefreshAnimation();


    void setAnimationListener(Animation.AnimationListener listener);
}
