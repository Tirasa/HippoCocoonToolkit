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
package net.tirasa.hct.repository.polling;

import java.util.LinkedList;
import java.util.List;
import javax.jcr.Session;
import org.hippoecm.hst.site.HstServices;
import org.onehippo.forge.repositoryeventlistener.hst.hippo.HippoEventSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HCTRepositoryPoller {

    private static final Logger LOG = LoggerFactory.getLogger(HCTRepositoryPoller.class);

    private List<HippoEventSubscriber> eventSubscribers = new LinkedList<HippoEventSubscriber>();

    private HCTEventPollingService hctEventPollingService;

    private List<HCTEventPollingThread> hippoEventPollingThreads;

    private void processInit() {
        hippoEventPollingThreads = new LinkedList<HCTEventPollingThread>();
        for (HippoEventSubscriber eventSubscriber : eventSubscribers) {
            HCTEventPollingThread hippoEventPollingThread =
                    new HCTEventPollingThread(eventSubscriber, hctEventPollingService);
            LOG.debug("Starting pooling service thread with name {}", eventSubscriber.getName());
            hippoEventPollingThread.setDaemon(true);
            hippoEventPollingThreads.add(hippoEventPollingThread);
            hippoEventPollingThread.start();
        }
    }

    public void destroy() {
        for (HCTEventPollingThread threadHippoEvent : hippoEventPollingThreads) {
            threadHippoEvent.stopThread();
        }
    }

    public void init() {
        LOG.debug("Starting poller");
        if (HstServices.isAvailable()) {
            processInit();
        } else {
            HippoRepositoryPollerThread hippoRepositoryPollerThread = new HippoRepositoryPollerThread();
            hippoRepositoryPollerThread.start();
        }
    }

    public void setEventSubscribers(List<HippoEventSubscriber> eventSubscribers) {
        this.eventSubscribers = eventSubscribers;
    }

    public void setHctEventPollingService(HCTEventPollingService hctEventPollingService) {
        this.hctEventPollingService = hctEventPollingService;
    }

    public class HippoRepositoryPollerThread extends Thread {

        private static final long THREAD_CHECKER = 2000L;

        private boolean threadRunning;

        public HippoRepositoryPollerThread() {
            super("HippoRepositoryPollerThread ");
            threadRunning = true;
        }

        @Override
        public void run() {
            while (threadRunning) {
                Session session = hctEventPollingService.getSession();
                if (HstServices.isAvailable() && session != null) {
                    processInit();
                    threadRunning = false;
                } else {
                    try {
                        sleep(THREAD_CHECKER);
                    } catch (InterruptedException iExp) {
                        LOG.error("thread is Interrupted", iExp);
                    }
                }
                if (session != null) {
                    session.logout();
                }
            }
        }
    }
}
