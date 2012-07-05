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
package net.tirasa.hct.repository;

import java.util.Locale;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import net.tirasa.hct.cocoon.sax.Constants.Availability;
import net.tirasa.hct.cocoon.sax.HippoRepositoryNotFoundException;
import org.apache.commons.lang3.ArrayUtils;
import org.hippoecm.hst.content.beans.ObjectBeanManagerException;
import org.hippoecm.hst.content.beans.standard.HippoDocument;

public class HCTDocument extends AbstractHCTEntity {

    private String path;

    private String uuid;

    public String getPath() {
        return path;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    public HippoDocument getHippoDocument(final HCTConnManager connManager,
            final Locale locale, final Availability availability)
            throws ObjectBeanManagerException, HippoRepositoryNotFoundException, RepositoryException {

        HippoDocument baseDoc = (HippoDocument) (path == null
                ? connManager.getObjMan().getObjectByUuid(uuid)
                : connManager.getObjMan().getObject(path));

        // Check existence
        if (baseDoc == null) {
            throw new HippoRepositoryNotFoundException("Could not find document " + path == null ? uuid : path);
        }

        // Check translation
        if (path != null) {
            if (baseDoc.getAvailableTranslationsBean().hasTranslation(locale.getLanguage())) {
                baseDoc = (HippoDocument) baseDoc.getAvailableTranslationsBean().getTranslation(locale.getLanguage()).
                        getContextualBean();
            } else {
                throw new HippoRepositoryNotFoundException("Could not find locale " + locale
                        + " for document " + path);
            }
        }

        // Check availability
        final Node parent = connManager.getSession().getNodeByIdentifier(baseDoc.getCanonicalHandleUUID());
        HippoDocument doc = null;
        for (final NodeIterator itor = parent.getNodes(baseDoc.getPath().
                substring(baseDoc.getPath().lastIndexOf('/') + 1)); itor.hasNext();) {

            final HippoDocument version = (HippoDocument) connManager.getObjMan().
                    getObjectByUuid(itor.nextNode().getIdentifier());
            if (ArrayUtils.contains((String[]) version.getProperty("hippo:availability"), availability.toString())) {
                doc = version;
            }
        }
        if (doc == null) {
            throw new HippoRepositoryNotFoundException("Document not available as " + availability);
        }

        return doc;
    }
}
