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
package org.onehippo.forge.hct.editor.forms;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.wicket.markup.html.form.IChoiceRenderer;

/**
 *
 * @param <ComponentType>
 *
 */
public class TypeChoiceRenderer<ComponentType> implements IChoiceRenderer {

    private static final long serialVersionUID = -1982585803345685399L;

    /**
     * Id component.
     */
    private String idType;
    /**
     * Component name value.
     */
    private String valueType;

    /**
     *
     * @param id - Component id
     * @param value - Component name
     */
    public TypeChoiceRenderer(final String id, final String value) {
        this.idType = id;
        this.valueType = value;
    }

    /**
     *
     * @param object - Selected component
     * @return value
     */
    @Override
    public final Object getDisplayValue(final Object object) {
        return getTypeValue(object, valueType);
    }

    /**
     *
     * @param object - Selected component
     * @param index - Component id
     * @return typeValue
     */
    @Override
    public final String getIdValue(final Object object, final int index) {
        return getTypeValue(object, idType).toString();
    }

    /**
     *
     * @param object - Selected component
     * @param type - Component type
     * @return property
     */
    private Object getTypeValue(final Object object, final String type) {
        try {
            return BeanUtils.getProperty(object, type);
        } catch (Exception err) {
            return object.toString();
        }
    }
}
