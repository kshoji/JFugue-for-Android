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

package org.staccato.functions;

import org.staccato.StaccatoParserContext;
import org.staccato.SubparserFunction;

public class PolyPressureFunction implements SubparserFunction
{
	private static PolyPressureFunction instance;
	
	public static PolyPressureFunction getInstance() {
		if (instance == null) {
			instance = new PolyPressureFunction();
		}
		return instance;
	}
	
	private PolyPressureFunction() { }
	
	@Override
	public String[] getNames() {
		return NAMES;
	}

	@Override
	public void apply(String parameters, StaccatoParserContext context) {
		String[] params = parameters.split(",");
		if (params.length == 2) {
			context.getParser().firePolyphonicPressureParsed(Byte.parseByte(params[0].trim()), Byte.parseByte(params[1].trim()));	
		} 
	}
	
	public static String[] NAMES = { "PP", "POLYPRESSURE", "POLY", "POLYPHONIC", "POLYPHONICPRESSURE" };
}
