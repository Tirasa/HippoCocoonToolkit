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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import javax.jcr.RepositoryException;
import net.tirasa.hct.cocoon.sax.Constants.Attribute;
import net.tirasa.hct.cocoon.sax.Constants.Availability;
import net.tirasa.hct.cocoon.sax.Constants.Element;
import net.tirasa.hct.cocoon.sax.Constants.StartEndDocumentFilter;
import net.tirasa.hct.cocoon.sax.Constants.State;
import net.tirasa.hct.hstbeans.HCTTaxonomyCategoryBean;
import net.tirasa.hct.hstbeans.HippoCompoundDocument;
import net.tirasa.hct.hstbeans.HippoDate;
import net.tirasa.hct.hstbeans.RelatedDocs;
import net.tirasa.hct.repository.HCTConnManager;
import net.tirasa.hct.repository.HCTDocument;
import net.tirasa.hct.repository.HCTQuery;
import net.tirasa.hct.repository.HCTQueryResult;
import net.tirasa.hct.util.TaxonomyUtils;
import org.apache.cocoon.configuration.Settings;
import org.apache.cocoon.pipeline.ProcessingException;
import org.apache.cocoon.pipeline.caching.CacheKey;
import org.apache.cocoon.pipeline.component.CachingPipelineComponent;
import org.apache.cocoon.sax.AbstractSAXTransformer;
import org.apache.cocoon.sax.util.XMLUtils;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.hippoecm.hst.content.beans.ObjectBeanManagerException;
import org.hippoecm.hst.content.beans.standard.HippoAsset;
import org.hippoecm.hst.content.beans.standard.HippoBean;
import org.hippoecm.hst.content.beans.standard.HippoDocument;
import org.hippoecm.hst.content.beans.standard.HippoFacetSelect;
import org.hippoecm.hst.content.beans.standard.HippoFolder;
import org.hippoecm.hst.content.beans.standard.HippoGalleryImageSet;
import org.hippoecm.hst.content.beans.standard.HippoHtml;
import org.hippoecm.hst.content.beans.standard.HippoItem;
import org.hippoecm.hst.content.beans.standard.HippoMirror;
import org.hippoecm.repository.HippoStdNodeType;
import org.hippoecm.repository.api.HippoNodeType;
import org.onehippo.forge.ecmtagging.TaggingNodeType;
import org.onehippo.taxonomy.api.TaxonomyNodeTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;

public class HippoRepositoryTransformer extends AbstractSAXTransformer implements CachingPipelineComponent {

    private static final Logger LOG = LoggerFactory.getLogger(HippoRepositoryTransformer.class);

    private transient Locale defaultLocale;

    private transient State state;

    private transient HCTConnManager connManager;

    private transient HCTDocument hctDocument;

    private transient HCTQuery hctQuery;

    @Override
    public void setConfiguration(final Map<String, ? extends Object> configuration) {
        this.setup((Map<String, Object>) configuration);
    }

    @Override
    public void setup(final Map<String, Object> parameters) {
        if (parameters == null) {
            return;
        }

        if (parameters.containsKey(Settings.ROLE)) {
            final Settings settings = (Settings) parameters.get(Settings.ROLE);
            final String localeString =
                    settings.getProperty("net.tirasa.hct.defaultLocale", Locale.getDefault().getLanguage());
            try {
                defaultLocale = LocaleUtils.toLocale(localeString);
            } catch (IllegalArgumentException e) {
                LOG.error("Could not parse provided '{}' as Locale", localeString, e);
                defaultLocale = Locale.getDefault();
            }
        }

        synchronized (this) {
            if (connManager == null) {
                connManager = HCTConnManager.getContentInstance();
                state = State.OUTSIDE;
            }
        }
    }

    @Override
    public void finish() {
        if (connManager.getSession() != null) {
            connManager.getSession().logout();
        }

        state = null;

        super.finish();
    }

    @Override
    public CacheKey constructCacheKey() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void findAndDumpImagesAndAssets(final HippoDocument doc, final HippoItemXMLDumper dumper)
            throws ObjectBeanManagerException, SAXException {

        final List<HippoGalleryImageSet> images = new ArrayList<HippoGalleryImageSet>();
        final List<HippoAsset> assets = new ArrayList<HippoAsset>();
        for (HippoMirror mirror : doc.getChildBeans(HippoMirror.class)) {
            final HippoBean bean = mirror.getReferencedBean();
            if (bean != null) {
                if (bean instanceof HippoGalleryImageSet) {
                    final Object subElement = connManager.getObjMan().getObjectByUuid(
                            (String) ((HippoFacetSelect) mirror).getProperty(HippoNodeType.HIPPO_DOCBASE));
                    if (subElement instanceof HippoGalleryImageSet) {
                        images.add((HippoGalleryImageSet) subElement);
                    }
                }
                if (bean instanceof HippoAsset) {
                    assets.add((HippoAsset) bean);
                }
            }
        }
        dumper.dumpImages(images, Element.IMAGE.getName(), true);
        dumper.dumpAssets(assets, Element.ASSET.getName(), true, hctDocument.getDateFormat(), hctDocument.getLocale());
    }

    private void compounds(final HippoDocument container, final HippoItemXMLDumper dumper, final XMLReader xmlReader)
            throws SAXException, RepositoryException, ObjectBeanManagerException, IOException {

        dumper.startHippoCompounds();
        for (HippoDocument compound : container.getChildBeans(HippoCompoundDocument.class)) {
            dumper.startHippoCompound(compound);

            // 1 Compound properties
            for (Entry<String, Object> entry : compound.getProperties().entrySet()) {
                dumper.dumpField(entry, hctDocument.getDateFormat(), hctDocument.getLocale());
            }

            // 2 Compound date fields
            for (HippoDate date : compound.getChildBeans(HippoDate.class)) {
                dumper.dumpDate(date.getName(), date.getCalendar(),
                        hctDocument.getDateFormat(), hctDocument.getLocale());
            }

            // 3 Compound HTML fields
            for (HippoHtml rtf : compound.getChildBeans(HippoHtml.class)) {
                dumper.dumpHtml(connManager.getObjMan(), rtf, xmlReader,
                        hctDocument.getDateFormat(), hctDocument.getLocale());
            }

            // 4 Compound images and assets
            findAndDumpImagesAndAssets(compound, dumper);

            compounds(compound, dumper, xmlReader);

            dumper.endHippoCompound(compound);
        }
        dumper.endHippoCompounds();
    }

    private void document()
            throws ObjectBeanManagerException, SAXException, IOException, RepositoryException {

        final HippoDocument doc = hctDocument.getHippoDocument(connManager);

        final HippoItemXMLDumper dumper = new HippoItemXMLDumper(this.getSAXConsumer());

        // 1. document
        dumper.startHippoItem(doc, doc.getPath());

        // 2. properties
        for (Entry<String, Object> entry : doc.getProperties().entrySet()) {
            if (TaggingNodeType.PROP_TAGS.equals(entry.getKey())) {
                dumper.dumpTags((String[]) entry.getValue());
            } else if (TaxonomyNodeTypes.HIPPOTAXONOMY_KEYS.equals(entry.getKey())) {
                dumper.dumpTaxonomies(TaxonomyUtils.getTaxonomies(connManager, (String[]) entry.getValue()),
                        hctDocument.getLocale());
            } else {
                dumper.dumpField(entry, hctDocument.getDateFormat(), hctDocument.getLocale());
            }
        }

        // 3. Date fields
        for (HippoDate date : doc.getChildBeans(HippoDate.class)) {
            dumper.dumpDate(date.getName(), date.getCalendar(), hctDocument.getDateFormat(), hctDocument.getLocale());
        }

        // 4. HTML fields
        final XMLReader xmlReader = new StartEndDocumentFilter(XMLUtils.createXMLReader(this.getSAXConsumer()));
        xmlReader.setContentHandler(this.getSAXConsumer());
        for (HippoHtml rtf : doc.getChildBeans(HippoHtml.class)) {
            dumper.dumpHtml(connManager.getObjMan(), rtf, xmlReader,
                    hctDocument.getDateFormat(), hctDocument.getLocale());
        }

        // 5. Images and Assets
        findAndDumpImagesAndAssets(doc, dumper);

        // 6. Compounds
        compounds(doc, dumper, xmlReader);

        // 7. Related documents
        final List<HippoDocument> relDocs = new ArrayList<HippoDocument>();
        for (RelatedDocs docs : doc.getChildBeans(RelatedDocs.class)) {
            for (String relDocUuid : docs.getRelatedDocsUuids()) {
                relDocs.add((HippoDocument) connManager.getObjMan().getObjectByUuid(relDocUuid));
            }
        }
        dumper.dumpRelatedDocs(relDocs, Element.DOCUMENT.getName(), true);

        dumper.endHippoItem(doc);
    }

    private void query() throws SAXException, RepositoryException, IOException, ObjectBeanManagerException {
        final HCTQueryResult queryResult = hctQuery.execute();
        LOG.debug("Query is {}", hctQuery.getSqlQuery());

        final HippoItemXMLDumper dumper = new HippoItemXMLDumper(this.getSAXConsumer());

        final HippoItem base = (HippoItem) connManager.getObjMan().getObject(hctQuery.getBase());
        dumper.startQueryResult(queryResult, base);

        if (hctQuery.isIncludeFolders()) {
            // 1. group matching documents by folder / taxonomy
            final Map<HippoItem, List<HippoItem>> resultByFolder = new HashMap<HippoItem, List<HippoItem>>();
            for (String uuid : queryResult.getUuids()) {
                final HippoItem item = (HippoItem) connManager.getObjMan().getObjectByUuid(uuid);

                if (hctQuery.getType() == HCTQuery.Type.TAXONOMY_DOCS) {
                    final String[] keys = item.getProperty(TaxonomyNodeTypes.HIPPOTAXONOMY_KEYS);
                    for (int i = 0; keys != null && i < keys.length; i++) {
                        if (hctQuery.getTaxonomies().keySet().contains(keys[i])) {
                            final HCTTaxonomyCategoryBean taxonomy = (HCTTaxonomyCategoryBean) connManager.getObjMan().
                                    getObject(hctQuery.getTaxonomies().get(keys[i]));
                            if (!resultByFolder.containsKey(taxonomy)) {
                                resultByFolder.put(taxonomy, new ArrayList<HippoItem>());
                            }
                            resultByFolder.get(taxonomy).add(item);
                        }
                    }
                } else {
                    final HippoFolder folder = (HippoFolder) item.getParentBean();
                    if (!resultByFolder.containsKey(folder)) {
                        resultByFolder.put(folder, new ArrayList<HippoItem>());
                    }
                    resultByFolder.get(folder).add(item);
                }
            }

            // 2. output results by folder / taxonomy
            for (Map.Entry<HippoItem, List<HippoItem>> entry : resultByFolder.entrySet()) {
                if (hctQuery.getType() == HCTQuery.Type.TAXONOMY_DOCS) {
                    dumper.startTaxonomy((HCTTaxonomyCategoryBean) entry.getKey(), hctQuery.getLocale());
                    for (HippoItem item : entry.getValue()) {
                        dumper.dumpItem(item,
                                TaxonomyUtils.buildPathInTaxonomy(entry.getKey().getPath(), item.getName()),
                                connManager, hctQuery);
                    }
                    dumper.endTaxonomy();
                } else {
                    dumper.startHippoItem(entry.getKey(), entry.getKey().getPath());
                    for (HippoItem item : entry.getValue()) {
                        dumper.dumpItem(item, item.getPath(), connManager, hctQuery);
                    }
                    dumper.endHippoItem(entry.getKey());
                }
            }
        } else {
            for (String uuid : queryResult.getUuids()) {
                final HippoItem item = (HippoItem) connManager.getObjMan().getObjectByUuid(uuid);

                switch (hctQuery.getType()) {
                    case TAXONOMIES:
                        dumper.startTaxonomy((HCTTaxonomyCategoryBean) item, hctQuery.getLocale());
                        dumper.endTaxonomy();
                        break;

                    case TAXONOMY_DOCS:
                        final String[] keys = item.getProperty(TaxonomyNodeTypes.HIPPOTAXONOMY_KEYS);
                        for (int i = 0; keys != null && i < keys.length; i++) {
                            if (hctQuery.getTaxonomies().keySet().contains(keys[i])) {
                                dumper.dumpItem(item, TaxonomyUtils.buildPathInTaxonomy(
                                        hctQuery.getTaxonomies().get(keys[i]), item.getName()), connManager, hctQuery);
                            }
                        }
                        break;

                    case DOCS:
                    case FOLDERS:
                    default:
                        dumper.dumpItem(item, item.getPath(), connManager, hctQuery);
                }
            }
        }

        dumper.endQueryResult();
    }

    private Locale parseLocale(final Attributes atts) {
        Locale locale;
        final String localeString = atts.getValue(Attribute.LOCALE.getName());
        if (StringUtils.isBlank(localeString)) {
            locale = defaultLocale;
        } else {
            try {
                locale = LocaleUtils.toLocale(localeString);
            } catch (IllegalArgumentException e) {
                LOG.error("Could not parse provided '{}' as Locale", localeString, e);
                locale = Locale.getDefault();
            }
        }

        return locale;
    }

    private String parseDateFormat(final Attributes atts) {
        String dateFormat = atts.getValue(Attribute.DATE_FORMAT.getName());
        if (StringUtils.isBlank(dateFormat)) {
            dateFormat = Constants.DEFAULT_DATE_FORMAT;
        }

        return dateFormat;
    }

    private Availability parseAvailability(final Attributes atts) {
        Availability availability;
        try {
            availability = Availability.valueOf(atts.getValue(Attribute.AVAILABILITY.getName()));
        } catch (IllegalArgumentException e) {
            availability = Availability.live;

            LOG.warn("Invalid availability specified, reverting to " + availability, e);
        }

        return availability;
    }

    private int parseDepth(final Attributes atts) {
        int depth = 0;
        if (StringUtils.isNotBlank(atts.getValue(Attribute.DEPTH.getName()))) {
            try {
                depth = Integer.parseInt(atts.getValue(Attribute.DEPTH.getName()));
            } catch (NumberFormatException e) {
                LOG.error("Invalid depth specified, reverting to default (0)", e);
            }
        }

        return depth;
    }

    private long parseSize(final Attributes atts) {
        long size = 5;
        if (StringUtils.isNotBlank(atts.getValue(Attribute.SIZE.getName()))) {
            try {
                size = Long.parseLong(atts.getValue(Attribute.SIZE.getName()));
            } catch (NumberFormatException e) {
                LOG.error("Invalid size specified, reverting to default (5)", e);
            }
        }

        return size;
    }

    private long parsePage(final Attributes atts) {
        long page = 0L;
        if (StringUtils.isNotBlank(atts.getValue(Attribute.PAGE.getName()))) {
            try {
                page = Long.parseLong(atts.getValue(Attribute.PAGE.getName()));
            } catch (NumberFormatException e) {
                LOG.error("Invalid page specified, assuming not paginated result", e);
            }
        }

        return page;
    }

    @Override
    public void startElement(final String uri, final String localName, final String name, final Attributes atts)
            throws SAXException {

        if (!Constants.NS_HCT.equals(uri)) {
            super.startElement(uri, localName, name, atts);
            return;
        }

        Element element;
        try {
            element = Element.fromName(localName);
        } catch (IllegalArgumentException e) {
            throw new SAXException("Invalid element found", e);
        }

        if (element == Element.DOCUMENT) {
            if (state != State.OUTSIDE) {
                throw new InvalidHCTRequestException(localName, state);
            }

            hctDocument = new HCTDocument();
            hctDocument.setPath(atts.getValue(Attribute.PATH.getName()));
            hctDocument.setUuid(atts.getValue(Attribute.UUID.getName()));
            hctDocument.setLocale(parseLocale(atts));
            hctDocument.setDateFormat(parseDateFormat(atts));
            hctDocument.setAvailability(parseAvailability(atts));
        }

        if (element == Element.FOLDERS) {
            if (state != State.OUTSIDE) {
                throw new InvalidHCTRequestException(localName, state);
            }

            hctQuery = new HCTQuery(connManager.getSession());
            hctQuery.setBase(atts.getValue(Attribute.BASE.getName()) == null
                    ? "/" : atts.getValue(Attribute.BASE.getName()));
            hctQuery.setReturnType(hctQuery.getBase().startsWith("/content/taxonomies/")
                    ? TaxonomyNodeTypes.NODETYPE_HIPPOTAXONOMY_CATEGORY : HippoStdNodeType.NT_FOLDER);
            hctQuery.setLocale(parseLocale(atts));
            hctQuery.setDateFormat(parseDateFormat(atts));
            hctQuery.setDepth(parseDepth(atts));
            hctQuery.setSize(parseSize(atts));
            hctQuery.setPage(parsePage(atts));

            if (hctQuery.getType() == HCTQuery.Type.FOLDERS) {
                final AttributesImpl localAtts = new AttributesImpl();
                localAtts.addAttribute(Constants.NS_EMPTY, Attribute.FIELD.getName(),
                        Attribute.FIELD.getName(), Constants.XSD_STRING, "hippostd:foldertype");
                localAtts.addAttribute(Constants.NS_EMPTY, Attribute.VALUE.getName(),
                        Attribute.VALUE.getName(), Constants.XSD_STRING, "new-translated-folder");
                try {
                    hctQuery.getFilter().addCond(State.INSIDE_FILTER_AND, Element.EQUALTO, localAtts);
                } catch (RepositoryException e) {
                    throw new SAXException(e);
                }
            }
        }

        if (element == Element.QUERY) {
            if (state != State.OUTSIDE) {
                throw new InvalidHCTRequestException(localName, state);
            }
            state = State.INSIDE_QUERY;

            hctQuery = new HCTQuery(connManager.getSession());
            hctQuery.setBase(atts.getValue(Attribute.BASE.getName()) == null
                    ? "/" : atts.getValue(Attribute.BASE.getName()));
            hctQuery.setReturnType(atts.getValue(Attribute.TYPE.getName()) == null
                    ? "nt:base" : atts.getValue(Attribute.TYPE.getName()));
            hctQuery.setLocale(parseLocale(atts));
            hctQuery.setDateFormat(parseDateFormat(atts));
            hctQuery.setAvailability(parseAvailability(atts));
            hctQuery.setDepth(parseDepth(atts));
            hctQuery.setSize(parseSize(atts));
            hctQuery.setPage(parsePage(atts));

            hctQuery.setIncludeFolders("true".equalsIgnoreCase(atts.getValue(Attribute.INCLUDE_FOLDERS.getName())));
        }

        if (element == Element.FILTER) {
            if (state != State.INSIDE_QUERY) {
                throw new InvalidHCTRequestException(localName, state);
            }
            state = State.INSIDE_FILTER;
        }

        if (element == Element.AND || element == Element.OR) {
            if (state != State.INSIDE_FILTER) {
                throw new InvalidHCTRequestException(localName, state);
            }

            if (element == Element.AND) {
                state = State.INSIDE_FILTER_AND;
            }
            if (element == Element.OR) {
                state = State.INSIDE_FILTER_OR;
            }
        }

        if (Constants.FILTER_ELEMENTS.contains(element)) {
            if (state != State.INSIDE_FILTER_AND && state != State.INSIDE_FILTER_OR) {
                throw new InvalidHCTRequestException(localName, state);
            }

            if (!Constants.Element.CONTAINS.getName().equals(localName)
                    && StringUtils.isBlank(atts.getValue(Attribute.FIELD.getName()))) {

                throw new InvalidHCTRequestException(Attribute.FIELD.getName() + " must be specified for " + localName);
            }
            try {
                hctQuery.getFilter().addCond(state, element, atts);
            } catch (RepositoryException e) {
                throw new SAXException(e);
            }
        }

        if (element == Element.ORDERBY) {
            if (state != State.INSIDE_QUERY) {
                throw new InvalidHCTRequestException(localName, state);
            }
            state = State.INSIDE_ORDERBY;
        }

        if (element == Element.DESCENDING || element == Element.ASCENDING) {
            if (state != State.INSIDE_ORDERBY) {
                throw new InvalidHCTRequestException(localName, state);
            }

            if (StringUtils.isBlank(atts.getValue(Attribute.FIELD.getName()))) {
                throw new InvalidHCTRequestException(Attribute.FIELD.getName() + " must be specified for " + localName);
            }

            if (element == Element.DESCENDING) {
                hctQuery.addOrderByDescending(atts.getValue(Attribute.FIELD.getName()));
            }
            if (element == Element.ASCENDING) {
                hctQuery.addOrderByAscending(atts.getValue(Attribute.FIELD.getName()));
            }
        }

        if (element == Element.RETURN) {
            if (state != State.INSIDE_QUERY) {
                throw new InvalidHCTRequestException(localName, state);
            }
            state = State.INSIDE_RETURN;
        }

        if (element == Element.FIELD) {
            if (state != State.INSIDE_RETURN) {
                throw new InvalidHCTRequestException(localName, state);
            }

            if (StringUtils.isBlank(atts.getValue(Attribute.NAME.getName()))) {
                throw new InvalidHCTRequestException(Attribute.NAME.getName() + " must be specified for " + localName);
            }

            hctQuery.addReturnField(atts.getValue(Attribute.NAME.getName()));
        }

        if (element == Element.TAGS) {
            if (state != State.INSIDE_RETURN) {
                throw new InvalidHCTRequestException(localName, state);
            }

            hctQuery.setReturnTags(true);
        }

        if (element == Element.TAXONOMIES) {
            if (state != State.INSIDE_RETURN) {
                throw new InvalidHCTRequestException(localName, state);
            }

            hctQuery.setReturnTaxonomies(true);
        }

        if (element == Element.IMAGES) {
            if (state != State.INSIDE_RETURN) {
                throw new InvalidHCTRequestException(localName, state);
            }

            hctQuery.setReturnImages(true);
        }

        if (element == Element.RELATED_DOCS) {
            if (state != State.INSIDE_RETURN) {
                throw new InvalidHCTRequestException(localName, state);
            }

            hctQuery.setReturnRelatedDocs(true);
        }
    }

    @Override
    public void endElement(final String uri, final String localName, final String name)
            throws SAXException {

        if (!Constants.NS_HCT.equals(uri)) {
            super.endElement(uri, localName, name);
            return;
        }

        Element element;
        try {
            element = Element.fromName(localName);
        } catch (IllegalArgumentException e) {
            throw new SAXException("Invalid element found", e);
        }

        if (element == Element.RETURN) {
            if (state != State.INSIDE_RETURN) {
                throw new InvalidHCTRequestException(localName, state);
            }
            state = State.INSIDE_QUERY;

            LOG.debug("Fields to be returned: {}; images: {}; relatedDocs: {}",
                    new Object[]{hctQuery.getReturnFields(), hctQuery.isReturnImages(),
                        hctQuery.isReturnRelatedDocs()});
        }

        if (element == Element.ORDERBY) {
            if (state != State.INSIDE_ORDERBY) {
                throw new InvalidHCTRequestException(localName, state);
            }
            state = State.INSIDE_QUERY;
        }

        if ((element == Element.AND || element == Element.OR)) {
            if (state != State.INSIDE_FILTER_AND && state != State.INSIDE_FILTER_OR) {
                throw new InvalidHCTRequestException(localName, state);
            }

            state = State.INSIDE_FILTER;
        }

        if (element == Element.FILTER) {
            if (state != State.INSIDE_FILTER) {
                throw new InvalidHCTRequestException(localName, state);
            }
            state = State.INSIDE_QUERY;
        }

        if (element == Element.QUERY) {
            if (state != State.INSIDE_QUERY) {
                throw new InvalidHCTRequestException(localName, state);
            }
            state = State.OUTSIDE;

            try {
                query();
            } catch (Exception e) {
                throw new ProcessingException("While performing query " + hctQuery.getSqlQuery(), e);
            }
        }

        if (element == Element.FOLDERS) {
            if (state != State.OUTSIDE) {
                throw new InvalidHCTRequestException(localName, state);
            }

            try {
                query();
            } catch (Exception e) {
                throw new ProcessingException("While performing query " + hctQuery.getSqlQuery(), e);
            }
        }

        if (element == Element.DOCUMENT) {
            if (state != State.OUTSIDE) {
                throw new InvalidHCTRequestException(localName, state);
            }

            try {
                document();
            } catch (Exception e) {
                throw new ProcessingException("While fetching document", e);
            }
        }
    }
}
