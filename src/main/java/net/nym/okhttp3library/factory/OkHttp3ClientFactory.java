/*
 * Copyright (c) 2017  Ni YueMing<niyueming@163.com>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */

package net.nym.okhttp3library.factory;

import android.content.Context;
import android.graphics.BitmapFactory;

import net.nym.okhttp3library.BuildConfig;
import net.nym.okhttp3library.cookie.CookieJarImpl;
import net.nym.okhttp3library.cookie.store.PersistentCookieStore;
import net.nym.okhttp3library.https.HttpsUtils;
import net.nym.okhttp3library.interceptor.LoggerInterceptor;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.Cache;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;

/**
 * @author niyueming
 * @date 2017-02-08
 * @time 10:27
 */

public class OkHttp3ClientFactory {
    private final static long MAX_CACHE_SIZE = 50 * 1024 * 1024;
    private final static int CONNECT_TIMEOUT = 60;  //秒
    private final static int READ_TIMEOUT = 60;  //秒
    private final static int WRITE_TIMEOUT = 60;  //秒

    public static OkHttpClient defaultOkHttpClient(Context context){
        OkHttpClient.Builder builder = defaultBuilder(context);
        return builder.build();
    }

    /**
     * https 证书配置
     * @param context
     * @param sslSocketFactory
     * @param trustManager
     * @return
     */
    public static OkHttpClient sslSocketOkHttpClient(Context context, SSLSocketFactory sslSocketFactory, X509TrustManager trustManager){
        OkHttpClient.Builder builder = defaultBuilder(context);
        builder.sslSocketFactory(sslSocketFactory);
        return builder.build();
    }

    public static OkHttpClient sslSocketOkHttpClient(Context context, HttpsUtils.SSLParams sslParams){
        return sslSocketOkHttpClient(context,sslParams.sSLSocketFactory,sslParams.trustManager);
    }

    public static OkHttpClient sslSocketOkHttpClient(Context context, InputStream[] certificates, InputStream bksFile, String password){
        HttpsUtils.SSLParams sslParams = HttpsUtils.getSslSocketFactory(certificates,bksFile,password);
        return sslSocketOkHttpClient(context,sslParams);
    }

    private static OkHttpClient.Builder defaultBuilder(Context context){
        OkHttpClient.Builder builder = new OkHttpClient().newBuilder()
                .cookieJar(new CookieJarImpl(new PersistentCookieStore(context)))   //cookie缓存
                .cache(new Cache(context.getCacheDir(), MAX_CACHE_SIZE))
//                .certificatePinner(CertificatePinner.DEFAULT) //证书锁定
                .connectionPool(new ConnectionPool(5, 5, TimeUnit.MINUTES))
//                .connectionSpecs()    //TLS ;代理？
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
//                .dispatcher(new Dispatcher())
//                .followRedirects(true)    //重定向，默认true
//                .followSslRedirects(true) //follow redirects from HTTPS to HTTP and from HTTP to HTTPS.默认true
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
//                .retryOnConnectionFailure()
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
//                .sslSocketFactory(sslParams.sSLSocketFactory) //自定义https证书
                ;
        if (BuildConfig.DEBUG){
            builder.addInterceptor(new LoggerInterceptor("OkHttp3", true));
        }
        return builder;
    }
}
