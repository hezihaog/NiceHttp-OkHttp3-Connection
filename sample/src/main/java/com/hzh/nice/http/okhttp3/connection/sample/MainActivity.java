package com.hzh.nice.http.okhttp3.connection.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hzh.logger.L;
import com.hzh.nice.http.NiceApiClient;
import com.hzh.nice.http.NiceHttpConfig;
import com.hzh.nice.http.base.ApiParams;
import com.hzh.nice.http.inter.Parser;
import com.hzh.nice.http.inter.Result;
import com.hzh.nice.http.okhttp3.connection.ApiByOkHttp;
import com.hzh.nice.http.okhttp3.connection.sample.bean.SearchEntity;
import com.hzh.nice.http.okhttp3.connection.sample.util.Const;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btnGet = (Button) findViewById(R.id.get);
        //初始化
        GsonParser parser = new GsonParser();
        NiceApiClient.init(getApplicationContext(),
                NiceHttpConfig
                        .newBuild(new ApiByOkHttp(getApplicationContext(), parser),
                                parser).setDebug(BuildConfig.DEBUG).build());

        btnGet.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
//                RequestManager.search(new ApiCallbackAdapter() {
//                    @Override
//                    public void onApiSuccess(Result res, String tag) {
//                        super.onApiSuccess(res, tag);
//                        SearchEntity result = (SearchEntity) res;
//                        L.d("result ::: " + result.getCount());
//                    }
//                }, Const.SearchType.ANDROID, Const.Config.pageCount + "", Const.Config.page + "");

                new Thread() {

                    @Override
                    public void run() {
                        super.run();
                        try {
                            SearchEntity result = (SearchEntity) NiceApiClient.getInstance().getApi()
                                    .getSync(Const.Api.domain
                                            + Const.Api.search
                                            + "/" + Const.SearchType.ANDROID
                                            + "/count/" + Const.Config.pageCount + "/page/" + Const.Config.page, new ApiParams(), SearchEntity.class);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "ok", Toast.LENGTH_SHORT).show();
                                }
                            });
                            L.d("result ::: " + result.getCount());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
        });
    }

    /**
     * Gson反序列化Json转换器
     */
    private static class GsonParser implements Parser {

        @Override
        public <T extends Result> T parse(String json, Class<T> clazz) {
            return new Gson().fromJson(json, clazz);
        }
    }
}