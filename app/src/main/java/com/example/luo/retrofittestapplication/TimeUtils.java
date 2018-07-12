package com.example.luo.retrofittestapplication;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
 * 			<td>2018-07-12 14:59</td>
 * 			<td>rcq</td>
 * 			<td>All</td>
 *			<td>Created.</td>
 * 		</tr>
 * </table>
 */
public class TimeUtils {
    /**
     * 秒与毫秒的倍数
     */
    public static final int SEC = 1000;
    /**
     * 分与毫秒的倍数
     */
    public static final int MIN = 60000;
    /**
     * 时与毫秒的倍数
     */
    public static final int HOUR = 3600000;
    /**
     * 天与毫秒的倍数
     */
    public static final int DAY = 86400000;

    public static final String DATE_FORMAT_DEFAULT = "yyyy-MM-dd HH:mm:ss";

    public static String getFriendlyTimeSpanByNow(long timeStamp) {
        long now = System.currentTimeMillis();
        long span = now - timeStamp;
        if (span < 0)
            return String.format("%tc", timeStamp);// U can read http://www.apihome.cn/api/java/Formatter.html to understand it.
        if (span < 1000) {
            return "刚刚";
        } else if (span < MIN) {
            return String.format("%d秒前", span / SEC);
        } else if (span < HOUR) {
            return String.format("%d分钟前", span / MIN);
        }

        long wee = (now / DAY) * DAY;
        if (timeStamp >= wee) {
            return String.format("今天%tR", timeStamp);
        } else if (timeStamp >= wee - DAY) {
            return String.format("昨天%tR", timeStamp);
        } else {
            return String.format("%tF", timeStamp);
        }
    }

    /**
     * Calculate friendly time span from specified time to current time points.
     *
     * @param timeString time string
     * @return friendly time span between two time points with fittest time unit
     *
     * <ul>
     * <li>如果小于1秒钟内，显示刚刚</li>
     * <li>如果在1分钟内，显示XXX秒前</li>
     * <li>如果在1小时内，显示XXX分钟前</li>
     * <li>如果在1小时外的今天内，显示今天15:32</li>
     * <li>如果是昨天的，显示昨天15:32</li>
     * <li>其余显示，2016-10-15</li>
     * <li>时间不合法的情况全部日期和时间信息，如星期六 十月 27 14:21:20 CST 2007</li>
     * </ul>
     */
    public static String getFriendlyTimeSpanByNow(String timeString) {
        return getFriendlyTimeSpanByNow(timeString, DATE_FORMAT_DEFAULT);
    }

    /**
     * Calculate friendly time span from specified time to current time points.
     *
     * @param timeString time string
     * @param pattern time format
     * @return friendly time span between two time points with fittest time unit
     *
     * <ul>
     * <li>如果小于1秒钟内，显示刚刚</li>
     * <li>如果在1分钟内，显示XXX秒前</li>
     * <li>如果在1小时内，显示XXX分钟前</li>
     * <li>如果在1小时外的今天内，显示今天15:32</li>
     * <li>如果是昨天的，显示昨天15:32</li>
     * <li>其余显示，2016-10-15</li>
     * <li>时间不合法的情况全部日期和时间信息，如星期六 十月 27 14:21:20 CST 2007</li>
     * </ul>
     */
    public static String getFriendlyTimeSpanByNow(String timeString, String pattern) {
        return getFriendlyTimeSpanByNow(string2Millis(timeString, pattern));
    }

    /**
     * Calculate friendly time span from specified time to current time points.
     *
     * @param date {@linkplain Date}
     * @return friendly time span between two time points with fittest time unit
     *
     * <ul>
     * <li>如果小于1秒钟内，显示刚刚</li>
     * <li>如果在1分钟内，显示XXX秒前</li>
     * <li>如果在1小时内，显示XXX分钟前</li>
     * <li>如果在1小时外的今天内，显示今天15:32</li>
     * <li>如果是昨天的，显示昨天15:32</li>
     * <li>其余显示，2016-10-15</li>
     * <li>时间不合法的情况全部日期和时间信息，如星期六 十月 27 14:21:20 CST 2007</li>
     * </ul>
     */
    public static String getFriendlyTimeSpanByNow(Date date) {
        return getFriendlyTimeSpanByNow(date.getTime());
    }

    public static long string2Millis(String timeString) {
        return string2Millis(timeString, DATE_FORMAT_DEFAULT);
    }

    public static long string2Millis(String timeString, String pattern) {
        try {
            return new SimpleDateFormat(pattern, Locale.getDefault()).parse(timeString).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return -1;
    }
}
