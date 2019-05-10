
package com.bingduoduo.editor.utils;

import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Check {
    //邮件验证
    private final static Pattern emailer = Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");

    //手机号验证
    private final static Pattern phoner = Pattern.compile("^((13[0-9])|(14[0-9])|(15[0-9])|(17[0-9])|(18[0-9]))\\d{8}$");


    public static boolean isEmpty(CharSequence str) {
        return isNull(str) || str.length() == 0;
    }

    public static boolean isEmpty(Object[] os) {
        return isNull(os) || os.length == 0;
    }

    public static boolean isEmpty(Collection<?> l) {
        return isNull(l) || l.isEmpty();
    }

    public static boolean isEmpty(Map<?, ?> m) {
        return isNull(m) || m.isEmpty();
    }

    /**
     * 判断int是否<0
     *
     * @param i the
     * @return the boolean
     */
    public static boolean isVain(int i) {
        return i < 0;
    }

    public static boolean isNull(Object o) {
        return o == null;
    }

    public static void CheckNull(Object o, String message) {
        if (o == null) throw new IllegalStateException(message);
    }

    /**
     * 判断给定字符串是否空白串。 空白串是指由空格、制表符、回车符、换行符组成的字符串 若输入字符串为null或空字符串，返回true
     * ViewRoot
     *
     * @param input
     * @return boolean
     */
    public static boolean isEmpty(String input) {
        if (input == null || "".equals(input))
            return true;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c != ' ' && c != '\t' && c != '\r' && c != '\n') {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断是不是一个合法的电子邮件地址
     *
     * @param email
     * @return
     */
    public static boolean isEmail(String email) {
        if (email == null || email.trim().length() == 0)
            return false;
        return emailer.matcher(email).matches();
    }

    /**
     * Is mobile no boolean.
     *
     * @param phone the phone
     * @return the boolean
     * @description 判断手机号格式是否正确
     */
    public static boolean isPhoneNo(String phone) {
        // 支持13X，14X，15X，17X，18X
        Matcher m = phoner.matcher(phone);
        return m.matches();
    }

    /**
     * 判断是否包含有特殊符号
     *
     * @param str the str
     * @return boolean boolean
     */
    public static boolean isConSpeCharacters(String str) {
        if (str.replaceAll("[\u4e00-\u9fa5]*[a-z]*[A-Z]*\\d*-*_*\\s*", "").length() == 0) {
            // 不包含特殊字符
            return false;
        }
        return true;
    }

}