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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.staccato.functions.FunctionManager;

public class FunctionPreprocessor implements Preprocessor 
{
	private static FunctionPreprocessor instance;
	
	private FunctionPreprocessor() { } 
	
	public static FunctionPreprocessor getInstance() {
		if (instance == null) {
			instance = new FunctionPreprocessor();
		}
		return instance;
	}

	private static Pattern functionPattern = Pattern.compile(":\\S+\\(\\p{ASCII}*\\)");
	private static Pattern namePattern = Pattern.compile(":\\S+\\(");
	private static Pattern paramPattern = Pattern.compile("\\(\\p{ASCII}*\\)");
	
	@Override
	public String preprocess(String s, StaccatoParserContext context) {
		StringBuilder buddy = new StringBuilder();
		int posPrev = 0;

		Matcher m = functionPattern.matcher(s);
		while (m.find()) {
			String functionName = null;
			String parameters = null;
			
			Matcher nameMatcher = namePattern.matcher(m.group());
			while (nameMatcher.find()) {
				functionName = nameMatcher.group().substring(1, nameMatcher.group().length()-1);
			}
			
			PreprocessorFunction function = FunctionManager.getInstance().getPreprocessorFunction(functionName);
			if (function == null) {
				return s; // We don't recognize the function. No problem, it could be a subparser function
			}

			Matcher paramMatcher = paramPattern.matcher(m.group());
			while (paramMatcher.find()) {
				parameters = paramMatcher.group().substring(1, paramMatcher.group().length()-1);
			}
			
			buddy.append(s.substring(posPrev, m.start()));
			buddy.append(function.apply(parameters, context));
			posPrev = m.end();
		}

		buddy.append(s.substring(posPrev, s.length()));
		return buddy.toString();
	}
}
