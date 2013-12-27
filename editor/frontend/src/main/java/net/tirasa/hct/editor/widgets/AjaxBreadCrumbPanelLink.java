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

import org.apache.wicket.extensions.breadcrumb.IBreadCrumbModel;
import org.apache.wicket.extensions.breadcrumb.IBreadCrumbParticipant;
import org.apache.wicket.extensions.breadcrumb.panel.BreadCrumbPanel;
import org.apache.wicket.model.IModel;
import org.hippoecm.frontend.plugin.IPluginContext;

public class AjaxBreadCrumbPanelLink extends AjaxBreadCrumbLink {

    private static final long serialVersionUID = 1105443743788954451L;

    /**
     * The bread crumb model.
     */
    private final IBreadCrumbModel breadCrumbModel;

    /**
     * factory for creating bread crumbs panels.
     */
    private final AjaxBreadCrumbPanelFactory breadCrumbPanelFactory;

    /**
     * the plugin context
     */
    private final IModel model;

    private String siteName;

    public AjaxBreadCrumbPanelLink(final String id,
            final IPluginContext context, final BreadCrumbPanel caller,
            final Class panelClass, final String siteName) {
        this(id, caller.getBreadCrumbModel(),
                new AjaxBreadCrumbPanelFactory(context, panelClass), siteName);
    }

    public AjaxBreadCrumbPanelLink(final String id,
            final IBreadCrumbModel breadCrumbModel,
            final AjaxBreadCrumbPanelFactory breadCrumbPanelFactory,
            final String siteName) {
        super(id, breadCrumbModel, siteName);

        if (breadCrumbModel == null) {
            throw new IllegalArgumentException(
                    "argument breadCrumbModel must be not null");
        }
        if (breadCrumbPanelFactory == null) {
            throw new IllegalArgumentException(
                    "argument breadCrumbPanelFactory must be not null");
        }

        this.siteName = siteName;
        this.model = null;
        this.breadCrumbModel = breadCrumbModel;
        this.breadCrumbPanelFactory = breadCrumbPanelFactory;
    }

    public AjaxBreadCrumbPanelLink(final String id,
            final IBreadCrumbModel breadCrumbModel,
            final AjaxBreadCrumbPanelFactory breadCrumbPanelFactory,
            final IModel model,
            final String siteName) {
        super(id, breadCrumbModel, siteName);

        if (breadCrumbModel == null) {
            throw new IllegalArgumentException(
                    "argument breadCrumbModel must be not null");
        }
        if (model == null) {
            throw new IllegalArgumentException(
                    "argument model must be not null");
        }
        if (breadCrumbPanelFactory == null) {
            throw new IllegalArgumentException(
                    "argument breadCrumbPanelFactory must be not null");
        }

        this.siteName = siteName;
        this.model = model;
        this.breadCrumbModel = breadCrumbModel;
        this.breadCrumbPanelFactory = breadCrumbPanelFactory;
    }

    @Override
    protected final IBreadCrumbParticipant getParticipant(
            final String componentId, final String siteName) {
        if (model != null) {
            return breadCrumbPanelFactory.create(
                    componentId, breadCrumbModel, model, siteName);
        }
        return breadCrumbPanelFactory.create(
                componentId, breadCrumbModel, siteName);

    }
}
