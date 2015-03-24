/*
 * Copyright (c) 2002 World Wide Web Consortium,
 * (Massachusetts Institute of Technology, Institut National de
 * Recherche en Informatique et en Automatique, Keio University). All
 * Rights Reserved. This program is distributed under the W3C's Software
 * Intellectual Property License. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.
 * See W3C License http://www.w3.org/Consortium/Legal/ for more details.
 */

package org.w3c.dom.xpath;

/**
 *  <meta name="usage" content="experimental"/>
 * <strong>DOM Level 3 WD Experimental:
 * The DOM Level 3 specification is at the stage 
 * of Working Draft, which represents work in 
 * progress and thus may be updated, replaced, 
 * or obsoleted by other documents at any time.</strong> <p>
 * The <code>XPathNSResolver</code> interface permit <code>prefix</code> 
 * strings in the expression to be properly bound to 
 * <code>namespaceURI</code> strings. <code>XPathEvaluator</code> can 
 * construct an implementation of <code>XPathNSResolver</code> from a node, 
 * or the interface may be implemented by any application.
 * <p>See also the <a href='http://www.w3.org/TR/2002/WD-DOM-Level-3-XPath-20020328'>Document Object Model (DOM) Level 3 XPath Specification</a>.
 */
public interface XPathNSResolver {
    /**
     * Look up the namespace URI associated to the given namespace prefix. The 
     * XPath evaluator must never call this with a <code>null</code> or 
     * empty argument, because the result of doing this is undefined.Null / 
     * empty prefix passed to XPathNSResolver should return default 
     * namespace.Do not permit <code>null</code>to be passed in invocation, 
     * allowing the implementation, if shared, to do anything it wants with 
     * a passed <code>null</code>.It would be confusing to specify more than 
     * this since the resolution of namespaces for XPath expressions never 
     * requires the default namespace.Null returns are problematic.No change.
     * They should be adequately addressed in core. Some implementations 
     * have not properly supported them, but they will be fixed to be 
     * compliant. Bindings are still free to choose alternative 
     * representations of <code>null</code>where required.
     * @param prefix The prefix to look for.
     * @return Returns the associated namespace URI or <code>null</code> if 
     *   none is found.
     */
    public String lookupNamespaceURI(String prefix);

}
