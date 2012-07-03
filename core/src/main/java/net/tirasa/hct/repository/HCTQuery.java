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
package net.tirasa.hct.repository;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.RowIterator;
import net.tirasa.hct.cocoon.sax.Constants;
import net.tirasa.hct.cocoon.sax.Constants.Availability;
import org.apache.jackrabbit.JcrConstants;
import org.hippoecm.repository.HippoStdNodeType;
import org.hippoecm.repository.api.HippoNodeType;
import org.hippoecm.repository.translation.HippoTranslationNodeType;
import org.onehippo.taxonomy.api.TaxonomyNodeTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HCTQuery extends AbstractHCTEntity {

    public enum Type {

        DOCS,
        FOLDERS,
        TAXONOMY_DOCS,
        TAXONOMIES

    }

    private static final Logger LOG = LoggerFactory.getLogger(HCTQuery.class);

    private String base;

    private transient String returnType;

    private int depth;

    private long page;

    private long size;

    private final transient Set<String> returnFields;

    private boolean returnTags = false;

    private boolean returnTaxonomies = false;

    private boolean returnImages = false;

    private boolean returnRelatedDocs = false;

    private boolean includeFolders = false;

    private final transient HCTQueryFilter filter;

    private final transient StringBuilder orderBy;

    private transient String sqlQuery;

    private final transient Map<String, String> taxonomies;

    private final transient Session session;

    public HCTQuery(final Session session) {
        super();
        this.session = session;

        returnFields = new HashSet<String>();
        filter = new HCTQueryFilter();
        orderBy = new StringBuilder();
        taxonomies = new HashMap<String, String>();
    }

    public String getBase() {
        return base;
    }

    public void setBase(final String base) {
        this.base = base;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(final int depth) {
        this.depth = depth;
    }

    public boolean isIncludeFolders() {
        return includeFolders;
    }

    public void setIncludeFolders(final boolean includeFolders) {
        this.includeFolders = includeFolders;
    }

    public long getPage() {
        return page;
    }

    public void setPage(final long page) {
        this.page = page;
    }

    public boolean isReturnTags() {
        return returnTags;
    }

    public void setReturnTags(final boolean returnTags) {
        this.returnTags = returnTags;
    }

    public boolean isReturnTaxonomies() {
        return returnTaxonomies;
    }

    public void setReturnTaxonomies(final boolean returnTaxonomies) {
        this.returnTaxonomies = returnTaxonomies;
    }

    public boolean isReturnImages() {
        return returnImages;
    }

    public void setReturnImages(final boolean returnImages) {
        this.returnImages = returnImages;
    }

    public boolean isReturnRelatedDocs() {
        return returnRelatedDocs;
    }

    public void setReturnRelatedDocs(final boolean returnRelatedDocs) {
        this.returnRelatedDocs = returnRelatedDocs;
    }

    public long getSize() {
        return size;
    }

    public void setSize(final long size) {
        this.size = size;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(final String returnType) {
        this.returnType = returnType;
    }

    public Set<String> getReturnFields() {
        return returnFields;
    }

    public boolean addReturnField(final String returnField) {
        return returnField != null && returnFields.add(returnField);
    }

    public boolean removeReturnField(final String returnField) {
        return returnField != null && returnFields.remove(returnField);
    }

    public HCTQueryFilter getFilter() {
        return filter;
    }

    public void addOrderByAscending(final String propertyName) {
        orderBy.append(Constants.QUERY_DEFAULT_SELECTOR).append('.').append('[').append(propertyName).append(']').
                append(" ASC, ");
    }

    public void addOrderByDescending(final String propertyName) {
        orderBy.append(Constants.QUERY_DEFAULT_SELECTOR).append('.').append('[').append(propertyName).append(']').
                append(" DESC, ");
    }

    public Type getType() {
        return returnType == null || base == null
                ? null
                : HippoStdNodeType.NT_FOLDER.equals(returnType)
                ? Type.FOLDERS
                : TaxonomyNodeTypes.NODETYPE_HIPPOTAXONOMY_CATEGORY.equals(returnType)
                ? Type.TAXONOMIES
                : base.startsWith("/content/taxonomies")
                ? Type.TAXONOMY_DOCS
                : Type.DOCS;
    }

    public HCTQueryResult execute(final Locale locale, final Availability availability)
            throws InvalidQueryException, RepositoryException {

        buildSQLQuery(locale, availability);
        LOG.debug("Elaborated JCR/SQL2 query: {}", getSqlQuery());
        final Query query = session.getWorkspace().getQueryManager().createQuery(getSqlQuery(), Query.JCR_SQL2);

        // first execute without boundaries (only to take total result size)
        final long totalResultSize = page == 0 ? 0 : query.execute().getRows().getSize();

        // then execute with page and offset, for actual result
        query.setLimit(size);
        if (page > 0) {
            query.setOffset((page - 1) * size);
        }
        LOG.debug("About to execute {}", query.getStatement());
        final RowIterator result = query.execute().getRows();

        final long totalPages = page == 0 ? 1L : (totalResultSize % size == 0
                ? totalResultSize / size
                : totalResultSize / size + 1);
        return new HCTQueryResult(locale, page, totalPages, result);
    }

    public Map<String, String> getTaxonomies() {
        return taxonomies;
    }

    private void findTaxonomies(final Node node, final int targetDepth)
            throws RepositoryException {

        if (targetDepth >= node.getDepth()) {
            taxonomies.put(node.getProperty(TaxonomyNodeTypes.HIPPOTAXONOMY_KEY).getString(), node.getPath());

            for (final NodeIterator nodes = node.getNodes(); nodes.hasNext();) {
                final Node child = nodes.nextNode();
                if (TaxonomyNodeTypes.NODETYPE_HIPPOTAXONOMY_CATEGORY.equals(child.getPrimaryNodeType().getName())) {
                    findTaxonomies(child, targetDepth);
                }
            }
        }
    }

    private void findDepthFrontier(final Node node, final Set<String> frontier, final int targetDepth)
            throws RepositoryException {

        if (targetDepth == node.getDepth()) {
            frontier.add(node.getPath());
        } else {
            for (final NodeIterator nodes = node.getNodes(); nodes.hasNext();) {
                final Node child = nodes.nextNode();
                if (HippoStdNodeType.NT_FOLDER.equals(child.getPrimaryNodeType().getName())
                        || TaxonomyNodeTypes.NODETYPE_HIPPOTAXONOMY_CATEGORY.equals(
                        child.getPrimaryNodeType().getName())) {

                    findDepthFrontier(child, frontier, targetDepth);
                }
            }
        }
    }

    private void addCondsToWhereClause(final List<String> conds, final StringBuilder clause, final String op) {
        boolean firstItem = clause.length() == 0;
        for (String cond : conds) {
            clause.insert(0, '(');
            if (!firstItem) {
                clause.append(op).append(' ');
            }

            clause.append(cond).append(") ");
        }
    }

    private void addJoinsAndCondsToQuery(final Map<HCTDocumentChildNode, HCTQueryFilter.ChildQueryFilter> children,
            final StringBuilder query, final StringBuilder whereClause, final StringBuilder andCondClause,
            final StringBuilder orCondClause) {

        for (Map.Entry<HCTDocumentChildNode, HCTQueryFilter.ChildQueryFilter> entry : children.entrySet()) {
            query.append("INNER JOIN [").append(entry.getKey().getType()).append("] AS ").
                    append(entry.getKey().getSelector()).append(" ON ISCHILDNODE(").append(entry.getKey().getSelector()).
                    append(", ").append(Constants.QUERY_DEFAULT_SELECTOR).append(") ");

            whereClause.insert(0, '(');
            whereClause.append("AND NAME(").append(entry.getKey().getSelector()).append(") = '").
                    append(entry.getKey().getName()).append("') ");

            addCondsToWhereClause(entry.getValue().getAndConds(), andCondClause, "AND");
            addCondsToWhereClause(entry.getValue().getOrConds(), orCondClause, "OR");
        }
    }

    private void buildSQLQuery(final Locale locale, final Availability availability) throws RepositoryException {
        LOG.debug("Query type: {}", getType());
        final String actualBase = getType() == Type.TAXONOMY_DOCS ? "/content/documents" : base;
        LOG.debug("Search base: {}", actualBase);

        final StringBuilder whereClause =
                new StringBuilder("ISDESCENDANTNODE(").append(Constants.QUERY_DEFAULT_SELECTOR).append(", '").
                append(actualBase).append("') ");

        final Node baseNode = session.getNode(actualBase);
        if (getType() == Type.TAXONOMY_DOCS) {
            final Node taxonomyBaseNode = session.getNode(base);
            if (!TaxonomyNodeTypes.NODETYPE_HIPPOTAXONOMY_CATEGORY.equals(
                    taxonomyBaseNode.getPrimaryNodeType().getName())) {

                throw new InvalidQueryException(base + " is not of type "
                        + TaxonomyNodeTypes.NODETYPE_HIPPOTAXONOMY_CATEGORY);
            }

            taxonomies.clear();
            findTaxonomies(taxonomyBaseNode, depth > 0 ? taxonomyBaseNode.getDepth() + depth - 1 : Integer.MAX_VALUE);
            final StringBuilder taxonomySubclause = new StringBuilder();
            for (String taxonomy : taxonomies.keySet()) {
                if (taxonomySubclause.length() > 0) {
                    taxonomySubclause.append("OR ");
                }

                taxonomySubclause.insert(0, '(');
                taxonomySubclause.append(Constants.QUERY_DEFAULT_SELECTOR).append('.').append('[').
                        append(TaxonomyNodeTypes.HIPPOTAXONOMY_KEYS).append("] = '").append(taxonomy).append("') ");
            }
            whereClause.insert(0, '(');
            whereClause.append("AND ").append(taxonomySubclause).append(") ");

            LOG.debug("Searching with taxonomies: {}", taxonomies);
        } else if (depth > 0) {
            final Set<String> depthFrontier = new HashSet<String>();
            findDepthFrontier(baseNode, depthFrontier, baseNode.getDepth() + depth);
            for (String depthFrontierPath : depthFrontier) {
                whereClause.insert(0, '(');
                whereClause.append("AND NOT ISDESCENDANTNODE(").append(Constants.QUERY_DEFAULT_SELECTOR).append(",'").
                        append(depthFrontierPath).append("')) ");
            }
        }

        if (availability != null) {
            whereClause.insert(0, '(');
            whereClause.append("AND ").append(Constants.QUERY_DEFAULT_SELECTOR).append('.').append('[').
                    append(HippoNodeType.HIPPO_AVAILABILITY).append("] = '").append(availability.name()).append("') ");
        }

        if (getType() != Type.TAXONOMIES) {
            whereClause.insert(0, '(');
            whereClause.append("AND ").append(Constants.QUERY_DEFAULT_SELECTOR).append('.').append('[').
                    append(HippoTranslationNodeType.LOCALE).append("] = '").append(locale).append("') ");
        }

        StringBuilder andCondClause = new StringBuilder();
        StringBuilder orCondClause = new StringBuilder();

        addCondsToWhereClause(filter.getAndConds(), andCondClause, "AND");
        addCondsToWhereClause(filter.getOrConds(), orCondClause, "OR");

        final StringBuilder query = new StringBuilder("SELECT ").append(Constants.QUERY_DEFAULT_SELECTOR).
                append(".[").append(JcrConstants.JCR_UUID).append("] FROM [").append(returnType).append("] AS ").
                append(Constants.QUERY_DEFAULT_SELECTOR).append(' ');

        addJoinsAndCondsToQuery(filter.getChildConds(), query, whereClause, andCondClause, orCondClause);

        query.append("WHERE ").append(whereClause);
        if (andCondClause.length() > 0) {
            query.append("AND ").append(andCondClause);
        }
        if (orCondClause.length() > 0) {
            query.append("OR ").append(orCondClause);
        }

        if (orderBy.length() > 2) {
            query.append("ORDER BY ").append(orderBy.toString().substring(0, orderBy.length() - 2));
        }

        sqlQuery = query.toString();
    }

    public String getSqlQuery() {
        return sqlQuery;
    }
}
