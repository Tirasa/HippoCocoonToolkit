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
package net.tirasa.hct.editor.data;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import org.apache.wicket.Session;
import org.apache.wicket.model.LoadableDetachableModel;
import org.hippoecm.frontend.session.UserSession;
import net.tirasa.hct.editor.beans.SiteBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DetachableSite extends LoadableDetachableModel {

    private static final Logger LOG =
            LoggerFactory.getLogger(DetachableSite.class);

    private static final long serialVersionUID = 2764588059698957298L;

    private String path;

    protected Node getRootNode() throws RepositoryException {
        return ((UserSession) Session.get()).getJcrSession().getRootNode();
    }

    public DetachableSite() {
    }

    public DetachableSite(final SiteBean site) {
        this(site.getPath());
    }

    public DetachableSite(final String path) {
        if (path == null || path.length() == 0) {
            throw new IllegalArgumentException();
        }
        this.path = path.startsWith("/") ? path.substring(1) : path;
    }

    public SiteBean getSite() {
        return (SiteBean) getObject();
    }

    public String getPath() {
        return path;
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
        } else if (obj instanceof DetachableSite) {
            final DetachableSite other = (DetachableSite) obj;
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
    protected SiteBean load() {
        if (path == null) {
            return new SiteBean();
        }
        // loads page from jcr
        try {
            return new SiteBean(getRootNode().getNode(path));
        } catch (RepositoryException e) {
            LOG.error("Unable to re-attach page with path '{}'", path, e);
            return null;
        }
    }
}
