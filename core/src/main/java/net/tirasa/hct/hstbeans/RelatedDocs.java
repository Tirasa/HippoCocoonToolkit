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
package net.tirasa.hct.hstbeans;

import java.util.ArrayList;
import java.util.List;
import org.hippoecm.hst.content.beans.Node;
import org.hippoecm.hst.content.beans.standard.HippoFacetSelect;
import org.hippoecm.hst.content.beans.standard.HippoFolder;
import org.hippoecm.repository.api.HippoNodeType;

@Node(jcrType = "relateddocs:docs")
public class RelatedDocs extends HippoFolder {

    public List<String> getRelatedDocsUuids() {
        final List<HippoFacetSelect> relatedDocs = getChildBeansByName("relateddocs:reldoc");

        final List<String> beans = new ArrayList<String>(relatedDocs.size());
        for (HippoFacetSelect facetSelect : relatedDocs) {
            if (facetSelect != null && facetSelect.getProperty(HippoNodeType.HIPPO_DOCBASE) != null) {
                beans.add((String) facetSelect.getProperty(HippoNodeType.HIPPO_DOCBASE));
            }
        }
        return beans;
    }
}
