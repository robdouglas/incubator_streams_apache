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

package com.datasift.test;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.streams.datasift.Datasift;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 *
 */
public class DatasiftSerDeTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(DatasiftSerDeTest.class);

    private ObjectMapper mapper = StreamsJacksonMapper.getInstance();




    @Test @Ignore
    public void Tests()
    {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, Boolean.TRUE);
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, Boolean.TRUE);
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, Boolean.TRUE);

        InputStream is = DatasiftSerDeTest.class.getResourceAsStream("/part-r-00000.json");
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);

        try {
            while (br.ready()) {
                String line = br.readLine();
                LOGGER.debug(line);
                System.out.println(line);
                Datasift ser = mapper.readValue(line, Datasift.class);

                String de = mapper.writeValueAsString(ser);

                LOGGER.debug(de);

                Datasift serde = mapper.readValue(de, Datasift.class);

//                Assert.assertEquals(ser, serde);

                LOGGER.debug(mapper.writeValueAsString(serde));
            }
        } catch( Exception e ) {
            e.printStackTrace();
            Assert.fail();
        }
    }
}
