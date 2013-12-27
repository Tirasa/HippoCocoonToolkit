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
package net.tirasa.hct.editor.forms;

import java.io.Serializable;

public class Component implements Serializable {

    private static final long serialVersionUID = 6780971966155213684L;

    /**
     * Component Id.
     */
    private Integer id;

    /**
     * Component name.
     */
    private String name;

    /**
     * Component Type constructor.
     *
     * @param idType - component idType
     * @param idName - component idName
     */
    public Component(final Integer idType, final String idName) {
        this.id = idType;
        this.name = idName;
    }

    /**
     *
     * @return id
     */
    public final Integer getId() {
        return id;
    }

    /**
     *
     * @param idType - component idType
     */
    public final void setId(final Integer idType) {
        this.id = idType;
    }

    /**
     *
     * @return name - Component Name
     */
    public final String getName() {
        return name;
    }

    /**
     *
     * @param idName - Component Name
     */
    public final void setName(final String idName) {
        this.name = idName;
    }
}
