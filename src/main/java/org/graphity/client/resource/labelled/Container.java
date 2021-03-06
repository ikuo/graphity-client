/**
 *  Copyright 2013 Martynas Jusevičius <martynas@graphity.org>
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
package org.graphity.client.resource.labelled;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.sun.jersey.api.core.ResourceContext;
import javax.servlet.ServletContext;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import org.graphity.client.model.impl.ResourceBase;
import org.graphity.processor.query.SelectBuilder;
import org.graphity.processor.vocabulary.LDP;
import org.graphity.server.model.SPARQLEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
@Path("/{path}/labelled")
public class Container extends ResourceBase
{
    private static final Logger log = LoggerFactory.getLogger(Container.class);

    private final String searchString;
    
    public Container(@Context UriInfo uriInfo, @Context SPARQLEndpoint endpoint, @Context OntClass ontClass,
            @Context Request request, @Context ServletContext servletContext, @Context HttpHeaders httpHeaders, @Context ResourceContext resourceContext)
    {
	super(uriInfo, endpoint, ontClass,
                request, servletContext, httpHeaders, resourceContext);
	this.searchString = getUriInfo().getQueryParameters().getFirst("label");
    }

    @Override
    public void init()
    {
        super.init();

	if (!(getSearchString() == null || getSearchString().isEmpty()) && getMatchedOntClass().hasSuperClass(LDP.Container))
	{
            SelectBuilder selectBuilder = getQueryBuilder().getSubSelectBuilder();
	    if (selectBuilder != null)
	    {
                selectBuilder.filter(RDFS.label.getLocalName(), getQueryBuilder().quoteRegexMeta(getSearchString())); // escape special regex() characters!
		if (log.isDebugEnabled()) log.debug("Search query: {} QueryBuilder: {}", getSearchString(), getQueryBuilder());
	    }
	}
    }
    
    public String getSearchString()
    {
	return searchString;
    }

    @Override
    public UriBuilder getPageUriBuilder()
    {
	if (getSearchString() != null) return super.getPageUriBuilder().queryParam("label", getSearchString());
	
	return super.getPageUriBuilder();
    }

    @Override
    public UriBuilder getPreviousUriBuilder()
    {
	if (getSearchString() != null) return super.getPreviousUriBuilder().queryParam("label", getSearchString());
	
	return super.getPreviousUriBuilder();
    }

    @Override
    public UriBuilder getNextUriBuilder()
    {
	if (getSearchString() != null) return super.getNextUriBuilder().queryParam("label", getSearchString());
	
	return super.getNextUriBuilder();
    }

}