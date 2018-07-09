package com.example.luo.retrofittestapplication;

import android.support.annotation.StringDef;

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
 * 			<td>2018-05-29 14:37</td>
 * 			<td>Rui chaoqun</td>
 * 			<td>All</td>
 *			<td>Created.</td>
 * 		</tr>
 * </table>
 */
@StringDef({ServiceType.ANDROID,
        ServiceType.ALL,
        ServiceType.IOS,
        ServiceType.ALL})
public @interface ServiceType {
    String ANDROID = "Android";
    String FULI = "福利";
    String IOS = "iOS";
    String ALL = "all";
}
