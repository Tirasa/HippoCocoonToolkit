/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package net.tirasa.hct.repository;

import java.util.Locale;
import javax.jcr.NodeIterator;

public class HCTQueryResult {

    private transient final Locale locale;

    private transient final long size;

    private transient final long page;

    private transient final long totalPages;

    private transient final NodeIterator result;

    public HCTQueryResult(final Locale locale, final long size, final long page, final long totalPages,
            final NodeIterator result) {

        this.locale = locale;
        this.size = size;
        this.page = page;
        this.totalPages = totalPages;
        this.result = result;
    }

    public Locale getLocale() {
        return locale;
    }

    public long getPage() {
        return page;
    }

    public NodeIterator getResult() {
        return result;
    }

    public long getSize() {
        return size;
    }

    public long getTotalPages() {
        return totalPages;
    }
}
