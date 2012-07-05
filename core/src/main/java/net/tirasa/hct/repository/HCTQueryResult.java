/*
 * Copyright (C) 2012 Tirasa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.tirasa.hct.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.jcr.RepositoryException;
import javax.jcr.query.RowIterator;
import net.tirasa.hct.cocoon.sax.Constants;
import org.apache.jackrabbit.JcrConstants;

public class HCTQueryResult {

    private transient final Locale locale;

    private transient final long page;

    private transient final long totalPages;

    private transient final List<String> uuids;

    public HCTQueryResult(final Locale locale, final long page, final long totalPages,
            final RowIterator result) throws RepositoryException {

        this.locale = locale;
        this.page = page;
        this.totalPages = totalPages;

        this.uuids = new ArrayList<String>();
        while (result.hasNext()) {
            this.uuids.add(result.nextRow().
                    getValue(Constants.QUERY_DEFAULT_SELECTOR + "." + JcrConstants.JCR_UUID).getString());
        }
    }

    public Locale getLocale() {
        return locale;
    }

    public long getPage() {
        return page;
    }

    public List<String> getUuids() {
        return uuids;
    }

    public long getTotalPages() {
        return totalPages;
    }
}
