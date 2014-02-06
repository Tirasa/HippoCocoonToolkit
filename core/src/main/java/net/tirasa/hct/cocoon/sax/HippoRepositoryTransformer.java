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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import javax.jcr.RepositoryException;
import net.tirasa.hct.cocoon.cache.AvailabilityLocaleCacheKey;
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
import net.tirasa.hct.repository.HCTTraversal;
import net.tirasa.hct.util.ObjectUtils;
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
import org.hippoecm.repository.api.HippoNodeType;
import org.onehippo.forge.ecmtagging.TaggingNodeType;
import org.onehippo.forge.ecmtagging.providers.AllTagsProvider;
import org.onehippo.taxonomy.api.TaxonomyNodeTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class HippoRepositoryTransformer extends AbstractSAXTransformer implements CachingPipelineComponent {

    private static final Logger LOG = LoggerFactory.getLogger(HippoRepositoryTransformer.class);

    private static final String PARAM_LOCALE = "locale";

    private static final String PARAM_AVAILABILITY = "availability";

    private Locale locale;

    private Availability availability;

    private State state;

    private transient HCTDocument hctDocument;

    private transient HCTTraversal hctTraversal;

    private transient HCTQuery hctQuery;

    @Override
    @SuppressWarnings("unchecked")
    public void setConfiguration(final Map<String, ? extends Object> configuration) {
        this.setup((Map<String, Object>) configuration);
    }

    @Override
    public void setup(final Map<String, Object> parameters) {
        if (parameters == null) {
            return;
        }

        if (parameters.containsKey(PARAM_AVAILABILITY)) {
            try {
                availability = Availability.valueOf((String) parameters.get(PARAM_AVAILABILITY));
            } catch (IllegalArgumentException e) {
                availability = Availability.live;

                LOG.warn("Invalid availability specified, reverting to " + availability, e);
            }
        } else {
            availability = Availability.live;

            LOG.warn("No availability specified, reverting to " + availability);
        }

        Locale defaultLocale;
        if (parameters.containsKey(Settings.ROLE)) {
            final Settings settings = (Settings) parameters.get(Settings.ROLE);
            final String localeString =
                    settings.getProperty("net.tirasa.hct.defaultLocale", Locale.getDefault().getLanguage());
            try {
                defaultLocale = LocaleUtils.toLocale(localeString);
            } catch (IllegalArgumentException e) {
                defaultLocale = Locale.getDefault();

                LOG.error("Could not parse provided '{}' as default Locale", localeString, e);
            }
        } else {
            defaultLocale = Locale.getDefault();
        }

        if (parameters.containsKey(PARAM_LOCALE)) {
            try {
                locale = LocaleUtils.toLocale((String) parameters.get(PARAM_LOCALE));
            } catch (IllegalArgumentException e) {
                locale = defaultLocale;

                LOG.error("Could not parse provided '{}' as Locale", parameters.get(PARAM_LOCALE), e);
            }
        } else {
            locale = defaultLocale;

            LOG.warn("No locale specified, reverting to " + locale);
        }

        state = State.OUTSIDE;
    }

    @Override
    public void finish() {
        state = null;

        super.finish();
    }

    @Override
    public CacheKey constructCacheKey() {
        return new AvailabilityLocaleCacheKey(availability, locale);
    }

    private void findAndDumpImagesAndAssets(final HCTConnManager connManager, final HippoDocument doc,
            final HippoItemXMLDumper dumper)
            throws ObjectBeanManagerException, SAXException {

        final List<HippoGalleryImageSet> images = new ArrayList<HippoGalleryImageSet>();
        final List<HippoAsset> assets = new ArrayList<HippoAsset>();
        for (HippoMirror mirror : doc.getChildBeans(HippoMirror.class)) {
            final HippoBean bean = mirror.getReferencedBean();
            if (bean != null) {
                if (bean instanceof HippoGalleryImageSet) {
                    final HippoItem subElement = ObjectUtils.getHippoItemByUuid(connManager,
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
        dumper.dumpAssets(assets, Element.ASSET.getName(), true, hctDocument.getDateFormat(), locale);
    }

    private void compounds(final HCTConnManager connManager, final HippoDocument container,
            final HippoItemXMLDumper dumper, final XMLReader xmlReader)
            throws SAXException, RepositoryException, ObjectBeanManagerException, IOException {

        dumper.startHippoCompounds();
        for (HippoDocument compound : container.getChildBeans(HippoCompoundDocument.class)) {
            dumper.startHippoCompound(compound);

            // 1 Compound properties
            for (Entry<String, Object> entry : compound.getProperties().entrySet()) {
                dumper.dumpField(entry, hctDocument.getDateFormat(), locale);
            }

            // 2 Compound date fields
            for (HippoDate date : compound.getChildBeans(HippoDate.class)) {
                dumper.dumpDate(date.getName(), date.getCalendar(), hctDocument.getDateFormat(), locale);
            }

            // 3 Compound HTML fields
            for (HippoHtml rtf : compound.getChildBeans(HippoHtml.class)) {
                dumper.dumpHtml(connManager, rtf, xmlReader, hctDocument.getDateFormat(), locale);
            }

            // 4 Compound images and assets
            findAndDumpImagesAndAssets(connManager, compound, dumper);

            compounds(connManager, compound, dumper, xmlReader);

            dumper.endHippoCompound(compound);
        }
        dumper.endHippoCompounds();
    }

    private void document(final HCTConnManager connManager)
            throws ObjectBeanManagerException, SAXException, IOException, RepositoryException {

        final HippoDocument doc = hctDocument.getHippoDocument(connManager, locale, availability);

        final HippoItemXMLDumper dumper = new HippoItemXMLDumper(this.getSAXConsumer());

        // 1. document
        dumper.startHippoItem(doc, doc.getPath());

        // 2. properties
        for (Entry<String, Object> entry : doc.getProperties().entrySet()) {
            if (TaggingNodeType.PROP_TAGS.equals(entry.getKey())) {
                dumper.dumpTags((String[]) entry.getValue());
            } else if (TaxonomyNodeTypes.HIPPOTAXONOMY_KEYS.equals(entry.getKey())) {
                dumper.dumpTaxonomies(TaxonomyUtils.getTaxonomies(connManager, (String[]) entry.getValue()), locale);
            } else {
                dumper.dumpField(entry, hctDocument.getDateFormat(), locale);
            }
        }

        // 3. Date fields
        for (HippoDate date : doc.getChildBeans(HippoDate.class)) {
            dumper.dumpDate(date.getName(), date.getCalendar(), hctDocument.getDateFormat(), locale);
        }

        // 4. HTML fields
        final XMLReader xmlReader = new StartEndDocumentFilter(XMLUtils.createXMLReader(this.getSAXConsumer()));
        xmlReader.setContentHandler(this.getSAXConsumer());
        for (HippoHtml rtf : doc.getChildBeans(HippoHtml.class)) {
            dumper.dumpHtml(connManager, rtf, xmlReader, hctDocument.getDateFormat(), locale);
        }

        // 5. Images and Assets
        findAndDumpImagesAndAssets(connManager, doc, dumper);

        // 6. Compounds
        compounds(connManager, doc, dumper, xmlReader);

        // 7. Related documents
        final List<HippoDocument> relDocs = new ArrayList<HippoDocument>();
        for (RelatedDocs docs : doc.getChildBeans(RelatedDocs.class)) {
            for (String relDocUuid : docs.getRelatedDocsUuids()) {
                HippoDocument docByUuid = ObjectUtils.getHippoItemByUuid(connManager, relDocUuid, HippoDocument.class);
                if (docByUuid != null) {
                    relDocs.add(doc);
                }
            }
        }
        dumper.dumpRelatedDocs(relDocs, Element.DOCUMENT.getName(), true);

        dumper.endHippoItem(doc);
    }

    private void tags(final HCTConnManager connManager) throws RepositoryException, SAXException {
        final HippoItemXMLDumper dumper = new HippoItemXMLDumper(this.getSAXConsumer());

        dumper.dumpTags(AllTagsProvider.getTags(connManager.getSession(), "tags"));
    }

    private <T extends HippoItem> void recursiveTraversal(final HippoItem item, final Class<T> traversalType,
            final int depth, final HippoItemXMLDumper dumper) throws SAXException, RepositoryException {

        dumper.startHippoItem(item, item.getPath());
        if (depth > 0) {
            final List<T> children = item.getChildBeans(traversalType);
            if (!children.isEmpty()) {
                for (T child : children) {
                    recursiveTraversal(child, traversalType, depth - 1, dumper);
                }
            }
        }
        dumper.endHippoItem(item);
    }

    private void traverse(final HCTConnManager connManager)
            throws ObjectBeanManagerException, SAXException, RepositoryException {

        if (hctTraversal == null) {
            throw new IllegalArgumentException("HCTTraversal is null");
        }
        final HippoItem base = ObjectUtils.getHippoItem(connManager, hctTraversal.getBase());
        if (base == null) {
            throw new IllegalArgumentException("base is null");
        }

        final Class<? extends HippoItem> traversalType = hctTraversal.getBase().startsWith("/content/taxonomies")
                ? HCTTaxonomyCategoryBean.class : HippoFolder.class;
        final HippoItemXMLDumper dumper = new HippoItemXMLDumper(this.getSAXConsumer());

        recursiveTraversal(base, traversalType, hctTraversal.getDepth(), dumper);
    }

    private void query(final HCTConnManager connManager)
            throws SAXException, RepositoryException, IOException, ObjectBeanManagerException {

        if (hctQuery == null) {
            throw new IllegalArgumentException("HCTQuery is null");
        }
        hctQuery.setSession(connManager.getSession());

        final HCTQueryResult queryResult = hctQuery.execute(locale, availability);
        LOG.debug("Query is {}", hctQuery.getSQLQuery());

        final HippoItemXMLDumper dumper = new HippoItemXMLDumper(this.getSAXConsumer());

        final HippoItem base = ObjectUtils.getHippoItem(connManager, hctQuery.getBase());
        dumper.startQueryResult(queryResult, base);

        if (hctQuery.isIncludeFolders()) {
            // 1. group matching documents by folder / taxonomy
            final Map<HippoItem, List<HippoItem>> resultByFolder = new HashMap<HippoItem, List<HippoItem>>();
            for (String uuid : queryResult.getUuids()) {
                final HippoItem item = ObjectUtils.getHippoItemByUuid(connManager, uuid);
                if (item != null) {
                    if (hctQuery.getType() == HCTQuery.Type.TAXONOMY_DOCS) {
                        final String[] keys = item.getProperty(TaxonomyNodeTypes.HIPPOTAXONOMY_KEYS);
                        for (int i = 0; keys != null && i < keys.length; i++) {
                            if (hctQuery.getTaxonomies().keySet().contains(keys[i])) {
                                final HCTTaxonomyCategoryBean taxonomy = ObjectUtils.getHippoItem(
                                        connManager, hctQuery.getTaxonomies().get(keys[i]),
                                        HCTTaxonomyCategoryBean.class);
                                if (taxonomy != null) {
                                    if (!resultByFolder.containsKey(taxonomy)) {
                                        resultByFolder.put(taxonomy, new ArrayList<HippoItem>());
                                    }
                                    resultByFolder.get(taxonomy).add(item);
                                }
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
            }

            // 2. output results by folder / taxonomy
            for (Map.Entry<HippoItem, List<HippoItem>> entry : resultByFolder.entrySet()) {
                if (hctQuery.getType() == HCTQuery.Type.TAXONOMY_DOCS) {
                    dumper.startTaxonomy((HCTTaxonomyCategoryBean) entry.getKey(), locale);
                    for (HippoItem item : entry.getValue()) {
                        dumper.dumpHippoItem(connManager, item, TaxonomyUtils.buildPathInTaxonomy(
                                entry.getKey().getPath(), item.getName()), hctQuery, locale);
                    }
                    dumper.endTaxonomy();
                } else {
                    dumper.startHippoItem(entry.getKey(), entry.getKey().getPath());
                    for (HippoItem item : entry.getValue()) {
                        dumper.dumpHippoItem(connManager, item, item.getPath(), hctQuery, locale);
                    }
                    dumper.endHippoItem(entry.getKey());
                }
            }
        } else {
            for (String uuid : queryResult.getUuids()) {
                final HippoItem item = ObjectUtils.getHippoItemByUuid(connManager, uuid);
                if (item != null) {
                    switch (hctQuery.getType()) {
                        case TAXONOMY_DOCS:
                            final String[] keys = item.getProperty(TaxonomyNodeTypes.HIPPOTAXONOMY_KEYS);
                            for (int i = 0; keys != null && i < keys.length; i++) {
                                if (hctQuery.getTaxonomies().keySet().contains(keys[i])) {
                                    dumper.dumpHippoItem(connManager, item, TaxonomyUtils.buildPathInTaxonomy(
                                            hctQuery.getTaxonomies().get(keys[i]), item.getName()),
                                            hctQuery, locale);
                                }
                            }
                            break;

                        case FOLDER_DOCS:
                        default:
                            dumper.dumpHippoItem(connManager, item, item.getPath(), hctQuery, locale);
                    }
                }
            }
        }

        dumper.endQueryResult();
    }

    private String parseDateFormat(final Attributes atts) {
        String dateFormat = atts.getValue(Attribute.DATE_FORMAT.getName());
        if (StringUtils.isBlank(dateFormat)) {
            dateFormat = Constants.DEFAULT_DATE_FORMAT;
        }

        return dateFormat;
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
            hctDocument.setDateFormat(parseDateFormat(atts));
        }

        if (element == Element.FOLDERS) {
            if (state != State.OUTSIDE) {
                throw new InvalidHCTRequestException(localName, state);
            }

            hctTraversal = new HCTTraversal();
            hctTraversal.setBase(atts.getValue(Attribute.BASE.getName()) == null
                    ? "/" : atts.getValue(Attribute.BASE.getName()));
            hctTraversal.setDepth(parseDepth(atts));
            hctTraversal.setSize(parseSize(atts));
        }

        if (element == Element.QUERY) {
            if (state != State.OUTSIDE) {
                throw new InvalidHCTRequestException(localName, state);
            }
            state = State.INSIDE_QUERY;

            hctQuery = new HCTQuery();
            hctQuery.setBase(atts.getValue(Attribute.BASE.getName()) == null
                    ? "/" : atts.getValue(Attribute.BASE.getName()));
            hctQuery.setReturnType(atts.getValue(Attribute.TYPE.getName()) == null
                    ? "nt:base" : atts.getValue(Attribute.TYPE.getName()));
            hctQuery.setDateFormat(parseDateFormat(atts));
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
            if (state == State.INSIDE_RETURN) {
                hctQuery.setReturnTags(true);
            } else if (state == State.OUTSIDE) {
                LOG.debug("Requiring tags, no processing needed here");
            } else {
                throw new InvalidHCTRequestException(localName, state);
            }
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
                    new Object[] { hctQuery.getReturnFields(), hctQuery.isReturnImages(),
                        hctQuery.isReturnRelatedDocs() });
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

            final HCTConnManager connManager = HCTConnManager.getContentInstance();
            try {
                query(connManager);
            } catch (Exception e) {
                throw new ProcessingException("While performing query " + hctQuery.getSQLQuery(), e);
            } finally {
                connManager.logout();
            }
        }

        if (element == Element.FOLDERS) {
            if (state != State.OUTSIDE) {
                throw new InvalidHCTRequestException(localName, state);
            }

            final HCTConnManager connManager = HCTConnManager.getContentInstance();
            try {
                traverse(connManager);
            } catch (Exception e) {
                throw new ProcessingException("While performing traversal " + hctTraversal, e);
            } finally {
                connManager.logout();
            }

        }

        if (element == Element.TAGS) {
            if (state != State.OUTSIDE) {
                throw new InvalidHCTRequestException(localName, state);
            }

            final HCTConnManager connManager = HCTConnManager.getContentInstance();
            try {
                tags(connManager);
            } catch (Exception e) {
                throw new ProcessingException("While fetching tags", e);
            } finally {
                connManager.logout();
            }
        }

        if (element == Element.DOCUMENT) {
            if (state != State.OUTSIDE) {
                throw new InvalidHCTRequestException(localName, state);
            }

            final HCTConnManager connManager = HCTConnManager.getContentInstance();
            try {
                document(connManager);
            } catch (Exception e) {
                throw new ProcessingException("While fetching document", e);
            } finally {
                connManager.logout();
            }
        }
    }
}
