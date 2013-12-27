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

import java.io.Serializable;
import java.util.List;
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

public class DocumentBean extends ComponentType
        implements Comparable<DocumentBean>, IClusterable, Serializable {

    private static final Logger LOG =
            LoggerFactory.getLogger(DocumentBean.class);

    public static final String TYPE = "Document";

    private static final long serialVersionUID = 8946955363106967602L;

    private transient Node node;

    private String nodePath;

    private String path;

    private String componentName;

    private String componentType;

    private String docType;

    private String pathDoc;

    private Map<String, String> properties = new TreeMap<String, String>();

    public DocumentBean(final Node node) throws RepositoryException {
        this.node = node;
        this.nodePath = node.getPath().substring(1);
        this.componentName = NodeNameCodec.decode(node.getName());

        final PropertyIterator pi = node.getProperties();
        while (pi.hasNext()) {
            final Property p = pi.nextProperty();
            final String name = p.getName();
            if (name.startsWith("jcr:")) {
                //skip
                continue;
            } else if (Properties.PROP_COMPTYPE.equals(name)) {
                componentType = p.getString();
            } else if (Properties.PROP_DOCTYPE.equals(name)) {
                docType = p.getString();
            } else if (Properties.PROP_PATHDOC.equals(name)) {
                pathDoc = p.getString();
            } else {
                properties.put(name, p.getString());
            }
        }
    }

    public DocumentBean() {
    }

    @Override
    public final String getComponentName() {
        return componentName;
    }

    public final void setComponentName(final String componentName) {
        this.componentName = componentName;
    }

    public final String getDocType() {
        return docType;
    }

    public final void setDocType(final String docType) {
        this.docType = docType;
    }

    public final String getPathDoc() {
        return pathDoc;
    }

    public final void setPathDoc(final String pathDoc) {
        this.pathDoc = pathDoc;
    }

    public static QueryManager getQueryManager() throws RepositoryException {
        return ((UserSession) Session.get()).getQueryManager();
    }

    public static boolean componentExists(final String componentName, final String siteName) {
        final StringBuilder relPath = new StringBuilder(Properties.HCT_ROOT);
        relPath.append(Properties.SLASH);
        relPath.append(siteName);
        relPath.append(Properties.SLASH);
        relPath.append(Properties.HCT_COMPONENTS);
        relPath.append(Properties.SLASH);
        relPath.append(componentName);
        try {
            @SuppressWarnings("deprecation")
            final Query query = getQueryManager().createQuery(relPath.toString(), Query.XPATH);
            return query.execute().getNodes().hasNext();
        } catch (RepositoryException e) {
            LOG.error("Unable to check if component '{}' "
                    + "exists, returning true", componentName, e);
            return true;
        }
    }

    /**
     * Create a new Document Node
     *
     * @throws RepositoryException
     */
    @Override
    public final void create(final String siteName) throws RepositoryException {
        if (componentExists(getComponentName(), siteName)) {
            throw new RepositoryException("Component already exists");
        }

        final StringBuilder relPath = new StringBuilder(Properties.HCT_ROOT);
        relPath.append(Properties.SLASH);
        relPath.append(siteName);
        relPath.append(Properties.SLASH);
        relPath.append(Properties.HCT_COMPONENTS);
        relPath.append(Properties.SLASH);
        relPath.append(getComponentName());

        node = ((UserSession) Session.get()).getRootNode().
                addNode(relPath.toString());
        setOrRemoveStringProperty(node, Properties.PROP_COMPTYPE, TYPE);
        setOrRemoveStringProperty(node, Properties.PROP_DOCTYPE, getDocType());
        setOrRemoveStringProperty(node, Properties.PROP_PATHDOC, getPathDoc());
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
    private final void setOrRemoveStringProperty(final Node node, final String name,
            final String value) throws RepositoryException {
        if (value == null && !node.hasProperty(name)) {
            return;
        }
        node.setProperty(name, value);
    }

    /**
     * save the current component
     *
     * @throws RepositoryException
     */
    @Override
    public final void save() throws RepositoryException {
        setOrRemoveStringProperty(node, Properties.PROP_COMPTYPE, TYPE);
        setOrRemoveStringProperty(node, Properties.PROP_DOCTYPE, getDocType());
        setOrRemoveStringProperty(node, Properties.PROP_PATHDOC, getPathDoc());
        node.getSession().save();
    }

    /**
     * Delete the current component
     *
     * @throws RepositoryException
     */
    @Override
    public final void delete() throws RepositoryException {
        final Node parent = node.getParent();
        node.remove();
        parent.getSession().save();
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public final boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || (obj.getClass() != this.getClass())) {
            return false;
        }
        final DocumentBean other = (DocumentBean) obj;
        return other.getPath().equals(getPath());
    }

    @Override
    public final String getPath() {
        return path;
    }

    @Override
    public final int hashCode() {
        return (null == path ? 0 : path.hashCode());
    }

    @Override
    public final int compareTo(final DocumentBean o) {

        final String thisName = getComponentName();
        final String otherName = o.getComponentName();
        final int len1 = thisName.length();
        final int len2 = otherName.length();
        int n = Math.min(len1, len2);
        final char[] v1 = thisName.toCharArray();
        final char[] v2 = otherName.toCharArray();
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

    @Override
    public final List<FieldBean> getFieldList() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public final String getComponentType() {
        return componentType;
    }

    public final void setComponentType(String componentType) {
        this.componentType = componentType;
    }

    @Override
    public final String getNodePath() {
        return this.nodePath;
    }

    public final String getDisplayName() {
        return componentName;
    }
}
