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

public class StaccatoMessages {
//	private static final String BUNDLE_NAME = "org.staccato.staccatomessages"; //$NON-NLS-1$
//
//	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);
//
//	private StaccatoMessages() { }
//
//	public static String getString(String key) {
//		try {
//			return RESOURCE_BUNDLE.getString(key);
//		} catch (MissingResourceException e) {
//			return '!' + key + '!';
//		}
//	}
//	
//    public static String OCTAVE_OUT_OF_RANGE = StaccatoMessages.getString("OCTAVE_OUT_OF_RANGE");
//    public static String NO_TIME_SIGNATURE_SEPARATOR = StaccatoMessages.getString("NO_TIME_SIGNATURE_SEPARATOR");
//    public static String CALCULATED_NOTE_OUT_OF_RANGE = StaccatoMessages.getString("CALCULATED_NOTE_OUT_OF_RANGE"); 
//    public static String VELOCITY_CHARACTER_NOT_RECOGNIZED = StaccatoMessages.getString("VELOCITY_CHARACTER_NOT_RECOGNIZED");
//    public static String NO_PARSER_FOUND = StaccatoMessages.getString("NO_PARSER_FOUND");

    //
    // Manually switch to the code above if staccatomessages.properties can be made to work 
    // Manually switch to the code below if staccatomessages.properties is not available
    //
    
	private StaccatoMessages() { }

	public static String getString(String key) {
		return key;
	}
	
    public static String OCTAVE_OUT_OF_RANGE = "The following value, parsed as an octave, is not in the expected range of 0 to 10:";
    public static String NO_TIME_SIGNATURE_SEPARATOR = "In the following element, could not find a slash ('/') to separate the numerator from the denominator in the Time Signature:";
    public static String CALCULATED_NOTE_OUT_OF_RANGE = "The following value for a note, calculated by computing (octave*12)+noteValue, is not in the range 0 - 127: "; 
    public static String VELOCITY_CHARACTER_NOT_RECOGNIZED = "The following character, parsed as a note velocity, is not recognized: ";
    public static String NO_PARSER_FOUND = "No parser was found for the following element: ";
}