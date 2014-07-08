/*
 * Copyright 2003,2004 The Apache Software Foundation.
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

package org.apache.xerces.impl.xs.util;

import org.apache.xerces.impl.xs.SchemaGrammar;
import org.apache.xerces.impl.xs.XSModelImpl;
import org.apache.xerces.xs.XSModel;
import org.apache.xerces.util.XMLGrammarPoolImpl;
import org.apache.xerces.xni.grammars.XMLGrammarDescription;


/**
 * Add a method that return an <code>XSModel</code> that represents components in
 * the schema grammars in this pool implementation.
 * 
 * @xerces.internal  
 * 
 * @version $Id: XSGrammarPool.java 320098 2004-10-06 15:14:55Z mrglavas $
 */
public class XSGrammarPool extends XMLGrammarPoolImpl {
    /**
     * Return an <code>XSModel</code> that represents components in
     * the schema grammars in this pool implementation.
     *
     * @return  an <code>XSModel</code> representing this schema grammar
     */
    public XSModel toXSModel() {
        java.util.Vector list = new java.util.Vector();
        for (int i = 0; i < fGrammars.length; i++) {
            for (Entry entry = fGrammars[i] ; entry != null ; entry = entry.next) {
                if (entry.desc.getGrammarType().equals(XMLGrammarDescription.XML_SCHEMA))
                    list.addElement(entry.grammar);
            }
        }

        int size = list.size();
        if (size == 0)
            return null;
        SchemaGrammar[] gs = new SchemaGrammar[size];
        for (int i = 0; i < size; i++)
            gs[i] = (SchemaGrammar)list.elementAt(i);
        return new XSModelImpl(gs);
    }


} // class XSGrammarPool
