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

package org.jfugue.midi;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jp.kshoji.javax.sound.midi.InvalidMidiDataException;
import jp.kshoji.javax.sound.midi.MidiSystem;
import jp.kshoji.javax.sound.midi.Patch;
import jp.kshoji.javax.sound.midi.Soundbank;
import jp.kshoji.javax.sound.midi.Synthesizer;

public class PatchProvider {
    private File soundbankFile;
    private boolean patchesProvided = false;
    private List<Patch> patches;
    
    public PatchProvider(File soundbankFile, int... patchIndexes) {
        this.soundbankFile = soundbankFile;
        this.patches = new ArrayList<Patch>();
        for (int patchIndex : patchIndexes) {
            this.patches.add(new Patch(MidiDefaults.DEFAULT_PATCH_BANK, patchIndex)); 
        }
        this.patchesProvided = true;
    }

    public PatchProvider(File soundbankFile, List<Patch> patches) {
        this.soundbankFile = soundbankFile;
        this.patches = patches;
        this.patchesProvided = true;
    }

    public PatchProvider(File soundbankFile) {
        this.soundbankFile = soundbankFile;
        this.patchesProvided = false;
    }
    
    public File getSoundbankFile() {
        return this.soundbankFile;
    }
    
    public List<Patch> getPatches() {
        return this.patches;
    }

    /** 
     * Loads the soundbank into the given synthesizer - or tries to. If the method is not successful,
     * it will return a status other than STATUS_OK.
     */
    public String loadPatchesIntoSynthesizer(Synthesizer synth) throws InvalidMidiDataException, IOException {
        if (getSoundbankFile() == null) {
            return STATUS_NO_SOUNDBANK_FILE;
        }
        
        Soundbank soundbank = MidiSystem.getSoundbank(getSoundbankFile());
        if (!synth.isSoundbankSupported(soundbank)) {
            return STATUS_SOUNDBANK_NOT_SUPPORTED;
        }
        
        if (this.patchesProvided) {
            synth.loadInstruments(soundbank, this.getPatches().toArray(new Patch[0]));
        } else {
            synth.loadAllInstruments(soundbank);
        }
        
        return STATUS_OK;
    }

    /** Status when loadPatchesIntoSynthesizer is successful */
    public final static String STATUS_OK = "OK";
    
    /** Status when the PatchProvider has no defined soundbank file */
    public final static String STATUS_NO_SOUNDBANK_FILE = "PatchProvider has no soundbank file";

    /** Status when the Synthesizer given to loadPatchesIntoSynthesizer cannot support the loaded soundbank */
    public final static String STATUS_SOUNDBANK_NOT_SUPPORTED = "Soundbank not supported by synthesizer";
}