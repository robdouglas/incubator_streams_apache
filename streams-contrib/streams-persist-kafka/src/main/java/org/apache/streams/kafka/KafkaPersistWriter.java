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

package org.apache.streams.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsPersistWriter;
import org.apache.streams.util.GuidUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class KafkaPersistWriter implements StreamsPersistWriter, Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaPersistWriter.class);

    protected volatile Queue<StreamsDatum> persistQueue;

    private ObjectMapper mapper = new ObjectMapper();

    private KafkaConfiguration config;

    private Producer<String, String> producer;

    public KafkaPersistWriter() {
       this(KafkaConfigurator.detectConfiguration(StreamsConfigurator.config.getConfig("kafka")));
    }

    public KafkaPersistWriter(KafkaConfiguration config) {
        this.config = config;
    }

    public void start() {
        Properties props = new Properties();

        props.put("metadata.broker.list", config.getBrokerlist());
        props.put("serializer.class", "kafka.serializer.StringEncoder");
        props.put("partitioner.class", "org.apache.streams.kafka.StreamsPartitioner");
        props.put("request.required.acks", "1");
        props.put("auto.create.topics.enable", "true");

        ProducerConfig config = new ProducerConfig(props);

        producer = new Producer<String, String>(config);

        new Thread(new KafkaPersistWriterTask(this)).start();
    }

    @Override
    public void write(StreamsDatum entry) {

        try {
            String text = mapper.writeValueAsString(entry);

            KeyedMessage<String, String> data = new KeyedMessage<String, String>(config.getTopic(), entry.getId(), text);

            producer.send(data);

        } catch (JsonProcessingException e) {
            LOGGER.warn("save: {}", e);
        }// put
    }

    @Override
    public void prepare(Object configurationObject) {
        this.persistQueue  = new ConcurrentLinkedQueue<StreamsDatum>();

        start();

    }

    @Override
    public void cleanUp() {
        producer.close();
    }
}
