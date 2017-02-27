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

package net.nym.okhttp3library;

import android.content.Context;
import android.support.annotation.NonNull;

import net.nym.httplibrary.NHttpManager;
import net.nym.httplibrary.utils.Platform;
import net.nym.okhttp3library.callback.OkHttp3Callback;
import net.nym.okhttp3library.factory.OkHttp3ClientFactory;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author niyueming
 * @date 2017-02-08
 * @time 14:56
 */

public class OkHttp3Manager implements NHttpManager<Request,OkHttp3Callback,Response> {
    private OkHttpClient mOkHttpClient;
    private static OkHttp3Manager my;
    private Platform mPlatform = Platform.get();
    private OkHttp3Manager(Context context){
        mOkHttpClient = OkHttp3ClientFactory.defaultOkHttpClient(context);
    }

    public static OkHttp3Manager getInstance(Context context){
        if (my == null){
            synchronized (OkHttp3Manager.class){
                if (my == null){
                    my = new OkHttp3Manager(context);
                }
            }
        }
        return my;
    }



    @Override
    public void cancelByTag(@NonNull Object tag) {
        if (mOkHttpClient == null){
            return;
        }
        for (Call call : mOkHttpClient.dispatcher().queuedCalls()){
            if (tag.equals(call.request().tag())){
                call.cancel();
            }
        }
        for (Call call : mOkHttpClient.dispatcher().runningCalls()){
            if (tag.equals(call.request().tag())){
                call.cancel();
            }
        }
    }

    @Override
    public void cancelAll() {
        if (mOkHttpClient != null){
            mOkHttpClient.dispatcher().cancelAll();
        }
    }

    @Override
    public Response execute(Request request) {
        Response response = null;
        Call call = mOkHttpClient.newCall(request);
        try {
            response = call.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    @Override
    public void enqueue(final Request request, OkHttp3Callback callback, final int id) {
        if (callback == null) {
            callback = OkHttp3Callback.okHttp3CallbackDefault;
        }
        final OkHttp3Callback finalOkHttp3Callback = callback;
        Call call = mOkHttpClient.newCall(request);

        mPlatform.execute(new Runnable() {
            @Override
            public void run() {
                finalOkHttp3Callback.onBefore(request,id);
            }
        });

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                sendFailResultCallback(call,e,finalOkHttp3Callback,id);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (call.isCanceled()){
                        sendFailResultCallback(call,new IOException("canceled!"),finalOkHttp3Callback,id);
                        return;
                    }
                    if (!finalOkHttp3Callback.validateResponse(response,id)){
                        sendFailResultCallback(
                                call
                                , new IOException("request failed , response's code is : " + response.code())
                                , finalOkHttp3Callback
                                , id
                        );
                        return;
                    }

                    Object o = finalOkHttp3Callback.parseNetworkResponse(response,id);
                    sendSuccessResultCallback(o,finalOkHttp3Callback,id);
                } catch (Exception e) {
                    e.printStackTrace();
                    sendFailResultCallback(call,e,finalOkHttp3Callback,id);
                }finally {
                    if (response.body() != null){
                        response.body().close();
                    }
                }

            }
        });
    }

    public void sendFailResultCallback(final Call call, final Exception e, final OkHttp3Callback okHttpCallback, final int id) {
        if (okHttpCallback == null) {
            return;
        }

        mPlatform.execute(new Runnable() {
            @Override
            public void run() {
                okHttpCallback.onError(e, e.getMessage(), id);
                okHttpCallback.onAfter(id);
            }
        });
    }

    public void sendSuccessResultCallback(final Object object, final OkHttp3Callback okHttpCallback, final int id){
        if (okHttpCallback == null) {
            return;
        }
        mPlatform.execute(new Runnable() {
            @Override
            public void run() {
                okHttpCallback.onResponse(object, id);
                okHttpCallback.onAfter(id);
            }
        });
    }
}
