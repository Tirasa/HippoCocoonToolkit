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
import net.tirasa.hct.editor.beans.SiteBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SiteDataProvider extends SortableDataProvider {

    private static final Logger LOG =
            LoggerFactory.getLogger(SiteDataProvider.class);

    private static final String QUERY_SITE_LIST =
            "hct:hct/*";

    private static List<SiteBean> siteList =
            new ArrayList<SiteBean>();

    private static String sessionId = "none";

    private static final long serialVersionUID = 731133731971608963L;

    /**
     *
     */
    public SiteDataProvider() {
    }

    /**
     *
     * @param first
     * @param count
     * @return
     */
    @Override
    public Iterator<SiteBean> iterator(final int first, final int count) {
        final List<SiteBean> sites = new ArrayList<SiteBean>();
        for (int i = first; i < (count + first); i++) {
            sites.add(siteList.get(i));
        }
        return sites.iterator();
    }

    /**
     *
     * @param object
     * @return
     */
    @Override
    public IModel model(final Object object) {
        return new DetachableSite((SiteBean) object);
    }

    /**
     *
     * @return
     */
    @Override
    public int size() {
        populateSiteList();
        return siteList.size();
    }

    /**
     * Populate list, refresh when a new session id is found or when dirty
     */
    private static void populateSiteList() {
        synchronized (SiteDataProvider.class) {
            siteList.clear();
            NodeIterator iter;
            try {
                @SuppressWarnings("deprecation")
                final Query listQuery = ((UserSession) Session.get()).getQueryManager().
                        createQuery(QUERY_SITE_LIST, Query.XPATH);
                iter = listQuery.execute().getNodes();
                while (iter.hasNext()) {
                    final Node node = iter.nextNode();
                    if (node != null) {
                        try {
                            siteList.add(new SiteBean(node));
                        } catch (RepositoryException e) {
                            LOG.warn("Unable to instantiate new page.", e);
                        }
                    }
                }
                Collections.sort(siteList);
            } catch (RepositoryException e) {
                LOG.error("Error while trying to query page nodes.", e);
            }
        }
    }

    public final List<SiteBean> getSiteList() {
        return (List<SiteBean>) siteList;
    }
}
