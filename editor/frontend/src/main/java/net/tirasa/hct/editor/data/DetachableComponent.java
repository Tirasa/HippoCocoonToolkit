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

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import org.apache.wicket.Session;
import org.apache.wicket.model.LoadableDetachableModel;
import org.hippoecm.frontend.session.UserSession;
import net.tirasa.hct.editor.Properties;
import net.tirasa.hct.editor.beans.CollectionBean;
import net.tirasa.hct.editor.beans.ComponentType;
import net.tirasa.hct.editor.beans.DocumentBean;
import net.tirasa.hct.editor.beans.ResourceBean;
import net.tirasa.hct.editor.beans.SummaryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DetachableComponent extends LoadableDetachableModel {

    private static final Logger LOG = LoggerFactory.getLogger(DetachableComponent.class);

    private static final long serialVersionUID = -3134575004865376495L;

    private String path;

    protected Node getRootNode() throws RepositoryException {
        return ((UserSession) Session.get()).getJcrSession().getRootNode();
    }

    public DetachableComponent() {
    }

    public DetachableComponent(final ComponentType component) {
        this(component.getNodePath());
    }

    public DetachableComponent(final String path) {
        if (path == null || path.length() == 0) {
            throw new IllegalArgumentException();
        }
        this.path = path.startsWith("/") ? path.substring(1) : path;
    }

    public ComponentType getComponent() {
        return (ComponentType) getObject();
    }

    @Override
    public int hashCode() {
        if (path == null) {
            return super.hashCode();
        }
        return path.hashCode();
    }

    /**
     * used for dataview with ReuseIfModelsEqualStrategy item reuse strategy
     *
     * @see org.apache.wicket.markup.repeater.ReuseIfModelsEqualStrategy
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (obj instanceof DetachableComponent) {
            final DetachableComponent other = (DetachableComponent) obj;
            if (path == null || other.path == null) {
                return false;
            }
            return path.equals(other.path);
        }
        return false;
    }

    /**
     * @see org.apache.wicket.model.LoadableDetachableModel#load()
     */
    @Override
    protected ComponentType load() {
        if (path == null) {
            return new ComponentType();
        }
        // loads component from jcr
        try {
            final Node n = getRootNode().getNode(path);
            final String type = n.getProperty(Properties.PROP_COMPTYPE).getString();
            if (SummaryBean.TYPE.equals(type)) {
                return new SummaryBean(getRootNode().getNode(path));
            } else if (ResourceBean.TYPE.equals(type)) {
                return new ResourceBean(getRootNode().getNode(path));
            } else if (DocumentBean.TYPE.equals(type)) {
                return new DocumentBean(getRootNode().getNode(path));
            } else {
                return new CollectionBean(getRootNode().getNode(path));
            }
        } catch (RepositoryException e) {
            LOG.error("Unable to re-attach component with path '{}'", path, e);
            return null;
        }
    }
}
