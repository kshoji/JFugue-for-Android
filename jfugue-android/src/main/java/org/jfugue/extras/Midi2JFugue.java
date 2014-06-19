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

import jp.kshoji.javax.sound.midi.InvalidMidiDataException;

public class Midi2JFugue
{
    public static void main(String[] args)
    {
        if (args.length < 3)
        {
            printUsage();
            System.exit(0);
        }
        
        Player player = new Player();
        Pattern pattern = null;
        try {
            pattern = player.loadMidi(new File(args[1]));
            pattern.savePattern(new File(args[2]));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
        }

        System.exit(0);
    }
    
    public static void printUsage()
    {
        System.out.println("Midi2JFugue - convert MIDI files to a JFugue MusicString pattern");
        System.out.println("Usage: Midi2JFugue <input-midi-filename> <output-pattern-filename>");
        System.out.println("Example: Midi2JFugue MySong.mid MyPattern.jfugue");
    }
}
