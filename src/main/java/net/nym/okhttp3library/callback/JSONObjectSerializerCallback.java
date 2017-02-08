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

package net.nym.okhttp3library.callback;

import net.nym.httplibrary.http.NGenericsSerializer;

import java.lang.reflect.ParameterizedType;

import okhttp3.Response;

/**
 * @author niyueming
 * @date 2017-02-07
 * @time 15:11
 */

public abstract class JSONObjectSerializerCallback<RESULT> extends OkHttp3Callback<RESULT> {
    private NGenericsSerializer mSerializer;

    public JSONObjectSerializerCallback(NGenericsSerializer serializer){
        mSerializer = serializer;
    }

    @Override
    public RESULT parseNetworkResponse(Response response, int id) throws Exception {
        String string = response.body().string();
        Class<RESULT> entityClass = (Class<RESULT>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        if (entityClass == String.class) {
            return (RESULT) string;
        }
        RESULT bean = null;
//        if (JSONUtils.isJSONObject(string)) {
            bean = mSerializer.transform(string, entityClass);
//        }
        return bean;
    }
}
