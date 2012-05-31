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
 */package net.tirasa.hct.editor;

import org.apache.wicket.ResourceReference;
import org.apache.wicket.extensions.breadcrumb.panel.IBreadCrumbPanelFactory;
import org.apache.wicket.model.IModel;
import org.hippoecm.frontend.plugin.IPlugin;
import org.hippoecm.frontend.plugin.IPluginContext;
import net.tirasa.hct.editor.crumbs.HctBreadCrumbPanel;
import net.tirasa.hct.editor.widgets.AjaxBreadCrumbPanelFactory;

public abstract class HctPlugin extends AjaxBreadCrumbPanelFactory
        implements IPlugin, IBreadCrumbPanelFactory {

    public static final String ADMIN_PANEL_ID = "hct.panel";

    private static final long serialVersionUID = -465766860392519149L;

    private final transient IPluginContext context;

    public HctPlugin(final IPluginContext context,
            final Class<? extends HctBreadCrumbPanel> panelClass) {
        super(context, panelClass);
        this.context = context;
    }

    public abstract ResourceReference getImage();

    public abstract IModel<String> getTitle();

    public abstract IModel<String> getHelp();

    @Override
    public final void start() {
        context.registerService(this, ADMIN_PANEL_ID);
    }

    @Override
    public void stop() {
    }
}
