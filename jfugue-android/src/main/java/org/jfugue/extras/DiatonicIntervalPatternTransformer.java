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
 * The DiatonicIntervalPatternTransformer transposes all notes in the given
 * Pattern by a diatonic interval (1 -- unison, 2 -- second, ... 8 -- octave, etc.). 
 * It only handles upward motion and assumes the key of C.  However, it could be
 * used in conjunction with IntervalPatternTransformer to change to another key and/or
 * perform downward motion.
 *
 * <p>
 * For general information on how Pattern Transformers work, refer to the JFugue
 * documentation.
 * </p>
 *
 *@author Bill Manaris (based on code by David Koelle)
 *@version 2.0m
 */
 
public class DiatonicIntervalPatternTransformer extends PatternTransformer
{
    private int interval = 1;
    
    /**
     * @param interval Number of diatonic intervals by which to change each note, can be only positive
     */
    public DiatonicIntervalPatternTransformer(int interval)
    {
        this.interval = interval;
    }
    
    /* Helper method, used to transpose a note by a certain diatonic interval */
    private Note adjustNote(Note note, int interval)
    {
        int noteValue;         // holds note's MIDI value (0 .. 127)
        int scaleDegree;       // holds the scale degree of a given note (assume key of C)
        boolean isPassingNote; // true if note is not in the scale of C, false otherwise.
        int octave = 0;        // holds octaves spanned by diatonic interval (zero means interval is 0..7)
        
        noteValue = (int)note.getValue();
        
        // check if interval is outside 1..7 range
        if (interval > 7)
        {
           octave = interval / 8;          // get octaves spanned by interval (1 or more)
           interval = (interval + 1) % 8;  // map interval into 1..7 range
        }
        else if (interval < 1)
           System.err.println("Error! DiatonicIntervalPatternTransformer can handle only positive intervals (" + interval + ")");
        
        // retrieve the scale degree of this note
        scaleDegree = NoteToDegree[noteValue % 12];

        // check if the note is in the scale of C (see NoteToDegree[] array)
        isPassingNote = (scaleDegree < 0);
        
        // if it's NOT in the scale, sharpen it temporarily, so as to be in the scale
        // for transposition purposes; but we need to remember to flatten it afterwards
        // (see NoteToDegree[] array)
        if (isPassingNote)
        {
           scaleDegree = -scaleDegree;
           noteValue  += 1;   
        }

        // transpose note to the given interval
        noteValue += DegreeToNote[scaleDegree + interval-1] - DegreeToNote[scaleDegree];

        // if originally not in the scale, let's flatten it (see NoteToDegree[] array)
        if (isPassingNote)
           noteValue -= 1;   
           
        // adjust note by octaves spanned by interval
        noteValue += (octave * 12);
        
        // handle overflow caused by a large/small interval and/or note value
        if ((noteValue > 128) || (noteValue < 0))
        {
           System.err.println("Warning! Note value overflow (" + noteValue + 
                              ") in DiatonicIntervalPatternTransformer");
           noteValue %= 128;  // wrap around (correct note, but many octaves off -- catches the ear's attention)
        }

        note.setValue((byte)noteValue);  // update MIDI value
        
        return note;  
    }
    
    /** Transforms the given note */
    public void noteEvent(Note note)
    {
        note = adjustNote(note, interval);
        getReturnPattern().addElement(note);
    }

    /** Transforms the given note */
    public void sequentialNoteEvent(Note note)
    {
        note = adjustNote(note, interval);
        getReturnPattern().addElement(note);
    }

    /** Transforms the given note */
    public void parallelNoteEvent(Note note)
    {
        note = adjustNote(note, interval);
        getReturnPattern().addElement(note);
    }
    
    /* Array to map a note value (0 .. 11) to the corresponding scale degree in C
       NoteToDegree array (A negative value indicates a flat note that is not in the scale.
       We use the negated value of the note on our right, to facilitate transposition of passing notes.
       This can be easily performed by taking the absolute value (equivalent to sharpening the note,
       so that is becomes the scale note to the right, transposing this scale note, and then subtracting
       one from the resultant note value (returning it to its flattened state).)
       index (MIDI note % 12): 0  1  2  3  4  5  6  7  8  9  10  11
       value (scale degree) :  1 -2  2 -3  3  4 -4  5 -6  6  -7   7 */
    private static byte NoteToDegree[] = {1, -2, 2, -3, 3, 4, -5, 5, -6, 6, -7, 7};
    
    /* Array to map a scale degree (1 .. 13) to the corresponding note value in C
       DegreeToNote array (index 0 is not being used, so we give it a "dummy" -128 value)
       index (scale degree):  1  2  3  4  5  6  7  8  9  10  11  12  13
       value (MIDI note)   :  0  2  4  5  7  9  11 12 14 16  17  19  21 */
    private static byte DegreeToNote[] = {-128, 0, 2, 4, 5, 7, 9, 11, 12, 14, 16, 17, 19, 21};
}
