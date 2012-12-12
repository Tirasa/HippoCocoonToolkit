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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.onehippo.taxonomy.plugin.api.EditableCategory;
import org.onehippo.taxonomy.plugin.api.EditableCategoryInfo;
import org.onehippo.taxonomy.plugin.api.TaxonomyException;

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
            public void onClick(AjaxRequestTarget target) {

                int position = Integer.parseInt(order.getDefaultModelObject().toString());
                position++;
                order.setDefaultModelObject(Integer.toString(position));
                target.addComponent(order);
            }
        }).setEnabled(editing);

        container.add(new AjaxLink("minus") {
            private static final long serialVersionUID = 9123164874596936371L;

            @Override
            public void onClick(AjaxRequestTarget target) {

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
            EditableCategory category = getCategory();

            if (category != null) {
                return category.getInfo(getCurrentLanguageSelection().getLanguageCode()).getString("order", "0");
            }
            return null;
        }

        @Override
        public void setObject(String object) {
            EditableCategoryInfo info = getCategory().getInfo(getCurrentLanguageSelection().getLanguageCode());

            try {
                info.setString("order", object);
            } catch (TaxonomyException e) {
                error(e.getMessage());
                redraw();
            }
        }

        @Override
        public void detach() {
        }
    }
}
