package org.apache.streams.kafka.test;

import java.io.IOException;
import java.util.Properties;

import kafka.server.KafkaConfig;
import kafka.server.KafkaServerStartable;
import kafka.utils.TestUtils;

import org.apache.curator.test.TestingServer;

public class TestKafkaCluster {
    KafkaServerStartable kafkaServer;
    TestingServer zkServer;

    public TestKafkaCluster() throws Exception {
        zkServer = new TestingServer();
        KafkaConfig config = getKafkaConfig(zkServer.getConnectString());
        kafkaServer = new KafkaServerStartable(config);
        kafkaServer.startup();
    }

    private static KafkaConfig getKafkaConfig(final String
                                                      zkConnectString) {
        scala.collection.Iterator<Properties> propsI =
                TestUtils.createBrokerConfigs(1).iterator();
        assert propsI.hasNext();
        Properties props = propsI.next();
        assert props.containsKey("zookeeper.connect");
        props.put("zookeeper.connect", zkConnectString);
        return new KafkaConfig(props);
    }

    public String getKafkaBrokerString() {
        return String.format("localhost:%d",
                kafkaServer.serverConfig().port());
    }

    public String getZkConnectString() {
        return zkServer.getConnectString();
    }

    public int getKafkaPort() {
        return kafkaServer.serverConfig().port();
    }

    public void stop() throws IOException {
        kafkaServer.shutdown();
        zkServer.stop();
    }
}