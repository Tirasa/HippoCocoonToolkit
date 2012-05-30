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
package org.onehippo.forge.hct.editor.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import org.apache.wicket.IClusterable;
import org.apache.wicket.Session;
import org.hippoecm.frontend.session.UserSession;
import org.hippoecm.repository.api.NodeNameCodec;
import org.onehippo.forge.hct.editor.Properties;
import org.onehippo.forge.hct.editor.forms.components.FilterCond;
import org.onehippo.forge.hct.editor.forms.components.OrderCond;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SummaryBean extends ComponentType
        implements Comparable<SummaryBean>, IClusterable, Serializable {

    private static final Logger LOG =
            LoggerFactory.getLogger(SummaryBean.class);

    public static final String TYPE = "Summary";

    private static final long serialVersionUID = -2987117898057985490L;

    private transient Node node;

    private String nodePath;

    private String path;

    private String componentName;

    private String componentType;

    private String base;

    private String type;

    private String depth;

    private String size;

    private boolean imageItem;

    private boolean relatedDocs;

    private List<FilterBean> filterList;

    private List<OrderBean> orderList;

    private List<FieldBean> fieldList;

    private Map<String, String> properties = new TreeMap<String, String>();

    public enum OperationType {

        AND, OR
    };

    public SummaryBean() {
    }

    public SummaryBean(final Node node) throws RepositoryException {
        this.node = node;
        this.nodePath = node.getPath().substring(1);
        this.componentName = NodeNameCodec.decode(node.getName());
        filterList = new ArrayList<FilterBean>();
        orderList = new ArrayList<OrderBean>();
        fieldList = new ArrayList<FieldBean>();

        final PropertyIterator pi = node.getProperties();
        while (pi.hasNext()) {
            final Property p = pi.nextProperty();
            String name = p.getName();
            if (name.startsWith("jcr:")) {
                //skip
                continue;

            } else if (Properties.PROP_COMPTYPE.equals(name)) {
                componentType = p.getString();
            } else if (Properties.PROP_BASE.equals(name)) {
                base = p.getString();
            } else if (Properties.PROP_TYPE.equals(name)) {
                type = p.getString();
            } else if (Properties.PROP_DEPTH.equals(Properties.PROP_DEPTH)) {
                depth = p.getString();
            } else if (Properties.PROP_SIZE.equals(Properties.PROP_SIZE)) {
                size = p.getString();
            } else {
                properties.put(name, p.getString());
            }
        }

        final NodeIterator iter = node.getNodes();
        while (iter.hasNext()) {
            final Node childNode = iter.nextNode();
            final String nodeName = childNode.getName();
            if (Properties.NODE_FILTER.equals(nodeName)) {
                createFilterList(childNode);
            }
            if (Properties.NODE_ORDERBY.equals(nodeName)) {
                createOrderList(childNode);
            }
            if (Properties.NODE_ORDERBY.equals(nodeName)) {
                createFieldList(childNode);
            }
        }
    }

    public static QueryManager getQueryManager() throws RepositoryException {
        return ((UserSession) Session.get()).getQueryManager();
    }

    public static boolean componentExists(final String componentName,
            final String siteName) {
        final StringBuilder relPath = new StringBuilder(Properties.HCT_ROOT);
        relPath.append(Properties.SLASH);
        relPath.append(siteName);
        relPath.append(Properties.SLASH);
        relPath.append(Properties.HCT_COMPONENTS);
        relPath.append(Properties.SLASH);
        relPath.append(componentName);
        try {
            Query query = getQueryManager().createQuery(
                    relPath.toString(), Query.XPATH);
            if (query.execute().getNodes().hasNext()) {
                return true;
            }
        } catch (RepositoryException e) {
            LOG.error("Unable to check if component '{}'"
                    + "exists, returning true", componentName, e);
            return true;
        }
        return false;
    }

    /**
     * Create a new Summary Node
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
        setOrRemoveStringProperty(node, Properties.PROP_BASE, getBase());
        setOrRemoveStringProperty(node, Properties.PROP_DEPTH, getDepth());
        setOrRemoveStringProperty(node, Properties.PROP_SIZE, getSize());
        setOrRemoveStringProperty(node, Properties.PROP_TYPE, getType());
        createNodeFilter(relPath.toString(), node);
        createNodeOrderBy(relPath.toString(), node);
        createNodeReturn(relPath.toString(), node);
        node.getParent().getSession().save();
    }

    private void createNodeFilter(final String relPath,
            final Node node) throws RepositoryException {
        Node condFieldNode;
        Node filterNode = node.addNode(Properties.NODE_FILTER);
        Node andNode = filterNode.addNode(Properties.NODE_FILTER_AND);
        Node orNode = filterNode.addNode(Properties.NODE_FILTER_OR);
        for (Iterator iter = this.getFilterList().iterator(); iter.hasNext();) {
            FilterBean bean = (FilterBean) iter.next();
            StringBuilder nodeName = new StringBuilder("hct:");
            nodeName.append(bean.getTypeCond());
            if (bean.getOperationType().toString().equals("AND")) {
                condFieldNode =
                        andNode.addNode(nodeName.toString().toLowerCase());
            } else {
                condFieldNode =
                        orNode.addNode(nodeName.toString().toLowerCase());
            }
            setOrRemoveStringProperty(condFieldNode,
                    Properties.PROP_FIELD, bean.getField());
            setOrRemoveStringProperty(condFieldNode,
                    Properties.PROP_VALUE, bean.getField());
        }
    }

    private void createNodeOrderBy(final String relPath,
            final Node node) throws RepositoryException {

        Node orderByNode = node.addNode(Properties.NODE_ORDERBY);
        for (Iterator iter = this.getOrderList().iterator(); iter.hasNext();) {
            OrderBean bean = (OrderBean) iter.next();
            StringBuilder nodeName = new StringBuilder("hct:");
            nodeName.append(bean.getOrderby());
            Node orderByFieldNode =
                    orderByNode.addNode(nodeName.toString().toLowerCase());
            setOrRemoveStringProperty(orderByFieldNode,
                    Properties.PROP_FIELD, bean.getOrderField());
        }
    }

    private void createNodeReturn(final String relPath,
            final Node node) throws RepositoryException {
        //Create Return Node
        Node returnNode = node.addNode(Properties.NODE_RETURN);
        for (Iterator iter = this.getFieldList().iterator(); iter.hasNext();) {
            FieldBean bean = (FieldBean) iter.next();
            Node returnFieldNode =
                    returnNode.addNode(Properties.NODE_RETURN_FIELD);
            setOrRemoveStringProperty(returnFieldNode,
                    Properties.PROP_NAME, bean.getFieldItem());
        }
        if (getImageItem()) {
            Node imageFieldNode =
                    returnNode.addNode(Properties.NODE_RETURN_IMAGE);
        }
        if (getRelatedDocs()) {
            Node relatedFieldNode =
                    returnNode.addNode(Properties.NODE_RETURN_RELATEDDOCS);
        }
    }

    /**
     *
     * @param node
     * @param name
     * @param value
     * @throws RepositoryException
     */
    private final void setOrRemoveStringProperty(
            final Node node, final String name,
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
    public final void save(final String siteName) throws RepositoryException {
        node.remove();
        node.getSession().save();
        create(siteName);
        node.getSession().save();
    }

    /**
     * Delete the current component
     *
     * @throws RepositoryException
     */
    @Override
    public final void delete() throws RepositoryException {
        Node parent = node.getParent();
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
        SummaryBean other = (SummaryBean) obj;
        return other.getPath().equals(getPath());
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public int hashCode() {
        return (null == path ? 0 : path.hashCode());
    }

    @Override
    public int compareTo(final SummaryBean o) {

        String thisName = getComponentName();
        String otherName = o.getComponentName();
        //
        int len1 = thisName.length();
        int len2 = otherName.length();
        int n = Math.min(len1, len2);
        char v1[] = thisName.toCharArray();
        char v2[] = otherName.toCharArray();
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

    private void createFilterList(final Node childNode)
            throws RepositoryException {

        FilterBean filter;
        NodeIterator iter = childNode.getNodes();
        while (iter.hasNext()) {
            final Node andOrNode = iter.nextNode();
            NodeIterator i = andOrNode.getNodes();
            while (i.hasNext()) {
                filter = new FilterBean();
                final Node item = i.nextNode();
                if (andOrNode.getName().substring(4).equals(
                        OperationType.AND.toString())) {
                    filter.setOperationType(FilterBean.OperationType.AND);
                } else {
                    filter.setOperationType(FilterBean.OperationType.OR);
                }
                filter.setTypeCond(FilterCond.Type.valueOf(
                        item.getName().substring(4).toUpperCase()));
                PropertyIterator pi = item.getProperties();
                while (pi.hasNext()) {
                    final Property p = pi.nextProperty();
                    String name = p.getName();
                    if (name.startsWith("jcr:")) {
                        //skip
                        continue;
                    } else if (Properties.PROP_FIELD.equals(name)) {
                        filter.setField(p.getString());
                    } else if (Properties.PROP_VALUE.equals(name)) {
                        filter.setFilterValue(p.getString());
                    }
                }
                filterList.add(filter);
            }
        }
    }

    private void createOrderList(final Node childNode)
            throws RepositoryException {

        OrderBean order;
        final NodeIterator iterator = childNode.getNodes();
        while (iterator.hasNext()) {
            order = new OrderBean();
            final Node n = iterator.nextNode();
            order.setOrderby(OrderCond.Type.valueOf(n.getName().substring(4).
                    toUpperCase().toUpperCase()));
            final PropertyIterator pi = n.getProperties();
            while (pi.hasNext()) {
                final Property p = pi.nextProperty();
                String name = p.getName();
                if (name.startsWith("jcr:")) {
                    //skip
                    continue;
                } else if (Properties.PROP_FIELD.equals(name)) {
                    order.setOrderField(p.getString());
                }
            }
            orderList.add(order);
        }
    }

    private void createFieldList(final Node childNode)
            throws RepositoryException {

        FieldBean field;
        final NodeIterator iterator = childNode.getNodes();
        while (iterator.hasNext()) {
            final Node n = iterator.nextNode();
            if (Properties.NODE_RETURN_FIELD.equals(n.getName())) {
                field = new FieldBean();
                field.setFieldItem(n.getProperty(Properties.PROP_NAME).
                        getString());
                fieldList.add(field);
            }
        }
        if (childNode.hasNode(Properties.NODE_RETURN_IMAGE)) {
            this.imageItem = true;
        }
        if (childNode.hasNode(Properties.NODE_RETURN_RELATEDDOCS)) {
            this.relatedDocs = true;
        }
    }

    @Override
    public final String getComponentName() {
        return componentName;
    }

    public final void setComponentName(final String componentName) {
        this.componentName = componentName;
    }

    public final String getBase() {
        return base;
    }

    public final void setBase(final String base) {
        this.base = base;
    }

    public final String getDepth() {
        return depth;
    }

    public final void setDepth(final String depth) {
        this.depth = depth;
    }

    public final String getSize() {
        return size;
    }

    public final void setSize(final String size) {
        this.size = size;
    }

    public final String getType() {
        return type;
    }

    public final void setType(final String type) {
        this.type = type;
    }

    public final boolean getImageItem() {
        return imageItem;
    }

    public final void setImageItem(final boolean imageItem) {
        this.imageItem = imageItem;
    }

    public final boolean getRelatedDocs() {
        return relatedDocs;
    }

    public final void setRelatedDocs(final boolean relatedDocs) {
        this.relatedDocs = relatedDocs;
    }

    public final List<FilterBean> getFilterList() {
        return filterList;
    }

    public final void setFilterList(final List filterList) {
        this.filterList = filterList;
    }

    public final List<OrderBean> getOrderList() {
        return orderList;
    }

    public final void setOrderList(final List orderList) {
        this.orderList = orderList;
    }

    @Override
    public final List<FieldBean> getFieldList() {
        return fieldList;
    }

    public final void setFieldList(final List fieldList) {
        this.fieldList = fieldList;
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

    @Override
    public final String getDisplayName() {
        return componentName;
    }
}
