<?xml version="1.0" encoding="UTF-8"?>
<!--
Copyright 2012 Martynas Jusevičius <martynas@graphity.org>

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
-->
<!DOCTYPE xsl:stylesheet [
    <!ENTITY java           "http://xml.apache.org/xalan/java/">
    <!ENTITY gc             "http://graphity.org/gc#">
    <!ENTITY gp             "http://graphity.org/gp#">
    <!ENTITY rdf            "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY rdfs           "http://www.w3.org/2000/01/rdf-schema#">
    <!ENTITY xsd            "http://www.w3.org/2001/XMLSchema#">
    <!ENTITY owl            "http://www.w3.org/2002/07/owl#">
    <!ENTITY geo            "http://www.w3.org/2003/01/geo/wgs84_pos#">
    <!ENTITY sparql         "http://www.w3.org/2005/sparql-results#">
    <!ENTITY http           "http://www.w3.org/2011/http#">
    <!ENTITY sd             "http://www.w3.org/ns/sparql-service-description#">
    <!ENTITY dbpedia-owl    "http://dbpedia.org/ontology/">
    <!ENTITY dc             "http://purl.org/dc/elements/1.1/">
    <!ENTITY dct            "http://purl.org/dc/terms/">
    <!ENTITY foaf           "http://xmlns.com/foaf/0.1/">
    <!ENTITY sp             "http://spinrdf.org/sp#">
    <!ENTITY void           "http://rdfs.org/ns/void#">
    <!ENTITY sioc           "http://rdfs.org/sioc/ns#">
    <!ENTITY list           "http://jena.hpl.hp.com/ARQ/list#">
]>
<xsl:stylesheet version="2.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xhtml="http://www.w3.org/1999/xhtml"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:gc="&gc;"
xmlns:gp="&gp;"
xmlns:rdf="&rdf;"
xmlns:rdfs="&rdfs;"
xmlns:owl="&owl;"
xmlns:sparql="&sparql;"
xmlns:geo="&geo;"
xmlns:dbpedia-owl="&dbpedia-owl;"
xmlns:dc="&dc;"
xmlns:dct="&dct;"
xmlns:foaf="&foaf;"
xmlns:sioc="&sioc;"
xmlns:sp="&sp;"
xmlns:sd="&sd;"
xmlns:void="&void;"
xmlns:list="&list;"
xmlns:uuid="java:java.util.UUID"
exclude-result-prefixes="#all">

    <xsl:import href="imports/local.xsl"/>
    <xsl:import href="imports/external.xsl"/>
    <xsl:import href="imports/dbpedia-owl.xsl"/>
    <xsl:import href="imports/dc.xsl"/>
    <xsl:import href="imports/dct.xsl"/>
    <xsl:import href="imports/doap.xsl"/>
    <xsl:import href="imports/foaf.xsl"/>
    <xsl:import href="imports/gc.xsl"/>
    <xsl:import href="imports/gp.xsl"/>
    <xsl:import href="imports/gr.xsl"/>
    <xsl:import href="imports/owl.xsl"/>
    <xsl:import href="imports/rdf.xsl"/>
    <xsl:import href="imports/rdfs.xsl"/>
    <xsl:import href="imports/sd.xsl"/>
    <xsl:import href="imports/sioc.xsl"/>
    <xsl:import href="imports/skos.xsl"/>
    <xsl:import href="imports/sp.xsl"/>
    <xsl:import href="imports/spin.xsl"/>
    <xsl:import href="imports/void.xsl"/>
    <xsl:import href="layout.xsl"/>

    <xsl:param name="gc:uri" as="xs:anyURI?"/>
    <xsl:param name="label" as="xs:string?"/>

    <xsl:variable name="gc:defaultMode" select="if ($gc:uri) then (xs:anyURI('&gc;ReadMode')) else (if (not(/rdf:RDF/*/rdf:type/@rdf:resource = '&http;Response') and $matched-ont-class/gc:defaultMode/@rdf:resource) then xs:anyURI($matched-ont-class/gc:defaultMode/@rdf:resource) else (if (key('resources', $gp:absolutePath)/rdf:type/@rdf:resource = ('&sioc;Container', '&sioc;Space')) then xs:anyURI('&gc;ListMode') else xs:anyURI('&gc;ReadMode')))" as="xs:anyURI"/>

    <xsl:key name="resources-by-endpoint" match="*" use="void:sparqlEndpoint/@rdf:resource"/>

    <rdf:Description rdf:nodeID="save-as">
	<rdfs:label xml:lang="en">Save as...</rdfs:label>
    </rdf:Description>

    <xsl:template match="rdf:RDF" mode="gc:TitleMode">
	<xsl:choose>
	    <xsl:when test="$gc:uri">
		<xsl:apply-templates select="key('resources', $gp:baseUri, document($gp:baseUri))" mode="gc:LabelMode"/>
		<xsl:text> - </xsl:text>
		<xsl:apply-templates select="key('resources', $gc:uri)" mode="gc:LabelMode"/>
	    </xsl:when>
	    <xsl:otherwise>
		<xsl:apply-imports/>
	    </xsl:otherwise>
	</xsl:choose>
    </xsl:template>

    <xsl:template match="/" mode="gc:NavBarMode">
	<div class="navbar navbar-fixed-top">
	    <div class="navbar-inner">
		<div class="container-fluid">
                    <button class="btn btn-navbar" onclick="if ($('#collapsing-navbar').hasClass('in')) $('#collapsing-navbar').removeClass('collapse in').height(0); else $('#collapsing-navbar').addClass('collapse in').height('auto');">
                        <span class="icon-bar"></span>
                        <span class="icon-bar"></span>
                        <span class="icon-bar"></span>
                    </button>

                    <a class="brand" href="{$gp:baseUri}">
                        <xsl:for-each select="key('resources', $gp:baseUri, document($gp:baseUri))">
                            <img src="{foaf:logo/@rdf:resource}">
                                <xsl:attribute name="alt"><xsl:apply-templates select="." mode="gc:LabelMode"/></xsl:attribute>
                            </img>
                        </xsl:for-each>
                    </a>

                    <div id="collapsing-navbar" class="nav-collapse collapse">
                        <form action="{$gp:baseUri}" method="get" class="navbar-form pull-left" accept-charset="UTF-8">
                            <div class="input-append">
                                <input type="text" name="uri" class="input-xxlarge">
                                    <xsl:if test="not(starts-with($gc:uri, $gp:baseUri))">
                                        <xsl:attribute name="value">
                                            <xsl:value-of select="$gc:uri"/>
                                        </xsl:attribute>
                                    </xsl:if>
                                    <xsl:if test="$label">
                                        <xsl:attribute name="value">
                                            <xsl:value-of select="$label"/>
                                        </xsl:attribute>
                                    </xsl:if>
                                </input>
                                <button type="submit" class="btn btn-primary">Go</button>
                            </div>
                        </form>

                        <xsl:if test="key('resources-by-space', $gp:baseUri, document($gp:baseUri))[@rdf:about = resolve-uri('sparql', $gp:baseUri) or @rdf:about = resolve-uri('ontology', $gp:baseUri)]">
                            <xsl:variable name="space" select="($gp:absolutePath, key('resources', $gp:absolutePath)/sioc:has_container/@rdf:resource)" as="xs:anyURI*"/>
                            <ul class="nav pull-right">
                                <xsl:apply-templates select="key('resources-by-space', $gp:baseUri, document($gp:baseUri))[@rdf:about = resolve-uri('sparql', $gp:baseUri) or @rdf:about = resolve-uri('ontology', $gp:baseUri)]" mode="gc:NavBarMode">
                                    <xsl:sort select="gc:label(.)" order="ascending" lang="{$gc:lang}"/>
                                    <xsl:with-param name="space" select="$space"/>
                                </xsl:apply-templates>
                            </ul>
                        </xsl:if>
                    </div>
		</div>
	    </div>
	</div>
    </xsl:template>
    
    <xsl:template match="rdf:RDF[$gc:uri]" mode="gc:ReadMode">
        <xsl:apply-templates select="key('resources', $gc:uri)" mode="#current"/>

        <xsl:apply-templates select="*[not(@rdf:about = $gc:uri)][not(key('predicates-by-object', @rdf:nodeID))]" mode="#current">
            <xsl:sort select="gc:label(.)" lang="{$gc:lang}"/>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template match="rdf:RDF[$gc:uri]" mode="gc:HeaderMode">
        <xsl:apply-templates select="key('resources', $gc:uri)" mode="#current"/>
    </xsl:template>
    
    <xsl:template match="@rdf:about[. = $gc:uri]" mode="gc:HeaderMode">
	<h1 class="page-header">
	    <xsl:apply-templates select="." mode="gc:InlineMode"/>
	</h1>
    </xsl:template>
    
    <xsl:template match="rdf:RDF[$gc:uri]" mode="gc:ModeSelectMode">
        <ul class="nav nav-tabs">
            <xsl:apply-templates select="key('resources-by-type', '&gc;Mode', document('&gc;'))[rdf:type/@rdf:resource = '&gc;ItemMode']" mode="#current">
                <xsl:sort select="gc:label(.)"/>                    
            </xsl:apply-templates>
        </ul>
    </xsl:template>

    <xsl:template match="@rdf:about" mode="gc:ModeSelectMode">
	<xsl:choose>
	    <xsl:when test="$gc:uri">
		<xsl:choose>
		    <xsl:when test=". = $gc:defaultMode">
			<a href="{$gp:absolutePath}{gc:query-string($gc:uri, ())}">
			    <xsl:apply-templates select=".." mode="gc:LabelMode"/>
			</a>		
		    </xsl:when>
		    <xsl:otherwise>
			<a href="{$gp:absolutePath}{gc:query-string($gc:uri, .)}">
			    <xsl:apply-templates select=".." mode="gc:LabelMode"/>
			</a>		
		    </xsl:otherwise>
		</xsl:choose>
	    </xsl:when>
	    <xsl:otherwise>
		<xsl:apply-imports/>
	    </xsl:otherwise>
	</xsl:choose>
    </xsl:template>

    <xsl:template match="*[@rdf:about = $gc:uri]" mode="gc:ModeToggleMode" priority="1">
        <div class="pull-right">
            <a class="btn btn-primary" href="{$gp:absolutePath}{gc:query-string(@rdf:about, xs:anyURI('&gc;CreateMode'))}">
                <xsl:apply-templates select="key('resources', 'save-as', document(''))" mode="gc:LabelMode"/>
            </a>
        </div>
    </xsl:template>

    <xsl:template match="rdf:RDF" mode="gc:CreateMode">
        <form class="form-horizontal" method="post" action="{$gp:absolutePath}{gc:query-string($gc:uri, $gc:mode)}" accept-charset="UTF-8">
	    <xsl:comment>This form uses RDF/POST encoding: http://www.lsrn.org/semweb/rdfpost.html</xsl:comment>
	    <xsl:call-template name="gc:InputTemplate">
		<xsl:with-param name="name" select="'rdf'"/>
		<xsl:with-param name="type" select="'hidden'"/>
	    </xsl:call-template>
            
            <xsl:apply-templates mode="#current"/>
            
	    <div class="form-actions">
		<button type="submit" class="btn btn-primary">Save</button>
	    </div>
	</form>
    </xsl:template>

    <xsl:template match="*[*][@rdf:about = $gc:uri]" mode="gc:CreateMode">
        <xsl:apply-templates select="." mode="gc:EditMode"/>
    </xsl:template>

    <xsl:template match="rdf:RDF[$gp:absolutePath = resolve-uri('sparql', $gp:baseUri)][$gc:endpointUri]" mode="gc:QueryResultMode">
        <div class="btn-group pull-right">
            <a href="{$gc:endpointUri}?query={encode-for-uri($query)}" class="btn">Source</a>
        </div>

        <xsl:apply-imports>
            <xsl:with-param name="result-doc" select="document(concat($gp:absolutePath, gc:query-string($gc:endpointUri, $query, $gc:mode, ())))"/>
        </xsl:apply-imports>
    </xsl:template>
    
    <xsl:template match="@rdf:about[$gp:absolutePath = resolve-uri('sparql', $gp:baseUri)][$gc:endpointUri]" mode="gc:ModeSelectMode" priority="1">
        <a href="{$gp:absolutePath}{gc:query-string($gc:endpointUri, $query, ., ())}">
            <xsl:apply-templates select=".." mode="gc:LabelMode"/>
        </a>
    </xsl:template>

    <!-- DOCUMENT -->

    <!-- only show edit mode for the main resource -->
    <xsl:template match="*[@rdf:about][$gc:uri][not(@rdf:about = $gc:uri)]" mode="gc:EditMode"/>
    
    <xsl:template match="@rdf:about[. = $gc:uri][../rdf:type/@rdf:resource = '&foaf;Document']" mode="gc:EditMode" priority="1">
	<xsl:param name="type" select="'text'" as="xs:string"/>
	<xsl:param name="id" as="xs:string?"/>
	<xsl:param name="class" as="xs:string?"/>
	<xsl:param name="disabled" select="false()" as="xs:boolean"/>
        
	<xsl:call-template name="gc:InputTemplate">
	    <xsl:with-param name="name" select="'sb'"/>
	    <xsl:with-param name="type" select="'hidden'"/>
	    <xsl:with-param name="id" select="generate-id()"/>
	    <xsl:with-param name="class" select="$class"/>
	    <xsl:with-param name="disabled" select="$disabled"/>
	    <xsl:with-param name="value" select="'this'"/>
	</xsl:call-template>
    </xsl:template>

    <xsl:template match="*[*][@rdf:about = $gc:uri][rdf:type/@rdf:resource = '&foaf;Document']" mode="gc:EditMode" priority="1">
        <xsl:param name="container" select="resolve-uri('saved', $gp:baseUri)" as="xs:anyURI"/>
        
        <xsl:apply-imports/>

        <xsl:apply-templates select="@rdf:about" mode="#current"/>

        <xsl:call-template name="gc:InputTemplate">
            <xsl:with-param name="name" select="'pu'"/>
            <xsl:with-param name="type" select="'hidden'"/>
            <xsl:with-param name="value" select="'&rdf;type'"/>
        </xsl:call-template>
        <xsl:call-template name="gc:InputTemplate">
            <xsl:with-param name="name" select="'ou'"/>
            <xsl:with-param name="type" select="'hidden'"/>
            <xsl:with-param name="value" select="'&gp;Item'"/>
        </xsl:call-template>
        <xsl:call-template name="gc:InputTemplate">
            <xsl:with-param name="name" select="'pu'"/>
            <xsl:with-param name="type" select="'hidden'"/>
            <xsl:with-param name="value" select="'&gp;slug'"/>
        </xsl:call-template>
        <xsl:call-template name="gc:InputTemplate">
            <xsl:with-param name="name" select="'ol'"/>
            <xsl:with-param name="type" select="'hidden'"/>
            <xsl:with-param name="value" select="xs:string(uuid:randomUUID())"/>
        </xsl:call-template>
        <xsl:call-template name="gc:InputTemplate">
            <xsl:with-param name="name" select="'pu'"/>
            <xsl:with-param name="type" select="'hidden'"/>
            <xsl:with-param name="value" select="'&sioc;has_container'"/>
        </xsl:call-template>
        <xsl:call-template name="gc:InputTemplate">
            <xsl:with-param name="name" select="'ou'"/>
            <xsl:with-param name="type" select="'hidden'"/>
            <xsl:with-param name="value" select="$container"/>
        </xsl:call-template>
    </xsl:template>

    <!-- THING -->
        
    <xsl:template match="@rdf:about[. = $gc:uri]" mode="gc:EditMode">
	<xsl:param name="type" select="'text'" as="xs:string"/>
	<xsl:param name="id" as="xs:string?"/>
	<xsl:param name="class" as="xs:string?"/>
	<xsl:param name="disabled" select="false()" as="xs:boolean"/>
        
	<xsl:call-template name="gc:InputTemplate">
	    <xsl:with-param name="name" select="'sb'"/>
	    <xsl:with-param name="type" select="'hidden'"/>
	    <xsl:with-param name="id" select="generate-id()"/>
	    <xsl:with-param name="class" select="$class"/>
	    <xsl:with-param name="disabled" select="$disabled"/>
	    <xsl:with-param name="value" select="'thing'"/>
	</xsl:call-template>
    </xsl:template>
    
    <xsl:template match="*[*][@rdf:about = $gc:uri]" mode="gc:EditMode">
        <xsl:param name="container" select="resolve-uri('saved', $gp:baseUri)" as="xs:anyURI"/>

        <xsl:apply-imports/>
        
        <xsl:apply-templates select="@rdf:about" mode="#current"/>
        
        <xsl:call-template name="gc:InputTemplate">
            <xsl:with-param name="name" select="'pu'"/>
            <xsl:with-param name="type" select="'hidden'"/>
            <xsl:with-param name="value" select="'&foaf;isPrimaryTopicOf'"/>
        </xsl:call-template>
        <xsl:call-template name="gc:InputTemplate">
            <xsl:with-param name="name" select="'ob'"/>
            <xsl:with-param name="type" select="'hidden'"/>
            <xsl:with-param name="value" select="'this'"/>
        </xsl:call-template>

        <xsl:call-template name="gc:InputTemplate">
            <xsl:with-param name="name" select="'sb'"/>
            <xsl:with-param name="type" select="'hidden'"/>
            <xsl:with-param name="value" select="'this'"/>
        </xsl:call-template>
        <xsl:call-template name="gc:InputTemplate">
            <xsl:with-param name="name" select="'pu'"/>
            <xsl:with-param name="type" select="'hidden'"/>
            <xsl:with-param name="value" select="'&rdf;type'"/>
        </xsl:call-template>
        <xsl:call-template name="gc:InputTemplate">
            <xsl:with-param name="name" select="'ou'"/>
            <xsl:with-param name="type" select="'hidden'"/>
            <xsl:with-param name="value" select="'&foaf;Document'"/>
        </xsl:call-template>
        <xsl:call-template name="gc:InputTemplate">
            <xsl:with-param name="name" select="'ou'"/>
            <xsl:with-param name="type" select="'hidden'"/>
            <xsl:with-param name="value" select="'&gp;Item'"/>
        </xsl:call-template>
        <xsl:call-template name="gc:InputTemplate">
            <xsl:with-param name="name" select="'pu'"/>
            <xsl:with-param name="type" select="'hidden'"/>
            <xsl:with-param name="value" select="'&gp;slug'"/>
        </xsl:call-template>
        <xsl:call-template name="gc:InputTemplate">
            <xsl:with-param name="name" select="'ol'"/>
            <xsl:with-param name="type" select="'hidden'"/>
            <xsl:with-param name="value" select="xs:string(uuid:randomUUID())"/>
        </xsl:call-template>
        <xsl:call-template name="gc:InputTemplate">
            <xsl:with-param name="name" select="'pu'"/>
            <xsl:with-param name="type" select="'hidden'"/>
            <xsl:with-param name="value" select="'&foaf;primaryTopic'"/>
        </xsl:call-template>
        <xsl:call-template name="gc:InputTemplate">
            <xsl:with-param name="name" select="'ob'"/>
            <xsl:with-param name="type" select="'hidden'"/>
            <xsl:with-param name="value" select="'thing'"/>
        </xsl:call-template>
        <xsl:call-template name="gc:InputTemplate">
            <xsl:with-param name="name" select="'pu'"/>
            <xsl:with-param name="type" select="'hidden'"/>
            <xsl:with-param name="value" select="'&sioc;has_container'"/>
        </xsl:call-template>
        <xsl:call-template name="gc:InputTemplate">
            <xsl:with-param name="name" select="'ou'"/>
            <xsl:with-param name="type" select="'hidden'"/>
            <xsl:with-param name="value" select="$container"/>
        </xsl:call-template>
    </xsl:template>
    
</xsl:stylesheet>