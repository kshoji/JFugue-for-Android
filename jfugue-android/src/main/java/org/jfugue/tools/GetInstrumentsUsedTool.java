/*
 * JFugue, an Application Programming Interface (API) for Music Programming
 * http://www.jfugue.org
 *
 * Copyright (C) 2003-2014 David Koelle
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jfugue.tools;

import java.util.ArrayList;
import java.util.List;

import org.jfugue.parser.ParserException;
import org.jfugue.parser.ParserListenerAdapter;
import org.jfugue.pattern.PatternProducer;
import org.staccato.StaccatoParser;

/**
 *  Returns a List<Byte> containing all of the instruments used in the given pattern.
 *  This is especially useful for loading instruments from a soundbank, to make sure
 *  you only load instruments that are needed by the pattern. 
 *
 * @author David Koelle
 * @version 4.0
 */
public class GetInstrumentsUsedTool extends ParserListenerAdapter
{
    private List<Byte> instruments;

    public GetInstrumentsUsedTool()
    {
        instruments = new ArrayList<Byte>();
    }

    @Override
    public void onInstrumentParsed(byte instrument) {
        if (!instruments.contains(instrument)) {
            instruments.add(instrument);
        }
    }

    public List<Byte> getInstrumentsUsedInPattern(PatternProducer patternProducer)
    {
    	this.instruments.clear();
    	
        StaccatoParser parser = new StaccatoParser();
        parser.addParserListener(this);
        try {
        	parser.parse(patternProducer);
        } catch (ParserException e) {
        	throw new RuntimeException(e);
        } 
        
        return this.instruments;
    }
}

