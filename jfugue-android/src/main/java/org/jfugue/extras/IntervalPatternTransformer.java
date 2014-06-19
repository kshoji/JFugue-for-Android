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
 * The IntervalPatternTransformer alters music by changing the interval, or step, for each
 * note in the given Pattern. For example, a C5 (note 60) raised 3 steps would turn into a 
 * D#5 (note 63).  The interval is passed in as a parameter.
 *
 * <p>
 * For general information on how Pattern Transformers work, refer to the JFugue
 * documentation.
 * </p>
 *
 *@author David Koelle
 *@version 2.0
 */
public class IntervalPatternTransformer extends PatternTransformer
{
    private int interval;
    
    /**
     * Instantiates a new IntervalPatternTransformer object.  The default value by which
     * to increase the duration is 1.
     */
    public IntervalPatternTransformer(int interval)
    {
        this.interval = interval;
    }

    /** Transforms the given note */
    public void noteEvent(Note note)
    {
        byte noteValue = note.getValue();
        noteValue += this.interval;
        note.setValue(noteValue);

        getReturnPattern().addElement(note);
    }

    /** Transforms the given note */
    public void sequentialNoteEvent(Note note)
    {
        byte noteValue = note.getValue();
        noteValue += this.interval;
        note.setValue(noteValue);

        getReturnPattern().addElement(note);
    }

    /** Transforms the given note */
    public void parallelNoteEvent(Note note)
    {
        byte noteValue = note.getValue();
        noteValue += this.interval;
        note.setValue(noteValue);

        getReturnPattern().addElement(note);
    }
}
