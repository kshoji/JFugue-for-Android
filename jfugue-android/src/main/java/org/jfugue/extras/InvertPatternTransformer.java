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
 * Inverts the notes of the given pattern, around the provided "fulcrum" note.
 * Suppose you are inverting about the note "D5".  When this
 * transformer comes across a "C5" note, for example, it computes the difference
 * between the nodes, then replaces C5 with a new note that is the same number
 * of intervals on the other side of the fulcrum.  C5 would become E5.    
 * 
 *@author David Koelle
 *@version 4.0
 * 
 */
public class InvertPatternTransformer extends PatternTransformer
{
    private byte fulcrumNoteValue;
    
    public InvertPatternTransformer(Note note)
    {
        this.fulcrumNoteValue = note.getValue();
    }

    /** Transforms the given note */
    @Override
    public void noteEvent(Note note)
    {
        doNoteEvent(note);
    }

    /** Transforms the given note */
    @Override
    public void sequentialNoteEvent(Note note)
    {
        doNoteEvent(note);
    }

    /** Transforms the given note */
    @Override
    public void parallelNoteEvent(Note note)
    {
        doNoteEvent(note);
    }
    
    private void doNoteEvent(Note note)
    {
        byte noteValue = note.getValue();
        
        if (noteValue > fulcrumNoteValue) {
            note.setValue((byte)(fulcrumNoteValue - (noteValue - fulcrumNoteValue)));
            getReturnPattern().addElement(note);
        } else if (noteValue < fulcrumNoteValue) {
            note.setValue((byte)(fulcrumNoteValue - (fulcrumNoteValue - noteValue)));
            getReturnPattern().addElement(note);
        } else {
            //  No change in note value
            getReturnPattern().addElement(note);
        }
    }
}

