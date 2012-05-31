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
package net.tirasa.hct.editor.panel;

import java.util.ArrayList;
import java.util.List;
import org.apache.wicket.Component;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.breadcrumb.IBreadCrumbModel;
import org.apache.wicket.extensions.breadcrumb.panel.BreadCrumbPanel;
import org.apache.wicket.extensions.breadcrumb.panel.IBreadCrumbPanelFactory;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.hippoecm.frontend.plugin.IPluginContext;
import net.tirasa.hct.editor.crumbs.HctBreadCrumbPanel;
import net.tirasa.hct.editor.data.PageDataProvider;
import net.tirasa.hct.editor.widgets.AdminDataTable;
import net.tirasa.hct.editor.widgets.AjaxBreadCrumbPanelLink;
import net.tirasa.hct.editor.widgets.AjaxLinkLabel;

/**
 * This panel displays a pageable list of pages.
 */
public class HctPagePanel extends HctBreadCrumbPanel {

    private static final long serialVersionUID = -6772212328061936202L;

    public HctPagePanel(final String id, final IPluginContext context,
            final IBreadCrumbModel breadCrumbModel, final String siteName) {
        super(id, breadCrumbModel);

        add(new Image("img_page", "images/page-48.png"));

        add(new AjaxBreadCrumbPanelLink("create-page",
                context, this, PagePanel.class, siteName));

        final List<IColumn> columns = new ArrayList<IColumn>();

        columns.add(new AbstractColumn(
                new ResourceModel("pageName"), "pageName") {

            private static final long serialVersionUID = -1822504503325964706L;

            @Override
            public void populateItem(final Item item,
                    final String componentId, final IModel model) {

                final AjaxLinkLabel action = new AjaxLinkLabel(componentId,
                        new PropertyModel(model, "pageName")) {

                    private static final long serialVersionUID =
                            3776750333491622263L;

                    @Override
                    public void onClick(final AjaxRequestTarget target) {
                        //panel.showView(target, model);
                        activate(new IBreadCrumbPanelFactory() {

                            private static final long serialVersionUID =
                                    299017652786542948L;

                            @Override
                            public BreadCrumbPanel create(
                                    final String componentId,
                                    final IBreadCrumbModel breadCrumbModel) {
                                return new ViewPagePanel(componentId,
                                        context, breadCrumbModel,
                                        model, siteName);
                            }
                        });
                    }
                };
                item.add(action);
            }
        });
      
       columns.add(new PropertyColumn(new ResourceModel("description"),
                "frontend:description", "description"));
       
        final AdminDataTable table = new AdminDataTable("table",
                columns, new PageDataProvider(siteName), 20);
        table.setOutputMarkupId(true);
        add(table);
    }

    @Override
    public final IModel getTitle(final Component page) {
        return new ResourceModel("admin-pages-title");
    }
}
