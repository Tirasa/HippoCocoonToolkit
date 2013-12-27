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
package net.tirasa.hct.editor.plugin;

import org.apache.wicket.ResourceReference;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import net.tirasa.hct.editor.HCTPlugin;
import net.tirasa.hct.editor.panel.HCTSitePanel;

public class HCTPluginSite extends HCTPlugin {

    private static final long serialVersionUID = -692590539874058169L;

    public HCTPluginSite(final IPluginContext context,
            final IPluginConfig config) {
        super(context, HCTSitePanel.class);
    }

    @Override
    public final ResourceReference getImage() {
        return new ResourceReference(getClass(), "site-48.png");
    }

    @Override
    public final IModel<String> getTitle() {
        return new ResourceModel("admin-sites-title");
    }

    @Override
    public final IModel<String> getHelp() {
        return new ResourceModel("admin-sites-title-help");
    }

}

