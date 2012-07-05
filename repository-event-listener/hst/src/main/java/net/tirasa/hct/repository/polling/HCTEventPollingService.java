/*
 * Copyright (C) 2012 Tirasa.
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
package net.tirasa.hct.repository.polling;

import java.util.LinkedList;
import java.util.List;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import org.onehippo.forge.repositoryeventlistener.hst.hippo.PollingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HCTEventPollingService implements PollingService {

    private static final Logger LOG = LoggerFactory.getLogger(HCTEventPollingService.class);

    private static final String HST_EVENTS_PATH = "/hippo:configuration/hippo:modules/hstevents/hippo:moduleconfig";

    private static final String HSTEVENTS_CLUSTERLOG_ITEM = "hstevents:clusterlogitem";

    private static final String HSTEVENTS_PREFIX = "hstevents:";

    private static final String JACKRABBIT_CLUSTER_NODE_ID = "org.apache.jackrabbit.core.cluster.node_id";

    private static final String QUERY_PART1 =
            "//hippo:log//*[@jcr:primaryType='hippolog:item' "
            + "and (@hippolog:eventClass ='org.hippoecm.repository.reviewedactions.FullReviewedActionsWorkflowImpl' "
            + "or @hippolog:eventClass ='org.hippoecm.repository.reviewedactions.BasicReviewedActionsWorkflowImpl' "
            + "or @hippolog:eventClass ='org.hippoecm.repository.standardworkflow.FolderWorkflowImpl') "
            + "and hippolog:timestamp > ";

    private static final String QUERY_PART2 = " ] order by @hippolog:timestamp ascending";

    private final SimpleCredentials defaultCredentials;

    private final SimpleCredentials writableCredentials;

    private String moduleId = "default";

    private long pollingTime;

    private Repository repository;

    public HCTEventPollingService(Repository repository, SimpleCredentials defaultCredentials,
            SimpleCredentials writableCredentials) {

        this.repository = repository;
        this.defaultCredentials = defaultCredentials;
        this.writableCredentials = writableCredentials;
        pollingTime = 5000L;
    }

    @Override
    public synchronized void writeLastProcessed(String subscriberName, Long timeStamp)
            throws RepositoryException {

        LOG.debug("subscriberName           {}", subscriberName);
        LOG.debug("processed item           {}", timeStamp.toString());

        Session wSession = null;
        try {
            wSession = getWritableSession();
            Node clusterNode = wSession.getNode(HST_EVENTS_PATH).hasNode(moduleId) ? wSession.getNode(HST_EVENTS_PATH).
                    getNode(moduleId) : wSession.getNode(HST_EVENTS_PATH).addNode(moduleId, HSTEVENTS_CLUSTERLOG_ITEM);
            clusterNode.setProperty(HSTEVENTS_PREFIX + subscriberName, timeStamp);
            wSession.save();
        } catch (ItemNotFoundException infExp) {
            LOG.error("Error in saving last node value ", infExp);
        } finally {
            if (wSession != null) {
                wSession.logout();
            }
        }
    }

    @Override
    public long getLastProcessed(String subscriberName) throws RepositoryException {
        LOG.debug("subscriberName           {}", subscriberName);
        Session session = null;
        long lastItem = -1L;
        try {
            session = getSession();
            if (session.getNode(HST_EVENTS_PATH).hasNode(moduleId)) {
                Node clusterNode = session.getNode(HST_EVENTS_PATH).getNode(moduleId);
                if (clusterNode != null && clusterNode.hasProperty(HSTEVENTS_PREFIX + subscriberName)) {
                    lastItem = clusterNode.getProperty(HSTEVENTS_PREFIX + subscriberName).getLong();
                }
            }
        } catch (ItemNotFoundException infExp) {
            LOG.error("Error in getting last processed item", infExp);
        } finally {
            if (session != null) {
                session.logout();
            }
        }
        LOG.debug("last item is             {}", lastItem);
        return lastItem;
    }

    @Override
    public List<Node> getNextLogNodes(String subscriberName, long lastItem) throws RepositoryException {
        LOG.debug("subscriberName           {}", subscriberName);
        LOG.debug("lastItem processed item  {}", lastItem);
        Session session = null;
        try {
            List<Node> nodes = new LinkedList<Node>();
            session = getSession();
            Query query = session.getWorkspace().getQueryManager().
                    createQuery(QUERY_PART1 + lastItem + QUERY_PART2, Query.XPATH);
            QueryResult queryResult = query.execute();
            NodeIterator nodeIterator = queryResult.getNodes();
            while (nodeIterator.hasNext()) {
                Node logNode = nodeIterator.nextNode();
                if (logNode != null) {
                    nodes.add(logNode);
                }
            }
            return nodes;
        } finally {
            if (session != null) {
                session.logout();
            }
        }
    }

    private Session getWritableSession() throws RepositoryException {
        return repository.login(writableCredentials);
    }

    public void destroy() {
        repository = null;
    }

    /**
     * Polling frequency in milli seconds
     *
     * @return
     */
    public long getPollingTime() {
        return pollingTime;
    }

    /**
     * This method will return a new session
     *
     * @return
     */
    public Session getSession() {
        Session jcrSession = null;
        try {
            jcrSession = repository.login(defaultCredentials);
            return jcrSession;
        } catch (RepositoryException rExp) {
            LOG.error("Error in getting session", rExp);
        }
        return jcrSession;
    }

    public void init() {
        String clusterValue = System.getProperty(JACKRABBIT_CLUSTER_NODE_ID);
        if (clusterValue != null) {
            LOG.debug("Cluster is defined   {}", clusterValue);
            moduleId = clusterValue;
            LOG.debug("Cluster id is            {}", moduleId);
        }
    }

    /**
     * Set the Polling frequency in milli seconds
     *
     * @return
     */
    public void setPollingTime(long pollingTime) {
        this.pollingTime = pollingTime;
    }
}
