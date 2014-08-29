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

package org.apache.streams.peoplepattern;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import com.typesafe.config.Config;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;
import org.apache.streams.data.util.ActivityUtil;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.Actor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

/**
 * Enrich actor with demographics
 */
public class AccountTypeProcessor implements StreamsProcessor {

    private final static String STREAMS_ID = "AccountTypeProcessor";

    private final static String EXTENSION = "account_type";

    private final static Logger LOGGER = LoggerFactory.getLogger(AccountTypeProcessor.class);

    private ObjectMapper mapper = StreamsJacksonMapper.getInstance();

    private PeoplePatternConfiguration peoplePatternConfiguration = null;

    private String authHeader;

    public AccountTypeProcessor() {
        Config config = StreamsConfigurator.config.getConfig("peoplepattern");
        peoplePatternConfiguration = PeoplePatternConfigurator.detectPeoplePatternConfiguration(config);
        LOGGER.info("creating AccountTypeProcessor");
    }

    public AccountTypeProcessor(PeoplePatternConfiguration peoplePatternConfiguration) {
        this.peoplePatternConfiguration = peoplePatternConfiguration;
        LOGGER.info("creating AccountTypeProcessor");
    }

    String baseUrl = "https://people8accountv02.apiary-mock.com";

    String endpoint = "/v0.2/accounttype/v0.2/accounttype";

    @Override
    public List<StreamsDatum> process(StreamsDatum entry) {

        List<StreamsDatum> result = Lists.newArrayList();

        Activity activity = mapper.convertValue(entry.getDocument(), Activity.class);

        Actor actor = activity.getActor();

        URI uri = null;

        String username = (String) ActivityUtil.ensureExtensions(mapper.convertValue(actor, ObjectNode.class)).get("screenName");

        try {
            uri = new URIBuilder()
                    .setScheme("https")
                    //.setHost("people8accountv02.apiary-mock.com")
                    //.setPath("/v0.2/accounttype/v0.2/accounttype")
                    .setHost("api.peoplepattern.com")
                    .setPath("/v0.2/accounttype/")
                    .setParameter("id", actor.getId())
                    .setParameter("name", actor.getDisplayName())
                    .setParameter("username", username)
                    .setParameter("description", actor.getSummary())
                    .build();
        } catch (URISyntaxException e) {
            LOGGER.error("URI error {}", uri.toString());
            return result;
        }

        HttpGet httpget = new HttpGet(uri);
        httpget.addHeader("content-type", "application/json");
        httpget.addHeader("Authorization", String.format("Basic %s", authHeader));

        CloseableHttpClient httpclient = HttpClients.createDefault();

        CloseableHttpResponse response = null;
        try {
            //response = httpclient.execute(httpget, context);
            response = httpclient.execute(httpget);
            HttpEntity entity = response.getEntity();
            // TODO: handle rate-limiting
            if (response.getStatusLine().getStatusCode() == 200 && entity != null) {
                String entityString = EntityUtils.toString(entity);

                LOGGER.debug(entityString);

                AccountType accountType = mapper.readValue(entityString, AccountType.class);

                Map<String, Object> extensions = ActivityUtil.ensureExtensions(mapper.convertValue(actor, ObjectNode.class));

                extensions.put(EXTENSION, accountType);

                actor.setAdditionalProperty(ActivityUtil.EXTENSION_PROPERTY, extensions);

                LOGGER.debug("Actor: {}", actor);

                activity.setActor(actor);

                entry.setDocument(activity);

                result.add(entry);
            }
        } catch (IOException e) {
            LOGGER.error("IO error {} - {}", uri.toString(), response);
            return result;
        } finally {
            try {
                response.close();
            } catch (IOException e) {}
            try {
                httpclient.close();
            } catch (IOException e) {}
        }

        return result;

    }

    @Override
    public void prepare(Object configurationObject) {
        // TODO: one client object
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(peoplePatternConfiguration.getUsername());
        stringBuilder.append(":");
        stringBuilder.append(peoplePatternConfiguration.getPassword());
        String string = stringBuilder.toString();
        authHeader = Base64.encodeBase64String(string.getBytes());

    }

    @Override
    public void cleanUp() {
        LOGGER.info("shutting down AccountTypeProcessor");
    }
};
