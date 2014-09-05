package org.apache.streams.elasticsearch.processor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsonorg.JsonOrgModule;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;
import org.apache.streams.jackson.StreamsJacksonMapper;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by sblackmon on 9/5/14.
 */
public class MetadataAsDocumentProcessor implements StreamsProcessor {

    public final static String STREAMS_ID = "DocumentToMetadataProcessor";

    private ObjectMapper mapper;

    @Override
    public void prepare(Object configurationObject) {
        mapper = StreamsJacksonMapper.getInstance();
        mapper.registerModule(new JsonOrgModule());
    }

    @Override
    public void cleanUp() {

    }

    @Override
    public List<StreamsDatum> process(StreamsDatum entry) {
        List<StreamsDatum> result = Lists.newArrayList();
        ObjectNode metadataObjectNode;
        try {
            metadataObjectNode = mapper.readValue((String) entry.getDocument(), ObjectNode.class);
        } catch (IOException e) {
            return result;
        }

        Map<String, Object> metadata = asMap(metadataObjectNode);
        entry.setMetadata(metadata);
        result.add(entry);
        return result;
    }

    public Map<String, Object> asMap(JsonNode node) {

        Iterator<Map.Entry<String, JsonNode>> iterator = node.fields();
        Map<String, Object> ret = Maps.newHashMap();

        Map.Entry<String, JsonNode> entry;

        while (iterator.hasNext()) {
            entry = iterator.next();
            if( entry.getValue().asText() != null )
                ret.put(entry.getKey(), entry.getValue().asText());
        }

        return ret;
    }
}
