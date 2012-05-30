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
package org.onehippo.forge.hct.editor.beans;

import java.io.Serializable;
import org.apache.wicket.IClusterable;
import org.onehippo.forge.hct.editor.forms.components.OrderCond;
import org.onehippo.forge.hct.editor.forms.components.OrderCond.Type;

/**
 * Order Condition.
 */
public class OrderBean implements Serializable, IClusterable {

    private static final long serialVersionUID = -953980078976868321L;

    private Type orderby;

    private String orderField;

    public final OrderCond.Type getOrderby() {
        return orderby;
    }

    public final void setOrderby(final OrderCond.Type orderby) {
        this.orderby = orderby;
    }

    public final String getOrderField() {
        return orderField;
    }

    public final void setOrderField(final String orderField) {
        this.orderField = orderField;
    }
}
