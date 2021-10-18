package com.raincat.dolby_beta.net;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Pair;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

/**
 * Http
 * Created by Administrator on 2018/3/29 0029.
 */
public class Http {
    private Request mRequest = new Request();
    private static ExecutorService exec = Executors.newFixedThreadPool(10);

    /**
     * @param method GET/POST
     * @param url    地址
     * @param param  参数
     * @param header 请求头
     */
    public Http(final String method, final String url, final HashMap<String, Object> header, final String param) {
        mRequest.header = header;
        mRequest.method = method;
        mRequest.param = param;
        mRequest.url = url;
    }

    /**
     * @param method GET/POST
     * @param url    地址
     * @param param  参数
     * @param header 请求头
     */
    public Http(final String method, final String url, final HashMap<String, Object> param, final HashMap<String, Object> header) {
        StringBuilder stringBuilder = new StringBuilder();
        if (param != null)
            for (Map.Entry entry : param.entrySet()) {
                stringBuilder.append(entry.getKey());
                stringBuilder.append("=");
                stringBuilder.append(Uri.encode(entry.getValue().toString()));
                stringBuilder.append("&");
            }
        if (stringBuilder.length() != 0)
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);

        mRequest.header = header;
        mRequest.method = method;
        mRequest.param = stringBuilder.toString();
        mRequest.url = url;
    }

    public String getResult() {
        return doHttp(mRequest);
    }

    private String doHttp(final Request request) {
        FutureTask<Pair<Integer, String>> future = new FutureTask<>(new Callable<Pair<Integer, String>>() {
            public Pair<Integer, String> call() {
                return post(request);
            }
        });
        exec.execute(future);
        try {
            Pair<Integer, String> pair = future.get();
            if (pair.first != 0 && mRequest.reTry > 0) {
                mRequest.reTry--;
                doHttp(mRequest);
            } else
                return pair.second;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private static Pair<Integer, String> post(Request request) {
        String result;
        int errorCode = 0;

        HttpURLConnection connection = null;
        InputStream is = null;
        try {
            URL url = new URL(request.url);// 获得URL对象
            connection = (HttpURLConnection) url.openConnection();// 获得HttpURLConnection对象
            connection.setRequestMethod(request.method);// 请求方式POST
            connection.setUseCaches(false);// 不使用缓存
            connection.setConnectTimeout(request.timeout);// 设置超时时间
            connection.setReadTimeout(request.timeout);// 设置读取超时时间
            connection.setInstanceFollowRedirects(true);// 自动执行 http 重定向
            if (request.method.equals("POST")) {
                connection.setDoInput(true);// 设置是否从httpUrlConnection读入，默认情况下是true;
                connection.setDoOutput(true);
                connection.setChunkedStreamingMode(0);//设置超时不自动重试
            }
            connection.setRequestProperty("Charset", "UTF-8");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Cookie", "os=android");

            if (request.header != null)
                for (Map.Entry<String, Object> entry : request.header.entrySet()) {
                    connection.setRequestProperty(entry.getKey(), entry.getValue().toString());
                }
            connection.connect();

            if (request.method.equals("POST") && !TextUtils.isEmpty(request.param)) {
                DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                out.writeBytes(request.param);
                out.flush();
                out.close();
            }

            // 响应码是否为200
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK)
                is = connection.getInputStream();
            else {
                is = connection.getErrorStream();
                errorCode = connection.getResponseCode();
            }

            // 获得输入流
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            // 包装字节流为字符流
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            result = response.toString();
        } catch (SocketException e) {
            errorCode = 2;
            e.printStackTrace();
            result = e.getMessage();
        } catch (OutOfMemoryError e) {
            errorCode = 3;
            e.printStackTrace();
            result = e.getMessage();
        } catch (SocketTimeoutException e) {
            errorCode = 4;
            e.printStackTrace();
            result = e.getMessage();
        } catch (Exception e) {
            e.printStackTrace();
            errorCode = -1;
            result = e.getMessage();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return new Pair<>(errorCode, result);
    }
}
