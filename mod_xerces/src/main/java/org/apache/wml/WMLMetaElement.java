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
 * <p>The meta element contains meta-info of an WML deck
 * (Section 11.3.2, WAP WML Version 16-Jun-1999)</p>
 *
 * @version $Id: WMLMetaElement.java 319808 2004-02-24 23:34:05Z mrglavas $
 * @author <a href="mailto:david@topware.com.tw">David Li</a>
 */
public interface WMLMetaElement extends WMLElement {

    /**
     * 'name' attribute specific the property name
     */
    public void setName(String newValue);
    public String getName();

    /**
     * 'http-equiv' attribute indicates the property should be
     * interpret as HTTP header.
     */
    public void setHttpEquiv(String newValue);
    public String getHttpEquiv();

    /**
     * 'forua' attribute specifies whether a intermediate agent should
     * remove this meta element. A value of false means the
     * intermediate agent must remove the element.
     */
    public void setForua(boolean newValue);
    public boolean getForua();

    /**
     * 'scheme' attribute specifies a form that may be used to
     * interpret the property value 
     */
    public void setScheme(String newValue);
    public String getScheme();

    /**
     * 'content' attribute specifies the property value 
     */
    public void setContent(String newValue);
    public String getContent();
}
