package com.github.mxsm.rain.uid.client.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * @author mxsm
 * @date 2022/5/5 15:07
 * @Since 1.0.0
 */
public abstract class UrlUtils {

    public static String[] parseUriAndPort(String url) {

        if (StringUtils.startsWithAny(url, "http://", "https://")) {
            url = StringUtils.replaceEach(url, new String[]{"http://", "https://"}, new String[]{"", ""});
        }
        String[] result = new String[2];
        String[] split = url.split(":");
        if (split.length == 2) {
            int index = url.lastIndexOf(":");
            result[0] = url.substring(0, index);
            result[1] = url.substring(index + 1);
        } else {
            result[0] = url;
            result[1] = "80";
        }
        return result;
    }

}
