package com.blackducksoftware.tools.commonframework.core.config;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackducksoftware.tools.commonframework.core.exception.CommonFrameworkException;

public class EProperties {
	private final static Logger logger = LoggerFactory.getLogger(EProperties.class.getName());

	// When we write property values to a config file,
	// we are responsible for escaping these characters
	private static final Character[] CHARACTERS_TO_ESCAPE = { '\\', '(', '[', '$', '=', ']', ')' };

	// When we read property values from a file,
	// we must unescape these characters
	private final Character[] CHARACTERS_TO_UNESCAPE = { '(', '[', '$', '=', ']', ')' };

	private final static List<Character> charsToEscape = Arrays.asList(CHARACTERS_TO_ESCAPE);
	private final List<Character> charsToUnEscape = Arrays.asList(CHARACTERS_TO_UNESCAPE);
	private Configuration config = new PropertiesConfiguration();
	private Properties propertiesObject; // lazily-generated

	public EProperties() {
		logger.info("EProperties() constructor");
	}

	public void load(final File file) throws CommonFrameworkException {
		propertiesObject = null;

		final Configurations configs = new Configurations();
		try {
			config = configs.properties(file);
		} catch (final ConfigurationException e) {
			throw new CommonFrameworkException("Error loading properties from file " + file.getAbsolutePath() + ": "
					+ e.getMessage());
		}
		getProperties();
	}

	private String unescape(String s) {
		logger.info("Before unescaping: " + s);
		final StringBuilder sb = new StringBuilder("\\x");
		for (final Character c : charsToUnEscape) {
			sb.setCharAt(1, c);
			final String target = sb.toString();
			final String replacement = String.valueOf(c);

			s = s.replace(target, replacement);
		}
		logger.info("After unescaping: " + s);
		return s;
	}

	public Properties getProperties() {
		if (config == null) {
			logger.warn("getProperties(): config is null!");
		}
		if (propertiesObject != null) {
			return propertiesObject;
		}
		propertiesObject = new Properties();
		final Iterator<String> iter = config.getKeys();
		while (iter.hasNext()) {
			final String key = iter.next();
			final String unEscapedValue = unescape(config.getString(key));
			logger.info("getProperties(): including: " + key + "=" + config.getString(key) + " --> " + unEscapedValue);
			propertiesObject.put(key, unEscapedValue);
		}
		return propertiesObject;
	}

	/**
	 * Escape the given string, escaping any special characters with another
	 * backslash
	 *
	 * @param s
	 * @return
	 */
	public static String escape(final String s) {
		final byte[] bufferIn = s.getBytes();
		final StringBuilder sb = new StringBuilder();
		for (int bufferInIndex = 0; bufferInIndex < bufferIn.length; bufferInIndex++) {

			final Character c = new Character(s.charAt(bufferInIndex));
			if (charsToEscape.contains(c)) {
				logger.info("Escaping: " + c);
				sb.append('\\');
			}
			sb.append(c);
		}

		final String escapedString = sb.toString();
		return escapedString;
	}

	// TODO can I kill this?
	public void addAll(final Properties sourceProps) {
		propertiesObject = null;

		for (final Object keyObj : sourceProps.keySet()) {
			final String key = (String) keyObj;
			logger.info("addAll(): adding: " + key + "=" + sourceProps.getProperty(key));
			config.addProperty(key, sourceProps.getProperty(key));
		}
		getProperties();
	}

	public void setProperty(final String key, final String value) {
		config.setProperty(key, value);
		getProperties().setProperty(key, value);
	}

	public boolean containsKey(final String key) {
		return config.containsKey(key);
	}

	public String getProperty(final String key) {
		return getProperties().getProperty(key);
	}

	public Set<Object> keySet() {
		return getProperties().keySet();
	}

	public int size() {
		return getProperties().size();
	}

	@Override
	public String toString() {
		return getProperties().toString();
	}
}
