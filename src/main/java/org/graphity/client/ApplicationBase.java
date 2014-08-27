/*
 * Copyright (C) 2013 Martynas Jusevičius <martynas@graphity.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.graphity.client;

import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.LocationMapper;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import org.graphity.client.locator.PrefixMapper;
import org.graphity.client.model.impl.GlobalResourceBase;
import org.graphity.client.provider.DataManagerProvider;
import org.graphity.client.resource.labelled.Container;
import org.graphity.client.provider.DoesNotExistExceptionMapper;
import org.graphity.client.provider.NotFoundExceptionMapper;
import org.graphity.client.provider.QueryExceptionHTTPMapper;
import org.graphity.client.provider.XSLTBuilderProvider;
import org.graphity.client.reader.RDFPostReader;
import org.graphity.client.writer.ModelXSLTWriter;
import org.graphity.processor.provider.ApplicationProvider;
import org.graphity.processor.provider.DatasetProvider;
import org.graphity.processor.provider.GraphStoreOriginProvider;
import org.graphity.processor.provider.OntologyProvider;
import org.graphity.processor.provider.SPARQLEndpointOriginProvider;
import org.graphity.server.provider.GraphStoreProvider;
import org.graphity.server.provider.ModelProvider;
import org.graphity.server.provider.QueryParamProvider;
import org.graphity.server.provider.ResultSetWriter;
import org.graphity.server.provider.SPARQLEndpointProvider;
import org.graphity.server.provider.UpdateRequestReader;
import org.graphity.client.util.DataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.system.SPINModuleRegistry;

/**
 * Graphity Client JAX-RS application base class.
 * Can be extended or used as it is (needs to be registered in web.xml).
 * Needs to register JAX-RS root resource classes and providers.
 * 
 * @author Martynas Jusevičius <martynas@graphity.org>
 * @see <a href="http://docs.oracle.com/javaee/6/api/javax/ws/rs/core/Application.html">JAX-RS Application</a>
 * @see <a href="http://docs.oracle.com/cd/E24329_01/web.1211/e24983/configure.htm#CACEAEGG">Packaging the RESTful Web Service Application Using web.xml With Application Subclass</a>
 */
public class ApplicationBase extends org.graphity.server.ApplicationBase
{
    private static final Logger log = LoggerFactory.getLogger(ApplicationBase.class);

    private final Set<Class<?>> classes = new HashSet<>();
    private final Set<Object> singletons = new HashSet<>();

    private @Context ServletContext servletContext;
    private @Context UriInfo uriInfo;
    
    /**
     * Initializes root resource classes and provider singletons
     */
    public ApplicationBase()
    {
	classes.add(GlobalResourceBase.class); // handles all
        //classes.add(ResourceBase.class);
        //classes.add(GraphStoreBase.class); // handles /service requests
	classes.add(Container.class); // handles /search

	//singletons.addAll(super.getSingletons());
	singletons.add(new ModelProvider());
	singletons.add(new ResultSetWriter());
	singletons.add(new QueryParamProvider());
	singletons.add(new UpdateRequestReader());

        //singletons.add(new OntClassProvider());
        //singletons.add(new QueryBuilderProvider());

        singletons.add(new ApplicationProvider());
        singletons.add(new DataManagerProvider());
        singletons.add(new org.graphity.server.provider.DataManagerProvider());
        singletons.add(new DatasetProvider());
        singletons.add(new OntologyProvider());
	singletons.add(new SPARQLEndpointProvider());
	singletons.add(new SPARQLEndpointOriginProvider());
        singletons.add(new GraphStoreProvider());
        singletons.add(new GraphStoreOriginProvider());
	singletons.add(new RDFPostReader());
        singletons.add(new DoesNotExistExceptionMapper());
	singletons.add(new NotFoundExceptionMapper());
	singletons.add(new QueryExceptionHTTPMapper());
	singletons.add(new ModelXSLTWriter()); // writes XHTML responses
	singletons.add(new XSLTBuilderProvider()); // loads XSLT stylesheet
    }

    /**
     * Initializes (post construction) DataManager, its LocationMapper and Locators, and Context
     * 
     * @see org.graphity.util.manager.DataManager
     * @see org.graphity.util.locator
     * @see <a href="http://jena.apache.org/documentation/javadoc/jena/com/hp/hpl/jena/util/FileManager.html">FileManager</a>
     * @see <a href="http://jena.apache.org/documentation/javadoc/jena/com/hp/hpl/jena/util/LocationMapper.html">LocationMapper</a>
     * @see <a href="http://jena.apache.org/documentation/javadoc/jena/com/hp/hpl/jena/util/Locator.html">Locator</a>
     * @see <a href="http://jena.apache.org/documentation/javadoc/arq/com/hp/hpl/jena/sparql/util/Context.html">Context</a>
     */
    @PostConstruct
    public void init()
    {
        if (log.isTraceEnabled()) log.trace("Application.init() with Classes: {} and Singletons: {}", getClasses(), getSingletons());

	SPINModuleRegistry.get().init(); // needs to be called before any SPIN-related code
        ARQFactory.get().setUseCaches(false); // enabled caching leads to unexpected QueryBuilder behaviour
        
	// initialize locally cached ontology mapping
	LocationMapper mapper = new PrefixMapper("prefix-mapping.n3"); // check if file exists?
	LocationMapper.setGlobalLocationMapper(mapper);
	if (log.isDebugEnabled()) log.debug("LocationMapper.get(): {}", LocationMapper.get());

        DataManager manager = new DataManager(mapper, ARQ.getContext(), getServletContext(), getUriInfo());
        FileManager.setStdLocators(manager);
	manager.addLocatorLinkedData();
	manager.removeLocatorURL();
        FileManager.setGlobalFileManager(manager);
	if (log.isDebugEnabled()) log.debug("FileManager.get(): {}", FileManager.get());

        OntDocumentManager.getInstance().setFileManager(FileManager.get());
        OntDocumentManager.getInstance().setCacheModels(false);
	if (log.isDebugEnabled()) log.debug("OntDocumentManager.getInstance().getFileManager(): {}", OntDocumentManager.getInstance().getFileManager());
        
        /*
	try
	{
	    singletons.add(new JSONLDWriter(XSLTBuilder.fromStylesheet(getSource("/static/org/graphity/client/xsl/rdfxml2json-ld.xsl")).
		resolver(DataManager.get()))); // writes JSON-LD responses
	}
	catch (TransformerConfigurationException ex)
	{
	    if (log.isErrorEnabled()) log.error("XSLT transformer config error", ex);
	}
	catch (FileNotFoundException ex)
	{
	    if (log.isErrorEnabled()) log.error("XSLT stylesheet not found", ex);
	}
	catch (URISyntaxException ex)
	{
	    if (log.isErrorEnabled()) log.error("XSLT stylesheet URI error", ex);
	}
	catch (MalformedURLException ex)
	{
	    if (log.isErrorEnabled()) log.error("XSLT stylesheet URL error", ex);
	}
        */
    }
    
    /**
     * Provides JAX-RS root resource classes.
     *
     * @return set of root resource classes
     * @see org.graphity.server.model
     * @see <a
     * href="http://docs.oracle.com/javaee/6/api/javax/ws/rs/core/Application.html#getClasses()">Application.getClasses()</a>
     */
    @Override
    public Set<Class<?>> getClasses()
    {
	return classes;
    }

    /**
     * Provides JAX-RS singleton objects (e.g. resources or Providers)
     * 
     * @return set of singleton objects
     * @see org.graphity.server.provider
     * @see <a href="http://docs.oracle.com/javaee/6/api/javax/ws/rs/core/Application.html#getSingletons()">Application.getSingletons()</a>
     */
    @Override
    public Set<Object> getSingletons()
    {
	return singletons;
    }

    /**
     * Provides XML source from filename
     * 
     * @param filename
     * @return XML source
     * @throws FileNotFoundException
     * @throws URISyntaxException 
     * @throws java.net.MalformedURLException 
     * @see <a href="http://docs.oracle.com/javase/6/docs/api/javax/xml/transform/Source.html">Source</a>
     */
    public Source getSource(String filename) throws FileNotFoundException, URISyntaxException, MalformedURLException
    {
	if (log.isDebugEnabled()) log.debug("Resource paths used to load Source: {} from filename: {}", getServletContext().getResourcePaths("/"), filename);
	URL xsltUrl = getServletContext().getResource(filename);
	if (xsltUrl == null) throw new FileNotFoundException("File '" + filename + "' not found");
	String xsltUri = xsltUrl.toURI().toString();
	if (log.isDebugEnabled()) log.debug("XSLT stylesheet URI: {}", xsltUri);
	return new StreamSource(xsltUri);
    }

    /**
     * Returns servlet context
     * 
     * @return injected ServletContext
     */
    public ServletContext getServletContext()
    {
	return servletContext;
    }

    /**
     * Returns URI information
     * 
     * @return injected UriInfo
     */
    public UriInfo getUriInfo()
    {
	return uriInfo;
    }

}
