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
package net.tirasa.hct.editor.forms;

import java.util.List;
import javax.jcr.RepositoryException;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.breadcrumb.IBreadCrumbModel;
import org.apache.wicket.extensions.breadcrumb.IBreadCrumbParticipant;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.validation.validator.StringValidator;
import org.hippoecm.frontend.session.UserSession;
import net.tirasa.hct.editor.Properties;
import net.tirasa.hct.editor.beans.SiteBean;
import net.tirasa.hct.editor.validators.NodeNameValidator;
import net.tirasa.hct.editor.validators.SiteNameValidator;
import net.tirasa.hct.editor.wicket.markup.html.AjaxTextFieldPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SiteForm extends Form {

    private static final Logger LOG =
            LoggerFactory.getLogger(SiteForm.class);

    private static final long serialVersionUID = 712357636277243353L;

    private SiteBean site;

    private String operationType;

    private final transient IModel model;

    public SiteForm(final String id, final IModel model,
            final IBreadCrumbModel breadCrumbModel,
            final FeedbackPanel feedmsg) {

        super(id);
        this.model = model;
        setOperationType(model);

        if (model == null) {
            site = new SiteBean();
            setModel(new CompoundPropertyModel<SiteBean>(site));
        } else {
            site = (SiteBean) model.getObject();
        }

        final AjaxTextFieldPanel name = new AjaxTextFieldPanel(
                "siteName", "siteName",
                new PropertyModel<String>(site, "siteName"), false);
        name.setEnabled(Properties.OP_CREATE.equals(operationType));
        name.setRequired(Properties.OP_CREATE.equals(operationType));
        name.addValidator(StringValidator.minimumLength(2));
        name.addValidator(new SiteNameValidator());
        name.addValidator(new NodeNameValidator());
        name.setRequired(Properties.OP_CREATE.equals(operationType));
        add(name);

        final AjaxTextFieldPanel description = new AjaxTextFieldPanel(
                "description", "description",
                new PropertyModel<String>(site, "description"), false);
        add(description);

        add(new AjaxButton("create-button", this) {

            private static final long serialVersionUID = -5783994974426198290L;

            @Override
            protected void onSubmit(final AjaxRequestTarget target,
                    final Form form) {

                try {
                    if (Properties.OP_CREATE.equals(operationType)) {
                        site.create();
                    } else {
                        site.save();
                    }

                    LOG.info("Site '" + site.getSiteName()
                            + "' created by "
                            + ((UserSession) Session.get()).getJcrSession().
                            getUserID());
                    Session.get().info(getString("site-created")
                            + ": " + site.getSiteName());
                    // one up
                    final List<IBreadCrumbParticipant> l =
                            breadCrumbModel.allBreadCrumbParticipants();
                    breadCrumbModel.setActive(l.get(l.size() - 2));
                } catch (RepositoryException e) {
                    Session.get().warn(getString(
                            "site-create-failed")
                            + ": " + site.getSiteName());
                    LOG.error("Unable to create site '"
                            + site.getSiteName() + "' : ", e);
                }
            }

            @Override
            protected void onError(final AjaxRequestTarget target,
                    final Form form) {
                // make sure the feedback panel is shown
                target.addComponent(feedmsg);
            }
        });

        // add a button that can be used to submit the form via ajax
        add(new AjaxButton("cancel-button") {

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

    private void setOperationType(final IModel model) {
        operationType = model == null
                ? Properties.OP_CREATE : Properties.OP_UPDATE;
    }
}
