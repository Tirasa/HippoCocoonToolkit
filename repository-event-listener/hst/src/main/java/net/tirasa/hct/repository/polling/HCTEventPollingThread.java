/*
 * Copyright (C) Tirasa
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

import java.util.Collection;
import java.util.List;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import org.onehippo.forge.repositoryeventlistener.hst.events.BaseHippoEventSubscriber;
import org.onehippo.forge.repositoryeventlistener.hst.events.HippoDocumentEvent;
import org.onehippo.forge.repositoryeventlistener.hst.hippo.EventType;
import org.onehippo.forge.repositoryeventlistener.hst.hippo.HippoEvent;
import org.onehippo.forge.repositoryeventlistener.hst.hippo.HippoEventSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HCTEventPollingThread extends Thread {

    private static final Logger LOG = LoggerFactory.getLogger(HCTEventPollingThread.class);

    private static final long DEFAULT_TIMESTAMP = -1L;

    private static final String HIPPOLOG_EVENT_DOCUMENT = "hippolog:eventDocument";

    private static final String HIPPOLOG_EVENT_METHOD = "hippolog:eventMethod";

    private static final String HIPPOLOG_EVENT_USER = "hippolog:eventUser";

    private static final String HIPPOLOG_TIMESTAMP = "hippolog:timestamp";

    private static final String SQUARE_BRACKET_SUFFIX = "]";

    /**
     * This method returns path that is affected by event
     *
     * @param documentPath
     * @return
     */
    private static String getDocumentNode(String documentPath) {
        String path = documentPath;
        if (documentPath.endsWith(SQUARE_BRACKET_SUFFIX)) {
            path = documentPath.substring(0, documentPath.indexOf('['));
        }
        return path;
    }

    private final HippoEventSubscriber eventSubscriber;

    private final HCTEventPollingService hctEventPollingService;

    private volatile boolean keepRunning = true;

    public HCTEventPollingThread(HippoEventSubscriber eventSubscriber, HCTEventPollingService hctEventPollingService) {
        super("HST Event Polling Thread - " + eventSubscriber.getName());
        this.eventSubscriber = eventSubscriber;
        this.hctEventPollingService = hctEventPollingService;
    }

    @Override
    public void run() {
        while (keepRunning) {
            try {
                if (!((BaseHippoEventSubscriber) eventSubscriber).isRunning()) {
                    keepRunning = false;
                    return;
                }
                long lastProcessItem = hctEventPollingService.getLastProcessed(eventSubscriber.getName());
                LOG.debug("Getting latest log items from {}", lastProcessItem);
                List<Node> logItems =
                        hctEventPollingService.getNextLogNodes(eventSubscriber.getName(), lastProcessItem);
                long timeStamp = processEvents(logItems);
                if (timeStamp > -1L) {
                    hctEventPollingService.writeLastProcessed(eventSubscriber.getName(), timeStamp);
                }
                synchronized (this) {
                    wait(hctEventPollingService.getPollingTime());
                }
            } catch (InterruptedException iExp) {
                keepRunning = false;
                LOG.error("Error during running thread", iExp);
            } catch (RepositoryException rExp) {
                keepRunning = false;
                LOG.error("Error during running thread", rExp);
            }
        }

    }

    /**
     * This method is responsible creating hippo event from log node. It will parse all the properties of log item and
     * populate in hippoevent
     *
     * @param logNode
     * @return
     */
    private HippoEvent createEvent(Node logNode) {
        HippoDocumentEvent hippoEvent = null;
        try {
            hippoEvent = new HippoDocumentEvent();
            if (logNode.hasProperty(HIPPOLOG_EVENT_METHOD)) {
                String eventMethod = logNode.getProperty(HIPPOLOG_EVENT_METHOD).getString();

                try {
                    hippoEvent.setType(EventType.valueOf(eventMethod));
                } catch (IllegalArgumentException e) {
                    LOG.debug("Event method is parsed by HippoEventSubscriber {} , ignore the event ", eventMethod);
                    return null;
                }
            }

            if (logNode.hasProperty(HIPPOLOG_EVENT_DOCUMENT)) {
                String documentPath = logNode.getProperty(HIPPOLOG_EVENT_DOCUMENT).getString();
                Node workFlowNode;
                if (documentPath.endsWith(SQUARE_BRACKET_SUFFIX)) {
                    documentPath = getDocumentNode(documentPath);
                    workFlowNode = hctEventPollingService.getSession().getNode(documentPath);
                } else {
                    workFlowNode = hctEventPollingService.getSession().getNode(documentPath);
                }

                if (workFlowNode != null) {
                    hippoEvent.setIdentifier(workFlowNode.getParent().getIdentifier());
                    hippoEvent.setPath(workFlowNode.getParent().getPath());
                } else {
                    if (hippoEvent.getType() == EventType.publish) {
                        LOG.error("Document node is not found returning null");
                        return null;
                    } else if (hippoEvent.getType() == EventType.depublish) {
                        LOG.debug("Document is un-published and removed, trying to get document path from log node");
                        hippoEvent.setPath(logNode.getProperty(HIPPOLOG_EVENT_DOCUMENT).getString());
                    }
                }
            }
            if (logNode.hasProperty(HIPPOLOG_TIMESTAMP)) {
                long timeStamp = logNode.getProperty(HIPPOLOG_TIMESTAMP).getLong();
                hippoEvent.setDate(timeStamp);
            }
            if (logNode.hasProperty(HIPPOLOG_EVENT_USER)) {
                String eventUser = logNode.getProperty(HIPPOLOG_EVENT_USER).getString();
                hippoEvent.setUserID(eventUser);
            }
        } catch (RepositoryException rExp) {
            LOG.error("Error in processing events " + rExp);
        }
        return hippoEvent;
    }

    /**
     * This method will call the onEvent method of subscriber
     *
     * @param hippoEvent
     * @return
     */
    private boolean processEvent(HippoEvent hippoEvent) {
        if (hippoEvent == null) {
            LOG.debug("Event instance is null, Ignore event");
            return true;
        }
        Session jcrSession = null;
        boolean eventFired = false;
        try {
            jcrSession = hctEventPollingService.getSession();
            if (jcrSession != null) {
                LOG.debug("firing the event {}", hippoEvent);
                ((BaseHippoEventSubscriber) eventSubscriber).onEvent(jcrSession, hippoEvent);
                eventFired = true;
            } else {
                LOG.debug("Unavailable to get session");
                LOG.info("Event {} is not fired, due un-availability of session ", hippoEvent);
            }
        } finally {
            if (jcrSession != null) {
                jcrSession.logout();
            }
        }
        return eventFired;
    }

    /**
     * Process the logNodes for events
     *
     * @param logItems
     * @return
     * @throws RepositoryException
     */
    private Long processEvents(Collection<Node> logItems) throws RepositoryException {
        if (logItems == null || logItems.isEmpty()) {
            LOG.debug("No pending log items to process");
            return DEFAULT_TIMESTAMP;
        }
        Long timeStamp = DEFAULT_TIMESTAMP;
        for (Node logItem : logItems) {
            if (!processEvent(createEvent(logItem))) {
                return timeStamp;
            }
            timeStamp = logItem.getProperty(HIPPOLOG_TIMESTAMP).getLong();
        }
        return timeStamp;
    }

    /**
     * Invoking this method will stop this thread.
     */
    public void stopThread() {
        keepRunning = false;
    }
}
