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
package net.tirasa.hct.editor.panel;

import java.util.List;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import net.tirasa.hct.editor.beans.ComponentType;
import net.tirasa.hct.editor.beans.FieldBean;

public class ReturnFieldPanel extends Panel {

    private static final long serialVersionUID = -5875423782820913114L;

    private CheckBox imageItem;

    private CheckBox relatedDocs;

    private FieldBean field;

    public ReturnFieldPanel(final String id, final ComponentType doc) {
        super(id);
        add(new FieldContainer("returnField", doc));
        add(new ImageContainer("imageField"));
        add(new RelatedDocsContainer("relatedDocsField"));
    }

    private class FieldContainer extends WebMarkupContainer {

        private static final long serialVersionUID = -2899826689653891769L;

        WebMarkupContainer fieldContainer;

        public FieldContainer(final String id, final ComponentType doc) {
            super(id);
            this.setOutputMarkupId(true);
            fieldContainer = new WebMarkupContainer("fieldContainer");
            this.add(fieldContainer);
            fieldContainer.setOutputMarkupId(true);
            fieldContainer.add(
                    new Field("fieldView", doc.getFieldList(), this));

            AjaxButton addNewButton = new IndicatingAjaxButton("addNewField",
                    new Model("addNewField")) {

                        private static final long serialVersionUID =
                        -4804368561204623354L;

                        @Override
                        protected void onSubmit(final AjaxRequestTarget target,
                                final Form form) {

                            FieldBean fieldItem = new FieldBean();
                            doc.getFieldList().add(fieldItem);
                            target.addComponent(fieldContainer);
                        }
                    };
            addNewButton.setDefaultFormProcessing(false);
            fieldContainer.add(addNewButton);
        }
    }

    private class Field extends ListView<FieldBean> {

        private static final long serialVersionUID = 2757321267384251935L;

        final private WebMarkupContainer fieldContainer;

        public Field(final String id,
                final List<? extends FieldBean> list,
                final WebMarkupContainer fieldContainer) {

            super(id, list);
            this.fieldContainer = fieldContainer;
        }

        @Override
        protected void populateItem(
                final ListItem<FieldBean> item) {

            final FieldBean fieldItem = item.getModelObject();
            final TextField field = new TextField("fieldItem",
                    new PropertyModel<String>(fieldItem, "fieldItem"));
            item.add(field);
            field.add(new AjaxFormComponentUpdatingBehavior("onchange") {

                private static final long serialVersionUID =
                        -1107858522700306810L;

                @Override
                protected void onUpdate(final AjaxRequestTarget target) {
                }
            });

            final AjaxButton dropButton = new IndicatingAjaxButton(
                    "dropButton", new Model("dropButton")) {

                        private static final long serialVersionUID =
                        -4804368561204623354L;

                        @Override
                        protected void onSubmit(
                                final AjaxRequestTarget target,
                                final Form form) {

                                    getList().remove(
                                            Integer.valueOf(getParent().getId()).intValue());
                                    target.addComponent(fieldContainer);
                                }
                    };

            dropButton.setDefaultFormProcessing(false);
            item.add(dropButton);
        }
    }

    private class ImageContainer extends WebMarkupContainer {

        private static final long serialVersionUID = 4806682576605129518L;

        public ImageContainer(final String id) {
            super(id);
            setOutputMarkupId(true);
            imageItem = new CheckBox("imageItem");
            add(imageItem);
        }
    }

    private class RelatedDocsContainer extends WebMarkupContainer {

        private static final long serialVersionUID = -2031468412785305644L;

        public RelatedDocsContainer(String id) {
            super(id);
            setOutputMarkupId(true);
            relatedDocs = new CheckBox("relatedDocs");
            add(relatedDocs);
        }
    }

    public String getImageItem() {
        return imageItem.getValue();
    }
}
