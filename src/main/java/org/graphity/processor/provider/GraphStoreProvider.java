/**
 *  Copyright 2014 Martynas Jusevičius <martynas@graphity.org>
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
package org.graphity.processor.provider;

import com.hp.hpl.jena.query.Dataset;
import javax.ws.rs.ext.ContextResolver;
import org.graphity.processor.model.GraphStoreFactory;
import org.graphity.server.model.GraphStore;
import org.graphity.server.model.GraphStoreOrigin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JAX-RS provider for Graph Store.
 * Needs to be registered in the application.
 * 
 * @author Martynas Jusevičius <martynas@graphity.org>
 * @see org.graphity.server.model.GraphStore
 */
public class GraphStoreProvider extends org.graphity.server.provider.GraphStoreProvider
{
    
    private static final Logger log = LoggerFactory.getLogger(GraphStoreProvider.class);

    public Dataset getDataset()
    {
	ContextResolver<Dataset> cr = getProviders().getContextResolver(Dataset.class, null);
	return cr.getContext(Dataset.class);
    }

    /**
     * Provides a proxy if graph store origin is configured, and a local dataset-backed graphs store if it is not.
     * 
     * @return graph store instance 
     */
    @Override
    public GraphStore getGraphStore()
    {
        GraphStoreOrigin origin = getGraphStoreOrigin();
        if (origin != null) // use proxy for remote store
        {
            return super.getGraphStore();
        }
        else // use local store
        {
            return GraphStoreFactory.create(getRequest(), getServletContext(), getDataset(), getDataManager());
        }
    }
    
}
