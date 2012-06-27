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
package net.tirasa.hct.editor.validators;

import java.util.regex.Pattern;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.validator.StringValidator;
import net.tirasa.hct.editor.beans.SiteBean;

public class SiteNameValidator extends StringValidator {

    private static final long serialVersionUID = 4569852459401738409L;
    
    protected void onValidate(final IValidatable validatable) {
        final String siteName = (String) validatable.getValue();
        if (SiteBean.siteExists(siteName)) {
            error(validatable, "SiteNameValidator.exists");
        }
    }
}