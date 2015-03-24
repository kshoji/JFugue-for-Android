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

package org.jfugue.provider;

import org.jfugue.theory.Key;

/**
 * This interface must be implemented by the parser responsible for Staccato strings.
 */
public interface KeyProvider
{
	/** Given a key signature, like "Cmaj" or "Kbbbb", return the corresponding Key */
    public Key createKey(String keySignature);
    
    /** Creates a key name, like Cmaj, given the root note's position in an octave (e.g., 0 for C) and a major or minor indicator - @see Scale MAJOR_SCALE_INDICATOR and MINOR_SCALE_INDICATOR */
    public String createKeyString(byte notePositionInOctave, byte scale);
    
    /** Converts the given Key to a byte value, from -7 for Cb major or Ab major to +7 for C# minor or A# minor, with 0 being C major or A minor */ 
    public byte convertKeyToByte(Key key);
}
