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
package net.tirasa.hct.editor.beans;

import java.util.Map;
import java.util.TreeMap;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import org.apache.wicket.IClusterable;
import org.apache.wicket.Session;
import org.hippoecm.frontend.session.UserSession;
import org.hippoecm.repository.api.NodeNameCodec;
import net.tirasa.hct.editor.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SiteBean implements Comparable<SiteBean>, IClusterable {

    private static final Logger LOG = LoggerFactory.getLogger(SiteBean.class);

    private static final long serialVersionUID = 1014287584862247354L;

    private String siteName;

    private String path;

    private Node node;

    private String description;

    private final Map<String, String> properties = new TreeMap<String, String>();

    public SiteBean() {
    }

    public SiteBean(final Node node) throws RepositoryException {
        this.node = node;
        this.path = node.getPath().substring(1);
        this.siteName = NodeNameCodec.decode(node.getName());
        this.setSiteProperties();
    }

    public final String getSiteName() {
        return siteName;
    }

    public final void setSiteName(final String siteName) {
        this.siteName = siteName;
    }

    public final String getDisplayName() {
        return siteName;
    }

    public final String getDescription() {
        return description;
    }

    public final void setDescription(final String description) {
        this.description = description;
    }

    public final String getPath() {
        return path;
    }

    public final void setSiteProperties() throws RepositoryException {
        final PropertyIterator pi = node.getProperties();
        while (pi.hasNext()) {
            final Property p = pi.nextProperty();
            final String name = p.getName();
            if (name.startsWith("jcr:")) {
                //skip
                continue;

            }
            if (Properties.PROP_DESCRIPTION.equals(name)) {
                description = p.getString();
            } else {
                properties.put(name, p.getString());
            }
        }
    }

    public static QueryManager getQueryManager() throws RepositoryException {
        return ((UserSession) Session.get()).getQueryManager();
    }

    public static boolean siteExists(final String siteName) {
        final StringBuilder queryString = new StringBuilder(Properties.HCT_ROOT);
        queryString.append(Properties.SLASH);
        queryString.append(siteName);

        try {
            @SuppressWarnings("deprecation")
            final Query query = getQueryManager().createQuery(queryString.toString(), Query.XPATH);
            return query.execute().getNodes().hasNext();
        } catch (RepositoryException e) {
            LOG.error("Unable to check if site '{}' "
                    + "exists, returning true", siteName, e);
            return true;
        }
    }

    /**
     * Create a new site
     *
     * @throws RepositoryException
     */
    public final void create() throws RepositoryException {
        if (siteExists(getSiteName())) {
            throw new RepositoryException("Site already exists");
        }

        final StringBuilder relPath = new StringBuilder(Properties.HCT_ROOT);
        relPath.append(Properties.SLASH);
        relPath.append(getSiteName());

        //Add site node
        node = ((UserSession) Session.get()).getRootNode().
                addNode(relPath.toString());
        setOrRemoveStringProperty(node,
                Properties.PROP_DESCRIPTION, getDescription());

        //Add child node
        node.addNode(Properties.HCT_COMPONENTS);
        node.addNode(Properties.HCT_PAGES);

        //save parent when adding a node
        node.getParent().getParent().getSession().save();
    }

    /**
     * Wrapper needed for spi layer which doesn't know if a property exists or
     * not
     *
     * @param node
     * @param name
     * @param value
     * @throws RepositoryException
     */
    private void setOrRemoveStringProperty(final Node node,
            final String name, final String value) throws RepositoryException {
        if (value == null && !node.hasProperty(name)) {
            return;
        }
        node.setProperty(name, value);
    }

    /**
     * save the current site
     *
     * @throws RepositoryException
     */
    public void save() throws RepositoryException {
        setOrRemoveStringProperty(node,
                Properties.PROP_DESCRIPTION, getDescription());
        node.getSession().save();
    }

    /**
     * Delete the current site
     *
     *
     * @throws RepositoryException
     */
    public void delete() throws RepositoryException {
        Node parent = node.getParent();
        node.remove();
        parent.getSession().save();
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || (obj.getClass() != this.getClass())) {
            return false;
        }
        final SiteBean other = (SiteBean) obj;
        return other.getPath().equals(getPath());
    }

    @Override
    public final int hashCode() {
        return (null == path ? 0 : path.hashCode());
    }

    @Override
    public final int compareTo(final SiteBean o) {

        final String thisName = getSiteName();
        final String otherName = o.getSiteName();

        final int len1 = thisName.length();
        final int len2 = otherName.length();
        int n = Math.min(len1, len2);
        final char v1[] = thisName.toCharArray();
        final char v2[] = otherName.toCharArray();
        int i = 0;
        int j = 0;

        if (i == j) {
            int k = i;
            int lim = n + i;
            while (k < lim) {
                char c1 = v1[k];
                char c2 = v2[k];
                if (c1 != c2) {
                    return c1 - c2;
                }
                k++;
            }
        } else {
            while (n-- != 0) {
                char c1 = v1[i++];
                char c2 = v2[j++];
                if (c1 != c2) {
                    return c1 - c2;
                }
            }
        }
        return len1 - len2;
    }
}
