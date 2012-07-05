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
package net.tirasa.hct.repository.event;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import net.tirasa.hct.cocoon.cache.AvailabilityLocaleCacheKey;
import net.tirasa.hct.cocoon.sax.Constants;
import net.tirasa.hct.util.ApplicationContextProvider;
import org.apache.cocoon.pipeline.caching.Cache;
import org.apache.cocoon.pipeline.caching.CacheKey;
import org.apache.cocoon.pipeline.caching.CompoundCacheKey;
import org.apache.cocoon.pipeline.caching.ParameterCacheKey;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.onehippo.forge.repositoryeventlistener.hst.events.BaseHippoEventSubscriber;
import org.onehippo.forge.repositoryeventlistener.hst.hippo.HippoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CacheExpirerRepositoryEventSubscriber extends BaseHippoEventSubscriber {

    private static final Logger LOG = LoggerFactory.getLogger(CacheExpirerRepositoryEventSubscriber.class);

    @Override
    public String getName() {
        return CacheExpirerRepositoryEventSubscriber.class.getName();
    }

    private boolean findExpiredCacheKeys(final CacheKey cacheKey,
            final Constants.Availability availability, final String locale) {

        if (cacheKey instanceof AvailabilityLocaleCacheKey) {
            AvailabilityLocaleCacheKey alck = (AvailabilityLocaleCacheKey) cacheKey;

            return alck.getAvailability().equals(availability)
                    && alck.getLocale().equals(LocaleUtils.toLocale(locale));
        }
        if (cacheKey instanceof CompoundCacheKey) {
            CompoundCacheKey compound = (CompoundCacheKey) cacheKey;
            boolean found = false;
            for (CacheKey inner : compound.getCacheKeys()) {
                found |= findExpiredCacheKeys(inner, availability, locale);
            }
            return found;
        }
        if (cacheKey instanceof ParameterCacheKey) {
            ParameterCacheKey pck = (ParameterCacheKey) cacheKey;
            Map<String, String> parameters = pck.getParameters();

            return parameters.containsKey("availability") && parameters.get("availability").equals(availability.name())
                    && parameters.containsKey("locale") && parameters.get("locale").equals(locale);
        }

        return false;
    }

    @Override
    public void onEvent(HippoEvent event) {
        LOG.debug("Event '{}' received about {}", event.getType(), event.getPath());

        String locale = null;
        try {
            Node node = getSession().getNodeByIdentifier(event.getIdentifier());
            for (NodeIterator itor = node.getNodes(); itor.hasNext() && locale == null;) {
                locale = itor.nextNode().getProperty("hippotranslation:locale").getString();
            }
        } catch (RepositoryException e) {
            LOG.error("Could not get JCR node for UUID {} or locale property", event.getIdentifier(), e);
        }

        if (StringUtils.isBlank(locale)) {
            return;
        }

        Constants.Availability availability;
        switch (event.getType()) {
            case publish:
            case depublish:
                availability = Constants.Availability.live;
                break;

            case obtainEditableInstance:
            case commitEditableInstance:
            default:
                availability = Constants.Availability.preview;
        }

        LOG.debug("Expiring {} {}", locale, availability);

        Cache cache = ApplicationContextProvider.getApplicationContext().getBean(Cache.class);
        LOG.debug("Cocoon cache obtained: {}", cache);

        Set<CacheKey> expired = new HashSet<CacheKey>();
        for (CacheKey key : cache.keySet()) {
            if (findExpiredCacheKeys(key, availability, locale)) {
                expired.add(key);
            }
        }

        LOG.debug("Cache keys to remove #{} {}", expired.size(), expired);

        LOG.debug("# of cache keys before removal: {}", cache.keySet().size());
        for (CacheKey key : expired) {
            cache.remove(key);
        }
        LOG.debug("# of cache keys after removal: {}", cache.keySet().size());
    }
}
