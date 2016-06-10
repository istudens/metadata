/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.metadata.ejb.test.common;

//import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 */
public class ValidationHelper {

    static final Map<String, String> catalogue;
    static {
        // the outcome of Xerces parser (XML Schema Grammar Loader) depends on the order of xsds which are passed to XMLSchemaFactory.newSchema
        catalogue = new LinkedHashMap();
        catalogue.put("http://www.jboss.org/j2ee/schema/jboss-ejb3-spec-2_0.xsd", "/schema/jboss-ejb3-spec-2_0.xsd");
        catalogue.put("http://www.jboss.org/j2ee/schema/jboss-ejb3-2_0.xsd", "/schema/jboss-ejb3-2_0.xsd");
        catalogue.put("http://java.sun.com/xml/ns/javaee/ejb-jar_3_1.xsd", "/schema/ejb-jar_3_1.xsd");
        catalogue.put("http://java.sun.com/xml/ns/javaee/javaee_6.xsd", "/schema/javaee_6.xsd");
        catalogue.put("http://java.sun.com/xml/ns/javaee/javaee_web_services_client_1_3.xsd", "/schema/javaee_web_services_client_1_3.xsd");
        catalogue.put("http://www.w3.org/2001/xml.xsd", "/schema/xml.xsd");
//        catalogue.put("http://www.jboss.org/j2ee/schema/trans-timeout-1_0.xsd", "/schema/trans-timeout-1_0.xsd");
//        // Somehow this gives a broken URI, see http://en.wikipedia.org/wiki/File_URI_scheme
//        catalogue.put("file://" + new File(System.getProperty("user.dir")).toURI().getPath() + "tx-test.xsd", "tx-test.xsd");
//        catalogue.put("file://" + new File(System.getProperty("user.dir")).toURI().getPath() + "cache-test.xsd", "cache-test.xsd");
    }

    private static InputStream inputStream(final Class loader, final String resource) {
        final InputStream in = loader.getResourceAsStream(resource);
        if (in == null) { throw new IllegalArgumentException("Can't find resource " + resource); }
        return in;
    }
    private static InputSource inputSource(final Class loader, final String systemId, final String resource) {
        final InputSource inputSource = new InputSource(inputStream(loader, resource));
        inputSource.setSystemId(systemId);
        return inputSource;
    }
    private static StreamSource streamSource(final Class loader, final String systemId, final String resource) {
        final StreamSource streamSource = new StreamSource(inputStream(loader, resource));
        streamSource.setSystemId(systemId);
        return streamSource;
    }

    public static Document parse(final InputSource is, final Class loader) throws ParserConfigurationException, IOException, SAXException {
        // parse an XML document into a DOM tree
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setAttribute(
                "http://java.sun.com/xml/jaxp/properties/schemaLanguage",
                "http://www.w3.org/2001/XMLSchema");
        factory.setNamespaceAware(true);
        factory.setValidating(true);
        DocumentBuilder parser = factory.newDocumentBuilder();
        parser.setEntityResolver(new SimpleEntityResolver(loader));
        parser.setErrorHandler(SimpleErrorHandler.INSTANCE);
        return parser.parse(is);
    }

    public static Document parseXsd(final InputSource is, final Class loader) throws ParserConfigurationException, IOException, SAXException {
        // parse an XML document into a DOM tree
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        final Map<String, String> catalogueToValidate = new LinkedHashMap<String, String>(catalogue);
//        // skip the following due to "javaee_web_services_client_1_3.xsd line 87 column 49 src-resolve: Cannot resolve the name 'javaee:descriptionGroup' to a(n) 'group' component."
//        catalogueToValidate.remove("http://java.sun.com/xml/ns/javaee/javaee_web_services_client_1_3.xsd");
        final Source[] schemas = new Source[catalogueToValidate.size()];
        int k = 0;
        for (Map.Entry<String, String> systemEntry : catalogueToValidate.entrySet()) {
            schemas[k++] = streamSource(loader, systemEntry.getKey(), systemEntry.getValue());
        }
        final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        schemaFactory.setErrorHandler(SimpleErrorHandler.INSTANCE);
        // FIXME ahhh, the following fails due to https://issues.apache.org/jira/browse/XERCESJ-1130
        // as multiple catalogue items (JBoss specific XSDs) redefine the javaee namespace
/*
        final String resource = "composite-javaee.xsd";
        final String systemId = "file://" + new File(System.getProperty("user.dir")).toURI().getPath() + resource;
        final StreamSource streamSource = new StreamSource(inputStream(loader, resource));
        streamSource.setSystemId(systemId);
        factory.setSchema(schemaFactory.newSchema(streamSource));
*/
        // Despite the XERCESJ-1130, Xerces cannot cope with multiple XSDs referring to each other
        // (which is the case of jboss-ejb3-spec-2_0.xsd and jboss-ejb3-2_0.xsd) as it loads and *fully*
        // parses the schemas one by one in the order they are passed in to the newSchema method, so in time
        // of parsing the former it does not understand elements which are defined in the latter.
        // Hence, these XSDs cannot be validated by Xerces.
        factory.setSchema(schemaFactory.newSchema(schemas));
        DocumentBuilder parser = factory.newDocumentBuilder();
        parser.setEntityResolver(new SimpleEntityResolver(loader));
        parser.setErrorHandler(SimpleErrorHandler.INSTANCE);
        return parser.parse(is);
    }

    static class SimpleEntityResolver implements EntityResolver {
        private Class loader;

        public SimpleEntityResolver(final Class loader) {
            this.loader = loader;
        }

        @Override
        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            System.out.println("resolve " + publicId + ", " + systemId);
            final String resource = catalogue.get(systemId);
            if (resource == null) { throw new SAXException("Can't resolve systemId " + systemId); }
            return inputSource(loader, systemId, resource);
        }
    }

    static class SimpleErrorHandler implements ErrorHandler {
        static final SimpleErrorHandler INSTANCE = new SimpleErrorHandler();

        @Override
        public void warning(SAXParseException exception) throws SAXException {
            System.err.println("warning: publicId[" + exception.getPublicId() + "] systemId[" + exception.getSystemId() + "] lineNo[" + exception.getLineNumber() + "] columnNo[" + exception.getColumnNumber() + "] " + exception.getMessage());
            throw exception;
        }

        @Override
        public void error(SAXParseException exception) throws SAXException {
            System.err.println("error: publicId[" + exception.getPublicId() + "] systemId[" + exception.getSystemId() + "] lineNo[" + exception.getLineNumber() + "] columnNo[" + exception.getColumnNumber() + "] " + exception.getMessage());
            throw exception;
        }

        @Override
        public void fatalError(SAXParseException exception) throws SAXException {
            System.err.println("fatalError: publicId[" + exception.getPublicId() + "] systemId[" + exception.getSystemId() + "] lineNo[" + exception.getLineNumber() + "] columnNo[" + exception.getColumnNumber() + "] " + exception.getMessage());
            throw exception;
        }
    }
}
