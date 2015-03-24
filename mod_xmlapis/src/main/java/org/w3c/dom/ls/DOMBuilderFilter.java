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

import org.w3c.dom.Element;
import org.w3c.dom.Node;         

/**
 * <strong>DOM Level 3 WD Experimental:
 * The DOM Level 3 specification is at the stage 
 * of Working Draft, which represents work in 
 * progress and thus may be updated, replaced, 
 * or obsoleted by other documents at any time.</strong> <p>
 * <code>DOMBuilderFilter</code>s provide applications the ability to examine 
 * nodes as they are being constructed during a parse. As each node is 
 * examined, it may be modified or removed, or the entire parse may be 
 * terminated early. 
 * <p>At the time any of the filter methods are called by the parser, the 
 * owner Document and DOMImplementation objects exist and are accessible. 
 * The document element is never passed to the <code>DOMBuilderFilter</code> 
 * methods, i.e. it is not possible to filter out the document element. The 
 * <code>Document</code>, <code>DocumentType</code>, <code>Notation</code>, 
 * and <code>Entity</code> nodes are not passed to the filter.
 * <p>All validity checking while reading a document occurs on the source 
 * document as it appears on the input stream, not on the DOM document as it 
 * is built in memory. With filters, the document in memory may be a subset 
 * of the document on the stream, and its validity may have been affected by 
 * the filtering.
 * <p> All default content, including default attributes, must be passed to 
 * the filter methods. 
 * <p> Any exception raised in the filter are ignored by the 
 * <code>DOMBuilder</code>. The description of these methods is not complete
 * <p>See also the <a href='http://www.w3.org/TR/2002/WD-DOM-Level-3-LS-20020725'>Document Object Model (DOM) Level 3 Load
and Save Specification</a>.
 */
public interface DOMBuilderFilter {
    // Constants returned by startElement and acceptNode
    /**
     * Accept the node.
     */
    public static final short FILTER_ACCEPT             = 1;
    /**
     * Reject the node abd its children.
     */
    public static final short FILTER_REJECT             = 2;
    /**
     * Skip this single node. The children of this node will still be 
     * considered. 
     */
    public static final short FILTER_SKIP               = 3;
    /**
     *  Interrupt the normal processing of the document. 
     */
    public static final short FILTER_INTERRUPT          = 4;

    /**
     * This method will be called by the parser after each <code>Element</code>
     *  start tag has been scanned, but before the remainder of the 
     * <code>Element</code> is processed. The intent is to allow the 
     * element, including any children, to be efficiently skipped. Note that 
     * only element nodes are passed to the <code>startElement</code> 
     * function.
     * <br>The element node passed to <code>startElement</code> for filtering 
     * will include all of the Element's attributes, but none of the 
     * children nodes. The Element may not yet be in place in the document 
     * being constructed (it may not have a parent node.) 
     * <br>A <code>startElement</code> filter function may access or change 
     * the attributes for the Element. Changing Namespace declarations will 
     * have no effect on namespace resolution by the parser.
     * <br>For efficiency, the Element node passed to the filter may not be 
     * the same one as is actually placed in the tree if the node is 
     * accepted. And the actual node (node object identity) may be reused 
     * during the process of reading in and filtering a document.
     * @param elt The newly encountered element. At the time this method is 
     *   called, the element is incomplete - it will have its attributes, 
     *   but no children. 
     * @return  <code>FILTER_ACCEPT</code> if this <code>Element</code> 
     *   should be included in the DOM document being built.  
     *   <code>FILTER_REJECT</code> if the <code>Element</code> and all of 
     *   its children should be rejected.  <code>FILTER_SKIP</code> if the 
     *   <code>Element</code> should be rejected. All of its children are 
     *   inserted in place of the rejected <code>Element</code> node.  
     *   <code>FILTER_INTERRUPT</code> if the filter wants to stop the 
     *   processing of the document. Interrupting the processing of the 
     *   document does no longer guarantee that the entire is XML well-formed
     *   .  Returning any other values will result in unspecified behavior. 
     */
    public short startElement(Element elt);

    /**
     * This method will be called by the parser at the completion of the 
     * parsing of each node. The node and all of its descendants will exist 
     * and be complete. The parent node will also exist, although it may be 
     * incomplete, i.e. it may have additional children that have not yet 
     * been parsed. Attribute nodes are never passed to this function.
     * <br>From within this method, the new node may be freely modified - 
     * children may be added or removed, text nodes modified, etc. The state 
     * of the rest of the document outside this node is not defined, and the 
     * affect of any attempt to navigate to, or to modify any other part of 
     * the document is undefined. 
     * <br>For validating parsers, the checks are made on the original 
     * document, before any modification by the filter. No validity checks 
     * are made on any document modifications made by the filter.
     * <br>If this new node is rejected, the parser might reuse the new node 
     * or any of its descendants.
     * @param enode The newly constructed element. At the time this method is 
     *   called, the element is complete - it has all of its children (and 
     *   their children, recursively) and attributes, and is attached as a 
     *   child to its parent. 
     * @return  <code>FILTER_ACCEPT</code> if this <code>Node</code> should 
     *   be included in the DOM document being built.  
     *   <code>FILTER_REJECT</code> if the <code>Node</code> and all of its 
     *   children should be rejected.  <code>FILTER_SKIP</code> if the 
     *   <code>Node</code> should be skipped and the <code>Node</code> 
     *   should be replaced by all the children of the <code>Node</code>.  
     *   <code>FILTER_INTERRUPT</code> if the filter wants to stop the 
     *   processing of the document. Interrupting the processing of the 
     *   document does no longer guarantee that the entire is XML well-formed
     *   . 
     */
    public short acceptNode(Node enode);

    /**
     *  Tells the <code>DOMBuilder</code> what types of nodes to show to the 
     * filter. See <code>NodeFilter</code> for definition of the constants. 
     * The constant <code>SHOW_ATTRIBUTE</code> is meaningless here, 
     * attribute nodes will never be passed to a 
     * <code>DOMBuilderFilter</code>. 
     */
    public int getWhatToShow();

}
