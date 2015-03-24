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

package org.w3c.dom.ls;

import org.w3c.dom.DOMException;

/**
 * <strong>DOM Level 3 WD Experimental:
 * The DOM Level 3 specification is at the stage 
 * of Working Draft, which represents work in 
 * progress and thus may be updated, replaced, 
 * or obsoleted by other documents at any time.</strong> <p>
 *  <code>DOMImplementationLS</code> contains the factory methods for creating 
 * objects that implement the <code>DOMBuilder</code> (parser) and 
 * <code>DOMWriter</code> (serializer) interfaces. 
 * <p> The expectation is that an instance of the 
 * <code>DOMImplementationLS</code> interface can be obtained by using 
 * binding-specific casting methods on an instance of the 
 * <code>DOMImplementation</code> interface or, if the <code>Document</code> 
 * supports the feature <code>"Core"</code> version <code>"3.0"</code> 
 * defined in , by using the method <code>Node.getInterface</code> with 
 * parameter values <code>"LS-Load"</code> and <code>"3.0"</code> 
 * (respectively). 
 * <p>See also the <a href='http://www.w3.org/TR/2002/WD-DOM-Level-3-LS-20020725'>Document Object Model (DOM) Level 3 Load
and Save Specification</a>.
 */
public interface DOMImplementationLS {
    // DOMIMplementationLSMode
    /**
     * Create a synchronous <code>DOMBuilder</code>.
     */
    public static final short MODE_SYNCHRONOUS          = 1;
    /**
     * Create an asynchronous <code>DOMBuilder</code>.
     */
    public static final short MODE_ASYNCHRONOUS         = 2;

    /**
     * Create a new <code>DOMBuilder</code>. The newly constructed parser may 
     * then be configured by means of its <code>setFeature</code> method, 
     * and used to parse documents by means of its <code>parse</code> 
     * method. 
     * @param mode  The <code>mode</code> argument is either 
     *   <code>MODE_SYNCHRONOUS</code> or <code>MODE_ASYNCHRONOUS</code>, if 
     *   <code>mode</code> is <code>MODE_SYNCHRONOUS</code> then the 
     *   <code>DOMBuilder</code> that is created will operate in synchronous 
     *   mode, if it's <code>MODE_ASYNCHRONOUS</code> then the 
     *   <code>DOMBuilder</code> that is created will operate in 
     *   asynchronous mode. 
     * @param schemaType  An absolute URI representing the type of the schema 
     *   language used during the load of a <code>Document</code> using the 
     *   newly created <code>DOMBuilder</code>. Note that no lexical 
     *   checking is done on the absolute URI. In order to create a 
     *   <code>DOMBuilder</code> for any kind of schema types (i.e. the 
     *   DOMBuilder will be free to use any schema found), use the value 
     *   <code>null</code>.  For W3C XML Schema , applications must use the 
     *   value <code>"http://www.w3.org/2001/XMLSchema"</code>. For XML DTD 
     *   , applications must use the value 
     *   <code>"http://www.w3.org/TR/REC-xml"</code>. Other Schema languages 
     *   are outside the scope of the W3C and therefore should recommend an 
     *   absolute URI in order to use this method. 
     * @return  The newly created <code>DOMBuilder</code> object. This 
     *   <code>DOMBuilder</code> is either synchronous or asynchronous 
     *   depending on the value of the <code>mode</code> argument.  By 
     *   default, the newly created <code>DOMBuilder</code> does not contain 
     *   a <code>DOMErrorHandler</code>, i.e. the value of the 
     *   <code>errorHandler</code> is <code>null</code>. However, 
     *   implementations may provide a default error handler at creation 
     *   time. In that case, the initial value of the 
     *   <code>errorHandler</code> attribute on the new created 
     *   <code>DOMBuilder</code> contains a reference to the default error 
     *   handler. 
     * @exception org.w3c.dom.DOMException
     *    NOT_SUPPORTED_ERR: Raised if the requested mode or schema type is 
     *   not supported. 
     */
    public DOMBuilder createDOMBuilder(short mode,
                                       String schemaType)
                                       throws DOMException;

    /**
     * Create a new <code>DOMWriter</code> object. <code>DOMWriter</code>s are 
     * used to serialize a DOM tree back into an XML document. 
     * @return The newly created <code>DOMWriter</code> object. By default, 
     *   the newly created <code>DOMWriter</code> does not contain a 
     *   <code>DOMErrorHandler</code>, i.e. the value of the 
     *   <code>errorHandler</code> is <code>null</code>. However, 
     *   implementations may provide a default error handler at creation 
     *   time. In that case, the initial value of the 
     *   <code>errorHandler</code> attribute on the new created 
     *   <code>DOMWriter</code> contains a reference to the default error 
     *   handler. 
     */
    public DOMWriter createDOMWriter();

    /**
     *  Create a new "empty" <code>DOMInputSource</code>. 
     * @return  The newly created <code>DOMInputSource</code> object. 
     */
    public DOMInputSource createDOMInputSource();

}
