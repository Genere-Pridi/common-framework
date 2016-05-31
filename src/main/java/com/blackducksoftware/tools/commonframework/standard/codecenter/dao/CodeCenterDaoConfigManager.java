/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 *  with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 *  under the License.
 *
 *******************************************************************************/

package com.blackducksoftware.tools.commonframework.standard.codecenter.dao;

import java.util.List;

import com.blackducksoftware.tools.commonframework.core.config.IConfigurationManager;

/**
 * An interface for ConfigurationManager subclasses that need to be consumable
 * by a CodeCenterDao object by managing a list of application custom attribute
 * names.
 * 
 * @author sbillings
 * 
 */
public interface CodeCenterDaoConfigManager extends IConfigurationManager {

    @Deprecated
    int getEstNumApps();

    @Deprecated
    void setEstNumApps(int estNumApps);

    /**
     * Add an application custom attribute to the list of attributes to track.
     * 
     * @param attrName
     */
    void addApplicationAttribute(String attrName);

    /**
     * Get the list of names of application custom attributes to track.
     * 
     * @return
     */
    List<String> getApplicationAttributeNames();

    @Deprecated
    String getCcDbServerName();

    @Deprecated
    void setCcDbServerName(String dbServer);

    @Deprecated
    int getCcDbPort();

    @Deprecated
    void setCcDbPort(int dbPort);

    @Deprecated
    String getCcDbUserName();

    @Deprecated
    void setCcDbUserName(String dbUser);

    @Deprecated
    String getCcDbPassword();

    @Deprecated
    void setCcDbPassword(String dbPassword);

    /**
     * Get the value of the "should ignore non-KB components" flag.
     * 
     * @return
     */
    boolean isSkipNonKbComponents();

}
