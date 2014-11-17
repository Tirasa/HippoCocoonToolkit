/*
 *  Copyright 2009-2013 Hippo B.V. (http://www.onehippo.com)
 *  Copyright 2012-2013 Tirasa.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.onehippo.taxonomy.plugin.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import net.tirasa.hct.taxonomy.frontend.HCTTaxonomyNodeTypes;

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.map.LazyMap;
import org.apache.wicket.model.IModel;
import org.hippoecm.frontend.i18n.model.NodeTranslator;
import org.hippoecm.frontend.model.JcrNodeModel;
import org.hippoecm.repository.api.HippoNodeType;
import org.hippoecm.repository.api.NodeNameCodec;
import org.onehippo.taxonomy.api.Category;
import org.onehippo.taxonomy.api.CategoryInfo;
import org.onehippo.taxonomy.api.TaxonomyNodeTypes;
import org.onehippo.taxonomy.plugin.api.EditableCategory;
import org.onehippo.taxonomy.plugin.api.EditableCategoryInfo;
import org.onehippo.taxonomy.plugin.api.TaxonomyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JcrCategory extends TaxonomyObject implements EditableCategory {

    private static final Logger LOG = LoggerFactory.getLogger(JcrCategory.class);

    private static final long serialVersionUID = 4494723358209679816L;

    public JcrCategory(IModel<Node> nodeModel, boolean editable) throws TaxonomyException {
        super(nodeModel, editable);
        try {
            final Node node = getNode();
            if (!node.isNodeType(TaxonomyNodeTypes.NODETYPE_HIPPOTAXONOMY_CATEGORY)) {
                throw new TaxonomyException("Node " + node.getPath() + " is not of type "
                        + TaxonomyNodeTypes.NODETYPE_HIPPOTAXONOMY_CATEGORY);
            }
        } catch (RepositoryException re) {
            throw new TaxonomyException("Error accessing node while creating JcrCategory object", re);
        }
    }

    @Override
    public List<EditableCategory> getChildren() {
        List<EditableCategory> result = new LinkedList<EditableCategory>();
        try {
            final Node node = getNode();
            final String nodePath = node.getPath();
            for (NodeIterator iter = node.getNodes(); iter.hasNext();) {
                Node child = iter.nextNode();
                if (child != null) {
                    try {
                        if (child.isNodeType(TaxonomyNodeTypes.NODETYPE_HIPPOTAXONOMY_CATEGORY)) {
                            result.add(toCategory(new JcrNodeModel(child), editable));
                        }
                    } catch (RepositoryException re) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Can't create a child category below " + nodePath, re);
                        } else {
                            LOG.warn("Can't create a child category below {}, message is {}", nodePath, re.getMessage());
                        }
                    } catch (TaxonomyException te) {
                        LOG.warn("TaxonomyException: can't create a child category below {}, message is {}" + nodePath,
                                te.getMessage());
                    }
                }
            }
        } catch (RepositoryException ex) {
            LOG.error("Failure getting category children", ex);
        }
        return result;
    }

    @Override
    public String getName() {
        return new NodeTranslator(getNodeModel()).getNodeName().getObject();
    }

    @Override
    public Category getParent() {
        try {
            Node node = getNode();
            Node parent = node.getParent();
            if (parent.isNodeType(TaxonomyNodeTypes.NODETYPE_HIPPOTAXONOMY_CATEGORY)) {
                return toCategory(new JcrNodeModel(parent), editable);
            }
        } catch (TaxonomyException te) {
            LOG.error("Parent not accessible", te);
        } catch (RepositoryException ex) {
            LOG.error(ex.getMessage());
        }
        return null;
    }

    @Override
    public List<? extends EditableCategory> getAncestors() {
        List<JcrCategory> ancestors = new LinkedList<JcrCategory>();
        ancestors.add(this);
        try {
            Node node = getNode();
            while (node.getDepth() > 0) {
                Node parent = node.getParent();
                if (parent.isNodeType(TaxonomyNodeTypes.NODETYPE_HIPPOTAXONOMY_CATEGORY)) {
                    ancestors.add(toCategory(new JcrNodeModel(parent), editable));
                } else if (parent.isNodeType(TaxonomyNodeTypes.NODETYPE_HIPPOTAXONOMY_TAXONOMY)) {
                    break;
                }
                node = parent;
            }
            Collections.reverse(ancestors);
        } catch (TaxonomyException te) {
            LOG.error("Can't create accurate list of ancestors", te.getMessage());
        } catch (RepositoryException ex) {
            LOG.error(ex.getMessage());
        }
        return ancestors;
    }

    @Override
    public String getPath() {
        List<? extends Category> ancestors = getAncestors();
        StringBuilder path = new StringBuilder();
        for (Category ancestor : ancestors) {
            path.append(ancestor.getName());
            path.append("/");
        }
        return path.toString();
    }

    @Override
    public JcrTaxonomy getTaxonomy() {
        try {
            Node node = getNode();
            while (node.getDepth() > 0) {
                Node parent = node.getParent();
                if (parent.isNodeType(TaxonomyNodeTypes.NODETYPE_HIPPOTAXONOMY_TAXONOMY)) {
                    return toTaxonomy(new JcrNodeModel(parent), editable);
                }
                node = parent;
            }
        } catch (RepositoryException ex) {
            LOG.error(ex.getMessage());
        }
        return null;
    }

    @Override
    public String getKey() {
        try {
            Node node = getNode();
            return node.getProperty(TaxonomyNodeTypes.HIPPOTAXONOMY_KEY).getString();
        } catch (RepositoryException ex) {
            LOG.error(ex.getMessage());
        }
        return null;
    }

    @Override
    public EditableCategoryInfo getInfo(final String language) {
        try {
            Node node = getNode();
            // <HCT>
            if (editable) {
                final NodeIterator docChidlren = node.getNodes(HCTTaxonomyNodeTypes.NODENAME_HIPPOTAXONOMY_DOCUMENTS);
                if (docChidlren == null || !docChidlren.hasNext()) {
                    if (!JcrHelper.isNodeType(node, HCTTaxonomyNodeTypes.NODETYPE_HIPPOTAXONOMY_FACETED)) {
                        node.addMixin(HCTTaxonomyNodeTypes.NODETYPE_HIPPOTAXONOMY_FACETED);
                    }
                    final Node documents = node.addNode(HCTTaxonomyNodeTypes.NODENAME_HIPPOTAXONOMY_DOCUMENTS,
                            "hippofacnav:facetnavigation");
                    documents.setProperty("hippo:docbase",
                            node.getSession().getNode("/content/documents").getIdentifier());
                    documents.setProperty("hippofacnav:facets",
                            new String[] { TaxonomyNodeTypes.HIPPOTAXONOMY_KEYS });
                    documents.setProperty("hippofacnav:filters",
                            new String[] { TaxonomyNodeTypes.HIPPOTAXONOMY_KEYS + "=" + this.getKey() });
                    documents.setProperty("hippofacnav:limit", 10000);
                }
            }
            // </HCT>

            if (JcrHelper.isNodeType(node, TaxonomyNodeTypes.NODETYPE_HIPPOTAXONOMY_TRANSLATED)) {
                NodeIterator translations = node.getNodes(TaxonomyNodeTypes.HIPPOTAXONOMY_TRANSLATION);
                while (translations.hasNext()) {
                    Node child = translations.nextNode();
                    if (child != null) {
                        try {
                            String lang = child.getProperty(HippoNodeType.HIPPO_LANGUAGE).getString();
                            if (lang.equals(language)) {
                                return new JcrCategoryInfo(new JcrNodeModel(child), editable);
                            }
                        } catch (PathNotFoundException pnfe) {
                            LOG.warn("PathNotFoundException accessing {}", HippoNodeType.HIPPO_LANGUAGE, pnfe);
                        }
                    }
                }
            }
            if (editable) {
                if (!JcrHelper.isNodeType(node, TaxonomyNodeTypes.NODETYPE_HIPPOTAXONOMY_TRANSLATED)) {
                    node.addMixin(TaxonomyNodeTypes.NODETYPE_HIPPOTAXONOMY_TRANSLATED);
                }
                Node child = node.addNode(TaxonomyNodeTypes.HIPPOTAXONOMY_TRANSLATION,
                        TaxonomyNodeTypes.NODETYPE_HIPPOTAXONOMY_TRANSLATION);
                child.setProperty(HippoNodeType.HIPPO_LANGUAGE, language);
                child.setProperty(HippoNodeType.HIPPO_MESSAGE, NodeNameCodec.decode(node.getName()));
                return new JcrCategoryInfo(new JcrNodeModel(child), editable);
            } else {
                return new EditableCategoryInfo() {

                    public void setDescription(String description) throws TaxonomyException {
                    }

                    public void setName(String name) throws TaxonomyException {
                    }

                    public void setSynonyms(String[] synonyms) throws TaxonomyException {
                    }

                    public Node getNode() throws ItemNotFoundException {
                        return null;
                    }

                    public String getDescription() {
                        return "";
                    }

                    public String getLanguage() {
                        return language;
                    }

                    public String getName() {
                        return JcrCategory.this.getName();
                    }

                    public String[] getSynonyms() {
                        return new String[0];
                    }

                    public Map<String, Object> getProperties() {
                        return Collections.emptyMap();
                    }

                    public String getString(String property) {
                        return "";
                    }

                    public String getString(String property, String defaultValue) {
                        return "";
                    }

                    public String[] getStringArray(String property) {
                        return new String[0];
                    }

                    public void setString(String property, String value) throws TaxonomyException {
                    }

                    public void setStringArray(String property, String[] values) throws TaxonomyException {
                    }
                };
            }
        } catch (RepositoryException ex) {
            LOG.error(ex.getMessage());
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, ? extends CategoryInfo> getInfos() {
        Map<String, ? extends CategoryInfo> map = new HashMap<String, EditableCategoryInfo>();
        return LazyMap.decorate(map,
                new Transformer() {

                    @Override
                    public Object transform(Object language) {
                        return getInfo((String) language);
                    }
                });
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof JcrCategory) {
            return ((JcrCategory) obj).getNodeModel().equals(getNodeModel());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getNodeModel().hashCode() ^ 9887;
    }

    @Override
    public JcrCategory addCategory(String key, String name, String locale) throws TaxonomyException {
        try {
            return createCategory(getNode(), key, name, locale);
        } catch (RepositoryException e) {
            throw new TaxonomyException("Could not create category with key " + key + ", name " + name + " and locale "
                    + locale, e);
        }
    }

    @Override
    public void remove() throws TaxonomyException {
        checkEditable();

        try {
            getNode().remove();
        } catch (RepositoryException e) {
            throw new TaxonomyException("Could not remove category", e);
        }
    }

}
