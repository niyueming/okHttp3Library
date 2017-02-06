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

package net.nym.okhttp3library.cookie;

import net.nym.httplibrary.cookie.store.NCookieStore;

import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

/**
 * @author niyueming
 * @date 2017-02-06
 * @time 09:41
 */

public class CookieJarImpl implements CookieJar
{
    private NCookieStore<Cookie> cookieStore;

    public CookieJarImpl(NCookieStore cookieStore)
    {
        if (cookieStore == null) {
            throw new IllegalArgumentException("cookieStore can not be null.");
        }
        this.cookieStore = cookieStore;
    }

    @Override
    public synchronized void saveFromResponse(HttpUrl url, List<Cookie> cookies)
    {
        cookieStore.add(url.uri(), cookies);
    }

    @Override
    public synchronized List<Cookie> loadForRequest(HttpUrl url)
    {
        return cookieStore.get(url.uri());
    }

    public NCookieStore getCookieStore()
    {
        return cookieStore;
    }
}
