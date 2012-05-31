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
import org.hippoecm.frontend.session.UserSession;
import net.tirasa.hct.editor.Properties;
import net.tirasa.hct.editor.beans.PageBean;
import net.tirasa.hct.editor.panel.PageFieldPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PageForm extends Form {

    private static final Logger LOG =
            LoggerFactory.getLogger(PageForm.class);

    private static final long serialVersionUID = 712357636277243353L;

    private PageBean page;

    private String operationType;

    private final transient IModel model;

    public PageForm(final String id, final IModel model,
            final IBreadCrumbModel breadCrumbModel,
            final FeedbackPanel feedmsg, final String siteName) {

        super(id);
        this.model = model;
        setOperationType(model);

        if (model == null) {
            page = new PageBean();
            setModel(new CompoundPropertyModel<PageBean>(page));
        } else {
            page = (PageBean) model.getObject();
        }

        final PageFieldPanel pageField =
                new PageFieldPanel("fieldPanel", page, operationType, siteName);
        pageField.setOutputMarkupId(true);
        add(pageField);

        add(new AjaxButton("create-button", this) {

            private static final long serialVersionUID = -5783994974426198290L;

            @Override
            protected void onSubmit(final AjaxRequestTarget target,
                    final Form form) {

                try {
                    if (Properties.OP_CREATE.equals(operationType)) {
                        page.create(siteName);
                    } else {
                        page.save();
                    }

                    LOG.info("Page '" + page.getPageName()
                            + "' created by "
                            + ((UserSession) Session.get()).getJcrSession().
                            getUserID());
                    Session.get().info(getString("page-created")
                            + ": " + page.getPageName());
                    // one up
                    final List<IBreadCrumbParticipant> l =
                            breadCrumbModel.allBreadCrumbParticipants();
                    breadCrumbModel.setActive(l.get(l.size() - 2));
                } catch (RepositoryException e) {
                    Session.get().warn(getString(
                            "page-create-failed")
                            + ": " + page.getPageName());
                    LOG.error("Unable to create page '"
                            + page.getPageName() + "' : ", e);
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
