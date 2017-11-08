/**
 * CommonFramework
 *
 * Copyright (C) 2017 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.blackducksoftware.tools.commonframework.core.config.server.parser;

import java.io.FileReader;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackducksoftware.tools.commonframework.core.config.server.ServerBean;
import com.blackducksoftware.tools.commonframework.core.config.server.ServerBeanList;
import com.thoughtworks.xstream.XStream;

/**
 * The Class XMLParser.
 */
public class XMLParser implements IGenericServerParser {

    /** The log. */
    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());

    @Override
    public List<ServerBean> processServerConfiguration(FileReader fileReader) {
	ServerBeanList serverBeanList = null;

	XStream xstream = new XStream();

	try {
	    xstream.processAnnotations(ServerBean.class);
	    xstream.processAnnotations(ServerBeanList.class);

	    serverBeanList = (ServerBeanList) xstream.fromXML(fileReader);

	    log.debug("Deserialized XML");

	} catch (Exception e) {
	    log.error("Unable to process XML", e);
	}

	return serverBeanList.getServers();
    }

}
