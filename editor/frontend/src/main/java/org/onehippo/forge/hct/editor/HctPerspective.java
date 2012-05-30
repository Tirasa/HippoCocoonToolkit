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
package org.onehippo.forge.hct.editor;

import org.apache.wicket.ResourceReference;
import org.apache.wicket.extensions.breadcrumb.BreadCrumbBar;
import org.apache.wicket.extensions.breadcrumb.IBreadCrumbModelListener;
import org.apache.wicket.extensions.breadcrumb.IBreadCrumbParticipant;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.hippoecm.frontend.dialog.IDialogService;
import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.hippoecm.frontend.plugins.standards.perspective.Perspective;
import org.hippoecm.frontend.plugins.yui.layout.WireframeBehavior;
import org.hippoecm.frontend.plugins.yui.layout.WireframeSettings;
import org.hippoecm.frontend.service.IconSize;
import org.onehippo.forge.hct.editor.crumbs.HctBreadCrumbBar;

public class HctPerspective extends Perspective {

    private static final long serialVersionUID = -5142961480434101722L;

    public HctPerspective(final IPluginContext context,
            final IPluginConfig config) {

        super(context, config);
        setOutputMarkupId(true);

        final BreadCrumbBar breadCrumbBar =
                new HctBreadCrumbBar("breadCrumbBar");
        add(breadCrumbBar);

        final HctPanelPlugin adminPanel =
                new HctPanelPlugin("panel", context, breadCrumbBar);
        add(adminPanel);
        breadCrumbBar.setActive(adminPanel);
        breadCrumbBar.addListener(new IBreadCrumbModelListener() {

            private static final long serialVersionUID = 4402908466374385050L;

            @Override
            public void breadCrumbActivated(
                    final IBreadCrumbParticipant previousParticipant,
                    final IBreadCrumbParticipant breadCrumbParticipant) {
                redraw();
            }

            @Override
            public void breadCrumbAdded(
                    final IBreadCrumbParticipant breadCrumbParticipant) {
                redraw();
            }

            @Override
            public void breadCrumbRemoved(
                    final IBreadCrumbParticipant breadCrumbParticipant) {
                redraw();
            }
        });

        add(new WireframeBehavior(
                new WireframeSettings(
                config.getPluginConfig("layout.wireframe"))));
        add(CSSPackageResource.getHeaderContribution(HctPerspective.class,
                "hct-perspective.css"));
    }

    @Override
    public final ResourceReference getIcon(final IconSize type) {
        return new ResourceReference(HctPerspective.class,
                "hct-perspective-" + type.getSize() + ".png");
    }

    public final void showDialog(final IDialogService.Dialog dialog) {
        getPluginContext().getService(IDialogService.class.getName(),
                IDialogService.class).show(dialog);
    }
}
