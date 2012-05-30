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
package org.onehippo.forge.hct.editor.forms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.jcr.RepositoryException;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.breadcrumb.IBreadCrumbModel;
import org.apache.wicket.extensions.breadcrumb.IBreadCrumbParticipant;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.hippoecm.frontend.session.UserSession;
import org.onehippo.forge.hct.editor.Properties;
import org.onehippo.forge.hct.editor.beans.CollectionBean;
import org.onehippo.forge.hct.editor.beans.ComponentType;
import org.onehippo.forge.hct.editor.beans.DocumentBean;
import org.onehippo.forge.hct.editor.beans.FieldBean;
import org.onehippo.forge.hct.editor.beans.FilterBean;
import org.onehippo.forge.hct.editor.beans.OrderBean;
import org.onehippo.forge.hct.editor.beans.ResourceBean;
import org.onehippo.forge.hct.editor.beans.SummaryBean;
import org.onehippo.forge.hct.editor.data.ComponentDataProvider;
import org.onehippo.forge.hct.editor.panel.CollectionPanel;
import org.onehippo.forge.hct.editor.panel.DocumentPanel;
import org.onehippo.forge.hct.editor.panel.ResourcePanel;
import org.onehippo.forge.hct.editor.panel.SummaryPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ComponentForm extends Form {

    private static final long serialVersionUID = 7420797339438924323L;

    private static final Logger LOG =
            LoggerFactory.getLogger(ComponentForm.class);

    private List<Component> typeComponent =
            Arrays.asList(new Component(0, "hct:document"),
            new Component(1, "hct:summary"),
            new Component(2, "hct:collection"),
            new Component(3, "hct:resource"));

    private final WebMarkupContainer mainContainer;

    private transient ComponentType component;

    private transient IModel model;

    private transient String operationType;
    
    private String siteName;

    public ComponentForm(final String id, final IModel model,
            final IBreadCrumbModel breadCrumbModel,
            final FeedbackPanel feedmsg, final String siteName) {

        super(id);

        this.model = model;
        this.siteName = siteName;

        setOperationType(model);

        mainContainer = new WebMarkupContainer("main");
        mainContainer.setOutputMarkupId(true);
        this.add(mainContainer);

        final Fragment fragment = new Fragment("typePanel",
                Properties.OP_CREATE.equals(operationType)
                ? "viewSelect" : "hiddenSelect", mainContainer);
        fragment.setOutputMarkupId(true);
        mainContainer.add(fragment);

        final DocumentPanel documentPanel = new DocumentPanel("summary",
                operationType, new DocumentBean(), siteName);
        documentPanel.setOutputMarkupId(true);
        documentPanel.setVisible(false);
        mainContainer.add(documentPanel);

        setComponentModel();

        if (Properties.OP_CREATE.equals(operationType)) {

            final DropDownChoice type = new DropDownChoice("type",
                    new Model(), typeComponent,
                    new TypeChoiceRenderer("id", "name"));
            type.setRequired(true);
            type.setVisible(Properties.OP_CREATE.equals(operationType));
            type.add(new AjaxFormComponentUpdatingBehavior("onchange") {

                private static final long serialVersionUID = -1107858522700306810L;

                @Override
                protected void onUpdate(final AjaxRequestTarget target) {
                    final int index = Integer.parseInt(type.getValue());
                    switch (index) {
                        case 0:
                            component = new DocumentBean();
                            setModel(new CompoundPropertyModel<ComponentType>(component));
                            displayDocumentForm();
                            break;
                        case 1:
                            component = new SummaryBean();
                            setModel(new CompoundPropertyModel<ComponentType>(component));
                            setBeanList(component);
                            displaySummaryForm();
                            break;
                        case 2:
                            component = new CollectionBean();
                            setModel(new CompoundPropertyModel<ComponentType>(component));
                            displayCollectionForm();
                            break;
                        case 3:
                            component = new ResourceBean();
                            setModel(new CompoundPropertyModel<ComponentType>(component));
                            setBeanList(component);
                            displayResourceForm();
                            break;
                        default:
                            break;
                    }
                    target.addComponent(mainContainer);
                }
            });

            fragment.add(type);
        }

        this.add(new AjaxButton("create-button", this) {

            private static final long serialVersionUID = -5783994974426198290L;

            @Override
            protected void onSubmit(final AjaxRequestTarget target,
                    final Form form) {

                try {
                    String componentName = component.getComponentName();
                    if (component instanceof DocumentBean) {
                        component = (DocumentBean) component;
                    }
                    if (component instanceof CollectionBean) {
                        component = (CollectionBean) component;
                    }
                    if (component instanceof SummaryBean) {
                        component = (SummaryBean) component;
                    }
                    if (component instanceof ResourceBean) {
                        component = (ResourceBean) component;
                    }
                    if (Properties.OP_CREATE.equals(operationType)) {
                        component.create(siteName);
                    } else {
                        component.save();
                    }
                    LOG.info("Component '"
                            + componentName
                            + "' created by "
                            + ((UserSession) Session.get()).getJcrSession().
                            getUserID());
                    Session.get().info(getString("component-created")
                            + ": " + component.getComponentName());
                    final List<IBreadCrumbParticipant> l =
                            breadCrumbModel.allBreadCrumbParticipants();
                    breadCrumbModel.setActive(l.get(l.size() - 2));
                } catch (RepositoryException e) {
                    Session.get().warn(getString("component-create-failed")
                            + " " + component.getComponentName());
                    LOG.error("Unable to create component '"
                            + component.getComponentName() + "' : ", e);
                }
            }

            @Override
            protected void onError(final AjaxRequestTarget target,
                    final Form form) {
                target.addComponent(feedmsg);
            }
        }.setDefaultFormProcessing(true));

        this.add(new AjaxButton("cancel-button") {

            private static final long serialVersionUID = 5166479650578194076L;

            @Override
            protected void onSubmit(final AjaxRequestTarget target,
                    final Form form) {
                // one up
                final List<IBreadCrumbParticipant> l =
                        breadCrumbModel.allBreadCrumbParticipants();
                breadCrumbModel.setActive(l.get(l.size() - 2));
            }
        }.setDefaultFormProcessing(false));
    }

    private void setBeanList(final ComponentType doc) {
        doc.setFilterList(new ArrayList<FilterBean>());
        doc.setOrderList(new ArrayList<OrderBean>());
        doc.setFieldList(new ArrayList<FieldBean>());
    }

    private void setComponentModel() {
        if (model != null) {
            if (model.getObject() instanceof DocumentBean) {
                component = (DocumentBean) model.getObject();
                displayDocumentForm();
            }
            if (model.getObject() instanceof CollectionBean) {
                component = (CollectionBean) model.getObject();
                displayCollectionForm();
            }
            if (model.getObject() instanceof SummaryBean) {
                component = (SummaryBean) model.getObject();
                displaySummaryForm();
            }
            if (model.getObject() instanceof ResourceBean) {
                component = (ResourceBean) model.getObject();
                displayResourceForm();
            }
            setModel(new CompoundPropertyModel<ComponentType>(component));
        }
    }

    private void setOperationType(final IModel model) {
        operationType = model == null
                ? Properties.OP_CREATE : Properties.OP_UPDATE;
    }

    private void displaySummaryForm() {
        final SummaryPanel summaryPanel = new SummaryPanel("summary",
                operationType, (SummaryBean) component, siteName);
        summaryPanel.setOutputMarkupId(true);
        summaryPanel.setVisible(true);
        mainContainer.addOrReplace(summaryPanel);
    }

    private void displayCollectionForm() {
        final CollectionPanel collectionPanel = new CollectionPanel("summary",
                operationType, (CollectionBean) component, siteName);
        collectionPanel.setOutputMarkupId(true);
        collectionPanel.setVisible(true);
        mainContainer.addOrReplace(collectionPanel);
    }

    private void displayDocumentForm() {
        final DocumentPanel documentPanel = new DocumentPanel("summary",
                operationType, (DocumentBean) component, siteName);
        documentPanel.setOutputMarkupId(true);
        documentPanel.setVisible(true);
        mainContainer.addOrReplace(documentPanel);
    }

    private void displayResourceForm() {
        final ResourcePanel resourcePanel = new ResourcePanel("summary",
                operationType, (ResourceBean) component, siteName);
        resourcePanel.setOutputMarkupId(true);
        resourcePanel.setVisible(true);
        mainContainer.addOrReplace(resourcePanel);
    }
}
