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
package net.tirasa.hct.editor.wicket.markup.html;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteTextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.IValidator;

public class AjaxTextFieldPanel extends FieldPanel<String> {

    private static final long serialVersionUID = 238940918106696068L;

    private List<String> choices = Collections.EMPTY_LIST;

    public AjaxTextFieldPanel(
            final String id,
            final String name,
            final IModel<String> model,
            final boolean active) {

        super(id, name, model, active);

        field = new AutoCompleteTextField<String>("textField", model) {

            private static final long serialVersionUID = -6648767303091874219L;

            @Override
            protected Iterator<String> getChoices(final String input) {
                final Pattern pattern = Pattern.compile(
                        Pattern.quote(input) + ".*",
                        Pattern.CASE_INSENSITIVE);

                final List<String> result = new ArrayList<String>();

                for (String choice : choices) {
                    if (pattern.matcher(choice).matches()) {
                        result.add(choice);
                    }
                }

                return result.iterator();
            }
        };

        add(field.setLabel(new Model(name)).setOutputMarkupId(true));

        if (active) {
            field.add(new AjaxFormComponentUpdatingBehavior("onchange") {

                private static final long serialVersionUID =
                        -1107858522700306810L;

                @Override
                protected void onUpdate(final AjaxRequestTarget art) {
                    // nothing to do
                }
            });
        }
    }

    public void addValidator(final IValidator validator) {
        this.field.add(validator);
    }

    public void setChoices(final List<String> choices) {
        if (choices != null) {
            this.choices = choices;
        }
    }
}
