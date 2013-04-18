/*
 *  Copyright 2009 Hippo.
 *  Copyright 2012 Tirasa.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.onehippo.taxonomy.plugin;

import java.util.ArrayList;
import java.util.List;
import javax.jcr.NodeIterator;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.onehippo.taxonomy.api.TaxonomyNodeTypes;
import org.onehippo.taxonomy.plugin.api.EditableCategory;
import org.onehippo.taxonomy.plugin.api.EditableCategoryInfo;
import org.onehippo.taxonomy.plugin.api.TaxonomyException;
import org.onehippo.taxonomy.plugin.model.JcrCategoryInfo;

public class HCTTaxonomyEditorPlugin extends TaxonomyEditorPlugin {

    private static final long serialVersionUID = 8329866080475231996L;

    public HCTTaxonomyEditorPlugin(final IPluginContext context, final IPluginConfig config) {
        super(context, config);

        final boolean editing = "edit".equals(config.getString("mode"));
        final Form container = getContainerForm();

        final Label order = new Label("order", new OrderModel());
        order.setOutputMarkupId(true);
        container.add(order);

        container.add(new AjaxLink("plus") {

            private static final long serialVersionUID = 9123164874596936371L;

            @Override
            public void onClick(final AjaxRequestTarget target) {
                int position = Integer.parseInt(order.getDefaultModelObjectAsString());
                position++;
                order.setDefaultModelObject(Integer.toString(position));
                target.addComponent(order);
            }
        }).setEnabled(editing);

        container.add(new AjaxLink("minus") {

            private static final long serialVersionUID = 9123164874596936371L;

            @Override
            public void onClick(final AjaxRequestTarget target) {
                int position = Integer.parseInt(order.getDefaultModelObjectAsString());
                if (position >= 1) {
                    position--;
                    order.setDefaultModelObject(Integer.toString(position));
                }
                target.addComponent(order);
            }
        }).setEnabled(editing);
    }

    private final class OrderModel implements IModel<String> {

        private static final long serialVersionUID = 6683840577511673813L;

        @Override
        public String getObject() {
            final EditableCategory category = getCategory();
            if (category != null) {
                return category.getInfo(getCurrentLanguageSelection().getLanguageCode()).getString("order", "0");
            }
            return null;
        }

        @Override
        public void setObject(final String object) {
            List<String> languages = new ArrayList<String>();

            final String selectedLanguage = getCurrentLanguageSelection().getLanguageCode();
            final EditableCategoryInfo selectedInfo = getCategory().getInfo(selectedLanguage);
            if (selectedInfo instanceof JcrCategoryInfo) {
                try {
                    final NodeIterator itor = ((JcrCategoryInfo) selectedInfo).getNode().getParent().
                            getNodes(TaxonomyNodeTypes.HIPPOTAXONOMY_TRANSLATION);
                    while (itor.hasNext()) {
                        languages.add(itor.nextNode().getProperty("hippo:language").getString());
                    }
                } catch (Exception e) {
                    LOG.error("Could not read available languages", e);
                }
            }

            if (languages.isEmpty()) {
                languages.add(selectedLanguage);
            }

            for (String language : languages) {
                final EditableCategoryInfo info = getCategory().getInfo(language);
                try {
                    info.setString("order", object);
                } catch (TaxonomyException e) {
                    error(e.getMessage());
                    redraw();
                }
            }
        }

        @Override
        public void detach() {
        }
    }
}
