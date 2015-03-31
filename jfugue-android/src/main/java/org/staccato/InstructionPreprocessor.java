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

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jfugue.pattern.PatternProducer;

/** Turns to uppercase all tokens that are not lyrics, markers, or functions */
public class InstructionPreprocessor implements Preprocessor 
{
	private static InstructionPreprocessor instance;
	
	public static InstructionPreprocessor getInstance() {
		if (instance == null) {
			instance = new InstructionPreprocessor();
		}
		return instance;
	}

    private static Pattern keyPattern = Pattern.compile("\\{\\p{ASCII}*?\\}");

    private Map<String, Instruction> instructions;
	
    private InstructionPreprocessor() {
        instructions = new HashMap<String, Instruction>();
    }

    public void addInstruction(String key, Instruction value) {
	    instructions.put(key, value);
	}

    public void addInstruction(String key, final PatternProducer value) {
    	addInstruction(key, value.getPattern().toString());
    }

    public void addInstruction(String key, final String value) {
        instructions.put(key, new Instruction() {
            @Override
            public String onInstructionReceived(String[] instructions) {
                return value;
            }
        });
    }

    @Override
    public String preprocess(String s, StaccatoParserContext context) {
        StringBuilder buddy = new StringBuilder();
        int posPrev = 0;
    
        // Sort all of the instruction keys by length, so we'll deal with the longer ones first
        String[] sizeSortedInstructions = new String[instructions.size()];
        Arrays.sort(instructions.keySet().toArray(sizeSortedInstructions), new Comparator<String>() {
        	@Override
        	public int compare(String s1, String s2) {
        		if (s1.length() < s2.length()) return 1;
        		if (s1.length() > s2.length()) return -1;
        		return 0;
        	}
        });

        boolean matchFound = false;
        Matcher m = keyPattern.matcher(s);
        while (m.find()) {
            String key = m.group();
            key = key.substring(1, key.length()-1); // Remove the braces
            for (String possibleMatch : sizeSortedInstructions) {
            	if (!matchFound) {
	                if (key.startsWith(possibleMatch)) {
	                    Instruction instruction = instructions.get(possibleMatch);
	                    String value = key;
	                    if (instruction != null) {
	                        value = instruction.onInstructionReceived(key.split(" "));
	                    }
	            
	                    buddy.append(s.substring(posPrev, m.start()));
	                    buddy.append(value);
	                    posPrev = m.end();
	                    matchFound = true;
	                }
            	}
            }
            if (!matchFound) {
            	posPrev = m.end();
            }
        }

        buddy.append(s.substring(posPrev, s.length()));
        return buddy.toString();
    }
}
