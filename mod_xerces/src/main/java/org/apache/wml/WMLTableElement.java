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
 * <p>'table' create a set of aligned columns of text and images.
 * (Section 11.8.5, WAP WML Version 16-Jun-1999)</p>
 *
 * @version $Id: WMLTableElement.java 319808 2004-02-24 23:34:05Z mrglavas $
 * @author <a href="mailto:david@topware.com.tw">David Li</a>
 */

public interface WMLTableElement extends WMLElement {

    /**
     * 'title' specifies a title for the table
     * (Section 11.8.5, WAP WML Version 16-Jun-1999)
     */
    public void setTitle(String newValue);
    public String getTitle();

    /**
     * 'align' set the align of the table
     * (Section 11.8.5, WAP WML Version 16-Jun-1999)
     */
    public void setAlign(String newValue);
    public String getAlign();

    /**
     * 'columns' specifies the number of columns
     * (Section 11.8.5, WAP WML Version 16-Jun-1999)
     */
    public void setColumns(int newValue);
    public int getColumns();

    /**
     * The xml:lang that specifics the natural or formal language in
     * which the document is written.
     * (Section 8.8, WAP WML Version 16-Jun-1999)
     */
    public void setXmlLang(String newValue);
    public String getXmlLang();
}
