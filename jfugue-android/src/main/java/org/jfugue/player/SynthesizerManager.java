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

package org.jfugue.player;

import jp.kshoji.javax.sound.midi.MidiSystem;
import jp.kshoji.javax.sound.midi.MidiUnavailableException;
import jp.kshoji.javax.sound.midi.Synthesizer;

public class SynthesizerManager {
	private static SynthesizerManager instance;

	public static SynthesizerManager getInstance() throws MidiUnavailableException {
		if (instance == null) {
			instance = new SynthesizerManager();
		}
		return instance;
	}
	
	private Synthesizer synth;

	private SynthesizerManager() throws MidiUnavailableException {
		this.synth = getDefaultSynthesizer();
	}
	
	public Synthesizer getDefaultSynthesizer() throws MidiUnavailableException {
		return MidiSystem.getSynthesizer();
	}
	
	public void setSynthesizer(Synthesizer synth) {
		this.synth = synth;
	}
	
	public Synthesizer getSynthesizer() {
		return this.synth;
	}

}
