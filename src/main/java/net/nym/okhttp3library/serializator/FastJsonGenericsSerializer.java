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

package net.nym.okhttp3library.serializator;

import com.alibaba.fastjson.JSON;

import net.nym.httplibrary.https.NGenericsSerializer;

import java.util.List;

/**
 * @author niyueming
 * @date 2017-02-07
 * @time 13:41
 */

public class FastJsonGenericsSerializer implements NGenericsSerializer {
    public static final FastJsonGenericsSerializer DEFAULT = new FastJsonGenericsSerializer();
    @Override
    public <T> T transform(String response, Class<T> tClass) {
        return JSON.parseObject(response,tClass);
    }

    @Override
    public <T> List<T> transformList(String response, Class<T> tClass) {
        return JSON.parseArray(response,tClass);
    }
}
