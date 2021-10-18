package com.raincat.dolby_beta.net;

import java.util.HashMap;

/**
 * 请求封装
 * Created by Administrator on 2018/3/29 0029.
 */

class Request {
    String method = "";
    String url = "";
    String param = "";
    HashMap<String, Object> header = new HashMap<>();

    int reTry = 0;
    int timeout = 10000;
}
