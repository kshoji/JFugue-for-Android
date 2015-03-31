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

import org.staccato.functions.FunctionManager;


public class FunctionSubparser implements Subparser 
{
	public static final char FUNCTION = ':';
	
	private static FunctionSubparser instance;
	
	public static FunctionSubparser getInstance() {
		if (instance == null) {
			instance = new FunctionSubparser();
		}
		return instance;
	}
	
	@Override
	public boolean matches(String music) {
		return music.charAt(0) == FUNCTION;
	}

	@Override
	public int parse(String music, StaccatoParserContext context) {
		if (music.charAt(0) == FUNCTION) {
			int posOpenParen = StaccatoUtil.findNextOrEnd(music, '(', 0);
			int posCloseParen = StaccatoUtil.findNextOrEnd(music, ')', posOpenParen);
			String functionName = music.substring(1, posOpenParen);
			String params = music.substring(posOpenParen+1, posCloseParen);
			params = ParenSpacesPreprocessor.unprocess(params);
			SubparserFunction function = FunctionManager.getInstance().getSubparserFunction(functionName);
			if (function != null) {
				context.getParser().fireFunctionParsed(functionName, params);
				function.apply(params, context);
			}
			return Math.min(posCloseParen+1, music.length());
		}
		else {
			return 0;
		}
	}

	/*
	 * Function Generator
	 */
	
	public static String generateFunctionCall(String functionName, Object val) {
		StringBuilder buddy = new StringBuilder();
		buddy.append(FunctionSubparser.FUNCTION);
		buddy.append(functionName);
		buddy.append("(");
		appendList(buddy, val.toString());
		buddy.append(")");
		return buddy.toString();
	}

    public static String generateFunctionCall(String functionName, byte... vals) {
        StringBuilder buddy = new StringBuilder();
        buddy.append(FunctionSubparser.FUNCTION);
        buddy.append(functionName);
        buddy.append("(");
        appendList(buddy, getStringForPossibleArray(vals));
        buddy.append(")");
        return buddy.toString();
    }

    private static String getStringForPossibleArray(byte... vals) {
        if (vals.length == 0) {
            return "";
        }
        else {
            StringBuilder bach = new StringBuilder();
            for (Object val : vals) {
                bach.append(val.toString());
                bach.append(",");
            }
            return bach.substring(0,  bach.length()-1);
        }
    }

	public static String generateParenParamIfNecessary(String functionId, String value) {
		StringBuilder buddy = new StringBuilder();
		buddy.append(functionId);
        if (value.indexOf(' ') + value.indexOf('\'') == -2) {
            buddy.append(value);
        } else {
            buddy.append("(");
            buddy.append(value);
            buddy.append(")");
        }
        return buddy.toString();
	}
	
	private static void appendList(StringBuilder buddy, Object... vals) { 
		for (int i=0; i < vals.length-1; i++) {
			buddy.append(vals[i]);
			buddy.append(",");
		}
		buddy.append(vals[vals.length-1]);
	}
	

}
