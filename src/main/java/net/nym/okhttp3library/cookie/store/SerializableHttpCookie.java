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

import net.nym.httplibrary.cookie.store.NSerializableHttpCookie;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import okhttp3.Cookie;

/**
 * @author niyueming
 * @date 2017-02-06
 * @time 09:32
 */

public class SerializableHttpCookie extends NSerializableHttpCookie<Cookie> {
    public SerializableHttpCookie(Cookie cookie) {
        super(cookie);
    }

    @Override
    protected void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(cookie.name());
        out.writeObject(cookie.value());
        out.writeLong(cookie.expiresAt());
        out.writeObject(cookie.domain());
        out.writeObject(cookie.path());
        out.writeBoolean(cookie.secure());
        out.writeBoolean(cookie.httpOnly());
        out.writeBoolean(cookie.hostOnly());
        out.writeBoolean(cookie.persistent());
    }

    @Override
    protected void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        String name = (String) in.readObject();
        String value = (String) in.readObject();
        long expiresAt = in.readLong();
        String domain = (String) in.readObject();
        String path = (String) in.readObject();
        boolean secure = in.readBoolean();
        boolean httpOnly = in.readBoolean();
        boolean hostOnly = in.readBoolean();
        boolean persistent = in.readBoolean();
        Cookie.Builder builder = new Cookie.Builder();
        builder = builder.name(name);
        builder = builder.value(value);
        builder = builder.expiresAt(expiresAt);
        builder = hostOnly ? builder.hostOnlyDomain(domain) : builder.domain(domain);
        builder = builder.path(path);
        builder = secure ? builder.secure() : builder;
        builder = httpOnly ? builder.httpOnly() : builder;
        clientCookie = builder.build();
    }
}
