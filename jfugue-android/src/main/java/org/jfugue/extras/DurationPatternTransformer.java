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
 * The DurationPatternTransformer multiplies the duration of all notes in the given
 * Pattern by a factor passed as a parameter.
 *
 * <p>
 * This transformer can be used to increase or decrease the duration of notes.  To increase
 * the duration, use a variable greater than 1.0.  To decrease the duration, use a value
 * less than 1.0.  The default value for this transformer is 1.0, which will result in
 * no change to your Pattern.
 * </p>
 *
 * <p>
 * For general information on how Pattern Transformers work, refer to the JFugue
 * documentation.
 * </p>
 *
 *@author David Koelle
 *@version 2.0
 */
public class DurationPatternTransformer extends PatternTransformer
{
    private double factor;
    
    /**
     * Instantiates a new DurationPatternTransformer object.  The default value by which
     * to multiply the duration is 1.0, which will result in no change to the given Music
     * String.
     */
    public DurationPatternTransformer(double factor)
    {
        this.factor = factor;
    }

    /** Transforms the given note */
    public void noteEvent(Note note)
    {
        double durationValue = note.getDecimalDuration();
        durationValue *= this.factor;
        note.setDecimalDuration(durationValue);

        getReturnPattern().addElement(note);
    }

    /** Transforms the given note */
    public void sequentialNoteEvent(Note note)
    {
        double durationValue = note.getDecimalDuration();
        durationValue *= this.factor;
        note.setDecimalDuration(durationValue);

        getReturnPattern().addElement(note);
    }

    /** Transforms the given note */
    public void parallelNoteEvent(Note note)
    {
        double durationValue = note.getDecimalDuration();
        durationValue *= this.factor;
        note.setDecimalDuration(durationValue);

        getReturnPattern().addElement(note);
    }
}
