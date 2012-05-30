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
package org.onehippo.forge.hct.editor.crumbs;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.breadcrumb.BreadCrumbBar;
import org.apache.wicket.extensions.breadcrumb.BreadCrumbLink;
import org.apache.wicket.extensions.breadcrumb.IBreadCrumbModel;
import org.apache.wicket.extensions.breadcrumb.IBreadCrumbParticipant;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

public class HctBreadCrumbBar extends BreadCrumbBar {

    private static final long serialVersionUID = -5273446310539788971L;

    @SuppressWarnings("unused")
    private static final class BreadCrumbComponent extends Panel {

        private static final long serialVersionUID = -6137983893124499048L;

        public BreadCrumbComponent(final String id, final int index,
                final IBreadCrumbModel breadCrumbModel,
                final IBreadCrumbParticipant participant,
                final boolean enableLink) {
            super(id);
            add(new Label("sep", "").setRenderBodyOnly(true));
            final BreadCrumbLink link =
                    new BreadCrumbLink("link", breadCrumbModel) {

                        private static final long serialVersionUID = 506897728694578583L;

                        @Override
                        protected IBreadCrumbParticipant getParticipant(
                                String componentId) {
                            return participant;
                        }
                    };
            link.setEnabled(enableLink);
            add(link);

            IModel title;
            if (participant instanceof IAdminParticipant) {
                title = ((IAdminParticipant) participant).getTitle(this);
            } else {
                title = new Model(participant.getTitle());
            }
            link.add(new Label("label", title).setRenderBodyOnly(true));
        }
    }

    public HctBreadCrumbBar(final String id) {
        super(id);
    }

    @Override
    protected final Component newBreadCrumbComponent(
            final String id, final int index, final int total,
            final IBreadCrumbParticipant breadCrumbParticipant) {
        final boolean enableLink =
                getEnableLinkToCurrent() || (index < (total - 1));
        return new BreadCrumbComponent(
                id, index, this, breadCrumbParticipant, enableLink);
    }
}
