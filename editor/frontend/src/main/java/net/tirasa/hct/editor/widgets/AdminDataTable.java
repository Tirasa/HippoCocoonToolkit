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

import java.util.List;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackHeadersToolbar;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxNavigationToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NoRecordsToolbar;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.OddEvenItem;
import org.apache.wicket.model.IModel;

public class AdminDataTable extends DataTable {

    private static final long serialVersionUID = 7831191713372614221L;

    public AdminDataTable(final String id, final List<IColumn> columns,
            final ISortableDataProvider dataProvider, final int rowsPerPage) {

        this(id, (IColumn[]) columns.toArray(
                new IColumn[columns.size()]), dataProvider, rowsPerPage);
    }

    public AdminDataTable(final String id, final IColumn[] columns,
            final ISortableDataProvider dataProvider,
            final int rowsPerPage) {

        super(id, columns, dataProvider, rowsPerPage);
        setOutputMarkupId(true);
        setVersioned(false);
        addTopToolbar(new AjaxNavigationToolbar(this));
        addBottomToolbar(new AjaxNavigationToolbar(this));
        addTopToolbar(new AjaxFallbackHeadersToolbar(this, dataProvider));
        addBottomToolbar(new NoRecordsToolbar(this));
    }

    @Override
    protected Item newRowItem(final String id,
            final int index, final IModel model) {
        return new OddEvenItem(id, index, model);
    }
}
