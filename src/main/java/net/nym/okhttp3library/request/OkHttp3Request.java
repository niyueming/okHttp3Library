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

package net.nym.okhttp3library.request;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.webkit.URLUtil;

import net.nym.httplibrary.NHttpManager;
import net.nym.httplibrary.http.METHOD;
import net.nym.httplibrary.http.NRequest;
import net.nym.httplibrary.utils.Platform;
import net.nym.okhttp3library.OkHttp3Manager;
import net.nym.okhttp3library.callback.OkHttp3Callback;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @author niyueming
 * @date 2017-02-08
 * @time 11:20
 */

public class OkHttp3Request implements NRequest<OkHttp3Callback, Response> {
    private final static int REQUEST_ID = -1;
    private static MediaType MEDIA_TYPE_PLAIN = MediaType.parse("text/plain;charset=utf-8");
    private static MediaType MEDIA_TYPE_STREAM = MediaType.parse("application/octet-stream");
    private Request request;
    private Object tag;
    private int requestId;
    private NHttpManager<Request,OkHttp3Callback,Response>  httpManager;

    private OkHttp3Request(Context context){
        httpManager = OkHttp3Manager.getInstance(context);
    }


    @Override
    public void cancel() {
        httpManager.cancelByTag(tag);
    }

    @Override
    public Response execute() {
        return httpManager.execute(request);
    }

    @Override
    public void enqueue(OkHttp3Callback callback) {
        httpManager.enqueue(request,callback,requestId);
    }

    public static class Builder{
        private int requestId = REQUEST_ID;
        private Context context;
        private Object tag;
        private METHOD method = METHOD.GET;
        private Request.Builder builder = new Request.Builder();
        private String url;
        private LinkedHashMap<String,String> params = new LinkedHashMap<>();
        private ArrayList<FileInput> files = new ArrayList<>();
        private ArrayList<OkHttp3Callback> callbacks = new ArrayList<>();

        public Builder(@NonNull Context context){
            this.context = context;
            tag = context;
        }

        public Builder method(METHOD method){
            this.method = method;
            return this;
        }

        public Builder addHeader(@NonNull String name,String value){
            builder.addHeader(name,value);
            return this;
        }

        public Builder addHeader(@NonNull Map<String,String> headers){
            builder.headers(Headers.of(headers));
            return this;
        }

        public Builder url(@NonNull String url){
            if (!URLUtil.isNetworkUrl(url)){
                throw new IllegalArgumentException("this url is not a network url");
            }
            this.url = url;
            return this;
        }

        public Builder params(@NonNull Map<String,String> params){
            this.params.putAll(params);
            return this;
        }

        public Builder addParams(@NonNull String key,@NonNull String value){
            this.params.put(key,value);
            return this;
        }

        public Builder files(@NonNull String key, @NonNull Map<String,File> files){
            for (String fileName : files.keySet()){
                this.files.add(new FileInput(key,fileName,files.get(fileName)));
            }
            return this;
        }

        public Builder addFile(@NonNull String key, @NonNull String fileName,@NonNull File file){
            this.files.add(new FileInput(key,fileName,file));
            return this;
        }

        public Builder requestId(int requestId){
            this.requestId = requestId;
            return this;
        }

        /**
         *
         * @param callback 用于请求上传数据回调进度用
         * @return
         */
        public Builder addCallback(@NonNull OkHttp3Callback callback){
            callbacks.add(callback);
            return this;
        }

        public OkHttp3Request build(){
            if (!URLUtil.isNetworkUrl(url)){
                throw new IllegalArgumentException("this url is not a network url");
            }
            switch (method){
                case GET:
                    builder.url(appendParams(url,params)).get();
                    break;
                case POST:
                    //Form形式
                    RequestBody formBody;
                    if (files == null || files.isEmpty()){
                        FormBody.Builder builder = new FormBody.Builder();
                        addFormParams(builder,params);
                        formBody = builder.build();
                    }else {
                        MultipartBody.Builder builder = new MultipartBody.Builder()
                                .setType(MultipartBody.FORM);
                        addFormParams(builder,params);
                        for (int i = 0, length = files.size();i < length;i ++){
                            FileInput fileInput = files.get(i);
                            RequestBody fileBody = RequestBody.create(
                                    MediaType.parse(guessMimeType(fileInput.fileName))
                                    ,fileInput.file
                            );
                            builder.addFormDataPart(fileInput.key,fileInput.fileName,fileBody);
                        }
                        formBody = builder.build();
                    }

                    if (!callbacks.isEmpty()){
                        formBody = new CountingRequestBody(formBody, new CountingRequestBody.Listener() {
                            @Override
                            public void onRequestProgress(final long bytesWritten, final long contentLength) {
                                Platform.get().execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        for (int i = 0, length = callbacks.size();i < length;i ++){
                                            callbacks.get(i).inProgress(bytesWritten,contentLength,requestId);
                                        }
                                    }
                                });
                            }
                        });
                    }
                    builder.url(url).post(formBody);

                    //TODO raw形式
                    /**
                     * ({@link OkHttp3Request#MEDIA_TYPE_PLAIN},{@link OkHttp3Request#MEDIA_TYPE_STREAM})
                     */
                    break;
                case HEAD:
                    builder.url(appendParams(url,params)).head();
                    break;
                default:

                    break;
            }
            Request request = builder.build();

            OkHttp3Request okHttp3Request = new OkHttp3Request(context);
            okHttp3Request.request = request;
            okHttp3Request.tag = tag;
            okHttp3Request.requestId = requestId;

            return okHttp3Request;
        }

        private String appendParams(String url, Map<String, String> params)
        {
            if (url == null || params == null || params.isEmpty())
            {
                return url;
            }
            Uri.Builder builder = Uri.parse(url).buildUpon();
            Set<String> keys = params.keySet();
            Iterator<String> iterator = keys.iterator();
            while (iterator.hasNext())
            {
                String key = iterator.next();
                builder.appendQueryParameter(key, params.get(key));
            }
            return builder.build().toString();
        }

        private void addFormParams(FormBody.Builder builder,Map<String,String> params)
        {
            if (params != null)
            {
                for (String key : params.keySet())
                {
                    builder.add(key, params.get(key));
                }
            }
        }

        private void addFormParams(MultipartBody.Builder builder,Map<String,String> params) {
            if (params != null) {
                for (String key : params.keySet()) {
                    builder.addPart(Headers.of("Content-Disposition", "form-data; name=\"" + key + "\""),
                            RequestBody.create(null, params.get(key)));
                }
            }
        }

        private String guessMimeType(String path) {
            FileNameMap fileNameMap = URLConnection.getFileNameMap();
            String contentTypeFor = null;
            try {
                contentTypeFor = fileNameMap.getContentTypeFor(URLEncoder.encode(path, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            if (contentTypeFor == null) {
                contentTypeFor = MEDIA_TYPE_STREAM.toString();
            }
            return contentTypeFor;
        }

    }
}
