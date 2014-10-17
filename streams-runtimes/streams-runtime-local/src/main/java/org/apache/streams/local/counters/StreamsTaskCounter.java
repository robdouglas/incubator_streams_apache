/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
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
package org.apache.streams.local.counters;

import net.jcip.annotations.ThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 */
@ThreadSafe
public class StreamsTaskCounter implements StreamsTaskCounterMXBean{

    public static final String NAME_TEMPLATE = "org.apache.streams.local:type=DatumCounter,name=%s";
    private static final Logger LOGGER = LoggerFactory.getLogger(StreamsTaskCounter.class);

    private AtomicLong emitted;
    private AtomicLong received;
    private AtomicLong errors;

    public StreamsTaskCounter(String id) {
        this.emitted = new AtomicLong(0);
        this.received = new AtomicLong(0);
        this.errors = new AtomicLong(0);
        try {
            ObjectName name = new ObjectName(String.format(NAME_TEMPLATE, id));
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            mbs.registerMBean(this, name);
        } catch (MalformedObjectNameException | InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException e) {
            LOGGER.error("Failed to register MXBean : {}", e);
            throw new RuntimeException(e);
        }
    }

    public void incrementEmittedCount() {
        this.incrementEmittedCount(1);
    }

    public void incrementEmittedCount(long delta) {
        this.emitted.addAndGet(delta);
    }

    public void incrementErrorCount() {
        this.incrementErrorCount(1);
    }

    public void incrementErrorCount(long delta) {
        this.errors.addAndGet(delta);
    }

    public void incrementReceivedCount() {
        this.incrementReceivedCount(1);
    }

    public void incrementReceivedCount(long delta) {
        this.received.addAndGet(delta);
    }

    @Override
    public double getErrorRate() {
        if(this.received.get() == 0) {
            return 0.0;
        }
        return (double) this.errors.get() / (double) this.received.get();
    }

    @Override
    public long getNumEmitted() {
        return this.emitted.get();
    }

    @Override
    public long getNumReceived() {
        return this.received.get();
    }

    @Override
    public long getNumUnhandledErrors() {
        return this.errors.get();
    }
}
