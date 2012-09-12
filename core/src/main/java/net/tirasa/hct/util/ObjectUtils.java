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
package net.tirasa.hct.util;

import javax.jcr.Node;
import net.tirasa.hct.repository.HCTConnManager;
import org.hippoecm.hst.content.beans.ObjectBeanManagerException;
import org.hippoecm.hst.content.beans.standard.HippoItem;

public class ObjectUtils {

    private ObjectUtils() {
    }

    public HippoItem getHippoItem(final HCTConnManager connManager, final Node node)
            throws ObjectBeanManagerException {

        return getHippoItem(connManager, node, HippoItem.class);
    }

    public static <T extends HippoItem> T getHippoItem(final HCTConnManager connManager, final Node node,
            final Class<T> clazz) throws ObjectBeanManagerException {

        final T result = (T) connManager.getObjConv().getObject(node);
        if (result == null) {
            throw new IllegalArgumentException("No object found for node " + node);
        }

        return result;
    }

    public static HippoItem getHippoItem(final HCTConnManager connManager, final String path)
            throws ObjectBeanManagerException {

        return getHippoItem(connManager, path, HippoItem.class);
    }

    public static <T extends HippoItem> T getHippoItem(final HCTConnManager connManager, final String path,
            final Class<T> clazz) throws ObjectBeanManagerException {

        final T result = (T) connManager.getObjMan().getObject(path);
        if (result == null) {
            throw new IllegalArgumentException("No object found at " + path);
        }

        return result;
    }

    public static HippoItem getHippoItemByUuid(final HCTConnManager connManager, final String uuid)
            throws ObjectBeanManagerException {

        return getHippoItemByUuid(connManager, uuid, HippoItem.class);
    }

    public static <T extends HippoItem> T getHippoItemByUuid(final HCTConnManager connManager, final String uuid,
            final Class<T> clazz) throws ObjectBeanManagerException {

        final T result = (T) connManager.getObjMan().getObjectByUuid(uuid);
        if (result == null) {
            throw new IllegalArgumentException("No object found with UUID " + uuid);
        }

        return result;
    }
}
