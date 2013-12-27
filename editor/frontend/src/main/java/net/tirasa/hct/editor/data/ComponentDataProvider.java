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
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import org.apache.wicket.Session;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.hippoecm.frontend.session.UserSession;
import net.tirasa.hct.editor.Properties;
import net.tirasa.hct.editor.beans.CollectionBean;
import net.tirasa.hct.editor.beans.ComponentType;
import net.tirasa.hct.editor.beans.DocumentBean;
import net.tirasa.hct.editor.beans.ResourceBean;
import net.tirasa.hct.editor.beans.SummaryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ComponentDataProvider extends SortableDataProvider {

    private static final long serialVersionUID = 1856468028453249354L;

    private static final Logger LOG =
            LoggerFactory.getLogger(ComponentDataProvider.class);

    private static transient List<ComponentType> componentList =
            new ArrayList<ComponentType>();

    private static String sessionId = "none";

    private static String siteName;

    /**
     *
     */
    public ComponentDataProvider(final String siteName) {

        this.siteName = siteName;
    }

    /**
     *
     * @param first
     * @param count
     * @return
     */
    @Override
    public Iterator<ComponentType> iterator(final int first, final int count) {

        final List<ComponentType> components = new ArrayList<ComponentType>();

        for (int i = first; i < (count + first); i++) {
            components.add(componentList.get(i));
        }
        return components.iterator();
    }

    /**
     *
     * @param object
     * @return
     */
    @Override
    public IModel model(final Object object) {
        return new DetachableComponent((ComponentType) object);
    }

    /**
     *
     * @return
     */
    @Override
    public int size() {
        populateComponentList();
        return componentList.size();
    }

    /**
     * Populate list, refresh when a new session id is found or when dirty
     */
    private static void populateComponentList() {
        synchronized (ComponentDataProvider.class) {
            componentList.clear();
            NodeIterator iter;

            final StringBuilder query =
                    new StringBuilder(Properties.HCT_ROOT);
            query.append(Properties.SLASH);
            query.append(siteName);
            query.append(Properties.SLASH);
            query.append(Properties.HCT_COMPONENTS);
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
                            final String type = node.getProperty(Properties.PROP_COMPTYPE).getString();
                            if (type.equals("Document")) {
                                componentList.add(new DocumentBean(node));
                            } else if (type.equals("Collection")) {
                                componentList.add(new CollectionBean(node));
                            } else if (type.equals("Summary")) {
                                componentList.add(new SummaryBean(node));
                            } else {
                                componentList.add(new ResourceBean(node));
                            }
                        } catch (RepositoryException e) {
                            LOG.warn("Unable to instantiate new component.", e);
                        }
                    }
                }
            } catch (RepositoryException e) {
                LOG.error("Error while trying to query component nodes.", e);
            }
        }
    }

    public final List<String> getComponentList() {
        final List<String> list = new ArrayList<String>();
        final ListIterator componentIterator = componentList.listIterator();
        while (componentIterator.hasNext()) {
            list.add(((ComponentType) componentIterator.next()).getComponentName());
        }
        return list;
    }
}
