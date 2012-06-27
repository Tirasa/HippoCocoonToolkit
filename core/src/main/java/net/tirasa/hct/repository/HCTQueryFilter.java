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
package net.tirasa.hct.repository;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.jcr.RepositoryException;
import net.tirasa.hct.cocoon.sax.Constants;
import net.tirasa.hct.cocoon.sax.Constants.Attribute;
import net.tirasa.hct.cocoon.sax.Constants.Element;
import net.tirasa.hct.cocoon.sax.Constants.State;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.value.ValueFactoryImpl;
import org.xml.sax.Attributes;

public class HCTQueryFilter {

    private final List<String> andConds = new ArrayList<String>();

    private final List<String> orConds = new ArrayList<String>();

    private final Map<HCTDocumentChildNode, ChildQueryFilter> childConds =
            new HashMap<HCTDocumentChildNode, ChildQueryFilter>();

    public void addCond(final State state, final Element element, final Attributes atts) throws RepositoryException {
        if (state != State.INSIDE_FILTER_AND && state != State.INSIDE_FILTER_OR) {
            throw new IllegalArgumentException(
                    "Only " + State.INSIDE_FILTER_AND + " and " + State.INSIDE_FILTER_OR + " are allowed");
        }

        if (!Constants.FILTER_ELEMENTS.contains(element)) {
            throw new IllegalArgumentException(element + " not in " + Constants.FILTER_ELEMENTS);
        }

        String childName = atts.getValue(Constants.Attribute.CHILD_NAME.getName());
        String childType = atts.getValue(Constants.Attribute.CHILD_TYPE.getName());
        HCTDocumentChildNode child = null;
        if (!StringUtils.isBlank(childName) && !StringUtils.isBlank(childType)) {
            child = new HCTDocumentChildNode(childName, childType);

            if (!childConds.containsKey(child)) {
                childConds.put(child, new ChildQueryFilter());
            }
        }

        String selector = child == null ? Constants.QUERY_DEFAULT_SELECTOR : child.getSelector();

        String condition = null;
        switch (element) {
            case EQUALTO:
                condition = getEqualTo(atts, selector);
                break;

            case NOT_EQUALTO:
                condition = getNotEqualTo(atts, selector);
                break;

            case CONTAINS:
                condition = getContains(atts, selector);
                break;

            case NOT_CONTAINS:
                condition = getNotContains(atts, selector);
                break;

            case LIKE:
                condition = getLike(atts, selector);
                break;

            case NOT_LIKE:
                condition = getNotLike(atts, selector);
                break;

            case ISNULL:
                condition = getIsNull(atts, selector);
                break;

            case NOT_NULL:
                condition = getNotNull(atts, selector);
                break;

            case GREATER_OR_EQUAL:
                condition = getGreaterOrEqualThan(atts, selector);
                break;

            case GREATER:
                condition = getGreaterThan(atts, selector);
                break;

            case LESS_OR_EQUAL:
                condition = getLessOrEqualThan(atts, selector);
                break;

            case LESS:
                condition = getLessThan(atts, selector);
                break;

            default:
        }

        if (condition == null) {
            throw new IllegalArgumentException("Could not build condition from " + element);
        }

        if (state == State.INSIDE_FILTER_AND) {
            if (child == null) {
                andConds.add(condition);
            } else {
                childConds.get(child).getAndConds().add(condition);
            }
        }
        if (state == State.INSIDE_FILTER_OR) {
            if (child == null) {
                orConds.add(condition);
            } else {
                childConds.get(child).getOrConds().add(condition);
            }
        }
    }

    private String buildComparableValue(final Attributes atts) throws RepositoryException {
        String propertyTypeString = atts.getValue(Constants.Attribute.TYPE.getName());
        if (StringUtils.isBlank(propertyTypeString)) {
            propertyTypeString = Constants.PropertyType.STRING.name();
        }
        Constants.PropertyType propertyType = Constants.PropertyType.valueOf(propertyTypeString);

        String result;
        switch (propertyType) {
            case BOOLEAN:
            case DOUBLE:
            case LONG:
                result = "CAST('" + atts.getValue(Attribute.VALUE.getName()) + "' AS " + propertyType.name() + ")";
                break;

            case DATE:
                String value = atts.getValue(Attribute.VALUE.getName());
                if (Constants.QUERY_FUNCTION_NOW.equals(value)) {
                    value = ValueFactoryImpl.getInstance().createValue(Calendar.getInstance()).getString();
                }

                result = "CAST('" + value + "' AS " + propertyType.name() + ")";
                break;

            case STRING:
            default:
                result = "'" + atts.getValue(Attribute.VALUE.getName()) + "'";
        }

        return result;
    }

    private String getEqualTo(final Attributes atts, final String selector) throws RepositoryException {
        return new StringBuilder().append(selector).append('.').append('[').
                append(atts.getValue(Attribute.FIELD.getName())).append(']').
                append(" = ").append(buildComparableValue(atts)).toString();
    }

    private String getNotEqualTo(final Attributes atts, final String selector) throws RepositoryException {
        return new StringBuilder().append(selector).append('.').append('[').
                append(atts.getValue(Attribute.FIELD.getName())).append(']').
                append(" <> ").append(buildComparableValue(atts)).toString();
    }

    private String getContains(final Attributes atts, final String selector) {
        StringBuilder result = new StringBuilder().append("CONTAINS(").append(selector).append('.');

        String field = atts.getValue(Attribute.FIELD.getName());
        if (!StringUtils.isBlank(field)) {
            result.append('*');
        } else {
            result.append('[').append(atts.getValue(Attribute.FIELD.getName())).append(']');
        }

        result.append(", '").append(atts.getValue(Attribute.VALUE.getName())).append("')");

        return result.toString();
    }

    private String getNotContains(final Attributes atts, final String selector) {
        return new StringBuilder().append("NOT ").append(getContains(atts, selector)).toString();
    }

    private String getLike(final Attributes atts, final String selector) {
        return new StringBuilder().append(selector).append('.').append('[').
                append(atts.getValue(Attribute.FIELD.getName())).append(']').
                append(" LIKE '%").append(atts.getValue(Attribute.VALUE.getName())).append("%'").toString();
    }

    private String getNotLike(final Attributes atts, final String selector) {
        return new StringBuilder().append("NOT ").append(getLike(atts, selector)).toString();
    }

    private String getIsNull(final Attributes atts, final String selector) {
        return new StringBuilder().append(selector).append('.').append('[').
                append(atts.getValue(Attribute.FIELD.getName())).append(']').
                append(" IS NULL").toString();
    }

    private String getNotNull(final Attributes atts, final String selector) {
        return new StringBuilder().append("NOT ").append(getIsNull(atts, selector)).toString();
    }

    private String getGreaterOrEqualThan(final Attributes atts, final String selector) throws RepositoryException {
        return new StringBuilder().append(selector).append('.').append('[').
                append(atts.getValue(Attribute.FIELD.getName())).append(']').
                append(" >= ").append(buildComparableValue(atts)).toString();
    }

    private String getGreaterThan(final Attributes atts, final String selector) throws RepositoryException {
        return new StringBuilder().append(selector).append('.').append('[').
                append(atts.getValue(Attribute.FIELD.getName())).append(']').
                append(" > ").append(buildComparableValue(atts)).toString();
    }

    private String getLessOrEqualThan(final Attributes atts, final String selector) throws RepositoryException {
        return new StringBuilder().append(selector).append('.').append('[').
                append(atts.getValue(Attribute.FIELD.getName())).append(']').
                append(" <= ").append(buildComparableValue(atts)).toString();
    }

    private String getLessThan(final Attributes atts, final String selector) throws RepositoryException {
        return new StringBuilder().append(selector).append('.').append('[').
                append(atts.getValue(Attribute.FIELD.getName())).append(']').
                append(" < ").append(buildComparableValue(atts)).toString();
    }

    public List<String> getAndConds() {
        return andConds;
    }

    public List<String> getOrConds() {
        return orConds;
    }

    public Map<HCTDocumentChildNode, ChildQueryFilter> getChildConds() {
        return childConds;
    }

    public class ChildQueryFilter {

        private final List<String> andConds = new ArrayList<String>();

        private final List<String> orConds = new ArrayList<String>();

        public List<String> getAndConds() {
            return andConds;
        }

        public List<String> getOrConds() {
            return orConds;
        }
    }
}
