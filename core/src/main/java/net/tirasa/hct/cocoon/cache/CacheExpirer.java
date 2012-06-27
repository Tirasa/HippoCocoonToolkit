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
package net.tirasa.hct.cocoon.cache;

import java.util.HashSet;
import java.util.Set;
import org.apache.cocoon.pipeline.caching.Cache;
import org.apache.cocoon.pipeline.caching.CacheKey;
import org.apache.cocoon.rest.controller.annotation.RESTController;
import org.apache.cocoon.rest.controller.annotation.SitemapParameter;
import org.apache.cocoon.rest.controller.method.Get;
import org.apache.cocoon.rest.controller.response.RestResponse;
import org.apache.cocoon.rest.controller.response.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

@RESTController
public class CacheExpirer implements Get {

    private static final Logger LOG = LoggerFactory.getLogger(CacheExpirer.class);

    @SitemapParameter
    private String availability;

    @SitemapParameter
    private String path;

    @Autowired
    private Cache cache;

    @Override
    public RestResponse doGet() throws Exception {
        if (availability == null || path == null) {
            LOG.error("Availability {} and path {} cannot be null", availability, path);
            return new Status(HttpStatus.BAD_REQUEST.value());
        }

        LOG.debug("Expiring {} {}", availability, path);

        Set<CacheKey> expired = new HashSet<CacheKey>();
        for (CacheKey key : cache.keySet()) {
            System.out.println("UUUUUUUUUUUUUUU " + key);
        }

        LOG.debug("Cache keys to remove {}", expired);

        for (CacheKey key : expired) {
            cache.remove(key);
        }

        return new Status(HttpStatus.OK.value());
    }
}
