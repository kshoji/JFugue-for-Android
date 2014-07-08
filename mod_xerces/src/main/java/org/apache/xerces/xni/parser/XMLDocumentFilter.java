/*
 * Copyright 2001, 2002,2004 The Apache Software Foundation.
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

package org.apache.xerces.xni.parser;

import org.apache.xerces.xni.XMLDocumentHandler;

/**
 * Defines a document filter that acts as both a receiver and an emitter
 * of document events.
 *
 * @author Andy Clark, IBM
 *
 * @version $Id: XMLDocumentFilter.java 319806 2004-02-24 23:15:58Z mrglavas $
 */
public interface XMLDocumentFilter 
    extends XMLDocumentHandler, XMLDocumentSource {


} // interface XMLDocumentFilter
