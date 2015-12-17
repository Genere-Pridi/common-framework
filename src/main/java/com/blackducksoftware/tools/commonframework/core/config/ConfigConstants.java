/*******************************************************************************
 * Copyright (C) 2015 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version 2 only
 * as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License version 2
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *******************************************************************************/
package com.blackducksoftware.tools.commonframework.core.config;

/**
 * This class provides access to a set of constants used by the config classes.
 * 
 * @author sbillings
 * 
 */
public class ConfigConstants {

    /**
     * The type of Black Duck Application that you want to configure for. <br>
     * CODECENTER - Black Duck Code Center <br>
     * PROTEX - Black Duck Code Center <br>
     * HUB - the Hub, not yet released <br>
     * GENERIC - Not a specific Black Duck App
     */
    public static enum APPLICATION {
        CODECENTER, PROTEX, HUB, GENERIC
    };

    // Name of the servers constant
    protected static final String SERVER_LIST_LOCATION = "server.list.location";

    protected static final String APPLICATION_NAME_PROPERTY = "application";

    // SERVER PROPERTIES
    // Name of overall container in the server config file
    public static final String SERVER_PROPERTY = "server";

    public static final String SERVERS_PROPERTY = "servers";

    protected static final String GENERIC_SERVER_NAME_PROPERTY_SUFFIX = "server.name";

    protected static final String GENERIC_USER_NAME_PROPERTY_SUFFIX = "user.name";

    protected static final String GENERIC_PASSWORD_PROPERTY_SUFFIX = "password";

    protected static final String GENERIC_ALIAS_PROPERTY_SUFFIX = "alias";

    protected static final String PASSWORD_ISENCRYPTED_SUFFIX = "isencrypted";

    protected static final String PASSWORD_ISPLAINTEXT_SUFFIX = "isplaintext";

    // Basic Protex Server props
    protected static final String PROTEX_PREFIX_PROPERTY = "protex";

    protected static final String PROTEX_SERVER_NAME_PROPERTY = PROTEX_PREFIX_PROPERTY
            + "." + GENERIC_SERVER_NAME_PROPERTY_SUFFIX;

    protected static final String PROTEX_USER_NAME_PROPERTY = PROTEX_PREFIX_PROPERTY
            + "." + GENERIC_USER_NAME_PROPERTY_SUFFIX;

    protected static final String PROTEX_PASSWORD_PROPERTY = PROTEX_PREFIX_PROPERTY
            + "." + GENERIC_PASSWORD_PROPERTY_SUFFIX;

    // Basic CC server props
    protected static final String CODE_CENTER_PREFIX_PROPERTY = "cc";

    protected static final String CC_SERVER_NAME_PROPERTY = CODE_CENTER_PREFIX_PROPERTY
            + "." + GENERIC_SERVER_NAME_PROPERTY_SUFFIX;

    protected static final String CC_USER_NAME_PROPERTY = CODE_CENTER_PREFIX_PROPERTY
            + "." + GENERIC_USER_NAME_PROPERTY_SUFFIX;

    protected static final String CC_PASSWORD_PROPERTY = CODE_CENTER_PREFIX_PROPERTY
            + "." + GENERIC_PASSWORD_PROPERTY_SUFFIX;

    // OBSOLETE: Optional way to force psw to be interpreted as plain text
    protected static final String PSW_ISPLAINTEXT_PROPERTY_SUFFIX = "password.isplaintext";

    // Optional way to force psw to be interpreted as encrypted
    protected static final String PSW_ISENCRYPTED_PROPERTY_SUFFIX = "password.isencrypted";

    protected static final String PROTEX_PSW_ISPLAINTEXT_PROPERTY = PROTEX_PREFIX_PROPERTY
            + "." + PSW_ISPLAINTEXT_PROPERTY_SUFFIX;

    protected static final String CC_PSW_ISPLAINTEXT_PROPERTY = CODE_CENTER_PREFIX_PROPERTY
            + "." + PSW_ISPLAINTEXT_PROPERTY_SUFFIX;

    // Mandatory Email Information
    protected final static String EMAIL_SMTP_ADDRESS = "email.smtp.address";

    protected final static String EMAIL_SMTP_TO_FIELD = "email.smtp.to";

    protected final static String EMAIL_SMTP_FROM_FIELD = "email.smtp.from";

    // Optional Email Information
    protected final static String EMAIL_TRIGGER_RULES = "email.trigger.rules";

    protected final static String EMAIL_SMTP_USE_AUTH = "email.use.auth";

    protected final static String EMAIL_SMTP_AUTH_LOGIN = "email.auth.username";

    protected final static String EMAIL_AUTH_PASSWORD_PREFIX = "email.auth";

    protected final static String EMAIL_SMTP_AUTH_PASSWORD = EMAIL_AUTH_PASSWORD_PREFIX
            + ".password";

    protected final static String EMAIL_PROTOCOL = "email.protocol";

    protected final static String EMAIL_SMTP_PORT = "email.smtp.port";

    // Proxy Information
    protected final static String PROXY_SERVER = "proxy.server";

    protected final static String PROXY_PORT = "proxy.port";

    protected final static String PROXY_HTTPS_SERVER = "proxy.https.server";

    protected final static String PROXY_HTTPS_PORT = "proxy.https.port";

    // SDK OPTIONS
    protected final static String SDK_TIMEOUT_SUFFIX = "sdk.timeout";

    protected final static String SDK_CHILD_COUNT = "sdk.cxf.children";

    // Protex Project Data
    protected static final String PROJECT_NAME_PROPERTY = "project.name";

    protected static final String PROJECT_ID_PROPERTY = "project.id";

    // Template mappings
    protected static final String PROPERTY_TEMPLATE_MAPPING = "template.mapping";

    // The number of SDK lines you want processed at any given time (all vs by chunks)
    // size = 0: process the rows without chunking
    // size > 0: process the rows by chunk size
    public static final String PROPERTY_CHUNKING_SIZE = "sdk.chunk.size";
}
