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
 * <strong>DOM Level 3 WD Experimental:
 * The DOM Level 3 specification is at the stage 
 * of Working Draft, which represents work in 
 * progress and thus may be updated, replaced, 
 * or obsoleted by other documents at any time.</strong> <p>
 * <code>XPathEvaluator</code>, which will provide evaluation of XPath 1.0 
 * expressions with no specialized extension functions or variables. It is 
 * expected that the <code>XPathEvaluator</code> interface will be 
 * implemented on the same object which implements the <code>Document</code> 
 * interface in an implementation which supports the XPath DOM module. 
 * <code>XPathEvaluator</code> implementations may be available from other 
 * sources that may provide support for special extension functions or 
 * variables which are not defined in this specification. The methods of 
 * XPathExpression should be named with more-XPath- specific names because 
 * the interface will often be implemented by the same object which 
 * implements document.No change.The point of interfaces is to localize the 
 * implementing namespace. This would make the method names unnecessarily 
 * long and complex even though there are no conflicts in the interface 
 * itself. The new core method getInterface is designed for discovering 
 * interfaces of additional modules that may not be directly implemented on 
 * the objects to which they are attached. This could be used to implement 
 * XPath on a separate object. The user only refers to the separate 
 * interfaces and not the proprietary aggregate implementation.Should entity 
 * refs be supported so that queries can be made on them?No change.We will 
 * not do this now. They are not part of the XPath data model. Note that 
 * they may be present in the hierarchy of returned nodes, but may not 
 * directly be requested or returned in the node set.What does createResult 
 * create when one wants to reuse the XPath?It is not useful.Removed method.
 * Should ordering be a separate flag, or a type of result that can be 
 * requested. As a type of result, it can be better optimized in 
 * implementations.It makes sense as a type of result. Changed.Removed 
 * method.Implementing XPathEvaluator on Document can be a problem due to 
 * conflicts in the names of the methods.The working group finds no better 
 * solution. GetInterface in Level 3 permits the object to be implemented 
 * separately. We should be committed to this. We will leave this issue open 
 * to see if we get more feedback on it.How does this interface adapt to 
 * XPath 2.0 and other query languages.No change.This interface is not 
 * intended to adapt to XPath 2.0 or other languages. The models of these 
 * are likely to be incompatible enough to require new APIs.For alternate 
 * implementations that can use this API, it can be obtained from different 
 * sources.Support for custom variables and functions would be very useful.
 * No change.It is possible for an implementation to supply alternative 
 * sources of an XPathEvaluator that can be customized with a custom 
 * variable and function context. We do not specify how this is 
 * accomplished. It is too complex to address in this version of the XPath 
 * DOM.
 * <p>See also the <a href='http://www.w3.org/TR/2002/WD-DOM-Level-3-XPath-20020328'>Document Object Model (DOM) Level 3 XPath Specification</a>.
 */
public interface XPathEvaluator {
    /**
     * Creates a parsed XPath expression with resolved namespaces. This is 
     * useful when an expression will be reused in an application since it 
     * makes it possible to compile the expression string into a more 
     * efficient internal form and preresolve all namespace prefixes which 
     * occur within the expression.createExpression should not raise 
     * exceptions about type coercion.This was already fixed in the public 
     * draft.
     * @param expression The XPath expression string to be parsed.
     * @param resolver The <code>resolver</code> permits translation of 
     *   prefixes within the XPath expression into appropriate namespace URIs
     *   . If this is specified as <code>null</code>, any namespace prefix 
     *   within the expression will result in <code>DOMException</code> 
     *   being thrown with the code <code>NAMESPACE_ERR</code>.
     * @return The compiled form of the XPath expression.
     * @exception XPathException
     *   INVALID_EXPRESSION_ERR: Raised if the expression is not legal 
     *   according to the rules of the <code>XPathEvaluator</code>i
     * @exception org.w3c.dom.DOMException
     *   NAMESPACE_ERR: Raised if the expression contains namespace prefixes 
     *   which cannot be resolved by the specified 
     *   <code>XPathNSResolver</code>.
     */
    public XPathExpression createExpression(String expression,
                                            XPathNSResolver resolver)
                                            throws XPathException, DOMException;

    /**
     * Adapts any DOM node to resolve namespaces so that an XPath expression 
     * can be easily evaluated relative to the context of the node where it 
     * appeared within the document. This adapter works by calling the 
     * method <code>lookupNamespacePrefix</code> on <code>Node</code>.It 
     * should be possible to create an XPathNSResolver that does not rely on 
     * a node, but which implements a map of resolutions that can be added 
     * to by the application.No change.The application can easily create 
     * this, which was why the interface was designed as it is. The 
     * specification will not require a specific factory at this time for 
     * application populated maps.There should be type restrictions on which 
     * types of nodes may be adapted by createNSResolver.No change.The 
     * namespace methods on the Node interface of the Level 3 core may be 
     * called without exception on all node types. In some cases no non-null 
     * namespace resolution will ever be returned. That is what may also be 
     * expected of this adapter.
     * @param nodeResolver The node to be used as a context for namespace 
     *   resolution.
     * @return <code>XPathNSResolver</code> which resolves namespaces with 
     *   respect to the definitions in scope for a specified node.
     */
    public XPathNSResolver createNSResolver(Node nodeResolver);

    /**
     * Evaluates an XPath expression string and returns a result of the 
     * specified type if possible.An exception needs to be raised when an 
     * XPath expression is evaluated on a node such as an EntityReference 
     * which cannot serve as an XPath context node.Done: NOT_SUPPORTED_ERR.A 
     * description is needed of what happens when the node passed to the 
     * evaluation function is a Text or CDATASection in the DOM case where 
     * the text may be fragmented between text nodes.Done.Eliminate the 
     * evaluate method from XPathEvaluator, forcing everyone to create 
     * expressions.No change.Any implementor can easily implement it by 
     * creating an expression. Having it available as a separate routine is 
     * a convenience and may be an optimization as well in some cases.Revert 
     * to multiple evaluateAs methods instead of passing a type code.No 
     * change.This is an alternative which eliminates a method argument 
     * while adding methods, but the type code is used to designate the type 
     * on returns anyway and using it as an argument to specify any coercion 
     * seems natural to many.Error exceptions are needed when there is a 
     * mismatch between the implementation of XPathEvaluator and the context 
     * node being evaluated.Done: WRONG_DOCUMENT_ERRConcern that the XPath 
     * API should only support natural results of XPath expression, without 
     * convenience coercion or alternative representations. Any special 
     * thing such as ordering should be added later to resultNo change.We 
     * have significant use cases for returning alternative types and 
     * representations by explicit request in advance.Eliminate the reusable 
     * result argument.No change.No. We have use cases for it, and there is 
     * already an implementation showing there is nothing wrong with it.
     * State that the XPathNSResolver argument may be a function in 
     * Javascript.Yes.There is an exception when there is a problem parsing 
     * the expression, but none when there is a problem evaluating the 
     * expression.No change.If the expression parsing was OK, then the worst 
     * that can happen is an empty result is returned.When requesting any 
     * type, the implementation should be permitted to return any type of 
     * node set, i.e. ordered or unordered, it finds convenient.No change.
     * The iterator it returns may contain ordered results, but identifying 
     * it as such produces undesirable results, because it would create 
     * complexity for the user -- requiring checking two types to see if the 
     * result was a node set -- or incompatibility caused by assuming it was 
     * always the one returned by a particular implementation the developer 
     * was using.NAMESPACE_ERR description is not appropriate to the way it 
     * is being used here.Make the description of NAMESPACE_ERR in the core 
     * specification more general.Should the INVALID_EXPRESSION_ERR be 
     * INVALID_SYNTAX_ERR?No change.We can improve the description of the 
     * error, but the name is appropriate as-is. It covers not only syntax 
     * errors but expression errors, such as when the implementation has no 
     * custom functions or variables but the expression specifies custom 
     * functions or variables.
     * @param expression The XPath expression string to be parsed and 
     *   evaluated.
     * @param contextNode The <code>context</code> is context node for the 
     *   evaluation of this XPath expression. If the XPathEvaluator was 
     *   obtained by casting the <code>Document</code> then this must be 
     *   owned by the same document and must be a <code>Document</code>, 
     *   <code>Element</code>, <code>Attribute</code>, <code>Text</code>, 
     *   <code>CDATASection</code>, <code>Comment</code>, 
     *   <code>ProcessingInstruction</code>, or <code>XPathNamespace</code> 
     *   node. If the context node is a <code>Text</code> or a 
     *   <code>CDATASection</code>, then the context is interpreted as the 
     *   whole logical text node as seen by XPath, unless the node is empty 
     *   in which case it may not serve as the XPath context.
     * @param resolver The <code>resolver</code> permits translation of 
     *   prefixes within the XPath expression into appropriate namespace URIs
     *   . If this is specified as <code>null</code>, any namespace prefix 
     *   within the expression will result in <code>DOMException</code> 
     *   being thrown with the code <code>NAMESPACE_ERR</code>.
     * @param type If a specific <code>type</code> is specified, then the 
     *   result will be coerced to return the specified type relying on 
     *   XPath type conversions and fail if the desired coercion is not 
     *   possible. This must be one of the type codes of 
     *   <code>XPathResult</code>.
     * @param result The <code>result</code> specifies a specific 
     *   <code>XPathResult</code> which may be reused and returned by this 
     *   method. If this is specified as <code>null</code>or the 
     *   implementation cannot reuse the specified result, a new 
     *   <code>XPathResult</code> will be constructed and returned.
     * @return The result of the evaluation of the XPath expression.
     * @exception XPathException
     *   INVALID_EXPRESSION_ERR: Raised if the expression is not legal 
     *   according to the rules of the <code>XPathEvaluator</code>i
     *   <br>TYPE_ERR: Raised if the result cannot be converted to return the 
     *   specified type.
     * @exception org.w3c.dom.DOMException
     *   NAMESPACE_ERR: Raised if the expression contains namespace prefixes 
     *   which cannot be resolved by the specified 
     *   <code>XPathNSResolver</code>.
     *   <br>WRONG_DOCUMENT_ERR: The Node is from a document that is not 
     *   supported by this XPathEvaluator.
     *   <br>NOT_SUPPORTED_ERR: The Node is not a type permitted as an XPath 
     *   context node.
     */
    public Object evaluate(String expression,
                           Node contextNode,
                           XPathNSResolver resolver,
                           short type,
                           Object result)
                                throws XPathException, DOMException;

}
