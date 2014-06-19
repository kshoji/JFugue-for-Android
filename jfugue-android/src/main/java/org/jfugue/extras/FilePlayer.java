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

import org.jfugue.Pattern;
import org.jfugue.Player;

import java.io.File;
import java.io.IOException;

/**
 * Plays music strings from a text file.
 *
 * <p>
 * Here's a sample file:
 *<font color="blue"><pre>
 * #
 * # "Inventio 13" - Beethoven   (First Measure)
 * #
 *
 * V1 T160
 * V1 Rs  E4s A4s C5s B4s E4s B4s D5s C5i      E5i     G#4i    E5i
 * V2 A2i     A3q             G#3i    A3s G#3s A3s C4s B3s E3s B3s D4s
 *</pre></font>
 *</p>
 *
 * <p>
 * Note the use of # as a comment character when used as the first character of a line.
 * </p>
 *
 * <p>
 * To use FilePlayer, enter "<i>java org.jfugue.FilePlayer input-filename [output-filename]</i>" from the
 * command prompt, where <i>input-filename</i> is the name of your text file that specifies
 * the music, and <i>output-filename</i> is the name of the MIDI file to create.  (If you
 * don't give <i>output-filename</i>, it will default to player.mid)
 * </p>
 *
 *@author David Koelle
 *@version 2.0
 *
 */
public class FilePlayer
{
    /** Given a filename, returns a string of the contents of that file.  If the file
     *  contains properly-formed music strings, then the contents of the file can
     *  be placed directly into a Pattern.
     *  <br><br>
     *  This method will regard any line that begins with a # character as a comment,
     *  and will not return the commented line.  Note - # characters at locations
     *  <i>other</i> than the first character of a line will not be seen as comment characters.
     */

    public static void main(String[] args)
    {
        String inFilename = null;
        String outFilename = null;

        if ((args.length > 1) && (args[1] != null)) {
            inFilename = args[1];
        } else {
            System.out.println("Need to specify an input file!");
            System.exit(0);
        }

        if ((args.length > 2) && (args[2] != null)) {
            outFilename = args[2];
        } 

        // Create a player, and play the music!
        try {
            Pattern pattern = Pattern.loadPattern(new File(inFilename));
            Player player = new Player();
            player.saveMidi(pattern, new File(outFilename));
            player.play(pattern);
            player.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        
        System.exit(0);
    }
}

