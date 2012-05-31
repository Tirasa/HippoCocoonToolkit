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

import java.util.List;
import javax.jcr.RepositoryException;
import org.apache.wicket.Component;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.breadcrumb.IBreadCrumbModel;
import org.apache.wicket.extensions.breadcrumb.IBreadCrumbParticipant;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.hippoecm.frontend.dialog.IDialogService;
import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.session.UserSession;
import net.tirasa.hct.editor.beans.SiteBean;
import net.tirasa.hct.editor.crumbs.HctBreadCrumbPanel;
import net.tirasa.hct.editor.data.DetachableSite;
import net.tirasa.hct.editor.data.SiteDataProvider;
import net.tirasa.hct.editor.widgets.AjaxBreadCrumbLink;
import net.tirasa.hct.editor.widgets.AjaxLinkLabel;
import net.tirasa.hct.editor.widgets.ConfirmDeleteDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ViewSitePanel extends HctBreadCrumbPanel {

    private static final Logger LOG =
            LoggerFactory.getLogger(ViewSitePanel.class);

    private static final long serialVersionUID = -5810203570740929822L;

    private final transient IModel model;

    public ViewSitePanel(final String id, final IPluginContext context,
            final IBreadCrumbModel breadCrumbModel,
            final IModel model) {
        super(id, breadCrumbModel);

        setOutputMarkupId(true);

        this.model = model;

        add(new Image("img_site", "images/site-48.png"));

        add(new Label("siteName", new PropertyModel(model, "siteName")));
        add(new Label("description", new PropertyModel(model, "description")));

        final AjaxBreadCrumbLink components =
                new AjaxBreadCrumbLink("manage-component", breadCrumbModel, null) {

                    private static final long serialVersionUID =
                            506897728694578583L;

                    @Override
                    protected IBreadCrumbParticipant getParticipant(
                            final String componentId, final String siteName) {
                        return new HctComponentPanel(id,
                                context, breadCrumbModel,
                                ((DetachableSite) model).getSite().getSiteName());
                    }
                };

        components.setVisible(true);
        add(components);

        final AjaxBreadCrumbLink page =
                new AjaxBreadCrumbLink("manage-page", breadCrumbModel, null) {

                    private static final long serialVersionUID =
                            506897728694578583L;

                    @Override
                    protected IBreadCrumbParticipant getParticipant(
                            final String componentId, final String siteName) {
                        return new HctPagePanel(id, context,
                                breadCrumbModel,
                                ((DetachableSite) model).getSite().getSiteName());
                    }
                };

        page.setVisible(true);
        add(page);

        // actions
        final AjaxBreadCrumbLink edit =
                new AjaxBreadCrumbLink("edit-site", breadCrumbModel, null) {

                    private static final long serialVersionUID =
                            506897728694578583L;

                    @Override
                    protected IBreadCrumbParticipant getParticipant(
                            final String componentId, final String siteName) {
                        return new SitePanel(componentId, context,
                                breadCrumbModel, model, siteName);
                    }
                };

        edit.setVisible(true);
        add(edit);

        add(new AjaxLinkLabel("delete-site", new ResourceModel("site-delete")) {

            private static final long serialVersionUID = 3776750333491622263L;

            @Override
            public void onClick(final AjaxRequestTarget target) {
                context.getService(IDialogService.class.getName(),
                        IDialogService.class).show(
                        new ConfirmDeleteDialog(model, this) {

                            private static final long serialVersionUID =
                                    -5828988483261392319L;

                            @Override
                            protected void onOk() {
                                deleteSite(model);
                            }

                            @Override
                            protected String getTitleKey() {
                                return "site-delete-title";
                            }

                            @Override
                            protected String getTextKey() {
                                return "site-delete-text";
                            }
                        });
            }
        });
    }

    private void deleteSite(final IModel model) {

        final SiteBean site = (SiteBean) model.getObject();

        if (site == null) {
            LOG.info("No site model found when trying to delete site. "
                    + "Probably the Ok button was double clicked.");
            return;
        }

        try {
            site.delete();
            LOG.info("Site '" + site.getSiteName() + "' deleted by "
                    + ((UserSession) Session.get()).getJcrSession().
                    getUserID());
            Session.get().info(getString("site-removed", model));
            // one up
            final List<IBreadCrumbParticipant> l =
                    getBreadCrumbModel().allBreadCrumbParticipants();
            getBreadCrumbModel().setActive(l.get(l.size() - 2));
        } catch (RepositoryException e) {
            Session.get().warn(getString("site-remove-failed", model));
            LOG.error("Unable to delete site '"
                    + site.getSiteName() + "' : ", e);
        }
    }

    @Override
    public final IModel<String> getTitle(final Component component) {
        return new StringResourceModel("site-view-title", component, model);
    }
}
