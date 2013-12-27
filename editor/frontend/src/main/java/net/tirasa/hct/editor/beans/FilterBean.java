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
package net.tirasa.hct.editor.beans;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;
import javax.jcr.Node;
import org.apache.wicket.IClusterable;
import net.tirasa.hct.editor.forms.components.FilterCond;
import net.tirasa.hct.editor.forms.components.FilterCond.Type;

/**
 * Filter Bean class.
 */
public class FilterBean implements Serializable, IClusterable {

    private static final long serialVersionUID = 7801366252780959129L;

    /**
     * List all Filter Type Condition.
     */
    public enum OperationType {

        AND,
        OR

    };

    /**
     * Operation Type Field.
     */
    private OperationType operationType;

    /**
     * First Field.
     */
    private String field;

    /**
     * Operation.
     */
    private Type typeCond;

    /**
     * Field to check.
     */
    private String filterValue;

    private transient Node node;

    private String nodePath;

    private Map<String, String> properties = new TreeMap<String, String>();

    public FilterBean() {
    }

    /**
     *
     * @return operation
     */
    public final FilterBean.OperationType getOperationType() {
        return operationType;
    }

    /**
     *
     * @param operationType
     */
    public final void setOperationType(
            final FilterBean.OperationType operationType) {
        this.operationType = operationType;
    }

    /**
     * Return Field value.
     *
     * @return field
     */
    public final String getField() {
        return field;
    }

    /**
     * Set Field value.
     *
     * @param field
     */
    public final void setField(final String field) {
        this.field = field;
    }

    /**
     *
     * @return
     */
    public final FilterCond.Type getTypeCond() {
        return typeCond;
    }

    /**
     *
     * @param typeCond
     */
    public final void setTypeCond(final FilterCond.Type typeCond) {
        this.typeCond = typeCond;
    }

    /**
     *
     * @return
     */
    public final String getFilterValue() {
        return filterValue;
    }

    /**
     *
     * @param filterValue
     */
    public final void setFilterValue(final String filterValue) {
        this.filterValue = filterValue;
    }
}
