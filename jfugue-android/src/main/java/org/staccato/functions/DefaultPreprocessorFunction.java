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

import org.staccato.DefaultNoteSettingsManager;
import org.staccato.PreprocessorFunction;
import org.staccato.StaccatoParserContext;

public class DefaultPreprocessorFunction implements PreprocessorFunction 
{
	private static DefaultPreprocessorFunction instance;
	
	private DefaultPreprocessorFunction() { } 
	
	public static DefaultPreprocessorFunction getInstance() {
		if (instance == null) {
			instance = new DefaultPreprocessorFunction();
		}
		return instance;
	}

	private static String OCTAVE = "OCTAVE";
	private static String BASE_OCTAVE = "BASS_OCTAVE";
	private static String DURATION = "DURATION";
	private static String ATTACK = "ATTACK";
	private static String DECAY = "DECAY";

	@Override
	public String apply(String parameters, StaccatoParserContext context) {
		String[] defaultSettings = parameters.split(",");
		for (String defaultSetting : defaultSettings) {
			String[] defaultValues = defaultSetting.split("=");
			if (defaultValues.length != 2) {
				throw new RuntimeException("DefaultProcessor found this setting, which is not in the form KEY=VALUE: "+defaultSetting);
			}
			String key = defaultValues[0];
			String value = defaultValues[1];
			
			if (key.equalsIgnoreCase(OCTAVE)) {
				DefaultNoteSettingsManager.getInstance().setDefaultOctave(Byte.parseByte(value));
			} else if (key.equalsIgnoreCase(BASE_OCTAVE)) {
				DefaultNoteSettingsManager.getInstance().setDefaultBassOctave(Byte.parseByte(value));
			} else if (key.equalsIgnoreCase(DURATION)) {
				double dur = 0.0d;
				try {
					dur = Double.parseDouble(value);
					DefaultNoteSettingsManager.getInstance().setDefaultDuration(dur);
				} catch (NumberFormatException e) {
					throw new RuntimeException("Currently, default duration must be specified as a decimal. For example, please use 0.5 for 'h', 0.25 for 'q', and so on. You had entered: "+value);
				}
			} else if (key.equalsIgnoreCase(ATTACK)) {
				DefaultNoteSettingsManager.getInstance().setDefaultOnVelocity(Byte.parseByte(value));
			} else if (key.equalsIgnoreCase(DECAY)) {
				DefaultNoteSettingsManager.getInstance().setDefaultOffVelocity(Byte.parseByte(value));
			} else {
				throw new RuntimeException("DefaultProcessor found this setting where the key is not recognized: "+defaultSetting+" (key should be one of the following: "+OCTAVE+", "+BASE_OCTAVE+", "+DURATION+", "+ATTACK+", or "+DECAY);
			}
		}
		
		return "";
	}

	@Override
	public String[] getNames() {
		return NAMES;
	}
	
	private String[] NAMES = new String[] { "DEFAULT", "DEFAULTS" };
}
