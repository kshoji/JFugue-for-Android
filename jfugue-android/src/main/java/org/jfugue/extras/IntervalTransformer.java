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

import org.jfugue.Note;
import org.jfugue.PatternTransformer;

public class IntervalTransformer extends PatternTransformer
{
    private int delta;
    
    /** Delta can be a positive or negative number indicating
     *  how many half-steps the note should be changed to.
     *  An octave is 12 steps.  
     * @param delta
     */
    public IntervalTransformer(int delta)
    {
        this.delta = delta;
    }
    
    public void noteEvent(Note note)
    {
        byte currentValue = note.getValue();
        int newValue = currentValue + delta;
        if ((newValue > 0) && (newValue < 128)) {
            note.setValue((byte)(currentValue + delta));
        } else {
            note.setValue((byte)0);
        }
        getReturnPattern().addElement(note);
    }
}
