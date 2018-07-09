package com.example.luo.retrofittestapplication;

import android.app.Application;

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
 * 			<td>2018-06-26 11:13</td>
 * 			<td>Rui chaoqun</td>
 * 			<td>All</td>
 *			<td>Created.</td>
 * 		</tr>
 * </table>
 */
public class TestApplication extends Application {
    private static TestApplication instance;


    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
}
