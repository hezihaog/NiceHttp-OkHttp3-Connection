package com.hzh.nice.http.okhttp3.connection.sample.util;

/**
 * Package: com.hzh.nice.http.sample
 * FileName: Const
 * Date: on 2017/11/9  下午7:05
 * Auther: zihe
 * Descirbe: 常量类
 * Email: hezihao@linghit.com
 */

public class Const {
    public static class Api {
        public static final String domain = "http://gank.io/api/";
        public static final String search = "search/query/listview/category";
    }

    public static class Config {
        public static final int page = 1;
        public static final int pageCount = 14;
        public static final int maxPageCount = 50;
    }

    public static class SearchType {
        public static final String ALL = "all";
        public static final String ANDROID = "Android";
        public static final String IOS = "iOS";
        public static final String VIDEO = "休息视频";
    }
}
