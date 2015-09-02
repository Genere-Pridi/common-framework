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

import java.util.Properties;
import java.util.Scanner;

import org.apache.commons.codec.binary.Base64;

import com.blackducksoftware.tools.commonframework.core.encryption.Password;

/**
 * Manages a single password as it is set in a configuration Properties object.
 * Use one of the two static factory methods (createFromProperty() or
 * createFromLine()) to create an instance. Figures out (best it can) whether it
 * is provided as plain text (reliable), base64-encoded (which might actually be
 * plain text that just happens to use only base64-encoding characters), or
 * encrypted/ascii85-encoded. Modern config files will only have plain text or
 * encrypted/Ascii85-encoded passwords. Legacy files may have base64-encoded
 * passwords. Has a single method that returns the password in plain text.
 * 
 * @author sbillings
 * 
 */
public class ConfigurationPassword {

    // Consider lazy loading some of these properties
    private String passwordInPlainText;
    private Properties props;
    private PasswordMetaProperties passwordMetaProperties;
    private final String propertyName;

    /**
     * This enum describes the state of the password as found in the config
     * file. If it's a modern config file, the user may or may not have
     * specified the "is encrypted" property. If it's a legacy property file,
     * the user may or may not have specified the "is plain text" property. If
     * the user has included both the "is plain text" and "is encrypted"
     * properties, the config file is invalid and an exception is thrown when it
     * is read.
     * 
     * @author sbillings
     * 
     */
    public enum PasswordMetaProperties {
	PLAIN_TEXT_TRUE, PLAIN_TEXT_FALSE, ENCRYPTED_TRUE, ENCRYPTED_FALSE, NONE
	// there is no BOTH; if both isplaintext and isencrypted are present,
	// we'll throw an exception
    }

    /**
     * Static factory method to create an instance for a property named
     * "password" (no prefix) and the Properties object it lives in.
     * 
     * @param props
     * @param suppliedAppNamePropertyName
     * @return
     */
    public static ConfigurationPassword createFromPropertyNoPrefix(
	    Properties props) {
	return new ConfigurationPassword(props);
    }

    /**
     * Static factory method to create an instance from a password property
     * prefix (like "protex" or "cc") and the Properties object it lives in. The
     * password property has some prefix that will be determined by parsing the
     * line.
     * 
     * @param props
     * @param suppliedAppNamePropertyName
     * @return
     */
    public static ConfigurationPassword createFromProperty(Properties props,
	    String suppliedAppNamePropertyName) {
	return new ConfigurationPassword(props, suppliedAppNamePropertyName);
    }

    /**
     * Static factory method to create an instance from a line from a Properties
     * file and the Properties object it lives in.
     * 
     * @param props
     * @param configFileLine
     * @return
     */
    public static ConfigurationPassword createFromLine(Properties props,
	    String configFileLine) {
	// Parse the password prefix from the given config file line
	Scanner scanner = null;
	String prefix = null;
	try {
	    scanner = new Scanner(configFileLine);
	    scanner.useDelimiter("\\.password=");
	    prefix = scanner.next(); // get prefix
	} finally {
	    if (scanner != null) {
		scanner.close();
	    }
	}

	return new ConfigurationPassword(props, prefix);
    }

    /**
     * Determines whether or not the password in the configuration file needs to
     * be encrypted/encoded.
     * 
     * @return
     */
    public boolean isInNeedOfEncryption() {
	switch (passwordMetaProperties) {
	case PLAIN_TEXT_TRUE:
	    return false; // user wants plain text
	case PLAIN_TEXT_FALSE:
	    return true; // user wants encryption
	case ENCRYPTED_TRUE:
	    return false; // it's already encrypted
	case ENCRYPTED_FALSE:
	    return false; // user wants plain text
	case NONE:
	default:
	    return true; // user wants encryption
	}
    }

    /**
     * Determines whether or not an "is encrypted" property needs to be added to
     * the configuration file.
     * 
     * @return
     */
    public boolean isInNeedOfNewEncryptionProperty() {
	switch (passwordMetaProperties) {
	case PLAIN_TEXT_TRUE:
	    return true; // legacy file
	case PLAIN_TEXT_FALSE:
	    return true; // legacy file
	case ENCRYPTED_TRUE:
	    return false; // encrypted property is already there
	case ENCRYPTED_FALSE:
	    return false; // encrypted property is already there
	case NONE:
	default:
	    return true; // encrypted property not there yet
	}
    }

    public boolean isEncryptedAfterUpgrade() {
	switch (passwordMetaProperties) {
	case PLAIN_TEXT_TRUE:
	    return false; // user wants plain text
	case PLAIN_TEXT_FALSE:
	    return true; // user wants encrypted
	case ENCRYPTED_TRUE:
	    return true; // user wants encrypted
	case ENCRYPTED_FALSE:
	    return false; // user wants plain text
	case NONE:
	default:
	    return true; // if no meta properties, we encrypt
	}
    }

    public String getPropertyName() {
	return propertyName;
    }

    /**
     * Get information about how the password was specified in the
     * configuration.
     * 
     * @return
     */
    public PasswordMetaProperties getPasswordMetaProperties() {
	return passwordMetaProperties;
    }

    /**
     * Get the plain text form of the password.
     * 
     * @return
     */
    public String getPlainText() {
	return passwordInPlainText;
    }

    /**
     * Get the encrypted/encoded form of the password.
     * 
     * @return
     * @throws Exception
     */
    public String getEncrypted() throws Exception {
	String plainTextPassword = getPlainText();
	if (plainTextPassword == null) {
	    return null;
	}
	return Password.encryptEncode(plainTextPassword);
    }

    /**
     * Private constructor; Call one of the static factory methods instead.
     * 
     * @param props
     * @param propertyNamePrefix
     */
    private ConfigurationPassword(Properties props, String propertyNamePrefix) {
	propertyName = propertyNamePrefix + "."
		+ ConfigConstants.GENERIC_PASSWORD_PROPERTY_SUFFIX;
	init(props);
    }

    /**
     * Private constructor; Call one of the static factory methods instead.
     * 
     * @param props
     * @param propertyNamePrefix
     */
    private ConfigurationPassword(Properties props) {
	propertyName = ConfigConstants.GENERIC_PASSWORD_PROPERTY_SUFFIX;
	init(props);
    }

    private void init(Properties props) {
	this.props = props;
	String passwordPropertyValue = props.getProperty(propertyName);
	if (passwordPropertyValue == null) {
	    return; // leave everything null
	}

	passwordMetaProperties = getPasswordProperties(propertyName);
	switch (passwordMetaProperties) {
	case ENCRYPTED_TRUE:
	    // Encrypted+Ascii85-encoded
	    try {
		passwordInPlainText = Password
			.decodeDecrypt(passwordPropertyValue);
	    } catch (Exception e) {
		throw new IllegalArgumentException(
			"Error attempting to decode/decrypt password set in "
				+ propertyName
				+ ".\n"
				+ "Please edit the configuration file to replace the encrypted password with the plain text password,\n"
				+ "and remove the corresponding .password.isencrypted property so the utility will re-encrypt it.\n"
				+ "If you want the password to be left as plain text in the configuration file, then set the\n"
				+ "corresponding .password.isencrypted property to false.");
	    }
	    break;
	case PLAIN_TEXT_FALSE:
	    // Base64-encoded (legacy file)
	    passwordInPlainText = new String(
		    Base64.decodeBase64(passwordPropertyValue));
	    break;

	case NONE:
	    // Plain text (assumed)
	    // This causes problems with RGT because gwt embeds an older version
	    // of the Base64 class.
	    // Haven't found a way to override that class, so we're removing
	    // the isBase64String check
	    // FALL THROUGH TO CASE BELOW
	case PLAIN_TEXT_TRUE:
	case ENCRYPTED_FALSE:
	    // Plain text (declared, or fell through from above after checking
	    // for possible base64-encoding)
	    passwordInPlainText = passwordPropertyValue;
	}
    }

    private PasswordMetaProperties getPasswordProperties(
	    String passwordPropertyName) {
	String isPlainTextPropertyName = passwordPropertyName + "."
		+ ConfigConstants.PASSWORD_ISPLAINTEXT_SUFFIX;
	String isEncryptedPropertyName = passwordPropertyName + "."
		+ ConfigConstants.PASSWORD_ISENCRYPTED_SUFFIX;
	String isPlainTextValue = props.getProperty(isPlainTextPropertyName);
	String isEncryptedValue = props.getProperty(isEncryptedPropertyName);

	if ((isPlainTextValue != null) && (isEncryptedValue != null)) {
	    throw new IllegalArgumentException("The configuration is invalid: "
		    + isPlainTextPropertyName + " and "
		    + isPlainTextPropertyName
		    + " should not both be set. Please remove "
		    + isPlainTextPropertyName);
	}
	if (isEncryptedValue != null) {
	    if ("true".equalsIgnoreCase(isEncryptedValue)) {
		return PasswordMetaProperties.ENCRYPTED_TRUE;
	    } else {
		return PasswordMetaProperties.ENCRYPTED_FALSE;
	    }
	} else if (isPlainTextValue != null) {
	    if ("true".equalsIgnoreCase(isPlainTextValue)) {
		return PasswordMetaProperties.PLAIN_TEXT_TRUE;
	    } else {
		return PasswordMetaProperties.PLAIN_TEXT_FALSE;
	    }
	}
	return PasswordMetaProperties.NONE;
    }

    @Override
    public String toString() {
	return "ConfigurationPassword [propertyName=" + propertyName + "]";
    }

}
