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

/**
 *  The MicrotonePreprocess lets a user express a microtone
 *  using 'm' followed by the frequency - e.g., m440. The MicrotonePreprocessor takes this String,
 *  parses the frequency value, figures out what Pitch Wheel and Note events need to be called to
 *  generate this frequency in MIDI, and returns the full set of Staccato Pitch Wheel and Note 
 *  events.
 */
public class MicrotonePreprocessor implements Preprocessor
{
	private static MicrotonePreprocessor instance;
	
	public static MicrotonePreprocessor getInstance() {
		if (instance == null) {
			instance = new MicrotonePreprocessor();
		}
		return instance;
	}

	private static Pattern microtonePattern = Pattern.compile("(^|\\s)[Mm]\\S+");
	private static Pattern frequencyPattern = Pattern.compile("[0-9.]+");
	private static Pattern qualifierPattern = Pattern.compile("[WHQISTXOADwhqistxoad/]+[0-9.]*\\S*");
	
	@Override
	public String preprocess(String s, StaccatoParserContext context) {
		StringBuilder buddy = new StringBuilder();
		int posPrev = 0;
	
		Matcher m = microtonePattern.matcher(s);
		while (m.find()) {
			buddy.append(s, posPrev, m.start());
			
			double frequency = 0.0d;
			Matcher frequencyMatcher = frequencyPattern.matcher(m.group());
			if (frequencyMatcher.find()) {
				frequency = Double.parseDouble(frequencyMatcher.group());
			} else {
			    throw new IllegalArgumentException("The following is not a valid microtone frequency: "+frequencyMatcher.group()); 
			}

			String qualifier = null;
            Matcher qualifierMatcher = qualifierPattern.matcher(m.group());
            if (qualifierMatcher.find()) {
                qualifier = qualifierMatcher.group();
            }
			if (qualifier == null) {
			    qualifier = "/" + DefaultNoteSettingsManager.getInstance().getDefaultDuration();
			}
			
			buddy.append(" ");
			buddy.append(convertFrequencyToStaccato(frequency, qualifier));
			
			posPrev = m.end(); 
		}
		buddy.append(s.substring(posPrev, s.length()));
		return buddy.toString().trim();
	}
	
    /**
     * Converts the given frequency to a music string that involves
     * the Pitch Wheel and notes to create the frequency
     * @param freq the frequency
     * @return a MusicString that represents the frequency
     */
    public static String convertFrequencyToStaccato(double frequency, String qualifier)
    {
        double totalCents = 1200 * Math.log(frequency / 16.3515978312876) / Math.log(2);
        double octave = Math.round(totalCents / 1200.0);
        double semitoneCents = totalCents - (octave * 1200.0);
        double semitone = Math.round(semitoneCents / 100.0);
        double microtonalAdjustment = semitoneCents - (semitone * 100.0);
        double pitches = 8192.0 + (microtonalAdjustment * 8192.0 / 100.0);

        double note = ((octave+1)*12)+semitone; // This gives a MIDI value, 0 - 128
        if (note > 127) note = 127;

        StringBuilder buddy = new StringBuilder();
        buddy.append(":PitchWheel(");
        buddy.append((int)pitches);
        buddy.append(") ");
        buddy.append((int)note);
        buddy.append(qualifier);
        buddy.append(" :PitchWheel(8192)"); // Reset the pitch wheel.  8192 = original pitch wheel position
        return buddy.toString();
    }
}
