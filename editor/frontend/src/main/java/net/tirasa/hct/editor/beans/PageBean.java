/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.tirasa.hct.editor.beans;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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

public class PageBean implements Comparable<PageBean>, IClusterable {

    private static final Logger LOG = LoggerFactory.getLogger(PageBean.class);

    private static final long serialVersionUID = 4986516249877856538L;

    private String path;

    private String pageName;

    private String description;

    private String select;

    private String top;

    private String bottom;

    private String left;

    private String center;

    private String right;

    private Map<String, String> properties = new TreeMap<String, String>();

    private transient Node node;

    public PageBean() {
    }

    public PageBean(final Node node) throws RepositoryException {
        this.node = node;
        this.path = node.getPath().substring(1);
        this.pageName = NodeNameCodec.decode(node.getName());
        this.setPageProperties();

    }

    public static QueryManager getQueryManager() throws RepositoryException {
        return ((UserSession) Session.get()).getQueryManager();
    }

    public static boolean pageExists(final String pageName,
            final String siteName) {
        final StringBuilder relPath =
                new StringBuilder(Properties.HCT_ROOT);
        relPath.append(Properties.SLASH);
        relPath.append(siteName);
        relPath.append(Properties.SLASH);
        relPath.append(Properties.HCT_PAGES);
        relPath.append(Properties.SLASH);
        relPath.append(pageName);

        try {
            final Query query = getQueryManager().createQuery(
                    relPath.toString(), Query.XPATH);
            if (query.execute().getNodes().hasNext()) {
                return true;
            }
        } catch (RepositoryException e) {
            LOG.error("Unable to check if page '{}' "
                    + "exists, returning true", pageName, e);
            return true;
        }
        return false;
    }

    public final void setPageProperties() throws RepositoryException {
        final PropertyIterator pi = node.getProperties();
        while (pi.hasNext()) {
            final Property p = pi.nextProperty();
            final String name = p.getName();
            if (name.startsWith("jcr:")) {
                //skip
                continue;

            } else if (Properties.PROP_DESCRIPTION.equals(name)) {
                description = p.getString();
            } else if (Properties.PROP_TOP.equals(name)) {
                top = p.getString();
            } else if (Properties.PROP_BOTTOM.equals(name)) {
                bottom = p.getString();
            } else if (Properties.PROP_LEFT.equals(name)) {
                left = p.getString();
            } else if (Properties.PROP_CENTER.equals(name)) {
                center = p.getString();
            } else if (Properties.PROP_RIGHT.equals(name)) {
                right = p.getString();
            } else {
                properties.put(name, p.getString());
            }
        }
    }

    public final String getPageName() {
        return pageName;
    }

    public final void setPageName(final String pageName) {
        this.pageName = pageName;
    }

    public final Map<String, String> getProperties() {
        return properties;
    }

    public final List<Entry<String, String>> getPropertiesList() {
        final List<Entry<String, String>> l = new ArrayList<Entry<String, String>>();
        for (Entry<String, String> e : properties.entrySet()) {
            l.add(e);
        }
        return l;
    }

    public final String getDisplayName() {
        return pageName;
    }

    public final String getDescription() {
        return description;
    }

    public final void setDescription(final String description) {
        this.description = description;
    }

    public final String getSelect() {
        return select;
    }

    public final void setSelect(final String select) {
        this.select = select;
    }

    public final String getTop() {
        return top;
    }

    public final void setTop(final String top) {
        this.top = top;
    }

    public final String getBottom() {
        return bottom;
    }

    public final void setBottom(final String bottom) {
        this.bottom = bottom;
    }

    public final String getPath() {
        return path;
    }

    public final String getCenter() {
        return center;
    }

    public final void setCenter(final String center) {
        this.center = center;
    }

    public final String getLeft() {
        return left;
    }

    public final void setLeft(final String left) {
        this.left = left;
    }

    public final Node getNode() {
        return node;
    }

    public final void setNode(final Node node) {
        this.node = node;
    }

    public final String getRight() {
        return right;
    }

    public final void setRight(final String right) {
        this.right = right;
    }

    public final void removeComponent(final String component) throws RepositoryException {
        if (component.equals(top)) {
            top = null;
        }
        if (component.equals(bottom)) {
            bottom = null;
        }
        if (component.equals(left)) {
            left = null;
        }
        if (component.equals(center)) {
            center = null;
        }
        if (component.equals(right)) {
            right = null;
        }
    }

    /**
     * Create a new page
     *
     * @throws RepositoryException
     */
    public final void create(final String siteName) throws RepositoryException {
        if (pageExists(getPageName(), siteName)) {
            throw new RepositoryException("Page already exists");
        }

        final StringBuilder relPath =
                new StringBuilder(Properties.HCT_ROOT);
        relPath.append(Properties.SLASH);
        relPath.append(siteName);
        relPath.append(Properties.SLASH);
        relPath.append(Properties.HCT_PAGES);
        relPath.append(Properties.SLASH);
        relPath.append(getPageName());

        node = ((UserSession) Session.get()).getRootNode().
                addNode(relPath.toString());
        setOrRemoveStringProperty(node,
                Properties.PROP_DESCRIPTION, getDescription());
        setOrRemoveStringProperty(node,
                Properties.PROP_TOP, getTop());
        setOrRemoveStringProperty(node,
                Properties.PROP_BOTTOM, getBottom());
        setOrRemoveStringProperty(node,
                Properties.PROP_LEFT, getLeft());
        setOrRemoveStringProperty(node,
                Properties.PROP_CENTER, getCenter());
        setOrRemoveStringProperty(node,
                Properties.PROP_RIGHT, getRight());

        //save parent when adding a node
        node.getParent().getSession().save();
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
     * save the current page
     *
     * @throws RepositoryException
     */
    public void save() throws RepositoryException {
        setOrRemoveStringProperty(node,
                Properties.PROP_DESCRIPTION, getDescription());
        setOrRemoveStringProperty(node, Properties.PROP_TOP, getTop());
        setOrRemoveStringProperty(node, Properties.PROP_BOTTOM, getBottom());
        setOrRemoveStringProperty(node, Properties.PROP_LEFT, getLeft());
        setOrRemoveStringProperty(node, Properties.PROP_CENTER, getCenter());
        setOrRemoveStringProperty(node, Properties.PROP_RIGHT, getRight());
        node.getSession().save();
    }

    /**
     * Delete the current page
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
        final PageBean other = (PageBean) obj;
        return other.getPath().equals(getPath());
    }

    @Override
    public final int hashCode() {
        return (null == path ? 0 : path.hashCode());
    }

    @Override
    public final int compareTo(final PageBean o) {

        final String thisName = getPageName();
        final String otherName = o.getPageName();

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
