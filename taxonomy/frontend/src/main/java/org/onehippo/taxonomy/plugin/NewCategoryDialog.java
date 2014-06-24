/*
 *  Copyright 2009-2013 Hippo B.V. (http://www.onehippo.com)
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.onehippo.taxonomy.plugin;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.util.value.IValueMap;
import org.hippoecm.frontend.dialog.AbstractDialog;
import org.onehippo.taxonomy.api.Taxonomy;
import org.onehippo.taxonomy.plugin.api.KeyCodec;

public class NewCategoryDialog extends AbstractDialog<Taxonomy> {
    private static final long serialVersionUID = 1L;

    private String key;
    private String name;
    private String pathKey;

    public NewCategoryDialog(final IModel<Taxonomy> taxonomyModel, final String pathKey) {
        super(taxonomyModel);

        name = "new category";

        add(new AttributeAppender("class", new Model<String>("hippo-editor"), " "));
        
        final FormComponent<String> keyField = new TextField<String>("key", new IModel<String>() {
            private static final long serialVersionUID = 1L;

            public String getObject() {
                return getKey();
            }

            public void setObject(String object) {
                key = object;
            }

            public void detach() {
            }
        });
        keyField.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
            }
        });
        keyField.setOutputMarkupId(true);
        add(keyField);

        FormComponent<String> nameField = new TextField<String>("name", new IModel<String>() {
            private static final long serialVersionUID = 1L;

            public String getObject() {
                return name;
            }

            public void setObject(String object) {
                name = object;
            }

            public void detach() {
            }
        });
        nameField.add(new OnChangeAjaxBehavior() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                target.addComponent(keyField);
            }
        });
        add(nameField);
    }

    protected String getKey() {
        if (key != null) {
            return key;
        }
        if (name == null) {
            return null;
        }
        String encoded = KeyCodec.encode(name);
        String key = encoded;
        int index = 0;
        Taxonomy taxonomy = getModelObject();
        while (taxonomy.getCategoryByKey(key) != null) {
            key = encoded + "_" + (++index);
        }
        return key;
    }

    protected String getName() {
        return name;
    }

    @Override
    public IValueMap getProperties() {
        return SMALL;
    }

    public IModel<String> getTitle() {
        return new StringResourceModel("new-category", this, null);
    }

}
