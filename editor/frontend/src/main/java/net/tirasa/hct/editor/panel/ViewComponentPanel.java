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
package net.tirasa.hct.editor.panel;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import javax.jcr.RepositoryException;
import org.apache.wicket.Component;
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
import net.tirasa.hct.editor.beans.ComponentType;
import net.tirasa.hct.editor.beans.PageBean;
import net.tirasa.hct.editor.crumbs.HCTBreadCrumbPanel;
import net.tirasa.hct.editor.data.PageDataProvider;
import net.tirasa.hct.editor.widgets.AjaxBreadCrumbLink;
import net.tirasa.hct.editor.widgets.AjaxLinkLabel;
import net.tirasa.hct.editor.widgets.ConfirmDeleteDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ViewComponentPanel extends HCTBreadCrumbPanel {

    private static final Logger LOG =
            LoggerFactory.getLogger(ViewComponentPanel.class);

    private static final long serialVersionUID = 2164445619684289831L;

    private final transient IModel model;

    private ComponentType component;

    public ViewComponentPanel(final String id, final IPluginContext context,
            final IBreadCrumbModel breadCrumbModel,
            final IModel model, final String siteName) {
        super(id, breadCrumbModel);
        setOutputMarkupId(true);
        this.model = model;

        add(new Image("img_component", "images/component-48.png"));

        add(new Label("componentName",
                new PropertyModel(model, "componentName")));

        add(new Label("componentType",
                new PropertyModel(model, "componentType")));

        final AjaxBreadCrumbLink edit =
                new AjaxBreadCrumbLink("edit-component",
                        breadCrumbModel, siteName) {

                    private static final long serialVersionUID =
                    506897728694578583L;

                    @Override
                    protected IBreadCrumbParticipant getParticipant(
                            final String componentId, final String siteName) {
                                return new ComponentPanel(
                                        componentId, context, breadCrumbModel,
                                        model, siteName);
                            }
                };

        edit.setVisible(true);
        add(edit);
        add(new AjaxLinkLabel("delete-component",
                new ResourceModel("component-delete")) {

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
                                        final String name =
                                        ((ComponentType) model.getObject())
                                        .getComponentName();
                                        deleteComponent(model);
                                        updatePageProperty(name, siteName);
                                    }

                                    @Override
                                    protected String getTitleKey() {
                                        return "component-delete-title";
                                    }

                                    @Override
                                    protected String getTextKey() {
                                        return "component-delete-text";
                                    }
                                });
                    }
                });
    }

    private void deleteComponent(final IModel model) {

        final ComponentType component = (ComponentType) model.getObject();
        if (component == null) {
            LOG.info("No component model found when trying to delete component."
                    + "Probably the Ok button was double clicked.");
            return;
        }
        final String componentName = component.getComponentName();
        try {
            component.delete();
            LOG.info("Component '" + componentName
                    + "' deleted by "
                    + ((UserSession) Session.get()).getJcrSession().getUserID());

            Session.get().info(getString("component-removed", model));
            // one up
            final List<IBreadCrumbParticipant> l =
                    getBreadCrumbModel().allBreadCrumbParticipants();
            getBreadCrumbModel().setActive(l.get(l.size() - 2));
        } catch (RepositoryException e) {
            Session.get().warn(getString("component-remove-failed", model));
            LOG.error("Unable to delete component '"
                    + componentName + "' : ", e);
        }
    }

    private void updatePageProperty(final String component,
            final String siteName) {
        try {
            final PageDataProvider dataProvider =
                    new PageDataProvider(siteName);
            dataProvider.size();
            final List l = dataProvider.getPageList();
            final Iterator iter = l.iterator();
            while (iter.hasNext()) {
                final PageBean page = (PageBean) iter.next();
                page.setPageProperties();
                page.removeComponent(component);
                page.save();
            }
        } catch (RepositoryException ex) {
            java.util.logging.Logger.getLogger(
                    ViewComponentPanel.class.getName()).
                    log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public final IModel<String> getTitle(final Component component) {
        return new StringResourceModel("component-view-title",
                component, model);
    }
}
