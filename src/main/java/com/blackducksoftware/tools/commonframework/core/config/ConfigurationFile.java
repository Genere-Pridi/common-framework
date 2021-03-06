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
package com.blackducksoftware.tools.commonframework.core.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class manages a configuration file. It can load the config file into a
 * Properties object and save a new version, preserving the original format, in
 * which all passwords needing encryption are encrypted and adjusting the
 * password meta-properties (removing *.password.isplaintext, and inserting
 * *.properties.isencrypted, where appropriate).
 * <p>
 * Password encryption works as follows:
 * <p>
 * *.password.isencrypted missing means: encrypt this password. <br>
 * *.password.isencrypted=true means: this password is already encrypted. <br>
 * *.password.isencrypted=false means: do not encrypt this password
 * <p>
 * Users will enter plain text passwords into the config file, and the utility
 * will automatically replace them in-place with encrypted passwords (setting
 * the new *.password.isencrypted property to true as it does it) unless it
 * finds *.password.isencrypted=false. - When creating a new config file, the
 * user enters the plain text password in the value of the *.password property.
 * If he does NOT want it to be encrypted automatically, he also inserts
 * *.password.isencrypted=false. - To change a password, the user changes the
 * *.password value to the plain text password. If he wants the password to be
 * automatically encrypted, he removes the *.password.isencrypted property. If
 * he wants the password to be left in plain text, he sets
 * *.password.isencrypted=false. - All password properties must be of the
 * pattern *.password. All properties of the form *.password must be passwords
 * that are subject to these encryption rules.
 * <p>
 * Legacy config file handling:
 * <p>
 * *.isplaintext=true means the password is plain text *.isplaintext=false means
 * the password is base64-encoded (which is now an obsolete representation)
 * *.isplaintext missing means the password is plain text (at least that's what
 * we'll assume) In this scenario, if the password looks like it might be
 * base64-encoded, a warning is written to the log by ConfigurationPassword.
 * <p>
 * The property *.password.isplaintext, if present, will determine whether the
 * utility reads the password as plain text (true) or base64-encoded (false). If
 * isplaintext=true was present, the password will be left in plain text, and
 * the isplaintext property will be replaced with isencrypted=false. - The
 * property *.password.isplaintext will be removed automatically. Once this
 * property is removed, the config file is no longer considered a legacy file. -
 * If the property *.password.isplaintext is missing, then the config file is
 * treated as a non-legacy file as described above. For plain text passwords,
 * this will work fine. For base64 encoded passwords, this will produce an
 * invalid encrypted password. When a utility encounters a non-encrypted
 * password that matches the base64-encoded pattern, it will write a warning to
 * the log which should make it easier to troubleshoot this scenario.
 * <p>
 * All isplaintext properties will be removed. If it does not exist, the
 * isencrypted property will be inserted. There is never any need to change the
 * value of an existing isencrypted property.
 *
 * @author sbillings
 *
 */
public class ConfigurationFile {
	private final Logger log = LoggerFactory.getLogger(this.getClass()
			.getName());

	private File file;

	// are there any passwords that need to be encrypted?
	private boolean inNeedOfUpdate = false;

	// The original file contents
	private List<String> lines;

	// The original properties from the file
	private ConfigurationProperties props;

	// a list of passwords that appear in the file
	private Map<String, ConfigurationPassword> configurationPasswords;

	private static final String PASSWORD_LINE_PATTERN_STRING = "^[a-zA-Z_\\-0-9.]*\\."
			+ ConfigConstants.GENERIC_PASSWORD_PROPERTY_SUFFIX + "=.*";

	private static final String PASSWORD_ISPLAINTEXT_LINE_PATTERN_STRING = "^[a-zA-Z_\\-0-9.]*\\."
			+ ConfigConstants.GENERIC_PASSWORD_PROPERTY_SUFFIX
			+ "."
			+ ConfigConstants.PASSWORD_ISPLAINTEXT_SUFFIX + "=.*";

	private static final Pattern passwordLinePattern = Pattern.compile(PASSWORD_LINE_PATTERN_STRING);
	private static final Pattern passwordIsPlainTextLinePattern = Pattern
			.compile(PASSWORD_ISPLAINTEXT_LINE_PATTERN_STRING);

	/**
	 * Construct a new ConfigurationFile given the file path. Load up a
	 * Properties object, and the lines array.
	 *
	 * @param configFilePath
	 *            the path to the config file.
	 */
	public ConfigurationFile(final String configFilePath) {
		file = new File(configFilePath);
		init(file);
	}

	public ConfigurationFile(final File file) {
		init(file);
	}

	public void init(final File file) {
		this.file = file;
		if (!file.exists()) {
			final String msg = "Configuration file: " + file.getAbsolutePath()
					+ " does not exist";
			log.error(msg);
			// A ConfigurationManager test depends on this message:
			throw new IllegalArgumentException("File DNE @: " + file.getName());
		}
		if (!file.canRead()) {
			final String msg = "Configuration file: " + file.getAbsolutePath()
					+ " is not readable";
			log.error(msg);
			throw new IllegalArgumentException(msg);
		}
		props = new ConfigurationProperties();
		try {
			props.load(file);
		} catch (final Exception e) {
			final String msg = "Error loading properties from file: "
					+ file.getAbsolutePath() + ": " + e.getMessage();
			log.error(msg);
			throw new IllegalArgumentException(msg);
		}

		try {
			lines = FileUtils.readLines(file, "UTF-8");
		} catch (final IOException e) {
			final String msg = "Error reading file: " + file.getAbsolutePath() + ": "
					+ e.getMessage();
			log.error(msg);
			throw new IllegalArgumentException(msg);
		}
		configurationPasswords = loadConfigurationPasswords();
		inNeedOfUpdate = hasPasswordsNeedingEncryptionOrPropertyUpdate();

	}

	public ConfigurationProperties getProperties() {
		return props;
	}

	/**
	 * Updates the file on disk by encrypting all passwords that should be
	 * encrypted. Passwords with *.password.isencrypted=false are not encrypted.
	 *
	 * @throws Exception
	 */
	private List<String> encryptPasswords() throws Exception {
		final List<String> updatedLines = new ArrayList<String>(lines.size() + 10);
		for (final String line : lines) {
			if (isPasswordIsPlainTextLine(line)) {
				continue; // omit *.password.isplaintext= lines from output
			}
			if (isPasswordLine(line)) {
				// TODO: It's overkill to re-create the ConfigurationPassword;
				// all you really need is the property name
				// then can look it up in the map. Maybe factor out the parsing
				// of the property name?
				// IF the psw lazy-loaded, then it wouldn't matter so much
				final ConfigurationPassword psw = ConfigurationPassword
						.createFromLine(props.getProperties(), line);
				if (psw.isInNeedOfEncryption()) {
					String encryptedLine = null;
					try {
						// In file, backslashes must be escaped (with a
						// backslash)
						encryptedLine = psw.getPropertyName() + "="
								+ ConfigurationProperties.escape(psw.getEncrypted());
					} catch (final Exception e) {
						log.error("Error encrypting passwords in file: "
								+ file.getAbsolutePath() + ": "
								+ e.getMessage());
						throw e;
					}
					updatedLines.add(encryptedLine);

				} else {
					updatedLines.add(line); // if no encryption needed: just
					// leave the original line
				}
				if (psw.isInNeedOfNewEncryptionProperty()) {
					final String value = psw.isEncryptedAfterUpgrade() ? "true"
							: "false";
					updatedLines.add(psw.getPropertyName() + "."
							+ ConfigConstants.PASSWORD_ISENCRYPTED_SUFFIX + "="
							+ value);
				}
			} else {
				updatedLines.add(line);
			}
		}
		return updatedLines;
	}



	/**
	 * Save the file, encrypting passwords that need it. Does not change the
	 * state of this object (lines and props continue to contain the original
	 * file contents).
	 *
	 * @return
	 * @throws IllegalArgumentException
	 *             to avoid the need to change ConfigurationManager too much.
	 */
	public List<String> saveWithEncryptedPasswords() {
		if (!isInNeedOfUpdate()) {
			return null;
		}
		log.info("Updating configuration file " + file.getAbsolutePath()
				+ "; encrypting passwords and adjusting password properties.");
		List<String> updatedLines = null;
		try {
			updatedLines = encryptPasswords();
		} catch (final Exception e) {
			final String msg = "Error encrypting passwords for file: "
					+ file.getAbsolutePath() + ": " + e.getMessage();
			log.error(msg);
			throw new IllegalArgumentException(msg);
		}
		try {
			FileUtils.writeLines(file, updatedLines);
		} catch (final IOException e) {
			final String msg = "Error saving file: " + file.getAbsolutePath() + ": "
					+ e.getMessage();
			log.error(msg);
			throw new IllegalArgumentException(msg);
		}
		return updatedLines;
	}

	/**
	 * Get the original file contents.
	 *
	 * @return
	 */
	List<String> getLines() {
		return lines;
	}

	/**
	 * Is this a *.password= line?
	 *
	 * @param line
	 * @return
	 */
	private boolean isPasswordLine(final String line) {
		final Matcher matcher = passwordLinePattern.matcher(line);
		return matcher.matches();
	}

	private boolean isPasswordIsPlainTextLine(final String line) {
		final Matcher matcher = passwordIsPlainTextLinePattern.matcher(line);
		return matcher.matches();
	}

	/**
	 * Get the list of passwords (and metadata) contained in this file
	 *
	 * @return
	 */
	private Map<String, ConfigurationPassword> loadConfigurationPasswords() {
		final Map<String, ConfigurationPassword> configurationPasswords = new HashMap<String, ConfigurationPassword>(
				8);
		for (final String line : lines) {
			if (isPasswordLine(line)) {
				// This is a *.password= line; does it need encrypting?
				final ConfigurationPassword psw = ConfigurationPassword
						.createFromLine(props.getProperties(), line);
				configurationPasswords.put(psw.getPropertyName(), psw);
			}
		}
		return configurationPasswords;
	}

	/**
	 * Determine whether this file needs any password-related changes. Either
	 * passwords that need encrypting or password meta-property changes. It
	 * turns out the latter test also covers obsolete meta-property removal,
	 * because if that's true, then "needs a new encryption property" is also
	 * true.
	 *
	 * @return
	 */
	private boolean hasPasswordsNeedingEncryptionOrPropertyUpdate() {
		for (final String pswPropertyName : configurationPasswords.keySet()) {
			final ConfigurationPassword psw = configurationPasswords
					.get(pswPropertyName);
			if (psw.isInNeedOfEncryption()
					|| psw.isInNeedOfNewEncryptionProperty()) {
				return true;
			}
		}
		return false;
	}

	public boolean isInNeedOfUpdate() {
		return inNeedOfUpdate;
	}

}
