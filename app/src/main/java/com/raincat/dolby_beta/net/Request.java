package com.raincat.dolby_beta.net;

import java.util.HashMap;

/**
 * <pre>
 *     author : RainCat
 *     time   : 2019/10/28
 *     desc   : 请求
 *     version: 1.0
 * </pre>
 */
class Request {
    String method = "";
    String url = "";
    String param = "";
    HashMap<String, Object> header = new HashMap<>();

    int reTry = 0;
    int timeout = 10000;
}
