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
package net.tirasa.hct.editor.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import org.apache.wicket.Session;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.hippoecm.frontend.session.UserSession;
import net.tirasa.hct.editor.Properties;
import net.tirasa.hct.editor.beans.PageBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PageDataProvider extends SortableDataProvider {

    private static final long serialVersionUID = 731133731971608963L;

    private static final Logger LOG =
            LoggerFactory.getLogger(PageDataProvider.class);

    private static List<PageBean> pageList =
            new ArrayList<PageBean>();

    private static String sessionId = "none";

    private static String siteName;

    public PageDataProvider(final String siteName) {

        this.siteName = siteName;
    }

    /**
     *
     * @param first
     * @param count
     * @return
     */
    @Override
    public Iterator<PageBean> iterator(final int first, final int count) {
        final List<PageBean> pages = new ArrayList<PageBean>();
        for (int i = first; i < (count + first); i++) {
            pages.add(pageList.get(i));
        }
        return pages.iterator();
    }

    /**
     *
     * @param object
     * @return
     */
    @Override
    public IModel model(final Object object) {
        return new DetachablePage((PageBean) object);
    }

    /**
     *
     * @return
     */
    @Override
    public int size() {
        populatePageList();
        return pageList.size();
    }

    /**
     * Populate list, refresh when a new session id is found or when dirty
     */
    private static void populatePageList() {
        synchronized (PageDataProvider.class) {
            pageList.clear();
            NodeIterator iter;

            final StringBuilder query = new StringBuilder(Properties.HCT_ROOT);
            query.append(Properties.SLASH);
            query.append(siteName);
            query.append(Properties.SLASH);
            query.append(Properties.HCT_PAGES);
            query.append(Properties.SLASH);
            query.append(Properties.STAR);

            try {
                @SuppressWarnings("deprecation")
                final Query listQuery = ((UserSession) Session.get()).getQueryManager().
                        createQuery(query.toString(), Query.XPATH);
                iter = listQuery.execute().getNodes();
                while (iter.hasNext()) {
                    final Node node = iter.nextNode();
                    if (node != null) {
                        try {
                            pageList.add(new PageBean(node));
                        } catch (RepositoryException e) {
                            LOG.warn("Unable to instantiate new page.", e);
                        }
                    }
                }
                Collections.sort(pageList);
            } catch (RepositoryException e) {
                LOG.error("Error while trying to query page nodes.", e);
            }
        }
    }

    public final List<PageBean> getPageList() {
        return (List<PageBean>) pageList;
    }
}
