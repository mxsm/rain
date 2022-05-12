package com.github.mxsm.rain.uid.core.utils;

import java.text.ParseException;
import java.util.Date;
import org.apache.commons.lang3.time.DateFormatUtils;


/**
 * @author mxsm
 * @date 2022/5/1 19:56
 * @Since 1.0.0
 */
public abstract class DateUtils {

    public static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    public static Date parseDate(String str, String pattern) {
        try {
            return org.apache.commons.lang3.time.DateUtils.parseDate(str, new String[]{pattern});
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static String formatByDateTimePattern(Date date) {
        return DateFormatUtils.format(date, DATETIME_PATTERN);
    }
}
