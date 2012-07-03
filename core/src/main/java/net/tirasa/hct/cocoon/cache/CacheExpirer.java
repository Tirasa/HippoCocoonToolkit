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
import java.util.Map;
import java.util.Set;
import org.apache.cocoon.pipeline.caching.Cache;
import org.apache.cocoon.pipeline.caching.CacheKey;
import org.apache.cocoon.pipeline.caching.CompoundCacheKey;
import org.apache.cocoon.pipeline.caching.ParameterCacheKey;
import org.apache.cocoon.rest.controller.annotation.RESTController;
import org.apache.cocoon.rest.controller.annotation.SitemapParameter;
import org.apache.cocoon.rest.controller.method.Get;
import org.apache.cocoon.rest.controller.response.RestResponse;
import org.apache.cocoon.rest.controller.response.Status;
import org.apache.commons.lang3.LocaleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

@RESTController
public class CacheExpirer implements Get {

    private static final Logger LOG = LoggerFactory.getLogger(CacheExpirer.class);

    @SitemapParameter
    private String locale;

    @SitemapParameter
    private String availability;

    @Autowired
    private Cache cache;

    private boolean findExpiredCacheKeys(final CacheKey cacheKey) {
        if (cacheKey instanceof AvailabilityLocaleCacheKey) {
            AvailabilityLocaleCacheKey alck = (AvailabilityLocaleCacheKey) cacheKey;

            return alck.getAvailability().name().equals(this.availability)
                    && alck.getLocale().equals(LocaleUtils.toLocale(this.locale));
        }
        if (cacheKey instanceof CompoundCacheKey) {
            CompoundCacheKey compound = (CompoundCacheKey) cacheKey;
            boolean found = false;
            for (CacheKey inner : compound.getCacheKeys()) {
                found |= findExpiredCacheKeys(inner);
            }
            return found;
        }
        if (cacheKey instanceof ParameterCacheKey) {
            ParameterCacheKey pck = (ParameterCacheKey) cacheKey;
            Map<String, String> parameters = pck.getParameters();

            return parameters.containsKey("availability") && parameters.get("availability").equals(this.availability)
                    && parameters.containsKey("locale") && parameters.get("locale").equals(this.locale);
        }

        return false;
    }

    @Override
    public RestResponse doGet() throws Exception {
        if (locale == null || availability == null) {
            LOG.error("locale {} and availability {} cannot be null", this.locale, this.availability);
            return new Status(HttpStatus.BAD_REQUEST.value());
        }

        LOG.debug("Expiring {} {}", this.locale, this.availability);

        Set<CacheKey> expired = new HashSet<CacheKey>();
        for (CacheKey key : cache.keySet()) {
            if (findExpiredCacheKeys(key)) {
                expired.add(key);
            }
        }

        LOG.debug("Cache keys to remove #{} {}", expired.size(), expired);

        LOG.debug("# of cache keys before removal: {}", this.cache.keySet().size());
        for (CacheKey key : expired) {
            cache.remove(key);
        }
        LOG.debug("# of cache keys after removal: {}", this.cache.keySet().size());

        return new Status(HttpStatus.OK.value());
    }
}
