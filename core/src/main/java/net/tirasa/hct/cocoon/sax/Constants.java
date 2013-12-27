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
package net.tirasa.hct.cocoon.sax;

import java.util.EnumSet;
import org.apache.commons.lang3.StringUtils;
import org.hippoecm.hst.content.beans.standard.HippoAsset;
import org.hippoecm.hst.content.beans.standard.HippoAvailableTranslationsBean;
import org.hippoecm.hst.content.beans.standard.HippoDirectory;
import org.hippoecm.hst.content.beans.standard.HippoDocument;
import org.hippoecm.hst.content.beans.standard.HippoFacetSelect;
import org.hippoecm.hst.content.beans.standard.HippoFixedDirectory;
import org.hippoecm.hst.content.beans.standard.HippoFolder;
import org.hippoecm.hst.content.beans.standard.HippoGalleryImage;
import org.hippoecm.hst.content.beans.standard.HippoGalleryImageSet;
import org.hippoecm.hst.content.beans.standard.HippoHtml;
import org.hippoecm.hst.content.beans.standard.HippoMirror;
import org.hippoecm.hst.content.beans.standard.HippoResource;
import org.hippoecm.hst.content.beans.standard.HippoStdPubWfRequest;
import org.hippoecm.hst.content.beans.standard.HippoTranslation;
import org.hippoecm.hst.content.beans.standard.facetnavigation.HippoFacetNavigation;
import org.hippoecm.hst.content.beans.standard.facetnavigation.HippoFacetResult;
import org.hippoecm.hst.content.beans.standard.facetnavigation.HippoFacetSearch;
import org.hippoecm.hst.content.beans.standard.facetnavigation.HippoFacetSubNavigation;
import org.hippoecm.hst.content.beans.standard.facetnavigation.HippoFacetsAvailableNavigation;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

public class Constants {

    public static enum Availability {

        live,
        preview

    }

    public static enum State {

        OUTSIDE,
        INSIDE_QUERY,
        INSIDE_FILTER,
        INSIDE_FILTER_AND,
        INSIDE_FILTER_OR,
        INSIDE_ORDERBY,
        INSIDE_RETURN

    }

    public static enum Element {

        DOCUMENT("document"),
        COMPOUNDS("compounds"),
        COMPOUND("compound"),
        FOLDERS("folders"),
        FOLDER("folder"),
        QUERY("query"),
        RETURN("return"),
        QUERY_RESULT("queryResult"),
        BASE("base"),
        FILTER("filter"),
        AND("and"),
        OR("or"),
        EQUALTO("equalTo"),
        NOT_EQUALTO("notEqualTo"),
        GREATER_OR_EQUAL("greaterOrEqual"),
        GREATER("greater"),
        ISNULL("isNull"),
        NOT_NULL("notNull"),
        LESS_OR_EQUAL("lessOrEqual"),
        LESS("less"),
        LIKE("like"),
        NOT_LIKE("notLike"),
        CONTAINS("contains"),
        NOT_CONTAINS("notContains"),
        ORDERBY("orderBy"),
        DESCENDING("descending"),
        ASCENDING("ascending"),
        VALUE("value"),
        TRANSLATIONS("translations"),
        TRANSLATION("translation"),
        RELATED_DOCS("relatedDocs"),
        FIELD("field"),
        IMAGES("images"),
        IMAGE("image"),
        ASSETS("assets"),
        ASSET("asset"),
        LINKS("links"),
        LINK("link"),
        TAXONOMIES("taxonomies"),
        TAXONOMY("taxonomy"),
        TAGS("tags"),
        TAG("tag");

        private String name;

        Element(final String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public static Element fromName(final String name) {
            if (StringUtils.isBlank(name)) {
                throw new IllegalArgumentException("Empty element name");
            }

            Element result = null;
            for (Element element : values()) {
                if (name.equals(element.getName())) {
                    result = element;
                }
            }

            if (result == null) {
                throw new IllegalArgumentException("Unexpected element name: " + name);
            }

            return result;

        }
    }

    public static final EnumSet<Element> FILTER_ELEMENTS = EnumSet.of(
            Element.EQUALTO, Element.NOT_EQUALTO, Element.CONTAINS, Element.NOT_CONTAINS,
            Element.LIKE, Element.NOT_LIKE, Element.ISNULL, Element.NOT_NULL,
            Element.GREATER_OR_EQUAL, Element.GREATER, Element.LESS_OR_EQUAL, Element.LESS);

    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    public static final Attributes EMPTY_ATTRS = new AttributesImpl();

    public static final String XSD_STRING = "xsd:string";

    public static final String XSD_INT = "xsd:integer";

    public static final String XSD_LONG = "xsd:long";

    public static final String XSD_DATETIME = "xsd:dateTime";

    public static final String NS_EMPTY = "";

    public static final String NS_HCT = "http://www.tirasa.net/hct/1.0";

    public static final String PREFIX_HCT = "hct";

    public static final String QUERY_DEFAULT_SELECTOR = "type";

    /**
     * Duplicate of ObjectConverterUtils, since there this field is private.
     *
     * @see org.hippoecm.hst.util.ObjectConverterUtils
     */
    public static final Class<?>[] DEFAULT_BUILT_IN_MAPPING_CLASSES = {
        HippoDocument.class,
        HippoFolder.class,
        HippoMirror.class,
        HippoFacetSelect.class,
        HippoDirectory.class,
        HippoFixedDirectory.class,
        HippoHtml.class,
        HippoResource.class,
        HippoStdPubWfRequest.class,
        HippoAsset.class,
        HippoGalleryImageSet.class,
        HippoGalleryImage.class,
        HippoTranslation.class,
        // facet navigation parts:
        HippoFacetSearch.class,
        HippoFacetNavigation.class,
        HippoFacetsAvailableNavigation.class,
        HippoFacetSubNavigation.class,
        HippoFacetResult.class,
        HippoAvailableTranslationsBean.class
    };

    public static enum Attribute {

        PATH("path"),
        UUID("uuid"),
        BASE("base"),
        TYPE("type"),
        SIZE("size"),
        PAGE("page"),
        TOTAL_PAGES("totalPages"),
        DEPTH("depth"),
        FIELD("field"),
        VALUE("value"),
        CHILD_NAME("childName"),
        CHILD_TYPE("childType"),
        LEFT("left"),
        RIGHT("right"),
        NAME("name"),
        DESC("description"),
        LOCALE("locale"),
        LOC_NAME("localizedName"),
        ORDER("order"),
        HEIGHT("height"),
        WIDTH("width"),
        MIMETYPE("mimeType"),
        SIZE_KB("sizeKb"),
        LAST_MOD("lastModified"),
        INCLUDE_FOLDERS("includeFolders"),
        DATE_FORMAT("dateFormat");

        private String name;

        Attribute(final String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public static enum PropertyType {

        STRING(javax.jcr.PropertyType.STRING),
        LONG(javax.jcr.PropertyType.LONG),
        DOUBLE(javax.jcr.PropertyType.DOUBLE),
        BOOLEAN(javax.jcr.PropertyType.BOOLEAN),
        DATE(javax.jcr.PropertyType.DATE);

        private int id;

        PropertyType(final int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

    static public class StartEndDocumentFilter extends XMLFilterImpl {

        public StartEndDocumentFilter(final XMLReader xmlReader) {
            super(xmlReader);
        }

        @Override
        public void startDocument()
                throws SAXException {
        }

        @Override
        public void endDocument()
                throws SAXException {
        }
    }
}
