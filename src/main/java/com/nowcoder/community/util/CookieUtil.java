package com.nowcoder.community.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public class CookieUtil {

    public static String getCookie(HttpServletRequest request, String name) {
        if(request == null || name == null) {
            throw new IllegalArgumentException("request or name can not be null");
        }
        Cookie[] cookies = request.getCookies();
        if(cookies == null) {
            return null;
        }
        else {
            for(Cookie cookie : cookies) {
                if(cookie.getName().equals(name)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
