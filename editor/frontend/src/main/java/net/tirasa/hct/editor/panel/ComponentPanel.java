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

import org.apache.wicket.Component;
import org.apache.wicket.extensions.breadcrumb.IBreadCrumbModel;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.hippoecm.frontend.plugin.IPluginContext;
import net.tirasa.hct.editor.crumbs.HCTBreadCrumbPanel;
import net.tirasa.hct.editor.forms.ComponentForm;

public class ComponentPanel extends HCTBreadCrumbPanel {

    private static final long serialVersionUID = 8199438351122694299L;

    public ComponentPanel(final String id, final IPluginContext context,
            final IBreadCrumbModel breadCrumbModel, final String siteName) {
        super(id, breadCrumbModel);
        setOutputMarkupId(true);

        final FeedbackPanel feedmsg = new FeedbackPanel("feedmsg");
        feedmsg.setOutputMarkupId(true);
        this.add(feedmsg);

        add(new ComponentForm("form", null, breadCrumbModel, feedmsg, siteName));
    }

    public ComponentPanel(final String id, final IPluginContext context,
            final IBreadCrumbModel breadCrumbModel,
            final IModel model, final String siteName) {
        super(id, breadCrumbModel);
        setOutputMarkupId(true);

        final FeedbackPanel feedmsg = new FeedbackPanel("feedmsg");
        feedmsg.setOutputMarkupId(true);
        this.add(feedmsg);

        add(new ComponentForm("form", model, breadCrumbModel, feedmsg, siteName));
    }

    @Override
    public final IModel<String> getTitle(final Component component) {
        return new StringResourceModel("component-create", component, null);
    }
}
