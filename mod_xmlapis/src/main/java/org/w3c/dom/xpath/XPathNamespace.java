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

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *  <meta name="usage" content="experimental"/>
 * <strong>DOM Level 3 WD Experimental:
 * The DOM Level 3 specification is at the stage 
 * of Working Draft, which represents work in 
 * progress and thus may be updated, replaced, 
 * or obsoleted by other documents at any time.</strong> <p>
 * The <code>XPathNamespace</code> interface is returned by 
 * <code>XPathResult</code> interfaces to represent the XPath namespace node 
 * type that DOM lacks. There is no public constructor for this node type. 
 * Attempts to place it into a hierarchy or a NamedNodeMap result in a 
 * <code>DOMException</code> with the code <code>HIERARCHY_REQUEST_ERR</code>
 * . This node is read only, so methods or setting of attributes that would 
 * mutate the node result in a DOMException with the code 
 * <code>NO_MODIFICATION_ALLOWED_ERR</code>.
 * <p>The core specification describes attributes of the <code>Node</code> 
 * interface that are different for different node node types but does not 
 * describe <code>XPATH_NAMESPACE_NODE</code>, so here is a description of 
 * those attributes for this node type. All attributes of <code>Node</code> 
 * not described in this section have a <code>null</code> or 
 * <code>false</code> value.
 * <p><code>ownerDocument</code> matches the <code>ownerDocument</code> of the 
 * <code>ownerElement</code> even if the element is later adopted.
 * <p><code>prefix</code> is the prefix of the namespace represented by the 
 * node.
 * <p><code>nodeName</code> is the same as <code>prefix</code>.
 * <p><code>nodeType</code> is equal to <code>XPATH_NAMESPACE_NODE</code>.
 * <p><code>namespaceURI</code> is the namespace URI of the namespace 
 * represented by the node.
 * <p><code>adoptNode</code>, <code>cloneNode</code>, and 
 * <code>importNode</code> fail on this node type by raising a 
 * <code>DOMException</code> with the code <code>NOT_SUPPORTED_ERR</code>.
 * importNode should also fail on XPathNamespace nodes.This was already 
 * fixed in the public draft.The Namespace node should be added to DOM Level 
 * 3 core and should be available via a read-only NamedNodeMap on element to 
 * reduce the confusion of adding a special node type for XPath.No change.
 * There are no known problems with this add-on node type and uses beyond 
 * XPath are not anticipated.<code>Node.namespaceValue</code> should be 
 * identical to Node.namespaceURI and not <code>null</code>.No change.It is 
 * not clear why it should be this way since the infoset does not dictate it.
 * <p>See also the <a href='http://www.w3.org/TR/2002/WD-DOM-Level-3-XPath-20020328'>Document Object Model (DOM) Level 3 XPath Specification</a>.
 */
public interface XPathNamespace extends Node {
    // XPathNodeType
    /**
     * The node is a <code>Namespace</code>.
     */
    public static final short XPATH_NAMESPACE_NODE      = 13;

    /**
     * The <code>Element</code> on which the namespace was in scope when it 
     * was requested. This does not change on a returned namespace node even 
     * if the document changes such that the namespace goes out of scope on 
     * that element and this node is no longer found there by XPath.
     */
    public Element getOwnerElement();

}
