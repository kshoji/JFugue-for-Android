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

import org.w3c.dom.Node;
import org.w3c.dom.DOMException;

/**
 *  <meta name="usage" content="experimental"/>
 * <strong>DOM Level 3 WD Experimental:
 * The DOM Level 3 specification is at the stage 
 * of Working Draft, which represents work in 
 * progress and thus may be updated, replaced, 
 * or obsoleted by other documents at any time.</strong> <p>
 * The <code>XPathExpression</code> interface represents a parsed and resolved 
 * XPath expression.The evaluateExpression method should be moved to the 
 * XPathExpression interface so you do not have to use / pass two interfaces 
 * just to use it.Done.XPathExpression should have a public reference to the 
 * XPathEvaluator that created it.No change.Lacks justification.
 * <p>See also the <a href='http://www.w3.org/TR/2002/WD-DOM-Level-3-XPath-20020328'>Document Object Model (DOM) Level 3 XPath Specification</a>.
 */
public interface XPathExpression {
    /**
     * Evaluates this XPath expression and returns a result.
     * @param contextNode The <code>context</code> is context node for the 
     *   evaluation of this XPath expression.If the XPathEvaluator was 
     *   obtained by casting the <code>Document</code> then this must be 
     *   owned by the same document and must be a <code>Document</code>, 
     *   <code>Element</code>, <code>Attribute</code>, <code>Text</code>, 
     *   <code>CDATASection</code>, <code>Comment</code>, 
     *   <code>ProcessingInstruction</code>, or <code>XPathNamespace</code> 
     *   node.If the context node is a <code>Text</code> or a 
     *   <code>CDATASection</code>, then the context is interpreted as the 
     *   whole logical text node as seen by XPath, unless the node is empty 
     *   in which case it may not serve as the XPath context.
     * @param type If a specific <code>type</code> is specified, then the 
     *   result will be coerced to return the specified type relying on 
     *   XPath conversions and fail if the desired coercion is not possible. 
     *   This must be one of the type codes of <code>XPathResult</code>.
     * @param result The <code>result</code> specifies a specific 
     *   <code>XPathResult</code> which may be reused and returned by this 
     *   method. If this is specified as <code>null</code>or the 
     *   implementation cannot reuse the specified result, a new 
     *   <code>XPathResult</code> will be constructed and returned.
     * @return The result of the evaluation of the XPath expression.
     * @exception org.w3c.dom.xpath.XPathException
     *   TYPE_ERR: Raised if the result cannot be converted to return the 
     *   specified type.
     * @exception org.w3c.dom.DOMException
     *   WRONG_DOCUMENT_ERR: The Node is from a document that is not supported 
     *   by the XPathExpression that created this 
     *   <code>XPathExpression</code>.
     *   <br>NOT_SUPPORTED_ERR: The Node is not a type permitted as an XPath 
     *   context node.
     */
    public Object evaluate(Node contextNode,
                           short type,
                           Object result)
                                throws XPathException, DOMException;

}
