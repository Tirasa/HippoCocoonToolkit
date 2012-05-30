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
package org.onehippo.forge.hct.editor.panel;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.breadcrumb.IBreadCrumbModel;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.hippoecm.frontend.plugin.IPluginContext;
import org.onehippo.forge.hct.editor.crumbs.HctBreadCrumbPanel;
import org.onehippo.forge.hct.editor.forms.PageForm;

public class PagePanel extends HctBreadCrumbPanel {

    private static final long serialVersionUID = 1832147074344581181L;

    public PagePanel(final String id, final IPluginContext context,
            final IBreadCrumbModel breadCrumbModel, final String siteName) {
        super(id, breadCrumbModel);

        final FeedbackPanel feedmsg = new FeedbackPanel("feedmsg");
        feedmsg.setOutputMarkupId(true);
        add(feedmsg);

        add(new PageForm("form", null, breadCrumbModel, feedmsg, siteName));

    }

    public PagePanel(final String id, final IPluginContext context,
            final IBreadCrumbModel breadCrumbModel,
            final IModel model, final String siteName) {
        super(id, breadCrumbModel);
        setOutputMarkupId(true);

        final FeedbackPanel feedmsg = new FeedbackPanel("feedmsg");
        feedmsg.setOutputMarkupId(true);
        add(feedmsg);

        add(new PageForm("form", model, breadCrumbModel, feedmsg, siteName));

    }

    @Override
    public final IModel<String> getTitle(final Component page) {
        return new StringResourceModel("page-create", page, null);
    }
}
