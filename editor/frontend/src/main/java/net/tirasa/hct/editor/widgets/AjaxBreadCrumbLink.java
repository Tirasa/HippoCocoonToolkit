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
package net.tirasa.hct.editor.widgets;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.extensions.breadcrumb.IBreadCrumbModel;
import org.apache.wicket.extensions.breadcrumb.IBreadCrumbParticipant;
import org.apache.wicket.version.undo.Change;

public abstract class AjaxBreadCrumbLink extends AjaxFallbackLink {

    private static final long serialVersionUID = 3879954144522675275L;

    /**
     * The bread crumb model.
     */
    private final IBreadCrumbModel breadCrumbModel;

    private final String siteName;

    /**
     * Construct.
     *
     * @param id The link id
     * @param breadCrumbModel The bread crumb model
     */
    public AjaxBreadCrumbLink(final String id, final IBreadCrumbModel breadCrumbModel, final String siteName) {
        super(id);
        this.breadCrumbModel = breadCrumbModel;
        this.siteName = siteName;
    }

    @Override
    public void onClick(final AjaxRequestTarget target) {

        final IBreadCrumbParticipant active = breadCrumbModel.getActive();
        if (active == null) {
            throw new IllegalStateException("The model has no active"
                    + "bread crumb. Before using " + this
                    + ", you have to have at least one bread crumb in the model");
        }

        final IBreadCrumbParticipant participant =
                getParticipant(active.getComponent().getId(), siteName);

        addStateChange(new Change() {

            private static final long serialVersionUID = 1648663648488911430L;

            @Override
            public void undo() {
                breadCrumbModel.setActive(active);
            }
        });

        breadCrumbModel.setActive(participant);
    }

    protected abstract IBreadCrumbParticipant getParticipant(final String componentId,
            final String siteName);
}
