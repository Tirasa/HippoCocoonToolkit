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
package net.tirasa.hct.cocoon.cache;

import net.tirasa.hct.cocoon.sax.Constants.Availability;
import org.apache.cocoon.pipeline.caching.AbstractCacheKey;
import org.apache.cocoon.pipeline.caching.CacheKey;
import org.apache.cocoon.pipeline.util.StringRepresentation;
import org.apache.cocoon.util.murmurhash.MurmurHashCodeBuilder;

public class AvailabilityCacheKey extends AbstractCacheKey {

    private final Availability availability;

    public AvailabilityCacheKey(Availability availability) {
        this.availability = availability;
    }

    public Availability getAvailability() {
        return availability;
    }

    @Override
    public boolean isValid(CacheKey cacheKey) {
        if (!(cacheKey instanceof AvailabilityCacheKey) || !this.equals(cacheKey)) {
            return false;
        }

        final AvailabilityCacheKey other = (AvailabilityCacheKey) cacheKey;
        return this.availability == other.availability;
    }

    /**
     * Never expires.
     *
     * @return 0 - never expires
     */
    @Override
    public long getLastModified() {
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AvailabilityCacheKey other = (AvailabilityCacheKey) obj;
        if (this.availability != other.availability) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return new MurmurHashCodeBuilder().append(this.getClass().getName()).
                append(this.availability.name()).toHashCode();
    }

    @Override
    public String toString() {
        return StringRepresentation.buildString(this, "availability=" + this.availability);
    }
}
