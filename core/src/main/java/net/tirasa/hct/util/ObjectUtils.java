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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import net.tirasa.hct.repository.HCTConnManager;
import org.hippoecm.hst.content.beans.ObjectBeanManagerException;
import org.hippoecm.hst.content.beans.standard.HippoItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObjectUtils {

    private static final Logger LOG = LoggerFactory.getLogger(ObjectUtils.class);

    private ObjectUtils() {
    }

    private static <T extends HippoItem> T returnHippoItem(final T item, final String location) {
        if (item == null) {
            LOG.error("Found null item at {}", location);
        }
        return item;
    }

    public static HippoItem getHippoItem(final HCTConnManager connManager, final Node node)
            throws ObjectBeanManagerException {

        return getHippoItem(connManager, node, HippoItem.class);
    }

    @SuppressWarnings("unchecked")
    public static <T extends HippoItem> T getHippoItem(final HCTConnManager connManager, final Node node,
            final Class<T> clazz) throws ObjectBeanManagerException {

        String path = null;
        try {
            path = node.getPath();
        } catch (RepositoryException e) {
            LOG.error("Cannot read path for {}", node, e);
        }
        LOG.debug("About to return HippoItem <{}> for node {}", clazz.getName(), path);

        return returnHippoItem((T) connManager.getObjConv().getObject(node), path);
    }

    public static HippoItem getHippoItem(final HCTConnManager connManager, final String path)
            throws ObjectBeanManagerException {

        return getHippoItem(connManager, path, HippoItem.class);
    }

    @SuppressWarnings("unchecked")
    public static <T extends HippoItem> T getHippoItem(final HCTConnManager connManager, final String path,
            final Class<T> clazz) throws ObjectBeanManagerException {

        LOG.debug("About to return HippoItem <{}> for path {}", clazz.getName(), path);

        T result = null;
        try {
            result = (T) connManager.getObjMan().getObject(URLDecoder.decode(path, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            LOG.error("Couldn't decode {}", path, e);
        }

        return returnHippoItem(result, path);
    }

    public static HippoItem getHippoItemByUuid(final HCTConnManager connManager, final String uuid)
            throws ObjectBeanManagerException {

        return getHippoItemByUuid(connManager, uuid, HippoItem.class);
    }

    @SuppressWarnings("unchecked")
    public static <T extends HippoItem> T getHippoItemByUuid(final HCTConnManager connManager, final String uuid,
            final Class<T> clazz) throws ObjectBeanManagerException {

        LOG.debug("About to return HippoItem <{}> for uuid {}", clazz.getName(), uuid);

        return returnHippoItem((T) connManager.getObjMan().getObjectByUuid(uuid), uuid);

    }
}
