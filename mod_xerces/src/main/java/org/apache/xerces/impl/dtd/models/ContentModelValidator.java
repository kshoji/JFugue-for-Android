/*
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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

package org.apache.xerces.impl.dtd.models;

import org.apache.xerces.xni.QName;

/**
 * @xerces.internal
 * 
 * @version $Id: ContentModelValidator.java 320090 2004-10-04 22:00:42Z mrglavas $
 */
public interface ContentModelValidator {

    //
    // Methods
    //

    /**
     * validate
     * 
     * @param children 
     * @param offset 
     * @param length 
     * 
     * @return The value -1 if fully valid, else the 0 based index of the child
     *         that first failed. If the value returned is equal to the number
     *         of children, then the specified children are valid but additional
     *         content is required to reach a valid ending state.
     */
    public int validate(QName[] children, int offset, int length);

} // interface ContentModelValidator
