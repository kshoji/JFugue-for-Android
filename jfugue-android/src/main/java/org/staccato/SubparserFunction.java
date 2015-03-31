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

public interface SubparserFunction
{
	/**
	 * Returns the names of this function, which are the same names that would
	 * be seen in the Staccato function call, e.g., "TRILL" for a trill function.
	 * The name of the function must be expressed in all capital letters.
	 * This method may return multiple names of a function to allow for abbreviations
	 * (e.g., TR or TRILL could both be legal names for the trill function)
	 * @return
	 */
	public String[] getNames();

	/**
     * @param parameters
     * @param context
     */
	public void apply(String parameters, StaccatoParserContext context);
}
