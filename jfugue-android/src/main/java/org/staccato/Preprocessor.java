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
 *  A Preprocessor takes a token from the Staccato string, does some computation on the string,
 *  and returns the String results of the computation so it may be included back into the
 *  Staccato string. This is used for functionality that can be expressed in a String but must
 *  be expanded to actual Staccato instructions. 
 * 
 *  The MicrotonePreprocess is an example of this. The user is allowed to express a microtone
 *  using 'm' followed by the frequency - e.g., m440. The MicrotonePreprocessor takes this String,
 *  parses the frequency value, figures out what Pitch Wheel and Note events need to be called to
 *  generate this frequency in MIDI, and returns the full set of Staccato Pitch Wheel and Note 
 *  events.
 */
public interface Preprocessor 
{
	public String preprocess(String musicString, StaccatoParserContext context);
}
