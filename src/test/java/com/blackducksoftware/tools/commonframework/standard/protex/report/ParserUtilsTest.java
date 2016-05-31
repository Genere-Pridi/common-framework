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
package com.blackducksoftware.tools.commonframework.standard.protex.report;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.blackducksoftware.tools.commonframework.standard.protex.report.ParserUtils;

public class ParserUtilsTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testOneReplacement() {
	String expectedText = "Testing \"";
	String text = "Testing &quot;";

	String actualText = ParserUtils.decode(text);
	assertEquals(expectedText, actualText);
    }

    @Test
    public void testTwoReplacements() {
	String expectedText = "\" Testing \"";
	String text = "&quot; Testing &quot;";

	String actualText = ParserUtils.decode(text);
	assertEquals(expectedText, actualText);
    }

    @Test
    public void testMultipleReplacements() {
	String expectedText = "<p>&nbsp;Hello World\"\"\"</p>";
	String text = "<p>&nbsp;Hello World&quot;&quot;&quot;</p>";

	String actualText = ParserUtils.decode(text);
	assertEquals(expectedText, actualText);
    }

}
