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

public class SysexFunction implements SubparserFunction
{
	private static SysexFunction instance;
	
	public static SysexFunction getInstance() {
		if (instance == null) {
			instance = new SysexFunction();
		}
		return instance;
	}
	
	private SysexFunction() { }
	
	@Override
	public String[] getNames() {
		return NAMES;
	}

	@Override
	public void apply(String parameters, StaccatoParserContext context) {
		String[] params = parameters.split(",");
		byte[] bytes = new byte[params.length];
		for (int i=0; i < params.length; i++) {
			bytes[i] = Byte.parseByte(params[i].trim());
		}
		context.getParser().fireSystemExclusiveParsed(bytes);	
	}
	
	public static String[] NAMES = { "SYSEX", "SE", "SX", "SYSTEM", "SYS", "SYSTEMEXCLUSIVE" };
}
