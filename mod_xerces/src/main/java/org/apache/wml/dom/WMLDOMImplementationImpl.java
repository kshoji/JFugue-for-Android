/*
 * Copyright 1999,2000,2004,2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wml.dom;

import org.apache.wml.*;
import org.w3c.dom.*;
import org.apache.xerces.dom.*;

/**
 * @xerces.internal
 * @version $Id: WMLDOMImplementationImpl.java 320279 2005-04-17 23:49:58Z mrglavas $
 * @author <a href="mailto:david@topware.com.tw">David Li</a>
 */
public class WMLDOMImplementationImpl extends DOMImplementationImpl implements WMLDOMImplementation {
    
    static final DOMImplementationImpl singleton = new WMLDOMImplementationImpl();
    
    /** NON-DOM: Obtain and return the single shared object */
    public static DOMImplementation getDOMImplementation() {
        return singleton;
    }  
    
    /**
     * @see org.w3c.dom.DOMImplementation
     */
    public Document createDocument(String namespaceURI, 
            String qualifiedName, 
            DocumentType doctype) throws DOMException {
        DocumentImpl doc = new WMLDocumentImpl(doctype);
        //((DocumentTypeImpl)doctype).ownerDocument = doc;
        Element e = doc.createElementNS( namespaceURI, qualifiedName);
        doc.appendChild(e);
        return doc;
    }
}

