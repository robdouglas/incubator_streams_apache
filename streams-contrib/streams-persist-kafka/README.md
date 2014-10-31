streams-persist-kafka
=====================

Read and write to Kafka

Example reader/writer configuration:

    kafka.metadata.broker.list=localhost:9092
    
    kafka.zk.connect=localhost:2181
    
    kafka.topic=topic
    
    kafka.groupid=group
    
java -cp streams.jar -Dconfig.file=application.conf \
        -Dkafka.metadata.broker.list=localhost:9092 class \
        -Dkafka.zk.connect=localhost:2181 \
        -Dkafka.topic=topic \
        -Dkafka.groupid=group
    
