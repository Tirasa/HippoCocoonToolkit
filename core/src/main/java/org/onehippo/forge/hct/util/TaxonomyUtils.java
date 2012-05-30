/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.onehippo.forge.hct.util;

import java.util.ArrayList;
import java.util.List;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import org.hippoecm.hst.content.beans.ObjectBeanManagerException;
import org.hippoecm.repository.api.HippoNodeType;
import org.onehippo.forge.hct.hstbeans.HCTTaxonomyCategoryBean;
import org.onehippo.forge.hct.repository.HCTConnManager;
import org.onehippo.forge.hct.taxonomy.frontend.HCTTaxonomyNodeTypes;
import org.onehippo.taxonomy.api.TaxonomyNodeTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TaxonomyUtils {

    private static final Logger LOG = LoggerFactory.getLogger(TaxonomyUtils.class);

    private TaxonomyUtils() {
    }

    public static String buildPathInTaxonomy(final String taxonomyPath, final String docName) {
        return taxonomyPath + "/" + HCTTaxonomyNodeTypes.NODENAME_HIPPOTAXONOMY_DOCUMENTS + "/"
                + HippoNodeType.HIPPO_RESULTSET + "/" + docName;
    }

    public static List<HCTTaxonomyCategoryBean> getTaxonomies(final HCTConnManager connManager, final String[] keys) {
        final List<HCTTaxonomyCategoryBean> taxonomies = new ArrayList<HCTTaxonomyCategoryBean>();

        for (String key : keys) {
            try {
                taxonomies.add(getTaxonomy(connManager, key));
            } catch (Exception e) {
                LOG.error("While getting taxonomy", e);
            }
        }

        return taxonomies;
    }

    public static HCTTaxonomyCategoryBean getTaxonomy(final HCTConnManager connManager, final String key)
            throws RepositoryException, ObjectBeanManagerException {

        final Query query = connManager.getSession().getWorkspace().getQueryManager().createQuery(
                "SELECT * FROM [" + TaxonomyNodeTypes.NODETYPE_HIPPOTAXONOMY_CATEGORY + "] AS taxonomy "
                + "WHERE (ISDESCENDANTNODE(taxonomy, '/content/taxonomies') "
                + "AND [" + TaxonomyNodeTypes.HIPPOTAXONOMY_KEY + "] = '" + key + "')",
                Query.JCR_SQL2);
        final NodeIterator result = query.execute().getNodes();

        if (!result.hasNext()) {
            throw new PathNotFoundException("Taxonomy category with key " + key);
        }

        return (HCTTaxonomyCategoryBean) connManager.getObjConv().getObject(result.nextNode());
    }
}
