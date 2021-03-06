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

import java.util.List;
import java.util.Locale;
import org.hippoecm.hst.content.beans.Node;
import org.hippoecm.hst.content.beans.standard.HippoItem;

@Node(jcrType = "hippotaxonomy:category")
public class HCTTaxonomyCategoryBean extends HippoItem {

    public String getKey() {
        return getProperty("hippotaxonomy:key");
    }

    @Override
    public String getLocalizedName() {
        return getLocalizedName(Locale.getDefault().getLanguage());
    }

    public String getLocalizedName(final String language) {
        String localizedName = getKey();
        final List<HCTTaxonomyTranslation> translations = getChildBeans(HCTTaxonomyTranslation.class);
        if (translations.size() == 1) {
            localizedName = translations.iterator().next().getMessage();
        } else {
            for (HCTTaxonomyTranslation translation : getChildBeans(HCTTaxonomyTranslation.class)) {
                if (translation.getLanguage().equals(language)) {
                    localizedName = translation.getMessage();
                }
            }
        }

        return localizedName;
    }

    public String getOrder() {
        return getOrder(Locale.getDefault().getLanguage());

    }

    public String getOrder(final String language) {
        String order = "";
        final List<HCTTaxonomyTranslation> translations = getChildBeans(HCTTaxonomyTranslation.class);
        if (translations.size() == 1) {
            order = translations.iterator().next().getOrder();
        } else {
            for (HCTTaxonomyTranslation translation : getChildBeans(HCTTaxonomyTranslation.class)) {
                if (translation.getLanguage().equals(language)) {
                    order = translation.getOrder();
                }
            }
        }

        return order;
    }
}
