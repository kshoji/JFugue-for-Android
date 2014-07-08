/*
 * Copyright 1999,2000,2004 The Apache Software Foundation.
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
package org.apache.wml;

/**
 * <p>The interface is modeled after DOM1 Spec for HTML from W3C.
 * The DTD used in this DOM model is from 
 * <a href="http://www.wapforum.org/DTD/wml_1.1.xml">
 * http://www.wapforum.org/DTD/wml_1.1.xml</a></p>
 *
 * <p>'access' element specifics the access control for the entire deck
 * (Section 11.3.1, WAP WML Version 16-Jun-1999)</p>
 *
 * @version $Id: WMLAccessElement.java 319808 2004-02-24 23:34:05Z mrglavas $
 * @author <a href="mailto:david@topware.com.tw">David Li</a>
 */

public interface WMLAccessElement extends WMLElement {

    /**
     * A deck's domain and path attributes specify which deck may
     * access it.  
     *
     * domain attribute is suffix-matched against the domain name
     * portion of the referring URI 
     */
    public void setDomain(String newValue);
    public String getDomain();

    /**
     * path attribute is prefix-matched against the path portion of
     * the referring URI 
     */
    public void setPath(String newValue);
    public String getPath();
}
