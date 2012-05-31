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

import java.util.Arrays;
import java.util.List;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.validation.validator.StringValidator;
import net.tirasa.hct.editor.Properties;
import net.tirasa.hct.editor.beans.FilterBean;
import net.tirasa.hct.editor.beans.FilterBean.OperationType;
import net.tirasa.hct.editor.beans.OrderBean;
import net.tirasa.hct.editor.beans.ResourceBean;
import net.tirasa.hct.editor.forms.components.FilterCond;
import net.tirasa.hct.editor.forms.components.OrderCond;
import net.tirasa.hct.editor.validators.ComponentNameValidator;
import net.tirasa.hct.editor.validators.NodeNameValidator;
import net.tirasa.hct.editor.wicket.markup.html.AjaxTextFieldPanel;

public class ResourcePanel extends Panel {

    private static final long serialVersionUID = 6493270146161654759L;

    private final IModel<List<FilterCond.Type>> attributeTypes =
            new LoadableDetachableModel<List<FilterCond.Type>>() {

                private static final long serialVersionUID = 5275935387613157437L;

                @Override
                protected List<FilterCond.Type> load() {
                    return Arrays.asList(FilterCond.Type.values());
                }
            };

    private final IModel<List<OrderCond.Type>> orderTypes =
            new LoadableDetachableModel<List<OrderCond.Type>>() {

                private static final long serialVersionUID =
                        5275935387613157437L;

                @Override
                protected List<OrderCond.Type> load() {
                    return Arrays.asList(OrderCond.Type.values());
                }
            };

    public ResourcePanel(final String id, final String operation,
            final ResourceBean component, final String siteName) {

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

        final AjaxTextFieldPanel base = new AjaxTextFieldPanel(
                "base", "base",
                new PropertyModel<String>(component, "base"), false);
        add(base);

        final AjaxTextFieldPanel type = new AjaxTextFieldPanel(
                "type", "type",
                new PropertyModel<String>(component, "type"), false);
        add(type);

        final AjaxTextFieldPanel depth = new AjaxTextFieldPanel(
                "depth", "depth",
                new PropertyModel<String>(component, "depth"), false);
        add(depth);

        final AjaxTextFieldPanel size = new AjaxTextFieldPanel(
                "size", "size",
                new PropertyModel<String>(component, "size"), false);
        add(size);

        final AjaxTextFieldPanel includeFolders = new AjaxTextFieldPanel(
                "includeFolders", "includeFolders",
                new PropertyModel<String>(component, "includeFolders"), false);
        add(includeFolders);

        final WebMarkupContainer filterContainer =
                new WebMarkupContainer("resourceFilterContainer");
        filterContainer.setOutputMarkupId(true);
        filterContainer.add(new Filter("searchView",
                component.getFilterList(), filterContainer));

        final AjaxButton addAndButton = new IndicatingAjaxButton("addAndButton",
                new Model("addAndButton")) {

            private static final long serialVersionUID = -4804368561204623354L;

            @Override
            protected void onSubmit(final AjaxRequestTarget target,
                    final Form form) {

                final FilterBean conditionWrapper = new FilterBean();
                conditionWrapper.setOperationType(OperationType.AND);
                component.getFilterList().add(conditionWrapper);
                target.addComponent(filterContainer);
            }
        };

        addAndButton.setDefaultFormProcessing(false);
        filterContainer.add(addAndButton);
        this.add(filterContainer);


        final AjaxButton addOrButton = new IndicatingAjaxButton("addOrButton",
                new Model("addOrButton")) {

            private static final long serialVersionUID = -4804368561204623354L;

            @Override
            protected void onSubmit(final AjaxRequestTarget target,
                    final Form form) {

                final FilterBean conditionWrapper =
                        new FilterBean();
                conditionWrapper.setOperationType(OperationType.OR);
                component.getFilterList().add(conditionWrapper);
                target.addComponent(filterContainer);
            }
        };

        addOrButton.setDefaultFormProcessing(false);
        filterContainer.add(addOrButton);

        final WebMarkupContainer orderContainer =
                new WebMarkupContainer("orderContainer");
        orderContainer.setOutputMarkupId(true);
        orderContainer.add(new OrderBy("orderView",
                component.getOrderList(), orderContainer));
        this.add(orderContainer);

        final AjaxButton addOrderButton =
                new IndicatingAjaxButton("addOrderButton",
                new Model("addOrderButton")) {

                    private static final long serialVersionUID =
                            -4804368561204623354L;

                    @Override
                    protected void onSubmit(final AjaxRequestTarget target,
                            final Form form) {

                        final OrderBean conditionWrapper =
                                new OrderBean();
                        component.getOrderList().add(conditionWrapper);
                        target.addComponent(orderContainer);
                    }
                };

        addOrderButton.setDefaultFormProcessing(false);
        orderContainer.add(addOrderButton);

        final ReturnFieldPanel returnFieldPanel =
                new ReturnFieldPanel("returnFieldPanel", component);
        returnFieldPanel.setOutputMarkupId(true);
        returnFieldPanel.setVisible(true);
        this.add(returnFieldPanel);
    }

    private class Filter extends ListView<FilterBean> {

        private static final long serialVersionUID = -6884593469654499557L;

        final private WebMarkupContainer summaryFilterContainer;

        public Filter(final String id,
                final List<? extends FilterBean> list,
                final WebMarkupContainer summaryFilterContainer) {

            super(id, list);
            this.summaryFilterContainer = summaryFilterContainer;
        }

        @Override
        protected void populateItem(
                final ListItem<FilterBean> item) {

            final FilterBean filterCondition =
                    item.getModelObject();

            item.add(new Label("operationType",
                    filterCondition.getOperationType().toString()));

            final TextField field = new TextField("field",
                    new PropertyModel<String>(filterCondition, "field"));
            item.add(field);
            field.add(new AjaxFormComponentUpdatingBehavior("onchange") {

                private static final long serialVersionUID =
                        -1107858522700306810L;

                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                }
            });

            final DropDownChoice<FilterCond.Type> typeCond =
                    new DropDownChoice<FilterCond.Type>(
                    "typeCond", new PropertyModel<FilterCond.Type>(
                    filterCondition, "typeCond"),
                    attributeTypes);
            typeCond.setRequired(true);
            item.add(typeCond);

            typeCond.add(new AjaxFormComponentUpdatingBehavior("onchange") {

                private static final long serialVersionUID =
                        -1107858522700306810L;

                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                }
            });

            final TextField<String> filterValue =
                    new TextField<String>("filterValue",
                    new PropertyModel<String>(
                    filterCondition, "filterValue"));
            item.add(filterValue);
            filterValue.add(new AjaxFormComponentUpdatingBehavior("onchange") {

                private static final long serialVersionUID = -1107858522700306810L;

                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                }
            });

            final AjaxButton dropButton = new IndicatingAjaxButton(
                    "dropButton", new Model("dropButton")) {

                private static final long serialVersionUID = -4804368561204623354L;

                @Override
                protected void onSubmit(
                        final AjaxRequestTarget target,
                        final Form form) {

                    getList().remove(
                            Integer.valueOf(getParent().getId()).intValue());
                    target.addComponent(summaryFilterContainer);
                }
            };

            dropButton.setDefaultFormProcessing(false);
            item.add(dropButton);
        }
    }

    private class OrderBy extends ListView<OrderBean> {

        private static final long serialVersionUID = -1538195134836188582L;

        private final WebMarkupContainer orderContainer;

        public OrderBy(final String id,
                final List<? extends OrderBean> list,
                final WebMarkupContainer orderContainer) {

            super(id, list);
            this.orderContainer = orderContainer;
        }

        @Override
        protected void populateItem(
                final ListItem<OrderBean> item) {

            final OrderBean orderCondition = item.getModelObject();

            final DropDownChoice<OrderCond.Type> orderby =
                    new DropDownChoice<OrderCond.Type>(
                    "orderby", new PropertyModel<OrderCond.Type>(
                    orderCondition, "orderby"),
                    orderTypes);
            orderby.setRequired(true);
            item.add(orderby);
            orderby.add(new AjaxFormComponentUpdatingBehavior("onchange") {

                private static final long serialVersionUID =
                        -1107858522700306810L;

                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                }
            });

            final TextField orderField = new TextField("orderField",
                    new PropertyModel(orderCondition, "orderField"));
            item.add(orderField);

            orderField.add(new AjaxFormComponentUpdatingBehavior("onchange") {

                private static final long serialVersionUID =
                        -1107858522700306810L;

                @Override
                protected void onUpdate(AjaxRequestTarget target) {
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
                    target.addComponent(orderContainer);
                }
            };
            dropButton.setDefaultFormProcessing(false);
            item.add(dropButton);
        }
    }
}
