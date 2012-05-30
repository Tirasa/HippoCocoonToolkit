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
package org.onehippo.forge.hct.cocoon.sax;

import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import javax.jcr.RepositoryException;
import org.apache.cocoon.sax.SAXConsumer;
import org.apache.cocoon.sax.util.XMLUtils;
import org.apache.commons.collections.keyvalue.DefaultMapEntry;
import org.apache.commons.lang3.StringUtils;
import org.hippoecm.hst.content.beans.ObjectBeanManagerException;
import org.hippoecm.hst.content.beans.manager.ObjectBeanManager;
import org.hippoecm.hst.content.beans.standard.HippoAsset;
import org.hippoecm.hst.content.beans.standard.HippoDocument;
import org.hippoecm.hst.content.beans.standard.HippoFacetSelect;
import org.hippoecm.hst.content.beans.standard.HippoFolder;
import org.hippoecm.hst.content.beans.standard.HippoGalleryImageSet;
import org.hippoecm.hst.content.beans.standard.HippoHtml;
import org.hippoecm.hst.content.beans.standard.HippoItem;
import org.hippoecm.repository.api.HippoNodeType;
import org.onehippo.forge.ecmtagging.TaggingNodeType;
import static org.onehippo.forge.hct.cocoon.sax.Constants.*;
import org.onehippo.forge.hct.cocoon.sax.Constants.Attribute;
import org.onehippo.forge.hct.cocoon.sax.Constants.Element;
import org.onehippo.forge.hct.cocoon.sax.Constants.StartEndDocumentFilter;
import org.onehippo.forge.hct.hstbeans.HCTTaxonomyCategoryBean;
import org.onehippo.forge.hct.hstbeans.HippoDate;
import org.onehippo.forge.hct.hstbeans.ImageLinkBean;
import org.onehippo.forge.hct.hstbeans.RelatedDocs;
import org.onehippo.forge.hct.repository.HCTConnManager;
import org.onehippo.forge.hct.repository.HCTQuery;
import org.onehippo.forge.hct.repository.HCTQueryResult;
import org.onehippo.forge.hct.util.TaxonomyUtils;
import org.onehippo.taxonomy.api.TaxonomyNodeTypes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;

public class HippoItemXMLDumper {

    final private transient SAXConsumer saxConsumer;

    public HippoItemXMLDumper(final SAXConsumer saxConsumer) {
        this.saxConsumer = saxConsumer;
    }

    public void startQueryResult(final HCTQueryResult result, final HippoItem base)
            throws SAXException, RepositoryException {

        final AttributesImpl attrs = new AttributesImpl();
        attrs.addAttribute(NS_EMPTY, Attribute.SIZE.getName(),
                Attribute.SIZE.getName(), XSD_LONG, String.valueOf(result.getSize()));
        attrs.addAttribute(NS_EMPTY, Attribute.PAGE.getName(),
                Attribute.PAGE.getName(), XSD_LONG, String.valueOf(result.getPage()));
        attrs.addAttribute(NS_EMPTY, Attribute.TOTAL_PAGES.getName(),
                Attribute.TOTAL_PAGES.getName(), XSD_LONG, String.valueOf(result.getTotalPages()));

        saxConsumer.startElement(NS_HCT, Element.QUERY_RESULT.getName(),
                PREFIX_HCT + ":" + Element.QUERY_RESULT.getName(), attrs);

        saxConsumer.startElement(NS_HCT, Element.BASE.getName(),
                PREFIX_HCT + ":" + Element.BASE.getName(), EMPTY_ATTRS);
        if (base != null) {
            if (base instanceof HCTTaxonomyCategoryBean) {
                startTaxonomy((HCTTaxonomyCategoryBean) base, result.getLocale());
                endTaxonomy();
            } else {
                startHippoItem(base, base.getPath());
                endHippoItem(base);
            }
        }
        saxConsumer.endElement(NS_HCT, Element.BASE.getName(), PREFIX_HCT + ":" + Element.BASE.getName());
    }

    public void endQueryResult() throws SAXException {
        saxConsumer.endElement(NS_HCT, Element.QUERY_RESULT.getName(),
                PREFIX_HCT + ":" + Element.QUERY_RESULT.getName());
    }

    public <T extends HippoItem> void startHippoItem(final T item, final String path)
            throws SAXException, RepositoryException {

        final Element elem = item instanceof HippoDocument
                ? Element.DOCUMENT : Element.FOLDER;

        final String localeString = item instanceof HippoDocument
                ? ((HippoDocument) item).getLocaleString()
                : item instanceof HippoFolder
                ? ((HippoFolder) item).getLocaleString()
                : "";

        AttributesImpl attrs = new AttributesImpl();
        attrs.addAttribute(NS_EMPTY, Attribute.NAME.getName(),
                Attribute.NAME.getName(), XSD_STRING, item.getName());
        attrs.addAttribute(NS_EMPTY, Attribute.TYPE.getName(),
                Attribute.TYPE.getName(), XSD_STRING, item.getNode().getPrimaryNodeType().getName());
        attrs.addAttribute(NS_EMPTY, Attribute.PATH.getName(),
                Attribute.PATH.getName(), XSD_STRING, path);
        attrs.addAttribute(NS_EMPTY, Attribute.LOC_NAME.getName(),
                Attribute.LOC_NAME.getName(), XSD_STRING, item.getLocalizedName());
        attrs.addAttribute(NS_EMPTY, Attribute.LOCALE.getName(),
                Attribute.LOCALE.getName(), XSD_STRING, localeString);

        // 0. the item
        saxConsumer.startElement(NS_HCT, elem.getName(), PREFIX_HCT + ":" + elem.getName(), attrs);

        // 1. translations
        saxConsumer.startElement(NS_HCT, Element.TRANSLATIONS.getName(),
                PREFIX_HCT + ":" + Element.TRANSLATIONS.getName(), EMPTY_ATTRS);
        for (String locale : item.getAvailableTranslationsBean().getAvailableLocales()) {
            if (!localeString.equals(locale)) {
                attrs = new AttributesImpl();
                attrs.addAttribute(NS_EMPTY, Attribute.LOCALE.getName(),
                        Attribute.LOCALE.getName(), XSD_STRING, locale);
                attrs.addAttribute(NS_EMPTY, Attribute.PATH.getName(),
                        Attribute.PATH.getName(), XSD_STRING,
                        item.getAvailableTranslationsBean().getTranslation(locale).getContextualBean().getPath());
                attrs.addAttribute(NS_EMPTY, Attribute.NAME.getName(),
                        Attribute.NAME.getName(), XSD_STRING,
                        item.getAvailableTranslationsBean().getTranslation(locale).getContextualBean().getName());
                attrs.addAttribute(NS_EMPTY, Attribute.LOC_NAME.getName(),
                        Attribute.LOC_NAME.getName(), XSD_STRING,
                        item.getAvailableTranslationsBean().getTranslation(locale).getContextualBean().
                        getLocalizedName());

                saxConsumer.startElement(NS_HCT, Element.TRANSLATION.getName(),
                        PREFIX_HCT + ":" + Element.TRANSLATION.getName(), attrs);

                if (item instanceof HippoDocument) {
                    attrs = new AttributesImpl();
                    attrs.addAttribute(NS_EMPTY, Attribute.NAME.getName(),
                            Attribute.NAME.getName(), XSD_STRING, "hippo:availability");
                    saxConsumer.startElement(NS_HCT, Element.FIELD.getName(),
                            PREFIX_HCT + ":" + Element.FIELD.getName(), attrs);

                    for (String value :
                            (String[]) item.getAvailableTranslationsBean().
                            getTranslation(locale).
                            getProperty("hippo:availability")) {

                        saxConsumer.startElement(NS_HCT, Element.VALUE.getName(),
                                PREFIX_HCT + ":" + Element.VALUE.getName(), EMPTY_ATTRS);
                        saxConsumer.characters(value.toCharArray(), 0, value.length());
                        saxConsumer.endElement(NS_HCT, Element.VALUE.getName(),
                                PREFIX_HCT + ":" + Element.VALUE.getName());
                    }

                    saxConsumer.endElement(NS_HCT, Element.FIELD.getName(),
                            PREFIX_HCT + ":" + Element.FIELD.getName());
                }

                saxConsumer.endElement(NS_HCT, Element.TRANSLATION.getName(),
                        PREFIX_HCT + ":" + Element.TRANSLATION.getName());
            }
        }
        saxConsumer.endElement(NS_HCT, Element.TRANSLATIONS.getName(),
                PREFIX_HCT + ":" + Element.TRANSLATIONS.getName());
    }

    public void endHippoItem(final HippoItem item)
            throws SAXException {

        final Element elem = item instanceof HippoDocument
                ? Element.DOCUMENT : Element.FOLDER;
        saxConsumer.endElement(NS_HCT, elem.getName(), PREFIX_HCT + ":" + elem.getName());
    }

    public void startHippoCompounds()
            throws SAXException, RepositoryException {

        saxConsumer.startElement(NS_HCT, Element.COMPOUNDS.getName(),
                PREFIX_HCT + ":" + Element.COMPOUNDS.getName(), EMPTY_ATTRS);
    }

    public void endHippoCompounds()
            throws SAXException {

        saxConsumer.endElement(NS_HCT, Element.COMPOUNDS.getName(), PREFIX_HCT + ":" + Element.COMPOUNDS.getName());
    }

    public void startHippoCompound(final HippoDocument compound)
            throws SAXException, RepositoryException {

        final AttributesImpl attrs = new AttributesImpl();
        attrs.addAttribute(NS_EMPTY, Attribute.NAME.getName(),
                Attribute.NAME.getName(), XSD_STRING, compound.getName());
        attrs.addAttribute(NS_EMPTY, Attribute.TYPE.getName(),
                Attribute.TYPE.getName(), XSD_STRING, compound.getNode().getPrimaryNodeType().getName());
        attrs.addAttribute(NS_EMPTY, Attribute.PATH.getName(),
                Attribute.PATH.getName(), XSD_STRING, compound.getPath());

        saxConsumer.startElement(NS_HCT, Element.COMPOUND.getName(),
                PREFIX_HCT + ":" + Element.COMPOUND.getName(), attrs);
    }

    public void endHippoCompound(final HippoDocument compound)
            throws SAXException {

        saxConsumer.endElement(NS_HCT, Element.COMPOUND.getName(), PREFIX_HCT + ":" + Element.COMPOUND.getName());
    }

    public void dumpAssets(final List<HippoAsset> assets, final String elementName, final boolean wrap,
            final String dateFormat, final Locale locale)
            throws SAXException {

        if (wrap) {
            saxConsumer.startElement(NS_HCT, Element.ASSETS.getName(),
                    PREFIX_HCT + ":" + Element.ASSETS.getName(), EMPTY_ATTRS);
        }

        final SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, locale);
        for (HippoAsset asset : assets) {
            final AttributesImpl attrs = new AttributesImpl();
            attrs.addAttribute(NS_EMPTY, Attribute.NAME.getName(),
                    Attribute.NAME.getName(), XSD_STRING, asset.getName());
            attrs.addAttribute(NS_EMPTY, Attribute.PATH.getName(),
                    Attribute.PATH.getName(), XSD_STRING, asset.getPath());
            attrs.addAttribute(NS_EMPTY, Attribute.MIMETYPE.getName(),
                    Attribute.MIMETYPE.getName(), XSD_STRING, asset.getAsset().getMimeType());
            attrs.addAttribute(NS_EMPTY, Attribute.SIZE_KB.getName(),
                    Attribute.SIZE_KB.getName(), XSD_INT, String.valueOf(asset.getAsset().getLengthKB()));
            attrs.addAttribute(NS_EMPTY, Attribute.LAST_MOD.getName(),
                    Attribute.LAST_MOD.getName(), XSD_DATETIME, sdf.format(asset.getAsset().
                    getLastModified().getTime()));

            saxConsumer.startElement(NS_HCT, elementName, PREFIX_HCT + ":" + elementName, attrs);
            saxConsumer.endElement(NS_HCT, elementName, PREFIX_HCT + ":" + elementName);
        }

        if (wrap) {
            saxConsumer.endElement(NS_HCT, Element.ASSETS.getName(),
                    PREFIX_HCT + ":" + Element.ASSETS.getName());
        }
    }

    public void dumpImages(final List<HippoGalleryImageSet> images, final String elementName, final boolean wrap)
            throws SAXException, ObjectBeanManagerException {

        if (wrap) {
            saxConsumer.startElement(NS_HCT, Element.IMAGES.getName(),
                    PREFIX_HCT + ":" + Element.IMAGES.getName(), EMPTY_ATTRS);
        }

        for (HippoGalleryImageSet img : images) {
            final AttributesImpl attrs = new AttributesImpl();
            attrs.addAttribute(NS_EMPTY, Attribute.PATH.getName(),
                    Attribute.PATH.getName(), XSD_STRING, img.getPath());
            attrs.addAttribute(NS_EMPTY, Attribute.NAME.getName(),
                    Attribute.NAME.getName(), XSD_STRING, img.getName());
            if (StringUtils.isNotBlank(img.getDescription())) {
                attrs.addAttribute(NS_EMPTY, Attribute.DESC.getName(),
                        Attribute.DESC.getName(), XSD_STRING, img.getDescription());
            }
            attrs.addAttribute(NS_EMPTY, Attribute.HEIGHT.getName(),
                    Attribute.HEIGHT.getName(), XSD_INT, String.valueOf(img.getOriginal().getHeight()));
            attrs.addAttribute(NS_EMPTY, Attribute.WIDTH.getName(),
                    Attribute.WIDTH.getName(), XSD_INT, String.valueOf(img.getOriginal().getWidth()));

            saxConsumer.startElement(NS_HCT, elementName, PREFIX_HCT + ":" + elementName, attrs);
            saxConsumer.endElement(NS_HCT, elementName, PREFIX_HCT + ":" + elementName);
        }

        if (wrap) {
            saxConsumer.endElement(NS_HCT, Element.IMAGES.getName(),
                    PREFIX_HCT + ":" + Element.IMAGES.getName());
        }
    }

    public void dumpRelatedDocs(final List<HippoDocument> relDocs, final String elementName, final boolean wrap)
            throws SAXException, ObjectBeanManagerException {

        if (wrap) {
            saxConsumer.startElement(NS_HCT, Element.RELATED_DOCS.getName(),
                    PREFIX_HCT + ":" + Element.RELATED_DOCS.getName(), EMPTY_ATTRS);
        }

        AttributesImpl attrs;
        for (HippoDocument relDoc : relDocs) {
            attrs = new AttributesImpl();
            attrs.addAttribute(NS_EMPTY, Attribute.NAME.getName(),
                    Attribute.NAME.getName(), XSD_STRING, relDoc.getName());
            attrs.addAttribute(NS_EMPTY, Attribute.PATH.getName(),
                    Attribute.PATH.getName(), XSD_STRING, relDoc.getPath());
            attrs.addAttribute(NS_EMPTY, Attribute.LOC_NAME.getName(),
                    Attribute.LOC_NAME.getName(), XSD_STRING, relDoc.getLocalizedName());
            attrs.addAttribute(NS_EMPTY, Attribute.LOCALE.getName(),
                    Attribute.LOCALE.getName(), XSD_STRING, relDoc.getLocalizedName());

            saxConsumer.startElement(NS_HCT, elementName, PREFIX_HCT + ":" + elementName, attrs);
            saxConsumer.endElement(NS_HCT, elementName, PREFIX_HCT + ":" + elementName);
        }

        if (wrap) {
            saxConsumer.endElement(NS_HCT, Element.RELATED_DOCS.getName(),
                    PREFIX_HCT + ":" + Element.RELATED_DOCS.getName());
        }
    }

    public void dumpTags(final String[] tags) throws SAXException {
        saxConsumer.startElement(NS_HCT, Element.TAGS.getName(),
                PREFIX_HCT + ":" + Element.TAGS.getName(), EMPTY_ATTRS);

        for (int i = 0; tags != null && i < tags.length; i++) {
            saxConsumer.startElement(NS_HCT, Element.TAG.getName(),
                    PREFIX_HCT + ":" + Element.TAG.getName(), EMPTY_ATTRS);
            saxConsumer.characters(tags[i].toCharArray(), 0, tags[i].length());
            saxConsumer.endElement(NS_HCT, Element.TAG.getName(),
                    PREFIX_HCT + ":" + Element.TAG.getName());
        }

        saxConsumer.endElement(NS_HCT, Element.TAGS.getName(),
                PREFIX_HCT + ":" + Element.TAGS.getName());
    }

    public void startTaxonomy(final HCTTaxonomyCategoryBean taxonomy, final Locale locale) throws SAXException {
        final AttributesImpl attrs = new AttributesImpl();
        attrs.addAttribute(NS_EMPTY, Attribute.NAME.getName(),
                Attribute.NAME.getName(), XSD_STRING, taxonomy.getKey());
        attrs.addAttribute(NS_EMPTY, Attribute.LOC_NAME.getName(),
                Attribute.LOC_NAME.getName(), XSD_STRING, taxonomy.getLocalizedName(locale.getLanguage()));
        attrs.addAttribute(NS_EMPTY, Attribute.PATH.getName(),
                Attribute.PATH.getName(), XSD_STRING, taxonomy.getPath());

        saxConsumer.startElement(NS_HCT, Element.TAXONOMY.getName(),
                PREFIX_HCT + ":" + Element.TAXONOMY.getName(), attrs);
    }

    public void endTaxonomy() throws SAXException {
        saxConsumer.endElement(NS_HCT, Element.TAXONOMY.getName(),
                PREFIX_HCT + ":" + Element.TAXONOMY.getName());
    }

    public void dumpTaxonomies(final List<HCTTaxonomyCategoryBean> taxonomies, final Locale locale)
            throws SAXException {

        saxConsumer.startElement(NS_HCT, Element.TAXONOMIES.getName(),
                PREFIX_HCT + ":" + Element.TAXONOMIES.getName(), EMPTY_ATTRS);

        for (HCTTaxonomyCategoryBean taxonomy : taxonomies) {
            startTaxonomy(taxonomy, locale);
            endTaxonomy();
        }

        saxConsumer.endElement(NS_HCT, Element.TAXONOMIES.getName(),
                PREFIX_HCT + ":" + Element.TAXONOMIES.getName());
    }

    public void dumpField(final Entry<String, Object> entry, final String dateFormat, final Locale locale)
            throws SAXException {

        final AttributesImpl attrs = new AttributesImpl();
        attrs.addAttribute(NS_EMPTY, Attribute.NAME.getName(), Attribute.NAME.getName(), XSD_STRING, entry.getKey());
        saxConsumer.startElement(NS_HCT, Element.FIELD.getName(), PREFIX_HCT + ":" + Element.FIELD.getName(), attrs);

        if (entry.getValue() instanceof String) {
            saxConsumer.startElement(NS_HCT, Element.VALUE.getName(),
                    PREFIX_HCT + ":" + Element.VALUE.getName(), EMPTY_ATTRS);
            saxConsumer.characters(((String) entry.getValue()).toCharArray(),
                    0, ((String) entry.getValue()).length());
            saxConsumer.endElement(NS_HCT, Element.VALUE.getName(),
                    PREFIX_HCT + ":" + Element.VALUE.getName());
        } else if (entry.getValue() instanceof Boolean) {
            saxConsumer.startElement(NS_HCT, Element.VALUE.getName(),
                    PREFIX_HCT + ":" + Element.VALUE.getName(), EMPTY_ATTRS);
            saxConsumer.characters(((Boolean) entry.getValue()).toString().toCharArray(),
                    0, ((Boolean) entry.getValue()).toString().length());
            saxConsumer.endElement(NS_HCT, Element.VALUE.getName(),
                    PREFIX_HCT + ":" + Element.VALUE.getName());
        } else if (entry.getValue() instanceof GregorianCalendar) {
            final SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, locale);
            final String date = sdf.format(((GregorianCalendar) entry.getValue()).getTime());

            saxConsumer.startElement(NS_HCT, Element.VALUE.getName(),
                    PREFIX_HCT + ":" + Element.VALUE.getName(), EMPTY_ATTRS);
            saxConsumer.characters(date.toCharArray(), 0, date.length());
            saxConsumer.endElement(NS_HCT, Element.VALUE.getName(),
                    PREFIX_HCT + ":" + Element.VALUE.getName());
        } else if (entry.getValue() instanceof String[]) {
            for (int i = 0; i < ((String[]) entry.getValue()).length; i++) {
                saxConsumer.startElement(NS_HCT, Element.VALUE.getName(),
                        PREFIX_HCT + ":" + Element.VALUE.getName(), EMPTY_ATTRS);
                saxConsumer.characters(((String[]) entry.getValue())[i].toCharArray(),
                        0, ((String[]) entry.getValue())[i].length());
                saxConsumer.endElement(NS_HCT, Element.VALUE.getName(),
                        PREFIX_HCT + ":" + Element.VALUE.getName());
            }
        }

        saxConsumer.endElement(NS_HCT, Element.FIELD.getName(), PREFIX_HCT + ":" + Element.FIELD.getName());
    }

    public void dumpDate(final String name, final Calendar calendar, final String dateFormat, final Locale locale)
            throws SAXException {

        dumpField(new DefaultMapEntry(name, calendar.getTime()), dateFormat, locale);
    }

    public void dumpHtml(final ObjectBeanManager objMan, final HippoHtml rtf, final XMLReader xmlReader,
            final String dateFormat, final Locale locale)
            throws SAXException, IOException, ObjectBeanManagerException {

        final AttributesImpl attrs = new AttributesImpl();
        attrs.addAttribute(NS_EMPTY, Attribute.NAME.getName(), Attribute.NAME.getName(), XSD_STRING, rtf.getName());
        saxConsumer.startElement(NS_HCT, Element.FIELD.getName(), PREFIX_HCT + ":" + Element.FIELD.getName(), attrs);

        xmlReader.parse(new InputSource(new StringReader(rtf.getContent())));

        final List<HippoGalleryImageSet> images = new ArrayList<HippoGalleryImageSet>();
        final List<HippoAsset> assets = new ArrayList<HippoAsset>();
        final List<HippoDocument> docs = new ArrayList<HippoDocument>();
        for (HippoFacetSelect facetSelect : rtf.getChildBeans(HippoFacetSelect.class)) {
            final Object subElement = objMan.getObjectByUuid(
                    (String) facetSelect.getProperty(HippoNodeType.HIPPO_DOCBASE));
            if (subElement instanceof HippoGalleryImageSet) {
                images.add((HippoGalleryImageSet) subElement);
            }
            if (subElement instanceof HippoAsset) {
                assets.add((HippoAsset) subElement);
            } else if (subElement instanceof HippoDocument) {
                docs.add((HippoDocument) subElement);
            }
        }
        saxConsumer.startElement(NS_HCT, Element.LINKS.getName(),
                PREFIX_HCT + ":" + Element.LINKS.getName(), EMPTY_ATTRS);
        dumpImages(images, Element.LINK.getName(), false);
        dumpAssets(assets, Element.LINK.getName(), false, dateFormat, locale);
        dumpRelatedDocs(docs, Element.LINK.getName(), false);
        saxConsumer.endElement(NS_HCT, Element.LINKS.getName(), PREFIX_HCT + ":" + Element.LINKS.getName());

        saxConsumer.endElement(NS_HCT, Element.FIELD.getName(), PREFIX_HCT + ":" + Element.FIELD.getName());
    }

    public void dumpItem(final HippoItem item, final String itemPath,
            final HCTConnManager connManager, final HCTQuery hctQuery)
            throws SAXException, RepositoryException, IOException, ObjectBeanManagerException {

        final XMLReader xmlReader = new StartEndDocumentFilter(XMLUtils.createXMLReader(saxConsumer));
        xmlReader.setContentHandler(saxConsumer);

        startHippoItem(item, itemPath);

        for (final String fieldName : hctQuery.getReturnFields()) {
            final Object fieldValue = item.getProperty(fieldName);
            if (fieldValue == null) {
                final List<HippoHtml> rtfs = item.getChildBeansByName(fieldName, HippoHtml.class);
                final List<HippoDate> dates = item.getChildBeansByName(fieldName, HippoDate.class);

                if (rtfs != null && !rtfs.isEmpty()) {
                    for (HippoHtml rtf : rtfs) {
                        dumpHtml(connManager.getObjMan(), rtf, xmlReader,
                                hctQuery.getDateFormat(), hctQuery.getLocale());
                    }
                }

                if (dates != null && !dates.isEmpty()) {
                    for (HippoDate date : dates) {
                        dumpDate(fieldName, date.getCalendar(), hctQuery.getDateFormat(), hctQuery.getLocale());
                    }
                }
            } else {
                dumpField(new DefaultMapEntry(fieldName, fieldValue),
                        hctQuery.getDateFormat(), hctQuery.getLocale());
            }
        }

        if (hctQuery.isReturnTags()) {
            dumpTags((String[]) item.getProperty(TaggingNodeType.PROP_TAGS));
        }

        if (hctQuery.isReturnTaxonomies()) {
            dumpTaxonomies(TaxonomyUtils.getTaxonomies(connManager,
                    (String[]) item.getProperty(TaxonomyNodeTypes.HIPPOTAXONOMY_KEYS)),
                    hctQuery.getLocale());
        }

        if (hctQuery.isReturnImages()) {
            final List<HippoGalleryImageSet> images = new ArrayList<HippoGalleryImageSet>();
            for (ImageLinkBean imgLink : item.getChildBeans(ImageLinkBean.class)) {
                final HippoGalleryImageSet image =
                        (HippoGalleryImageSet) connManager.getObjMan().getObjectByUuid(imgLink.getImageSetUuid());
                if (image != null) {
                    images.add(image);
                }
            }
            dumpImages(images, Element.IMAGE.getName(), true);
        }

        if (hctQuery.isReturnRelatedDocs()) {
            final List<HippoDocument> relDocs = new ArrayList<HippoDocument>();
            for (RelatedDocs docs : item.getChildBeans(RelatedDocs.class)) {
                for (String relDocUuid : docs.getRelatedDocsUuids()) {
                    relDocs.add((HippoDocument) connManager.getObjMan().getObjectByUuid(relDocUuid));
                }
            }
            dumpRelatedDocs(relDocs, Element.DOCUMENT.getName(), true);
        }

        endHippoItem(item);
    }
}
