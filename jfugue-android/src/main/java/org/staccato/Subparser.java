/*
 * JFugue, an Application Programming Interface (API) for Music Programming
 * http://www.jfugue.org
 *
 * Copyright (C) 2003-2014 David Koelle
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.staccato;

/**
 * This makes it easy to extend the parser. 
 *  
 * @author David Koelle
 * 
 */
public interface Subparser {
    /**
     * Indicates whether the subparser should be responsible for parsing the given music string. 
     * 
     * @param music The Staccato music string to consider 
     * @return true if this subparser will accept the music string, false otherwise
     */
    public boolean matches(String music);
    
    /**
     * Parses the given music string.
     * 
     * @param music The Staccato music string to parse
     * @param packet
     * @param context
     * @return index Updated parsing index into the Staccato music string.   
     */
	public int parse(String music, StaccatoParserContext context);
}
