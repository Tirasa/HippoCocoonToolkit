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
package net.tirasa.hct.editor;

import java.util.Iterator;
import java.util.List;
import org.apache.wicket.Component;
import org.apache.wicket.extensions.breadcrumb.IBreadCrumbModel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.widgets.AbstractView;
import net.tirasa.hct.editor.crumbs.HCTBreadCrumbPanel;
import net.tirasa.hct.editor.widgets.AjaxBreadCrumbPanelLink;

public class HCTPanelPlugin extends HCTBreadCrumbPanel {

    private static final long serialVersionUID = -7452200457282472023L;

    public HCTPanelPlugin(final String id, final IPluginContext context,
            final IBreadCrumbModel breadCrumbModel) {
        super(id, breadCrumbModel);

        add(new AbstractView<HCTPlugin>("panels",
                new AdminPluginProvider(context)) {
            private static final long serialVersionUID = -5883876020383905671L;

            @Override
            protected void populateItem(final Item<HCTPlugin> item) {
                final HCTPlugin service = item.getModelObject();
                final AjaxBreadCrumbPanelLink link =
                        new AjaxBreadCrumbPanelLink(
                        "link", getBreadCrumbModel(), service, "");
                link.add(new Image("img", service.getImage()));
                link.add(new Label(
                        "title", service.getTitle()).setRenderBodyOnly(true));
                link.add(new Label("help", service.getHelp()));
                item.add(link);
            }
        });
    }

    static class AdminPluginProvider implements IDataProvider<HCTPlugin> {

        private static final long serialVersionUID = -7682624922789215681L;

        private transient IPluginContext context;

        private transient List<HCTPlugin> services;

        public AdminPluginProvider(final IPluginContext context) {
            this.context = context;
        }

        private void load() {
            if (services == null) {
                services = context.getServices(
                        HCTPlugin.ADMIN_PANEL_ID, HCTPlugin.class);
            }
        }

    @Override
        public Iterator<HCTPlugin> iterator(final int first, final int count) {
            load();
            return services.subList(first, first + count).iterator();
        }

        @Override
        public int size() {
            load();
            return services.size();
        }

        @Override
        public IModel<HCTPlugin> model(final HCTPlugin object) {
            return new Model(object);
        }

        @Override
        public void detach() {
            services = null;
        }
    }

    @Override
    public final IModel getTitle(final Component component) {
        return new StringResourceModel("admin-title", component, null);
    }
}
