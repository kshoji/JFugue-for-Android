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

package org.jfugue.mitools;

import java.util.Arrays;
import java.util.List;

import org.jfugue.pattern.Pattern;

/**
 * Lets you...  
 * 
 *  
 * @author David Koelle
 *
 */
public class Recombinator 
{
    private List<Object> things0AsList;
    private Object[] things0AsArray;
    private Object[] things0WorkingArray;

    private List<Object> things1AsList;
    private Object[] things1AsArray;
    private Object[] things1WorkingArray;

    public Recombinator() { }
    
    public Recombinator(List<Object> things0, List<Object> things1) {
    	things0AsList = things0;
    	things1AsList = things1;
    }
    
    public Recombinator(List<Object> things0, Object[] things1) {
    	things0AsList = things0;
    	things1AsArray = things1;
    }

    public Recombinator(Object[] things0, List<Object> things1) {
    	things0AsArray = things0;
    	things1AsList = things1;
    }
    
    public Recombinator(Object[] things0, Object[] things1) {
    	things0AsArray = things0;
    	things1AsArray = things1;
    }

    public Recombinator put(int index, List<Object> things) {
        switch (index) {
        	case 0 : things0AsList = things; things0AsArray = null; break;
        	case 1 : things1AsList = things; things1AsArray = null; break;
        	default : throw new IllegalArgumentException("Index to Recombinator must be either 0 or 1; provided value is "+index); 
        }
        return this;
    }

    public Recombinator put(int index, Object[] things) {
        switch (index) {
        	case 0 : things0AsList = null; things0AsArray = things; break;
        	case 1 : things1AsList = null; things1AsArray = things; break;
        	default : throw new IllegalArgumentException("Index to Recombinator must be either 0 or 1; provided value is "+index); 
        }
        return this;
    }

    public Pattern recombine(String instructions, LoopBehavior option)
    {
    	createWorkingArrays();
    	switch (option) {
    		case LOOP_BOTH: return loopBoth(instructions); 
    		case LOOP_0_FIRST: return loop0(instructions); 
    		case LOOP_1_FIRST: return loop1(instructions); 
    		default : return null; 
    	}
    }
    
    /** 
     * Given $0 = { A, B } and $1 = { C, D }, loopBoth("$0v$1") gives { AvC, BvD }
     * If one array is longer than the other, the shorter array is repeated. 
     * 
     * @param instructions
     * @return
     */
    private Pattern loopBoth(String instructions) {
    	Pattern pattern = new Pattern();
    	for (int i=0; i < Math.max(things0WorkingArray.length, things1WorkingArray.length); i++) {
       		String patternPiece = instructions.replaceAll("\\$0", things0WorkingArray[i % things0WorkingArray.length].toString());
       		pattern.add(patternPiece.replaceAll("\\$1", things1WorkingArray[i % things1WorkingArray.length].toString()));
       	}
    	return pattern;
    }

    /** 
     * Given $0 = { A, B } and $1 = { C, D }, loop1("$0v$1") gives { AvC, AvD, BvC, BvD } 
     * 
     * @param instructions
     * @return
     */
    private Pattern loop1(String instructions) {
    	Pattern pattern = new Pattern();
    	for (Object obj0 : things0WorkingArray) {
    		String patternPiece = instructions.replaceAll("\\$0", obj0.toString());
    		for (Object obj1 : things1WorkingArray) {
    			pattern.add(patternPiece.replaceAll("\\$1", obj1.toString()));
        	}
    	}
    	return pattern;
    }

    /** 
     * Given $0 = { A, B } and $1 = { C, D }, loop1("$0v$1") gives { AvC, BvC, AvD, BvD } 
     * 
     * @param instructions
     * @return
     */
    private Pattern loop0(String instructions) {
    	Pattern pattern = new Pattern();
    	for (Object obj1 : things1WorkingArray) {
    		String patternPiece = instructions.replaceAll("\\$1", obj1.toString());
    		for (Object obj0 : things0WorkingArray) {
    			pattern.add(patternPiece.replaceAll("\\$0", obj0.toString()));
        	}
    	}
    	return pattern;
    }

    private void createWorkingArrays() {
    	if (things0AsArray != null) {
    		things0WorkingArray = Arrays.copyOf(things0AsArray, things0AsArray.length);
    	} else if (things0AsList != null) {
    		things0AsList.toArray(things0WorkingArray);
    	} else {
    		throw new RuntimeException("Recombinator cannot recombine; there is no set of objects for $0");
    	}

    	if (things1AsArray != null) {
    		things1WorkingArray = Arrays.copyOf(things1AsArray, things1AsArray.length);
    	} else if (things1AsList != null) {
    		things1AsList.toArray(things1WorkingArray);
    	} else {
    		throw new RuntimeException("Recombinator cannot recombine; there is no set of objects for $1");
    	}
    }

    public enum LoopBehavior {
    	LOOP_0_FIRST, LOOP_1_FIRST, LOOP_BOTH;
    }
}

