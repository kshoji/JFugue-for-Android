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

import org.jfugue.*;

/**
 * Reverses a given pattern.
 *
 *@author David Koelle
 *@version 2.0
 */
public class ReversePatternTransformer extends PatternTransformer
{
    public ReversePatternTransformer()
    {
        super();
    }

    /**
     * ReversePatternTransformer does not require that the user specify any variables.
     */
    public String getParameters()
    {
        return "";
    }

    public String getDescription()
    {
        return "Reverses the given pattern";
    }

    public void voiceEvent(Voice voice)
    {
        insert(voice.getMusicString(), " ");
    }

    public void timeEvent(Time time)
    {
        // nothing to do?
    }

    public void tempoEvent(Tempo tempo)
    {
        insert(tempo.getMusicString(), " ");
    }

    public void keySignatureEvent(KeySignature keySig)
    {
        insert(keySig.getMusicString(), " ");
    }

    public void instrumentEvent(Instrument instrument)
    {
        insert(instrument.getMusicString(), " ");
    }

    public void controllerEvent(Controller controller)
    {
        insert(controller.getMusicString(), " ");
    }

    public void channelPressureEvent(ChannelPressure channelPressure)
    {
        insert(channelPressure.getMusicString(), " ");
    }

    public void polyphonicPressureEvent(PolyphonicPressure polyphonicPressure)
    {
        insert(polyphonicPressure.getMusicString(), " ");
    }

    public void pitchBendEvent(PitchBend pitchBend)
    {
        insert(pitchBend.getMusicString(), " ");
    }

    public void noteEvent(Note note)
    {
        insert(note.getMusicString(), " ");
    }

    public void sequentialNoteEvent(Note note)
    {
        insert(note.getMusicString().substring(1, note.getMusicString().length()), "_");
    }

    public void parallelNoteEvent(Note note)
    {
        insert(note.getMusicString().substring(1, note.getMusicString().length()), "+");
    }
    
    private void insert(String string, String connector)
    {
        StringBuilder buddy = new StringBuilder();
        buddy.append(string);
        buddy.append(connector);
        buddy.append(getReturnPattern().getMusicString());
        getReturnPattern().setMusicString(buddy.toString());
    }

    public static final String INTERVAL = "interval";
}