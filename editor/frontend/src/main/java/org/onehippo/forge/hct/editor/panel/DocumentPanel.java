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

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.validation.validator.StringValidator;
import org.onehippo.forge.hct.editor.Properties;
import org.onehippo.forge.hct.editor.beans.DocumentBean;
import org.onehippo.forge.hct.editor.validators.ComponentNameValidator;
import org.onehippo.forge.hct.editor.validators.NodeNameValidator;
import org.onehippo.forge.hct.editor.wicket.markup.html.AjaxTextFieldPanel;

public class DocumentPanel extends Panel {

    private static final long serialVersionUID = 6738841585013513441L;

    public DocumentPanel(final String id, final String operation,
            final DocumentBean component, final String siteName) {
        
        super(id);

        final AjaxTextFieldPanel name = new AjaxTextFieldPanel(
                "componentName", "componentName",
                new PropertyModel<String>(component, "componentName"), false);
        name.setEnabled(Properties.OP_CREATE.equals(operation));
        name.setRequired(Properties.OP_CREATE.equals(operation));
        name.addValidator(StringValidator.minimumLength(2));
        name.addValidator(new ComponentNameValidator(siteName));
        name.addValidator(new NodeNameValidator());
        add(name);
        
        final AjaxTextFieldPanel docType = new AjaxTextFieldPanel(
                "docType", "docType",
                new PropertyModel<String>(component, "docType"), false);
        add(docType);

        final AjaxTextFieldPanel pathDoc = new AjaxTextFieldPanel(
                "pathDoc", "pathDoc",
                new PropertyModel<String>(component, "pathDoc"), false);
        add(pathDoc);
    }
}
