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

import java.util.ArrayList;
import java.util.List;

/**
 * This class contains a collection of methods that change the order of
 * "elements" in a string, where each element is separated by a space.
 * Typically, these elements might be musical notes (such as "A B C"),
 * but they can also be durations (such as "w h q"), and you can use
 * the Recombinator to combine two Strings, one containing notes and one
 * containing durations, to generate new music from existing musical elements.
 *  
 * @version 5.0
 * @author David Koelle (dmkoelle@gmail.com)
 */
public class Rearranger 
{
	/** 
	 * Takes a String (intended to be a Staccato string of music notes, not including
	 * other elements like voice or instrument selections), splits it on spaces,
	 * and returns a String in which the first element is moved to the 
	 * back end of the String.
	 * 
	 * Example: rotate("A B C D") --> "B C D A"
	 */
	public static String rotate(String elementsSeparatedBySpaces) {
		return rotate(elementsSeparatedBySpaces, 1);
	}
	
	/**
	 * Takes a String (intended to be a Staccato string of music notes, not including
	 * other elements like voice or instrument selections), splits it on spaces,
	 * and returns a String in which the first 'numRotations' elements are moved to the 
	 * back end of the String.
	 * 
	 * Example 1: rotate("A B C D", 1) --> "B C D A"
	 * Example 2: rotate("A B C D", 3) --> "D A B C"
	 */
	public static String rotate(String elementsSeparatedBySpaces, int numRotations) {
		String[] strings = elementsSeparatedBySpaces.split(" ");
		StringBuilder buddy = new StringBuilder();
		for (int i=numRotations; i < strings.length + numRotations; i++) {
			buddy.append(strings[i % strings.length]);
			buddy.append(" ");
		}
		return buddy.toString().trim();
	}

	/** 
	 * Takes a String (intended to be a Staccato string of music notes, not including
	 * other elements like voice or instrument selections), splits it on spaces,
	 * and returns a String that contains randomizes the order of the elements.
	 * Each element in the input string is represented once and only once in the
	 * return string.
	 * 
	 * Example 1: randomize("A B C D E") --> "D A B E C"
	 * Example 2: randomize("A B C D E") --> "E B D C A"
	 * 
	 */
	public static String randomize(String elementsSeparatedBySpaces) {
		String[] strings = elementsSeparatedBySpaces.split(" ");
		List<String> ss = new ArrayList<String>();
		for (int i=0; i < strings.length; i++) {
			ss.add(strings[i]);
		}

		StringBuilder buddy = new StringBuilder();
		for (int i=0; i < strings.length; i++) {
			int r = (int)(Math.random() * ss.size());
			buddy.append(ss.get(r));
			buddy.append(" ");
			ss.remove(r);
		}
		return buddy.toString().trim();
	}

	/**
	 * Takes a String (intended to be a Staccato string of music notes, not including
	 * other elements like voice or instrument selections), splits it on spaces, and
	 * randomly selects from the given elements to create a return String with 
	 * 'numElementsInResult' number of elements. Each element provided in the input
	 * string may occur 0...numElementsInResult times in the return string.
	 * 
	 * Example 1: createStringFromElements("A B C", 6) --> "A B A A C B"
	 * Example 2: createStringFromElements("A B C", 6) --> "B C B C C C"
	 * 
	 */
	public static String createStringFromElements(String elementsSeparatedBySpaces, int numElementsInResult) {
		String[] strings = elementsSeparatedBySpaces.split(" ");
		StringBuilder buddy = new StringBuilder();
		for (int i=0; i < numElementsInResult; i++) {
			buddy.append(strings[(int)(Math.random() * strings.length)]);
			buddy.append(" ");
		}
		return buddy.toString().trim();
	}
	
	/**
	 * Takes a String (intended to be a Staccato string of music notes, not including
	 * other elements like voice or instrument selections), splits it on spaces, and
	 * returns a String in which 'stringToAppend' is appended to the end of each element.
	 * 
	 * Example 1: appendToElements("A B C", "3") --> "A3 B3 C3"
	 * Example 2: appendToElements("C D E", "q") --> "Cq Dq Eq"
	 */
	public static String appendToElements(String elementsSeparatedBySpaces, String stringToAppend) {
		String[] strings = elementsSeparatedBySpaces.split(" ");
		StringBuilder buddy = new StringBuilder();
		for (String s : strings) {
			buddy.append(s);
			buddy.append(stringToAppend);
			buddy.append(" ");
		}
		return buddy.toString().trim();
	}
	
	/**
	 * Takes a String (intended to be a Staccato string of music notes, not including
	 * other elements like voice or instrument selections), splits it on spaces, and
	 * returns a String in which each element in the input is represented only once.
	 * The order of the elements in the return string will be the same as the order
	 * of the first appearance of each element in the input string.
	 * 
	 * Example: appendToElements("A B C B C B") --> "A B C"
	 */
	public static String createUniqueElements(String elementsSeparatedBySpaces) {
		String[] strings = elementsSeparatedBySpaces.split(" ");
		List<String> elementList = new ArrayList<String>();
		StringBuilder buddy = new StringBuilder();
		for (String s : strings) {
			if (!elementList.contains(s)) {
				buddy.append(s);
				buddy.append(" ");
				elementList.add(s);
			}
		}
		return buddy.toString().trim();
	}
}
