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

import java.util.List;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import org.apache.wicket.Session;
import org.hippoecm.frontend.session.UserSession;
import org.hippoecm.repository.api.NodeNameCodec;
import net.tirasa.hct.editor.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *
 */
public class ComponentType {

    private static final Logger LOG =
            LoggerFactory.getLogger(DocumentBean.class);

    private Node node;

    private String nodePath;

    private String componentName;

    private String path;

    private List<FilterBean> filterList;

    private List<OrderBean> orderList;

    private List<FieldBean> fieldList;

    public ComponentType(final Node node) throws RepositoryException {
        this.node = node;
        this.nodePath = node.getPath().substring(1);
        this.componentName = NodeNameCodec.decode(node.getName());
    }

    public static QueryManager getQueryManager() throws RepositoryException {
        return ((UserSession) Session.get()).getQueryManager();
    }

    public static boolean componentExists(final String componentName,
            final String siteName) {
        final StringBuilder queryString =
                new StringBuilder(Properties.HCT_ROOT);
        queryString.append(Properties.SLASH);
        queryString.append(siteName);
        queryString.append(Properties.SLASH);
        queryString.append(Properties.HCT_COMPONENTS);
        queryString.append(Properties.SLASH);
        queryString.append(componentName);

        try {
            @SuppressWarnings("deprecation")
            final Query query = getQueryManager().createQuery(queryString.toString(), Query.XPATH);
            return query.execute().getNodes().hasNext();
        } catch (RepositoryException e) {
            LOG.error("Unable to check if component '{}' exists, returning true", componentName, e);
            return true;
        }
    }

    public ComponentType() {
    }

    public String getPath() {
        return path;
    }

    public String getComponentName() {
        return componentName;
    }

    public String getNodePath() {
        return nodePath;
    }

    public String getDisplayName() {
        return componentName;
    }

    public void create(final String siteName) throws RepositoryException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void save() throws RepositoryException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void delete() throws RepositoryException {
        final Node parent = node.getParent();
        node.remove();
        parent.getSession().save();
    }

    public List<FilterBean> getFilterList() {
        return filterList;
    }

    public void setFilterList(final List filterList) {
        this.filterList = filterList;
    }

    public List<OrderBean> getOrderList() {
        return orderList;
    }

    public void setOrderList(final List orderList) {
        this.orderList = orderList;
    }

    public List<FieldBean> getFieldList() {
        return fieldList;
    }

    public void setFieldList(final List fieldList) {
        this.fieldList = fieldList;
    }
}
