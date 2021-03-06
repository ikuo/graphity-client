/**
 *  Copyright 2012 Martynas Jusevičius <martynas@graphity.org>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.graphity.processor.locator;

import com.hp.hpl.jena.util.TypedStream;
import com.sun.jersey.api.uri.UriTemplate;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.WebContent;
import org.graphity.client.util.XSLTBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Jena-compatible Locator that uses GRDDL (XSLT) stylesheet to load RDF data (possibly from a remote location)
 * 
 * @author Martynas Jusevičius <martynas@graphity.org>
 * @see org.graphity.processor.util.DataManager
 */
public class LocatorGRDDL extends LocatorLinkedData
{
    private static final Logger log = LoggerFactory.getLogger(LocatorGRDDL.class);
    
    private Source stylesheet = null;
    private UriTemplate uriTemplate = null;
    private XSLTBuilder builder = null;
    private URIResolver resolver = null;

    public LocatorGRDDL(String uriTemplate, Source stylesheet, URIResolver resolver) throws TransformerConfigurationException
    {
	this(new UriTemplate(uriTemplate), stylesheet, resolver);
    }
    
    public LocatorGRDDL(UriTemplate uriTemplate, Source stylesheet, URIResolver resolver) throws TransformerConfigurationException
    {
	if (uriTemplate == null) throw new IllegalArgumentException("XSLT stylesheet Source cannot be null");
	if (stylesheet == null) throw new IllegalArgumentException("URIResolver cannot be null");
	if (resolver == null) throw new IllegalArgumentException("URIResolver cannot be null");
	
	this.uriTemplate = uriTemplate;
	this.stylesheet = stylesheet;
	builder = XSLTBuilder.fromStylesheet(stylesheet);
	this.resolver = resolver;
    }

    /**
     * Reads RDF from URI, transforms it using XSLT stylesheet and returns RDF/XML stream.
     * RDF/XML is buffered.
     * 
     * @param filenameOrURI remote URI
     * @return RDF/XML stream
     */
    @Override
    public TypedStream open(String filenameOrURI)
    {
	if (log.isDebugEnabled()) log.debug("Opening URI {} via GRDDL: {}", filenameOrURI, getStylesheet().getSystemId());
	
	if (!getUriTemplate().match(filenameOrURI, new HashMap<String, String>()))
	{
	    if (log.isDebugEnabled()) log.debug("URI {} does not match UriTemplate {} of this GRDDL locator", filenameOrURI, getUriTemplate());
	    return null;	    
	}
	
	TypedStream ts = super.open(filenameOrURI);
	if (ts == null)
	{
	    if (log.isDebugEnabled()) log.debug("Could not open HTTP stream from URI: {}", filenameOrURI);
	    return null;
	}

	try
	{
	    ByteArrayOutputStream bos = new ByteArrayOutputStream();

	    getXSLTBuilder().document(new StreamSource(ts.getInput())).
		resolver(getURIResolver()).
		result(new StreamResult(bos)).
		parameter("uri", new URI(filenameOrURI)).
		transform();
	    
	    if (log.isTraceEnabled()) log.trace("GRDDL RDF/XML output: {}", bos.toString());

	    return new TypedStream(new BufferedInputStream(new ByteArrayInputStream(bos.toByteArray())),
		    Lang.RDFXML.getContentType().getContentType(),
		    "UTF-8");
	}
	catch (TransformerException ex)
	{
	    if (log.isErrorEnabled()) log.error("Error in GRDDL XSLT transformation", ex);
	}
	catch (URISyntaxException ex)
	{
	    if (log.isErrorEnabled()) log.error("Error parsing location URI", ex);
	}

	return null;
    }

    @Override
    public  Map<String, Double> getQualifiedTypes()
    {
	Map<String, Double> xmlType = new HashMap<>();
	xmlType.put(WebContent.contentTypeXML, null);
	return xmlType;
    }
	
    @Override
    public String getName()
    {
	return "LocatorGRDDL(" + getStylesheet().getSystemId() + ")";
    }

    public UriTemplate getUriTemplate()
    {
	return uriTemplate;
    }

    protected XSLTBuilder getXSLTBuilder()
    {
	return builder;
    }

    public URIResolver getURIResolver()
    {
	return resolver;
    }

    public Source getStylesheet()
    {
        return stylesheet;
    }

}
