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
package net.tirasa.hct.cocoon.sax;

public class HippoRepositoryNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 6954455054874613925L;

    public HippoRepositoryNotFoundException(final String message) {
        super(message);
    }

    public HippoRepositoryNotFoundException(final String message,
            final Throwable cause) {

        super(message, cause);
    }
}
