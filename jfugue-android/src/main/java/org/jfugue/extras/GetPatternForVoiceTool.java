/*
 * JFugue - API for Music Programming
 * Copyright (C) 2003-2008  David Koelle
 *
 * http://www.jfugue.org 
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *  
 */

package org.jfugue.extras;

import org.jfugue.ChannelPressure;
import org.jfugue.Controller;
import org.jfugue.Instrument;
import org.jfugue.JFugueElement;
import org.jfugue.KeySignature;
import org.jfugue.Layer;
import org.jfugue.Measure;
import org.jfugue.Note;
import org.jfugue.ParserListenerAdapter;
import org.jfugue.Pattern;
import org.jfugue.PitchBend;
import org.jfugue.PolyphonicPressure;
import org.jfugue.Tempo;
import org.jfugue.Time;
import org.jfugue.Voice;

/**
 * Returns all of the MusicString events that are played in the requested Voice (i.e., Channel)
 * 
 *@author David Koelle
 *@version 3.0
 * 
 */
public class GetPatternForVoiceTool extends ParserListenerAdapter
{
    private byte voice = 0;
    private byte activeVoice = 0;
    private Pattern pattern;

    public GetPatternForVoiceTool(int voice)
    {
        this.voice = (byte)voice;
        reset();
    }
    
    public void reset()
    {
        pattern = new Pattern();
        activeVoice = 0;
    }
    
    public void voiceEvent(Voice voice)
    {
        if (activeVoice != voice.getVoice())
        {
            this.activeVoice = voice.getVoice();
            addElementIfActiveVoice(voice);
        }
    }

    /**
     * Tempo changes affect the voice regardless of what voice they appear to be in
     */
    public void tempoEvent(Tempo tempo) 
    {
        pattern.add(tempo.getMusicString());
    }

    /**
     * Key Signature changes affect the voice regardless of what voice they appear to be in
     */
    public void keySignatureEvent(KeySignature keySig)  
    {
        pattern.add(keySig.getMusicString());
    }
    
    /**
     * Called when the parser encounters an instrument event.
     * @param instrument the event that has been parsed
     * @see Instrument
     */
    public void instrumentEvent(Instrument instrument) 
    {
        addElementIfActiveVoice(instrument);
    }

    /**
     * Called when the parser encounters a layer event.
     * @param layer the event that has been parsed
     * @see Layer
     */
    public void layerEvent(Layer layer) 
    {
        addElementIfActiveVoice(layer);
    }

    /**
     * Called when the parser encounters a measure event.
     * @param measure the event that has been parsed
     * @see Measure
     */
    public void measureEvent(Measure measure) 
    {
        addElementIfActiveVoice(measure);
    }
    
    /**
     * Called when the parser encounters a time event.
     * @param time the event that has been parsed
     * @see Time
     */
    public void timeEvent(Time time) 
    {
        addElementIfActiveVoice(time);
    }
    
    
    /**
     * Called when the parser encounters a controller event.
     * @param controller the event that has been parsed
     */
    public void controllerEvent(Controller controller) 
    {
        addElementIfActiveVoice(controller);
    }
    
    /**
     * Called when the parser encounters a channel pressure event.
     * @param channelPressure the event that has been parsed
     * @see ChannelPressure
     */
    public void channelPressureEvent(ChannelPressure channelPressure) 
    {
        addElementIfActiveVoice(channelPressure);
    }
    
    /**
     * Called when the parser encounters a polyphonic pressure event.
     * @param polyphonicPressure the event that has been parsed
     * @see PolyphonicPressure
     */
    public void polyphonicPressureEvent(PolyphonicPressure polyphonicPressure) 
    {
        addElementIfActiveVoice(polyphonicPressure);
    }
    
    /**
     * Called when the parser encounters a pitch bend event.
     * @param pitchBend the event that has been parsed
     * @see PitchBend
     */
    public void pitchBendEvent(PitchBend pitchBend) 
    {
        addElementIfActiveVoice(pitchBend);
    }

    /**
     * Called when the parser encounters an initial note event.
     * @param note the event that has been parsed
     * @see Note
     */
    public void noteEvent(Note note)
    {
        addElementIfActiveVoice(note);
    }

    /**
     * Called when the parser encounters a sequential note event.
     * @param note the event that has been parsed
     * @see Note
     */
    public void sequentialNoteEvent(Note note) 
    { 
        addElementIfActiveVoice(note);
    }

    /**
     * Called when the parser encounters a parallel note event.
     * @param note the event that has been parsed
     * @see Note
     */
    public void parallelNoteEvent(Note note) 
    {
        addElementIfActiveVoice(note);
    }
    

    private void addElementIfActiveVoice(JFugueElement element)
    {
        if (activeVoice == voice)
        {
            pattern.add(element.getMusicString());
        }
    }
    
    public Pattern getPattern()
    {
        return pattern;
    }
}

