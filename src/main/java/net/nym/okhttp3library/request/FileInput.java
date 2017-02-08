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

import java.io.File;
import java.util.Locale;

/**
 * @author niyueming
 * @date 2017-02-08
 * @time 14:25
 */

public class FileInput {
    public String key;
    public String fileName;
    public File file;
    public FileInput(String key,String fileName,File file){
        this.key = key;
        this.fileName = fileName;
        this.file = file;
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault()
                ,"FileInput{key='%s',fileName='%s',file='%s'}"
                ,key,fileName,file.getAbsolutePath());
    }
}
