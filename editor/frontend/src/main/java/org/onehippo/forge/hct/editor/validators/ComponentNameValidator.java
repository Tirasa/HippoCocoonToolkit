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
package org.onehippo.forge.hct.editor.validators;

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.validator.StringValidator;
import org.onehippo.forge.hct.editor.beans.ComponentType;

public class ComponentNameValidator extends StringValidator {

    private static final long serialVersionUID = -1954980841204311846L;

    private String siteName;

    public ComponentNameValidator(final String siteName) {
        this.siteName = siteName;
    }

    @Override
    protected void onValidate(final IValidatable validatable) {
        final String componentName = (String) validatable.getValue();
        if (ComponentType.componentExists(componentName, siteName)) {
            error(validatable);
        }
    }

    @Override
    protected String resourceKey() {
        return "ComponentNameValidator.exists";
    }
}
