/*
 *  Copyright 2009 Hippo.
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
import java.util.LinkedList;
import java.util.List;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import org.apache.wicket.model.IModel;
import org.hippoecm.frontend.i18n.model.NodeTranslator;
import org.hippoecm.frontend.model.JcrNodeModel;
import org.hippoecm.repository.api.NodeNameCodec;
import net.tirasa.hct.taxonomy.frontend.HCTTaxonomyNodeTypes;
import org.onehippo.taxonomy.api.Category;
import org.onehippo.taxonomy.api.TaxonomyNodeTypes;
import org.onehippo.taxonomy.plugin.api.EditableCategory;
import org.onehippo.taxonomy.plugin.api.EditableCategoryInfo;
import org.onehippo.taxonomy.plugin.api.TaxonomyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copied from original SVN for adding call to HCT custom taxonomy document navigation.
 *
 * @see
 * https://forge.onehippo.org/svn/taxonomy/taxonomy/tags/taxonomy-1.08.03/addon/frontend/src/main/java/org/onehippo/taxonomy/plugin/model/JcrCategory.java
 */
public class JcrCategory extends TaxonomyObject implements EditableCategory {
    private static final long serialVersionUID = 1L;

    static final Logger log = LoggerFactory.getLogger(JcrCategory.class);

    public JcrCategory(IModel<Node> nodeModel, boolean editable) {
        super(nodeModel, editable);
        try {
            if (!getNode().isNodeType(TaxonomyNodeTypes.NODETYPE_HIPPOTAXONOMY_CATEGORY)) {
                throw new RuntimeException("Invalid node type");
            }
        } catch (RepositoryException e) {
            throw new RuntimeException("Could not determine node type", e);
        }
    }

    public List<EditableCategory> getChildren() {
        List<EditableCategory> result = new LinkedList<EditableCategory>();
        try {
            Node node = getNode();
            for (NodeIterator iter = node.getNodes(); iter.hasNext();) {
                Node child = iter.nextNode();
                if (child.isNodeType(TaxonomyNodeTypes.NODETYPE_HIPPOTAXONOMY_CATEGORY)) {
                    result.add(toCategory(new JcrNodeModel(child), editable));
                }
            }
        } catch (RepositoryException ex) {
            log.error(ex.getMessage());
        }
        return result;
    }

    public String getName() {
        return new NodeTranslator(getNodeModel()).getNodeName().getObject();
    }

    public Category getParent() {
        try {
            Node node = getNode();
            Node parent = node.getParent();
            if (parent.isNodeType(TaxonomyNodeTypes.NODETYPE_HIPPOTAXONOMY_CATEGORY)) {
                return toCategory(new JcrNodeModel(parent), editable);
            }
        } catch (RepositoryException ex) {
            log.error(ex.getMessage());
        }
        return null;
    }

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
        } catch (RepositoryException ex) {
            log.error(ex.getMessage());
        }
        return ancestors;
    }

    public String getPath() {
        List<? extends Category> ancestors = getAncestors();
        StringBuilder path = new StringBuilder();
        for (Category ancestor : ancestors) {
            path.append(ancestor.getName());
            path.append("/");
        }
        return path.toString();
    }

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
            log.error(ex.getMessage());
        }
        return null;
    }

    public String getKey() {
        try {
            Node node = getNode();
            return node.getProperty(TaxonomyNodeTypes.HIPPOTAXONOMY_KEY).getString();
        } catch (RepositoryException ex) {
            log.error(ex.getMessage());
        }
        return null;
    }

    public EditableCategoryInfo getInfo(final String language) {
        try {
            Node node = getNode();
            if (JcrHelper.isNodeType(node, TaxonomyNodeTypes.NODETYPE_HIPPOTAXONOMY_TRANSLATED)) {
                NodeIterator translations = node.getNodes(TaxonomyNodeTypes.HIPPOTAXONOMY_TRANSLATION);
                while (translations.hasNext()) {
                    Node child = translations.nextNode();
                    String lang = child.getProperty("hippo:language").getString();
                    if (lang.equals(language)) {
                        return new JcrCategoryInfo(new JcrNodeModel(child), editable);
                    }
                }
            }         
            if (editable) {
                if (!JcrHelper.isNodeType(node, TaxonomyNodeTypes.NODETYPE_HIPPOTAXONOMY_TRANSLATED)) {
                    node.addMixin(TaxonomyNodeTypes.NODETYPE_HIPPOTAXONOMY_TRANSLATED);
                }
                Node child = node.addNode(TaxonomyNodeTypes.HIPPOTAXONOMY_TRANSLATION,
                        TaxonomyNodeTypes.NODETYPE_HIPPOTAXONOMY_TRANSLATION);
                child.setProperty("hippo:language", language);
                child.setProperty("hippo:message", NodeNameCodec.decode(node.getName()));
                
                // <HCT>
                if (!JcrHelper.isNodeType(node, HCTTaxonomyNodeTypes.NODETYPE_HIPPOTAXONOMY_FACETED)) {
                    node.addMixin(HCTTaxonomyNodeTypes.NODETYPE_HIPPOTAXONOMY_FACETED);
                }
                Node documents = node.addNode(HCTTaxonomyNodeTypes.NODENAME_HIPPOTAXONOMY_DOCUMENTS,
                        "hippofacnav:facetnavigation");
                documents.setProperty("hippo:docbase", node.getSession().getNode("/content/documents").getIdentifier());
                documents.setProperty("hippofacnav:facets", new String[]{"hippotaxonomy:keys"});
                documents.setProperty("hippofacnav:filters", new String[]{"hippotaxonomy:keys=" + node.getName()});
                // </HCT>   
                
                return new JcrCategoryInfo(new JcrNodeModel(child), editable);
            } else {
                return new EditableCategoryInfo() {

                    public void setDescription(String description) throws TaxonomyException {
                    }

                    public void setName(String name) throws TaxonomyException {
                    }

                    public void setSynonyms(String[] synonyms) throws TaxonomyException {
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

                };
            }
        } catch (RepositoryException ex) {
            log.error(ex.getMessage());
        }
        return null;
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

    public JcrCategory addCategory(String key, String name) throws TaxonomyException {
        try {
            return createCategory(getNode(), key, name);
        } catch (RepositoryException e) {
            throw new TaxonomyException("Could not create category", e);
        }
    }

    public void remove() throws TaxonomyException {
        checkEditable();

        try {
            getNode().remove();
        } catch (RepositoryException e) {
            throw new TaxonomyException("Could not remove category", e);
        }
    }

}