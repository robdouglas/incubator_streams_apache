package org.apache.streams.kafka.test;

import com.google.common.collect.Lists;
import kafka.admin.AdminUtils;
import kafka.utils.TestUtils;
import kafka.utils.ZKStringSerializer$;
import org.I0Itec.zkclient.ZkClient;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.kafka.KafkaConfiguration;
import org.apache.streams.kafka.KafkaPersistReader;
import org.apache.streams.kafka.KafkaPersistWriter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import scala.collection.JavaConversions;
import scala.collection.Seq;

import java.util.Properties;

/**
 * Created by sblackmon on 10/20/14.
 */
public class TestKafkaPersist {

    private TestKafkaCluster testKafkaCluster;
    private KafkaConfiguration testConfiguration;

    private String testTopic = "testTopic";

    @Before
    public void prepareTest() {


        try {
            testKafkaCluster = new TestKafkaCluster();
        } catch (Throwable e ) {
            e.printStackTrace();
        }

        testConfiguration = new KafkaConfiguration();
        testConfiguration.setBrokerlist(testKafkaCluster.getKafkaBrokerString());
        testConfiguration.setZkconnect(testKafkaCluster.getZkConnectString());
        testConfiguration.setTopic(testTopic);

        ZkClient zkClient = new ZkClient(testKafkaCluster.getZkConnectString(), 1000, 1000, ZKStringSerializer$.MODULE$);

        AdminUtils.createTopic(zkClient, testTopic, 1, 1, new Properties());
    }

    @Test
    public void testPersistWriterString() {

        assert(testConfiguration != null);
        assert(testKafkaCluster != null);

        KafkaPersistWriter testPersistWriter = new KafkaPersistWriter(testConfiguration);
        testPersistWriter.prepare(null);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException ie) {
            //Handle exception
        }

        String testJsonString = "{\"dummy\":\"true\"}";

        testPersistWriter.write(new StreamsDatum(testJsonString, "test"));

        testPersistWriter.cleanUp();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException ie) {
            //Handle exception
        }

        KafkaPersistReader testPersistReader = new KafkaPersistReader(testConfiguration);
        try {
            testPersistReader.prepare(null);
        } catch( Throwable e ) {
            e.printStackTrace();
            Assert.fail();
        }

        testPersistReader.startStream();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException ie) {
            //Handle exception
        }

        StreamsResultSet testResult = testPersistReader.readCurrent();

        testPersistReader.cleanUp();

        assert(testResult.size() == 1);

    }
}
