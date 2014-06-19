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

import java.util.ArrayList;
import java.util.List;

import org.jfugue.Instrument;
import org.jfugue.MusicStringParser;
import org.jfugue.ParserListenerAdapter;
import org.jfugue.Pattern;

/**
 * Returns a List<Byte> containing all of the instruments used in the given pattern.
 *
 *@author David Koelle
 *@version 4.0
 *
 */
public class GetInstrumentsUsedTool extends ParserListenerAdapter
{
    private List<Byte> instruments;

    public GetInstrumentsUsedTool()
    {
        instruments = new ArrayList<Byte>();
    }

    @Override
    public void instrumentEvent(Instrument instrument)
    {
        byte b = instrument.getInstrument();
        if (!instruments.contains(b)) {
            instruments.add(b);
        }
    }

    public List<Byte> getInstrumentsUsedInPattern(Pattern pattern)
    {
        MusicStringParser parser = new MusicStringParser();
        parser.addParserListener(this);
        parser.parse(pattern);

        return instruments;
    }
}

