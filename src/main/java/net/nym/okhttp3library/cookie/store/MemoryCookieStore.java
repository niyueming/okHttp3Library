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

package net.nym.okhttp3library.cookie.store;

import net.nym.httplibrary.cookie.store.NCookieStore;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import okhttp3.Cookie;

/**
 * @author niyueming
 * @date 2017-02-06
 * @time 11:08
 */

public class MemoryCookieStore implements NCookieStore<Cookie> {
    private final HashMap<String, List<Cookie>> allCookies = new HashMap<>();

    @Override
    public void add(URI url, List<Cookie> cookies) {
        List<Cookie> oldCookies = allCookies.get(url.getHost());

        if (oldCookies != null) {
            Iterator<Cookie> itNew = cookies.iterator();
            Iterator<Cookie> itOld = oldCookies.iterator();
            while (itNew.hasNext()) {
                String va = itNew.next().name();
                while (va != null && itOld.hasNext()) {
                    String v = itOld.next().name();
                    if (v != null && va.equals(v)) {
                        itOld.remove();
                    }
                }
            }
            oldCookies.addAll(cookies);
        } else {
            allCookies.put(url.getHost(), cookies);
        }


    }

    @Override
    public List<Cookie> get(URI uri) {
        List<Cookie> cookies = allCookies.get(uri.getHost());
        if (cookies == null) {
            cookies = new ArrayList<>();
            allCookies.put(uri.getHost(), cookies);
        }
        return cookies;

    }

    @Override
    public boolean removeAll() {
        allCookies.clear();
        return true;
    }

    @Override
    public List<Cookie> getCookies() {
        List<Cookie> cookies = new ArrayList<>();
        Set<String> httpUrls = allCookies.keySet();
        for (String url : httpUrls) {
            cookies.addAll(allCookies.get(url));
        }
        return cookies;
    }


    @Override
    public boolean remove(URI uri, Cookie cookie) {
        List<Cookie> cookies = allCookies.get(uri.getHost());
        if (cookie != null) {
            return cookies.remove(cookie);
        }
        return false;
    }
}