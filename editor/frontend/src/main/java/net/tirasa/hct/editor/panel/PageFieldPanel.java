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
package net.tirasa.hct.editor.panel;

import java.util.List;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.validation.validator.StringValidator;
import net.tirasa.hct.editor.Properties;
import net.tirasa.hct.editor.beans.PageBean;
import net.tirasa.hct.editor.data.ComponentDataProvider;
import net.tirasa.hct.editor.validators.NodeNameValidator;
import net.tirasa.hct.editor.validators.PageNameValidator;
import net.tirasa.hct.editor.wicket.markup.html.AjaxDropDownChoicePanel;
import net.tirasa.hct.editor.wicket.markup.html.AjaxTextFieldPanel;

public class PageFieldPanel extends Panel {

    private static final long serialVersionUID = 1696885619860054309L;

    public PageFieldPanel(final String id,
            final PageBean page,
            final String operationType, final String siteName) {

        super(id);

        final AjaxTextFieldPanel name = new AjaxTextFieldPanel(
                "pageName", "pageName",
                new PropertyModel<String>(page, "pageName"), false);
        name.setEnabled(Properties.OP_CREATE.equals(operationType));
        name.setRequired(Properties.OP_CREATE.equals(operationType));
        name.addValidator(StringValidator.minimumLength(2));
        name.addValidator(new PageNameValidator(siteName));
        name.addValidator(new NodeNameValidator());
        name.setRequired(Properties.OP_CREATE.equals(operationType));
        add(name);

        final AjaxTextFieldPanel description = new AjaxTextFieldPanel(
                "description", "description",
                new PropertyModel<String>(page, "description"), false);
        add(description);

        final ComponentDataProvider componentDataProvider =
                new ComponentDataProvider(siteName);

        componentDataProvider.size();

        final List<String> componentNameList =
                componentDataProvider.getComponentList();

        final AjaxDropDownChoicePanel top =
                new AjaxDropDownChoicePanel("top", "top",
                new PropertyModel(page, "top"), true);
        top.setChoices(componentNameList);
        add(top);

        final AjaxDropDownChoicePanel left =
                new AjaxDropDownChoicePanel("left", "left",
                new PropertyModel(page, "left"), true);
        left.setChoices(componentNameList);
        add(left);

        final AjaxDropDownChoicePanel bottom =
                new AjaxDropDownChoicePanel("bottom", "bottom",
                new PropertyModel(page, "bottom"), true);
        bottom.setChoices(componentNameList);
        add(bottom);

        final AjaxDropDownChoicePanel center =
                new AjaxDropDownChoicePanel("center", "center",
                new PropertyModel(page, "center"), true);
        center.setChoices(componentNameList);
        add(center);

        final AjaxDropDownChoicePanel right =
                new AjaxDropDownChoicePanel("right", "right",
                new PropertyModel(page, "right"), true);
        right.setChoices(componentNameList);
        add(right);
    }
}
