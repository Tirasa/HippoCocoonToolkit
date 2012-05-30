/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.onehippo.forge.hct.repository;

import javax.jcr.Credentials;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import org.apache.cocoon.pipeline.SetupException;
import org.apache.commons.lang3.ArrayUtils;
import org.hippoecm.hst.component.support.spring.util.MetadataReaderClasspathResourceScanner;
import org.hippoecm.hst.content.beans.manager.ObjectBeanManager;
import org.hippoecm.hst.content.beans.manager.ObjectBeanManagerImpl;
import org.hippoecm.hst.content.beans.manager.ObjectConverter;
import org.hippoecm.hst.content.beans.standard.HippoBean;
import org.hippoecm.hst.site.HstServices;
import org.hippoecm.hst.util.ObjectConverterUtils;
import org.onehippo.forge.hct.cocoon.sax.Constants;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public final class HCTConnManager {

    private final transient Repository repository;

    private final transient Session session;

    private final transient ObjectBeanManager objMan;

    private final transient ObjectConverter objConv;

    public static HCTConnManager getContentInstance() {
        return new HCTConnManager(Credentials.class.getName() + ".default");
    }

    public static HCTConnManager getBinaryInstance() {
        return new HCTConnManager(Credentials.class.getName() + ".binaries");
    }

    private HCTConnManager(final String componentName) {
        if (!HstServices.isAvailable()) {
            throw new SetupException("HstServices not available");
        }

        final Credentials credentials = HstServices.getComponentManager().getComponent(componentName);
        repository = HstServices.getComponentManager().getComponent(Repository.class.getName());

        try {
            session = repository.login(credentials);
        } catch (RepositoryException e) {
            throw new SetupException("While log in to the Hippo repository", e);
        }

        final MetadataReaderClasspathResourceScanner scanner = new MetadataReaderClasspathResourceScanner();
        scanner.setResourceLoader(new ClassPathXmlApplicationContext());
        try {
            final String[] fallbackNodeTypes = (String[]) ArrayUtils.add(
                    ObjectConverterUtils.getDefaultFallbackNodeTypes(), "hippo:compound");

            objConv = ObjectConverterUtils.createObjectConverter(
                    ObjectConverterUtils.getAnnotatedClasses(scanner,
                    "classpath*:org/onehippo/forge/hct/hstbeans/**/*.class"),
                    (Class<? extends HippoBean>[]) Constants.DEFAULT_BUILT_IN_MAPPING_CLASSES,
                    fallbackNodeTypes, false);
            objMan = new ObjectBeanManagerImpl(session, objConv);
        } catch (Exception e) {
            throw new SetupException("While creating HST ObjectManager", e);
        }
    }

    public ObjectConverter getObjConv() {
        return objConv;
    }

    public ObjectBeanManager getObjMan() {
        return objMan;
    }

    public Repository getRepository() {
        return repository;
    }

    public Session getSession() {
        return session;
    }
}
