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
import org.apache.wicket.IClusterable;

/**
 * FieldBean
 */
public class FieldBean implements Serializable, IClusterable {

    private static final long serialVersionUID = -4207102499108894079L;

    private String fieldItem;

    public final String getFieldItem() {
        return fieldItem;
    }

    public final void setFieldItem(final String fieldItem) {
        this.fieldItem = fieldItem;
    }
}
