/*
 * Copyright (C) 2012 Martynas Jusevičius <martynas@graphity.org>
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

package org.graphity.analytics;

import com.hp.hpl.jena.ontology.OntDocumentManager;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import org.graphity.provider.ModelProvider;
import org.graphity.provider.RDFResourceXSLTWriter;
import org.graphity.util.LocatorLinkedData;
import org.graphity.vocabulary.Graphity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
public class Application extends javax.ws.rs.core.Application
{
    private static final Logger log = LoggerFactory.getLogger(LocatorLinkedData.class);
    private Set<Class<?>> classes = new HashSet<Class<?>>();
    private Set<Object> singletons = new HashSet<Object>();
    @Context private ServletContext context = null;
    //@Context private UriInfo uriInfo = null;
    
    @PostConstruct
    public void init()
    {
	log.debug("Application.init() ServletContext: {}", context);
	//log.debug("Application.init() UriInfo.getBaseUri(): {}", uriInfo.getBaseUri().toString());
	
	//OntModel ontModel = ModelFactory.createOntologyModel();
	try
	{
	    // http://incubator.apache.org/jena/documentation/ontology/#compound_ontology_documents_and_imports_processing
	    OntDocumentManager.getInstance().addAltEntry(Graphity.getURI(), context.getRealPath("/WEB-INF/graphity.ttl"));
	    //ontModel.read(context.getResourceAsStream("/WEB-INF/ontology.ttl"), getUriInfo().getBaseUri().toString(), FileUtils.langTurtle);
	} catch (Exception ex)
	{
	    log.warn("Could not load ontology", ex);
	}

    }
    
    @Override
    public Set<Class<?>> getClasses()
    {
        classes.add(Resource.class);
        classes.add(FrontPageResource.class);
        classes.add(OAuthResource.class);
	
	//classes.add(ModelProvider.class);
	//classes.add(ResourceXSLTWriter.class);

        return classes;
    }

    @Override
    public Set<Object> getSingletons()
    {
	//singletons.add(new OAuthResource());
	singletons.add(new RDFResourceXSLTWriter());
	singletons.add(new ModelProvider());

	return singletons;
    }
}
