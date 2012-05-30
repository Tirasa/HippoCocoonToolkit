/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onehippo.forge.hct.repository;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.onehippo.forge.hct.cocoon.sax.Constants;
import org.onehippo.forge.hct.cocoon.sax.Constants.Attribute;
import org.onehippo.forge.hct.cocoon.sax.Constants.Element;
import org.onehippo.forge.hct.cocoon.sax.Constants.State;
import org.xml.sax.Attributes;

public class HCTQueryFilter {

    private final List<String> andConds = new ArrayList<String>();

    private final List<String> orConds = new ArrayList<String>();

    public void addCond(final State state, final Element element, final Attributes atts) {
        if (state != State.INSIDE_FILTER_AND && state != State.INSIDE_FILTER_OR) {
            throw new IllegalArgumentException(
                    "Only " + State.INSIDE_FILTER_AND + " and " + State.INSIDE_FILTER_OR + " are allowed");
        }

        if (!Constants.FILTER_ELEMENTS.contains(element)) {
            throw new IllegalArgumentException(element + " not in " + Constants.FILTER_ELEMENTS);
        }

        String condition = null;
        switch (element) {
            case EQUALTO:
                condition = getEqualTo(atts);
                break;

            case NOT_EQUALTO:
                condition = getNotEqualTo(atts);
                break;

            case CONTAINS:
                condition = getContains(atts);
                break;

            case NOT_CONTAINS:
                condition = getNotContains(atts);
                break;

            case LIKE:
                condition = getLike(atts);
                break;

            case NOT_LIKE:
                condition = getNotLike(atts);
                break;

            case ISNULL:
                condition = getIsNull(atts);
                break;

            case NOT_NULL:
                condition = getNotNull(atts);
                break;

            case GREATER_OR_EQUAL:
                condition = getGreaterOrEqualThan(atts);
                break;

            case GREATER:
                condition = getGreaterThan(atts);
                break;

            case LESS_OR_EQUAL:
                condition = getLessOrEqualThan(atts);
                break;

            case LESS:
                condition = getLessThan(atts);
                break;

            default:
        }

        if (condition != null && state == State.INSIDE_FILTER_AND) {
            andConds.add(condition);
        }
        if (condition != null && state == State.INSIDE_FILTER_OR) {
            orConds.add(condition);
        }
    }

    private String getEqualTo(final Attributes atts) {
        return new StringBuilder().append('[').append(atts.getValue(Attribute.FIELD.getName())).append(']').
                append(" = '").append(atts.getValue(Attribute.VALUE.getName())).append('\'').toString();
    }

    private String getNotEqualTo(final Attributes atts) {
        return new StringBuilder().append('[').append(atts.getValue(Attribute.FIELD.getName())).append(']').
                append(" <> '").append(atts.getValue(Attribute.VALUE.getName())).append('\'').toString();
    }

    private String getContains(final Attributes atts) {
        StringBuilder result = new StringBuilder().append("CONTAINS(");

        String field = atts.getValue(Attribute.FIELD.getName());
        if (StringUtils.isBlank(field)) {
            result.append("type.*");
        } else {
            result.append('[').append(atts.getValue(Attribute.FIELD.getName())).append(']');
        }

        result.append(", '").append(atts.getValue(Attribute.VALUE.getName())).append("')");

        return result.toString();
    }

    private String getNotContains(final Attributes atts) {
        return new StringBuilder().append("NOT ").append(getContains(atts)).toString();
    }

    private String getLike(final Attributes atts) {
        return new StringBuilder().append('[').append(atts.getValue(Attribute.FIELD.getName())).append(']').
                append(" LIKE '%").append(atts.getValue(Attribute.VALUE.getName())).append("%'").toString();
    }

    private String getNotLike(final Attributes atts) {
        return new StringBuilder().append("NOT ").append(getLike(atts)).toString();
    }

    private String getIsNull(final Attributes atts) {
        return new StringBuilder().append('[').append(atts.getValue(Attribute.FIELD.getName())).append(']').
                append(" IS NULL").toString();
    }

    private String getNotNull(final Attributes atts) {
        return new StringBuilder().append("NOT ").append(getIsNull(atts)).toString();
    }

    private String getGreaterOrEqualThan(final Attributes atts) {
        return new StringBuilder().append('[').append(atts.getValue(Attribute.FIELD.getName())).append(']').
                append(" >= '").append(atts.getValue(Attribute.VALUE.getName())).append('\'').toString();
    }

    private String getGreaterThan(final Attributes atts) {
        return new StringBuilder().append('[').append(atts.getValue(Attribute.FIELD.getName())).append(']').
                append(" > '").append(atts.getValue(Attribute.VALUE.getName())).append('\'').toString();
    }

    private String getLessOrEqualThan(final Attributes atts) {
        return new StringBuilder().append('[').append(atts.getValue(Attribute.FIELD.getName())).append(']').
                append(" <= '").append(atts.getValue(Attribute.VALUE.getName())).append('\'').toString();
    }

    private String getLessThan(final Attributes atts) {
        return new StringBuilder().append('[').append(atts.getValue(Attribute.FIELD.getName())).append(']').
                append(" < '").append(atts.getValue(Attribute.VALUE.getName())).append('\'').toString();
    }

    public List<String> getAndConds() {
        return andConds;
    }

    public List<String> getOrConds() {
        return orConds;
    }
}
