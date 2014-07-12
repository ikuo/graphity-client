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
package org.graphity.processor.provider;

import com.hp.hpl.jena.query.Dataset;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.PerRequestTypeInjectableProvider;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;
import org.graphity.client.util.DataManager;
import org.graphity.processor.model.Application;
import org.graphity.processor.model.SPARQLEndpointFactory;
import org.graphity.server.model.SPARQLEndpoint;
import org.graphity.server.model.SPARQLEndpointProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
@Provider
public class SPARQLEndpointProxyProvider extends PerRequestTypeInjectableProvider<Context, SPARQLEndpointProxy> implements ContextResolver<SPARQLEndpointProxy>
{
    private static final Logger log = LoggerFactory.getLogger(SPARQLEndpointProxyProvider.class);

    @Context Providers providers;
    @Context ServletContext servletContext;
    @Context UriInfo uriInfo;
    @Context Request request;
    
    public SPARQLEndpointProxyProvider()
    {
	super(SPARQLEndpointProxy.class);
    }

    public Request getRequest()
    {
	return request;
    }

    public ServletContext getServletContext()
    {
	return servletContext;
    }

    public UriInfo getUriInfo()
    {
	return uriInfo;
    }

    public Providers getProviders()
    {
	return providers;
    }
    
    public Dataset getDataset()
    {
	ContextResolver<Dataset> cr = getProviders().getContextResolver(Dataset.class, null);
	return cr.getContext(Dataset.class);
    }

    public DataManager getDataManager()
    {
	ContextResolver<DataManager> cr = getProviders().getContextResolver(DataManager.class, null);
	return cr.getContext(DataManager.class);
    }

    public SPARQLEndpoint getSPARQLEndpoint()
    {
	ContextResolver<SPARQLEndpoint> cr = getProviders().getContextResolver(SPARQLEndpoint.class, null);
	return cr.getContext(SPARQLEndpoint.class);
    }

    public Application getApplication()
    {
	ContextResolver<Application> cr = getProviders().getContextResolver(Application.class, null);
	return cr.getContext(Application.class);
    }

    @Override
    public Injectable<SPARQLEndpointProxy> getInjectable(ComponentContext cc, Context context)
    {
	return new Injectable<SPARQLEndpointProxy>()
	{
	    @Override
	    public SPARQLEndpointProxy getValue()
	    {
		return getEndpointProxy();
	    }
	};
    }

    @Override
    public SPARQLEndpointProxy getContext(Class<?> type)
    {
	return getEndpointProxy();
    }

    public SPARQLEndpointProxy getEndpointProxy()
    {
        return SPARQLEndpointFactory.createProxy(getRequest(), getServletContext(),
                getDataManager(), getApplication());
    }
    
}