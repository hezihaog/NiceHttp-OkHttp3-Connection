package com.hzh.nice.http.okhttp3.connection;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import com.hzh.nice.http.base.Api;
import com.hzh.nice.http.base.ApiParams;
import com.hzh.nice.http.callback.ApiCallback;
import com.hzh.nice.http.inter.Result;
import com.hzh.nice.http.util.ApiUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by XuYang on 2016/12/28.
 * Api接口，OkHttp的实现
 */

public class ApiByOkHttp implements Api {
    private final Handler mainHandler;
    private OkHttpClient client;

    public ApiByOkHttp(Context context) {
        mainHandler = new Handler(context.getMainLooper());
        client = new OkHttpClient.Builder().connectTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS).build();
    }

    /**
     * 提供OkHttpClient
     *
     * @return okHttpClient实例
     */
    public OkHttpClient getOkHttpClient() {
        return client;
    }

    @Override
    public Result getSync(String url, ApiParams params, Class clazz) throws Exception {
        ApiUtil.printRequest(url, params);
        Uri.Builder builder = Uri.parse(url).buildUpon();
        final Map<String, ArrayList<String>> paramsList = params.getParams();
        if (paramsList != null) {
            for (Map.Entry<String, ArrayList<String>> entry : paramsList.entrySet()) {
                ArrayList<String> values = entry.getValue();
                if (values != null) {
                    for (int i = 0; i < values.size(); i++) {
                        String value = values.get(i);
                        builder.appendQueryParameter(entry.getKey(), value);
                    }
                }
            }
        }
        Request request = new Request.Builder().url(builder.build().toString()).build();
        Call call = client.newCall(request);
        Response response = null;
        Result res = null;
        try {
            response = call.execute();
            String result = response.body().string();
            ApiUtil.printResult(clazz.getName(), result);
            res = ApiUtil.parseResult(clazz, result);
        } catch (IOException e) {
            throw e;
        } finally {
            if (response != null) {
                response.body().close();
            }
        }
        return res;
    }

    @Override
    public void get(final ApiCallback callback, String url, ApiParams params, final Class clazz, final String tag) {
        ApiUtil.printRequest(url, params);
        Uri.Builder builder = Uri.parse(url).buildUpon();
        final Map<String, ArrayList<String>> paramsList = params.getParams();
        if (paramsList != null) {
            for (Map.Entry<String, ArrayList<String>> entry : paramsList.entrySet()) {
                ArrayList<String> values = entry.getValue();
                if (values != null) {
                    for (int i = 0; i < values.size(); i++) {
                        String value = values.get(i);
                        builder.appendQueryParameter(entry.getKey(), value);
                    }
                }
            }
        }
        Request request = new Request.Builder().url(builder.build().toString()).build();
        Call call = client.newCall(request);
        final Runnable start = new Runnable() {
            @Override
            public void run() {
                if (callback != null) {
                    try {
                        callback.onApiStart(tag);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            start.run();
        } else {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    start.run();
                }
            });
        }
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                ApiUtil.printResult(clazz.getName(), result);
                Result res;
                try {
                    res = ApiUtil.parseResult(clazz, result);
                    if (callback != null) {
                        final Result finalRes = res;
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    callback.onApiSuccess(finalRes, tag);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (callback != null) {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    callback.onParseError(tag);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                } finally {
                    response.body().close();
                }
            }

            @Override
            public void onFailure(Call call, final IOException e) {
                if (callback != null) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                callback.onApiFailure(e, 0, "网络错误", tag);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public Result postSync(String url, ApiParams params, Class clazz) throws Exception {
        ApiUtil.printRequest(url, params);
        MultipartBody.Builder builder = new MultipartBody.Builder();
        final Map<String, ArrayList<String>> paramsList = params.getParams();
        if (paramsList != null) {
            for (Map.Entry<String, ArrayList<String>> entry : paramsList.entrySet()) {
                ArrayList<String> values = entry.getValue();
                if (values != null) {
                    for (int i = 0; i < values.size(); i++) {
                        String value = values.get(i);
                        builder.addFormDataPart(entry.getKey(), value);
                    }
                }
            }
        }
        Map<String, ArrayList<File>> files = params.getFiles();
        if (files != null) {
            for (Map.Entry<String, ArrayList<File>> entry : files.entrySet()) {
                ArrayList<File> filesList = entry.getValue();
                if (filesList != null) {
                    for (int i = 0; i < filesList.size(); i++) {
                        File file = filesList.get(i);
                        if (file != null && file.exists()) {
                            RequestBody fileBody = RequestBody.create(MediaType.parse("application/octet-stream"), file);
                            builder.addFormDataPart(entry.getKey(), file.getName(), fileBody);
                        }
                    }
                }
            }
        }
        Request request = new Request.Builder().url(url).post(builder.build()).build();
        Call call = client.newCall(request);
        Response response = null;
        Result res = null;
        try {
            response = call.execute();
            String result = response.body().string();
            ApiUtil.printResult(clazz.getName(), result);
            res = ApiUtil.parseResult(clazz, result);
        } catch (IOException e) {
            throw e;
        } finally {
            if (response != null) {
                response.body().close();
            }
        }
        return res;
    }

    @Override
    public void post(final ApiCallback callback, String url, ApiParams params, final Class clazz, final String tag) {
        ApiUtil.printRequest(url, params);
        MultipartBody.Builder builder = new MultipartBody.Builder();
        final Map<String, ArrayList<String>> paramsList = params.getParams();
        if (paramsList != null) {
            for (Map.Entry<String, ArrayList<String>> entry : paramsList.entrySet()) {
                ArrayList<String> values = entry.getValue();
                if (values != null) {
                    for (int i = 0; i < values.size(); i++) {
                        String value = values.get(i);
                        builder.addFormDataPart(entry.getKey(), value);
                    }
                }
            }
        }
        Map<String, ArrayList<File>> files = params.getFiles();
        if (files != null) {
            for (Map.Entry<String, ArrayList<File>> entry : files.entrySet()) {
                ArrayList<File> filesList = entry.getValue();
                if (filesList != null) {
                    for (int i = 0; i < filesList.size(); i++) {
                        File file = filesList.get(i);
                        if (file != null && file.exists()) {
                            RequestBody fileBody = RequestBody.create(MediaType.parse("application/octet-stream"), file);
                            builder.addFormDataPart(entry.getKey(), file.getName(), fileBody);
                        }
                    }
                }
            }
        }
        Request request = new Request.Builder().url(url).post(builder.build()).build();
        Call call = client.newCall(request);
        final Runnable start = new Runnable() {
            @Override
            public void run() {
                if (callback != null) {
                    try {
                        callback.onApiStart(tag);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            start.run();
        } else {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    start.run();
                }
            });
        }
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                ApiUtil.printResult(clazz.getName(), result);
                Result res;
                try {
                    res = ApiUtil.parseResult(clazz, result);
                    if (callback != null) {
                        final Result finalRes = res;
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    callback.onApiSuccess(finalRes, tag);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (callback != null) {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    callback.onParseError(tag);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                } finally {
                    response.body().close();
                }
            }

            @Override
            public void onFailure(Call call, final IOException e) {
                if (callback != null) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                callback.onApiFailure(e, 0, "网络错误", tag);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        });
    }
}
