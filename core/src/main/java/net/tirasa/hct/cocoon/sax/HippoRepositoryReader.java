/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package net.tirasa.hct.cocoon.sax;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import javax.jcr.Node;
import org.apache.cocoon.pipeline.ProcessingException;
import org.apache.cocoon.pipeline.caching.CacheKey;
import org.apache.cocoon.pipeline.component.CachingPipelineComponent;
import org.apache.cocoon.sitemap.component.AbstractReader;
import org.hippoecm.hst.content.beans.ObjectBeanManagerException;
import org.hippoecm.hst.content.beans.standard.HippoAsset;
import org.hippoecm.hst.content.beans.standard.HippoGalleryImageBean;
import org.hippoecm.hst.content.beans.standard.HippoGalleryImageSet;
import org.hippoecm.hst.content.beans.standard.HippoResource;
import org.hippoecm.hst.servlet.utils.ResourceUtils;
import net.tirasa.hct.repository.HCTConnManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HippoRepositoryReader extends AbstractReader implements CachingPipelineComponent {

    private enum ImageType {

        thumbnail,
        original

    }

    private static final Logger LOG = LoggerFactory.getLogger(HippoRepositoryReader.class);

    private transient HCTConnManager connManager;

    private transient String contentType;

    public HippoRepositoryReader() {
        super();
    }

    public HippoRepositoryReader(final URL source) {
        super(source);
    }

    @Override
    public void setConfiguration(final Map<String, ? extends Object> configuration) {
        this.setup((Map<String, Object>) configuration);
    }

    @Override
    public void setup(final Map<String, Object> parameters) {
        if (parameters == null) {
            return;
        }
        if (parameters.containsKey("source")) {
            super.setSource((URL) parameters.get("source"));
        }

        if (connManager == null) {
            synchronized (this) {
                connManager = HCTConnManager.getBinaryInstance();
            }
        }
    }

    @Override
    public void finish() {
        if (connManager.getSession() != null) {
            connManager.getSession().logout();
        }

        super.finish();
    }

    private void readFromRepositoryAndWriteToOutputStream(final Node node) {
        if (node == null) {
            throw new ProcessingException("Null Node to be read");
        }

        BufferedInputStream repositoryIS = null;
        try {
            contentType = node.getProperty(ResourceUtils.DEFAULT_BINARY_MIME_TYPE_PROP_NAME).getString();

            repositoryIS = new BufferedInputStream(node.getProperty(
                    ResourceUtils.DEFAULT_BINARY_DATA_PROP_NAME).getBinary().getStream());
            final byte[] buffer = new byte[1024];
            while (repositoryIS.available() != 0) {
                final int len = repositoryIS.read(buffer);
                this.outputStream.write(buffer, 0, len);
            }
        } catch (Exception e) {
            throw new ProcessingException("While reading node", e);
        } finally {
            if (repositoryIS != null) {
                try {
                    repositoryIS.close();
                } catch (IOException ioe) {
                    LOG.error("While closing InputSTream", ioe);
                }
            }
        }
    }

    private Node getImageNode(final Object obj, final ImageType type) {
        final HippoGalleryImageSet img = (HippoGalleryImageSet) obj;
        final HippoGalleryImageBean actualImg = type == ImageType.original
                ? img.getOriginal() : img.getThumbnail();

        return actualImg.getNode();
    }

    @Override
    public void execute() {
        if (this.source == null) {
            throw new IllegalArgumentException(getClass().getSimpleName() + " has no source configured to read from.");
        }

        // Take original URL from the file:/ for to an absolute path in the
        // Hippo repository
        String stringSource = source.toExternalForm().substring(source.toExternalForm().indexOf(':') + 1);

        // Try to guess if this is an image (and adapt path on 
        // repository accordingly
        final ImageType imageType = stringSource.endsWith(":" + ImageType.thumbnail.name())
                ? ImageType.thumbnail : ImageType.original;
        if (stringSource.endsWith(":" + imageType.name())) {
            LOG.debug("Selected image type is {}", imageType);

            stringSource = stringSource.substring(0, stringSource.lastIndexOf(':'));
        }

        Object obj;
        try {
            obj = connManager.getObjMan().getObject(stringSource);
            if (obj == null) {
                throw new HippoRepositoryNotFoundException("Could not read at " + source);
            }
        } catch (ObjectBeanManagerException e) {
            throw new ProcessingException("While reading image", e);
        }

        Node node = null;
        if (obj instanceof HippoGalleryImageSet) {
            node = getImageNode(obj, imageType);
        } else if (obj instanceof HippoAsset) {
            final List<HippoResource> resources = ((HippoAsset) obj).getChildBeans(HippoResource.class);
            if (resources != null && !resources.isEmpty()) {
                node = resources.get(0).getNode();
            }
        }
        readFromRepositoryAndWriteToOutputStream(node);
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public CacheKey constructCacheKey() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
