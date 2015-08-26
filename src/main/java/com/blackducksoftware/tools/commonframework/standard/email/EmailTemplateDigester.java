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
package com.blackducksoftware.tools.commonframework.standard.email;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.digester.Digester;
import org.xml.sax.SAXException;

public class EmailTemplateDigester {

    public static EmailTemplate getEmailTemplate(InputStream aStream)
	    throws IOException, SAXException {
	Digester digester = new Digester();
	digester.setValidating(false);

	digester.addObjectCreate("email", EmailTemplate.class);

	digester.addBeanPropertySetter("email/from", "from");
	digester.addBeanPropertySetter("email/to", "to");
	digester.addBeanPropertySetter("email/subject", "subject");
	digester.addBeanPropertySetter("email/style", "style");
	digester.addBeanPropertySetter("email/body", "body");

	return (EmailTemplate) digester.parse(aStream);

    }
}
