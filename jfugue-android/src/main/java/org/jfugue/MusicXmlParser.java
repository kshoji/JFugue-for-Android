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

package org.jfugue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;

import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.DocType;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Node;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

//	helper class
class XMLpart extends Object
{	public String	ID;
	public String	part_name;
	public String	score_instruments;
	public String	midi_instruments;	//	channel1|name1~channel2|name2
	public XMLpart()
	{	ID = "";
		part_name = "";
		score_instruments = "";
		midi_instruments = "";
	}
};

/**
 * voiceDef
 * MusicString voice can be a combination of part and voice
 */

class voiceDef
{
	int	part;
	int voice;
}

/**
 * Parses a MusicXML file, and fires events for <code>ParserListener</code> interfaces
 * when tokens are interpreted. The <code>ParserListener</code> does intelligent things
 * with the resulting events, such as create music, draw sheet music, or
 * transform the data.
 *
 * As of Version 3.0, the Parser supports turning MIDI Sequences into JFugue Patterns with the parse(Sequence)
 * method.  In this case, the ParserListeners established by a ParserBuilder use the parsed
 * events to construct the Pattern string.
 *
 * MusicXmlParser.parse can be called with a file name, File, InputStream, or Reader
 * 
 * @author E.Philip Sobolik
 *
 */
public final class MusicXmlParser extends Parser
{	private Map<String, Object> dictionaryMap;
	private Builder xomBuilder;
	private Document xomDoc;
	private String[] volumes = {"pppppp", "ppppp", "pppp", "ppp", "pp", "p", "mp",
								"mf", "f", "ff", "fff", "ffff", "fffff", "ffffff"};
	//	note difference between maxVolume and minVolume should be divisible by 13
	private byte	minVelocity = 10;
	private byte	maxVelocity = 127;
	private byte	curVelocity = Note.DEFAULT_VELOCITY;
	private byte	beats;		//	beats per measure
	private byte	divisions;	//	divisions per beat
	private int		curVoice;	//	current voice
	private int		nextVoice;	//	next available voice # for a new voice
	private voiceDef[]	voices;
//	private	double	totalMeasurePct;
//	private double	lastNoteInMeasureDuration;	//	adjusted duration of the
												//	last note in the measure
	
	public MusicXmlParser()
	{	xomBuilder = new Builder();
		dictionaryMap = new HashMap<String, Object>();
		JFugueDefinitions.populateDictionary(dictionaryMap);
		beats = 1;
		divisions = 1;
		curVoice = -1;
		nextVoice = 0;
		voices = new voiceDef[15];
//		totalMeasurePct = 0.;
//		lastNoteInMeasureDuration = 0.f;
	}

    public void parse(String musicXmlString)
    {
        try 
        { xomDoc = xomBuilder.build(musicXmlString, (String)null);  // URI is null
        } 
        catch (ValidityException e)
        {   e.printStackTrace();
        }
        catch (ParsingException e)
        {   e.printStackTrace();
        }
        catch (IOException e)
        {   e.printStackTrace();
        }

        parse();
    }

	public void parse(File fileXMLin)
	{	try
		{	xomDoc = xomBuilder.build(fileXMLin);
		}
		catch (ValidityException e)
		{	e.printStackTrace();
		}
		catch (ParsingException e)
		{	e.printStackTrace();
		}
		catch (IOException e)
		{	e.printStackTrace();
		}

		parse();
	}
	
	public void parse(FileInputStream fisXMLin)
	{	try
		{	xomDoc = xomBuilder.build(fisXMLin);
		}
		catch (ValidityException e)
		{	e.printStackTrace();
		}
		catch (ParsingException e)
		{	e.printStackTrace();
		}
		catch (IOException e)
		{	e.printStackTrace();
		}

		parse();
	}
	
	public void parse(Reader rXMLin)
	{	try
		{	xomDoc = xomBuilder.build(rXMLin);
		}
		catch (ValidityException e)
		{	e.printStackTrace();
		}
		catch (ParsingException e)
		{	e.printStackTrace();
		}
		catch (IOException e)
		{	e.printStackTrace();
		}

		parse();
	}
	
    /////////////////////////////////////////////////////////////////////////
    // Tempo methods
    //

    /** The default value for the Tempo. */
    private int tempo = 120;

    /**
     * Sets the tempo for the current song.  Tempo is measured in "pulses per quarter".
     * The parser uses this value to convert note durations, which are relative values and
     * not directly related to time measurements, into actual times.  For example, a whole
     * note has the same duration as four quarter notes, but neither a whole note nor a
     * quarter note equates to any real-life time delay until it's multplied by the tempo.
     *
     * The default value for Tempo is 120 pulses per quarter.
     *
     * @param tempo the tempo for the current song, in pulses per quarter.
     */
    protected void setTempo(int tempo)
    {	this.tempo = tempo;
    }

    /**
     * Returns the tempo for the current song.
     */
    protected int getTempo()
    {	return this.tempo;
    }

    //
    // End Tempo methods
    /////////////////////////////////////////////////////////////////////////

    /**
     * Parses a MusicXML file and fires events to subscribed <code>ParserListener</code>
     * interfaces.  As the file is parsed, events are sent
     * to <code>ParserLisener</code> interfaces, which are responsible for doing
     * something interesting with the music data, such as playing the music,
     * displaying it as sheet music, or transforming the pattern.
     *
     * the input is a XOM Document, which has been built previously
     * @throws Exception if there is an error parsing the pattern
     */
	
	public void parse() throws JFugueException
	{	DocType docType = xomDoc.getDocType();
		Element	root = xomDoc.getRootElement();
	
		if (docType.getRootElementName().compareToIgnoreCase("score-partwise") == 0)
		{	Element partlist = root.getFirstChildElement("part-list");
			Elements parts = partlist.getChildElements();
			XMLpart[] partHeaders = new XMLpart[parts.size()];
			for (int p = 0; p < parts.size(); ++p)
			{	partHeaders[p] = new XMLpart();
				parsePartHeader(parts.get(p), partHeaders[p]);
			}
			parts = root.getChildElements("part");
			for (int p = 0; p < parts.size(); ++p)
			{	parsePart(p, parts.get(p), partHeaders);
			}
		}
	}
	/**
	 * Parses a <code>part</code> element in the <code>part-list</code> section
	 * @param part is the <code>part</code> element
	 * @param partHeader is the array of <code>XMLpart</code> classes that stores
	 * the <code>part-list</code> elements
	 */
	private void parsePartHeader(Element part, XMLpart partHeader)
	{	//	ID
		Attribute ID = part.getAttribute("id");
		//	may be changed by midi-instrument below
		partHeader.ID = ID.getValue();
		//	part-name
		Element partName = part.getFirstChildElement("part-name");
		partHeader.part_name = partName.getValue();
		//	may or may not have 1 or more score-instrument and 
		//	midi-instrument elements
		//	score-instruments
		int x;
		Elements scoreInsts = part.getChildElements("score-instrument");
		for (x = 0; x < scoreInsts.size(); ++x )
		{	partHeader.score_instruments += scoreInsts.get(x).getValue();
			if (x < scoreInsts.size()-1)
				partHeader.score_instruments += "~";
		}
		//	midi-instruments
		Elements midiInsts = part.getChildElements("midi-instrument");
		for (x = 0; x < midiInsts.size(); ++x )
		{	Element midi_instrument = midiInsts.get(x);
			Element midi_channel = midi_instrument.getFirstChildElement("midi-channel");
			String midiChannel = (midi_channel == null) ? "" : midi_channel.getValue();
			if (midiChannel.length() > 0)
			{	partHeader.midi_instruments += midiChannel;
				partHeader.midi_instruments += "|";
			}
			Element midi_inst = midi_instrument.getFirstChildElement("midi-name");
			String midiInst = (midi_inst == null) ? "" : midi_inst.getValue();
			if (midiInst.length() < 1)
			{	Element midi_bank = midi_instrument.getFirstChildElement("midi-bank"); 
				midiInst = (midi_bank == null) ? "" : midi_bank.getValue();
				if (midiInst.length() < 1)
				{	Element midi_program = midi_instrument.getFirstChildElement("program");
					midiInst = (midi_program == null) ? "" : midi_program.getValue();
				}
			}
			partHeader.midi_instruments += midiInst;
			if (x < midiInsts.size()-1)
				partHeader.midi_instruments += "~";
		}
	}
	/**
	 * Parses a <code>part</code> and fires all the appropriate note events
	 * @param part is the entire <code>part</part>
	 * @param partHeaders is the array of XMLpart classes that contains
	 * instrument info for the <code>part</code>s
	 */
	private void parsePart(int p, Element part, XMLpart[] partHeaders)
	{	for (int x = 0; x < partHeaders.length; ++x)
		{	if (part.getAttribute("id").getValue().equals(partHeaders[x].ID))
			{	if (partHeaders[x].midi_instruments.length() < 1)
				{	parseVoice(p, x);
					parsePartElementInstruments(p, partHeaders[x].part_name);
			
				}
				else
				{	
					parsePartElementInstruments(p, partHeaders[x].midi_instruments);
				
				}
			}
		}
		Elements measures = part.getChildElements("measure");
		for (int m = 0; m < measures.size(); ++m)
		{	Element measure = measures.get(m);
			Element attributes = measure.getFirstChildElement("attributes");

			if (attributes != null)
			{	//	default key = Cmaj
				byte key = 0, scale = 0;	//	scale 0 = minor, 1 = major
				Element attr = attributes.getFirstChildElement("key");
				if (attr != null)
				{	Element eKey = attr.getFirstChildElement("fifths");
					if (eKey != null)
						key = Byte.parseByte(eKey.getValue());
					Element eMode = attr.getFirstChildElement("mode");
					if (eMode != null)
					{	String mode = eMode.getValue();
						if (mode.compareToIgnoreCase("major") == 0)
							scale = 0;
						else if (mode.compareToIgnoreCase("minor") == 0)
							scale = 1;
						else
				            throw new JFugueException(JFugueException.KEYSIG_EXC, mode);
					}
				}
				else
					scale = 0;	//	default = major
		        fireKeySignatureEvent(new KeySignature(key, scale));
		        
		        //	divisions and beats used to calculate duration when note type not present
		        Element element_divisions = attributes.getFirstChildElement("divisions");
		        if (element_divisions != null)
		        	this.divisions = Byte.valueOf(element_divisions.getValue());
		        Element element_time = attributes.getFirstChildElement("time");
		        if (element_time != null)
		        {	Element element_beats = element_time.getFirstChildElement("beats");
		        	if (element_beats != null)
		        		this.beats = Byte.valueOf(element_beats.getValue());
		        }
			}
			
	        //	tempo
			Element direction = measure.getFirstChildElement("direction");
			if (direction != null)
			{	Element directionType = direction.getFirstChildElement("direction-type");
				if (directionType != null)
				{	Element metronome = directionType.getFirstChildElement("metronome");
					if (metronome != null)
					{	Element beatUnit = metronome.getFirstChildElement("beat-unit");
						String sBeatUnit = beatUnit.getValue();
						if (sBeatUnit.compareToIgnoreCase("quarter") != 0)
							throw new JFugueException(JFugueException.BEAT_UNIT_MUST_BE_QUARTER, sBeatUnit);
						Element bpm = metronome.getFirstChildElement("per-minute");
						if (bpm != null)
						{	this.setTempo(BPMtoPPM(Float.parseFloat(bpm.getValue())));
							fireTempoEvent(new Tempo(this.getTempo()));
						}
					}
				}
			}

	        //	notes
	        Elements notes = measure.getChildElements("note");
//	        totalMeasurePct = 0.f;
	        for (int n = 0; n < notes.size(); ++n)
	        	parseNote(p, notes.get(n));
/*	attempt to adjust for rounding errors with un-supported durations
	        //	if the total length of all the notes doesn't equal a full measure,
	        //	add a pad rest
	        float	minDif = (1.f / (beats * divisions));
	        double	padDur = (1. - totalMeasurePct);
	        if (padDur > minDif)
	        {	Note pad = new Note();
	        	pad.setDecimalDuration(padDur);
	        	pad.setRest(true);
	        	fireNoteEvent(pad);
	        }
*/	        
	        fireMeasureEvent(new Measure());
		}	//	end of measure
	}

	/**
	 * parses MusicXML note Element
	 * @param note is the note Element to parse
	 */
	private void parseNote(int p, Element note)
	{	Note newNote = new Note();
		boolean isRest = false;
        boolean isStartOfTie = false;
        boolean isEndOfTie = false;
		byte	noteNumber = 0;
		byte	octaveNumber = 0;
 //       long	durationNumber = 0;
        double	decimalDuration;

        //	skip grace notes
        if (note.getFirstChildElement("grace") != null)
        	return;
        
        Element voice = note.getFirstChildElement("voice");
        if (voice != null)
        	parseVoice(p, Integer.parseInt(voice.getValue()));
        
		Element pitch = note.getFirstChildElement("pitch");
		if (pitch != null)
		{	String sStep = pitch.getFirstChildElement("step").getValue();
			switch(sStep.charAt(0))
			{	case 'C':	noteNumber = 0;		break;
				case 'D':	noteNumber = 2;		break;
				case 'E':	noteNumber = 4;		break;
				case 'F':	noteNumber = 5;		break;
				case 'G':	noteNumber = 7;		break;
				case 'A':	noteNumber = 9;		break;
				case 'B':	noteNumber = 11;	break;
			}
			Element Alter = pitch.getFirstChildElement("alter");
			if (Alter != null)
			{	String sAlter = Alter.getValue();
				if (sAlter != null)
				{	noteNumber += Integer.parseInt(sAlter);
					if (noteNumber > 11)
						noteNumber = 0;
					else if (noteNumber < 0)
						noteNumber = 11;
				}
			}
			Element Octave = pitch.getFirstChildElement("octave");
			if (Octave != null)
			{	String sOctave = Octave.getValue();
				if (sOctave != null)
					octaveNumber = Byte.parseByte(sOctave);
			}
			
	        // Compute the actual note number, based on octave and note
	        int intNoteNumber = (octaveNumber * 12) + noteNumber;
	        if ( intNoteNumber > 127) {
	            throw new JFugueException(JFugueException.NOTE_OCTAVE_EXC,"", Integer.toString(intNoteNumber));
	        }
	        noteNumber = (byte)intNoteNumber;
		}
		else
			isRest = true;
		
		//	duration
//		Element type = note.getFirstChildElement("type");
//		if (type == null)
		{	//	get duration from duration element rather than type element
			Element element_duration = note.getFirstChildElement("duration");
			
			decimalDuration = (element_duration == null)
							? beats * divisions
							:  Double.parseDouble(element_duration.getValue()) / (beats * divisions);
		}
/*		else
		{	String sDuration = type.getValue();
			if (sDuration.compareToIgnoreCase("whole") == 0)
				durationNumber = 1;
			else if (sDuration.compareToIgnoreCase("half") == 0
				durationNumber = 2;
			else if (sDuration.compareToIgnoreCase("quarter") == 0)
				durationNumber = 4;
			else if (sDuration.compareToIgnoreCase("eighth") == 0)
				durationNumber = 8;
			else if (sDuration.compareToIgnoreCase("16th") == 0)
				durationNumber = 16;
			else if (sDuration.compareToIgnoreCase("32nd") == 0)
				durationNumber = 32;
			else if (sDuration.compareToIgnoreCase("64th") == 0)
				durationNumber = 64;
			else
	            throw new JFugueException(JFugueException.NOTE_DURATION_EXC, "", sDuration);
			decimalDuration = 1.0 / durationNumber;
			Element element_dot = note.getFirstChildElement("dot");
			if (element_dot != null)
				decimalDuration *= 1.5;
		}
  */
		// Tempo is in PPQ (Pulses Per Quarter).  Turn that into
        // "PPW", then multiply that by durationNumber for WHQITXN notes
        double PPW = (double)this.getTempo() * 4.0; // 4 quarter notes in a whole note
        long duration = (long)(PPW * decimalDuration);
        
        Element notations = note.getFirstChildElement("notations");
        if (notations != null)
        {   //	ties
        	Element tied = notations.getFirstChildElement("tied");
        	if (tied != null)
        	{	Attribute tiedType = tied.getAttribute("type");
        		{	String sTiedType = tiedType.getValue();
        			if (sTiedType.compareToIgnoreCase("start") == 0)
        				isStartOfTie = true;
        			else if (sTiedType.compareToIgnoreCase("end") == 0)
        				isEndOfTie = true;
        		}
        	}
            //	velocity
        	Element dynamics = notations.getFirstChildElement("dynamics");
        	if (dynamics != null)
        	{	Node dynamic = dynamics.getChild(0);
        		if (dynamic != null)
        		{	for (int x = 0; x < this.volumes.length; ++x)
        			{	if (dynamic.getValue().compareToIgnoreCase(this.volumes[x]) == 0)
	        			{	this.curVelocity = (byte)(((this.maxVelocity - this.minVelocity)
        									/	(this.volumes.length - 1)) * x);
	        			}
        				
        			}
        		}
        	}
        }
        byte attackVelocity = this.curVelocity;
        byte decayVelocity = this.curVelocity;

        // Set up the note
        if (isRest)
        {   newNote.setRest(true);
            newNote.setDuration(duration);
            newNote.setAttackVelocity( (byte)0 );          // turn off sound for rest notes
            newNote.setDecayVelocity( (byte)0 );
        }
        else
        {	newNote.setValue(noteNumber);
            newNote.setDuration(duration);
            newNote.setStartOfTie(isStartOfTie);
            newNote.setEndOfTie(isEndOfTie);
            newNote.setAttackVelocity(attackVelocity);
            newNote.setDecayVelocity(decayVelocity);
        }
        //	ToDo - SEQUENTIAL
        Element element_chord = note.getFirstChildElement("chord");
        newNote.setType( (element_chord == null) ? Note.FIRST : Note.PARALLEL);

/*	attempt to adjust for rounding errors in non-supported durations        
        if (newNote.getType() == Note.FIRST)
        {	if ((totalMeasurePct + decimalDuration) > 1.)
        	{	decimalDuration = 1. - totalMeasurePct;
        		lastNoteInMeasureDuration = decimalDuration;
        		totalMeasurePct = 1.;
        	}
        	else
        	{	float	minDif = (1.f / (beats * divisions));
        		if (1. - (totalMeasurePct + decimalDuration) < minDif)
        		{	decimalDuration = (1. - totalMeasurePct);
        			totalMeasurePct = 1.;
        		}
        		else
        			totalMeasurePct += decimalDuration;
        	}
	    }
        else
        	if (totalMeasurePct == 1.)	//	just did a last note in measure
        		decimalDuration = lastNoteInMeasureDuration;
 */        		
        newNote.setDecimalDuration(decimalDuration);
    	fireNoteEvent(newNote);
	}

    /**
     * Looks up a string's value in the dictionary.  The dictionary is used to
     * keep memorable names of obscure numbers - for example, the string FLUTE
     * is set to a value of 73, so when users want to play music with a flute,
     * they can say "I[Flute]" instead of "I[73]".
     *
     * <p>
     * The Dictionary feature also lets users define constants so that if the
     * value of something were to change, it only needs to be changed in one
     * place.  For example, MY_FAVORITE_INSTRUMENT could be set to 73, then you
     * can say "I[My_Favorite_Instrument]" when you want to play with that
     * instrument.  If your favorite instrument were ever to change, you only
     * have to make the change in one place, instead of every place where you
     * give the Instrument command.
     * </p>
     *
     * @param bracketedString the string to look up in the dictionary
     * @returns the definition of the string
     * @throws JFugueException if there is a problem looking up bracketedString
     */
    private String dictionaryLookup(String bracketedString) throws JFugueException
    {	int indexOfOpeningBracket = bracketedString.indexOf("[");
        int indexOfClosingBracket = bracketedString.indexOf("]");

        String word = null;
        if ((indexOfOpeningBracket != -1) && (indexOfClosingBracket != -1)) {
            word = bracketedString.substring(indexOfOpeningBracket+1,indexOfClosingBracket);
        }
        else {
            // It appears that "bracketedString" wasn't bracketed.
            word = bracketedString;
        }
        word = word.toUpperCase();

        String definition = (String)dictionaryMap.get(word);
        while ((definition != null) && (dictionaryMap.containsKey(definition.toUpperCase()))) {
            definition = (String)dictionaryMap.get(definition.toUpperCase());
        }

        // If there is no definition for this word, see if the word is actually a number.
        if (null == definition) {
            char ch = 0;
            boolean isNumber = true;
            for (int i=0; i < word.length(); i++) {
                ch = word.charAt(i);
                if ((!Character.isDigit(ch) && (ch != '.'))) {
                    isNumber = false;
                }
            }
            if (isNumber) {
                trace("Dictionary lookup returning the number ",word);
                return word;
            } else {
                //throw new JFugueException(JFugueException.WORD_NOT_DEFINED_EXC,word,bracketedString);
            	definition = "";
            }
        }
        trace("Word ",word," is defined as ",definition);
        return definition;
    }

    /**
     * Look up a byte from the dictionary
     * @param bracketedString the string to look up
     * @returns the byte value of the definition
     * @throws JFugueException if there is a problem getting a byte from the dictionary look-up
     */
    private byte getByteFromDictionary(String bracketedString) throws JFugueException
    {	String definition = dictionaryLookup(bracketedString);
    	Byte newbyte = null;
    	if (definition.length() > 0)
    	{	try {
    			newbyte = new Byte(definition);
        	} catch (NumberFormatException e) {
        		throw new JFugueException(JFugueException.EXPECTED_BYTE,definition,bracketedString);
        	}
    	}
    	else
    		newbyte = new Byte("-1");
        return newbyte.byteValue();
    }

    /**
     * Look up a long from the dictionary
     * @param bracketedString the string to look up
     * @returns the long value of the definition
     * @throws JFugueException if there is a problem getting a long from the dictionary look-up
     */
    private long getLongFromDictionary(String bracketedString) throws JFugueException
    {	String definition = dictionaryLookup(bracketedString);
        Long newlong = null;
        try {
            newlong = new Long(definition);
        } catch (NumberFormatException e) {
            throw new JFugueException(JFugueException.EXPECTED_LONG,definition,bracketedString);
        }
        return newlong.longValue();
    }

    /**
     * Look up an int from the dictionary
     * @param bracketedString the string to look up
     * @returns the int value of the definition
     * @throws JFugueException if there is a problem getting a int from the dictionary look-up
     */
    private int getIntFromDictionary(String bracketedString) throws JFugueException
    {	String definition = dictionaryLookup(bracketedString);
        Integer newint = null;
        try {
            newint = new Integer(definition);
        } catch (NumberFormatException e) {
            throw new JFugueException(JFugueException.EXPECTED_INT,definition,bracketedString);
        }
        return newint.intValue();
    }

    /**
     * Look up a double from the dictionary
     * @param bracketedString the string to look up
     * @returns the double value of the definition
     * @throws JFugueException if there is a problem getting a double from the dictionary look-up
     */
    private double getDoubleFromDictionary(String bracketedString) throws JFugueException
    {	String definition = dictionaryLookup(bracketedString);
        Double newdouble = null;
        try {
            newdouble = new Double(definition);
        } catch (NumberFormatException e) {
            throw new JFugueException(JFugueException.EXPECTED_DOUBLE,definition,bracketedString);
        }
        return newdouble.doubleValue();
    }

       

	/**
     * Parses a voice and fires a voice element
     * @param v is the voice number 1 - 16
     * @throws JFugueException if there is a problem parsing the element
     */
    private void parseVoice(int p, int v) throws JFugueException
    {	//	XML part ID's are 1-based, JFugue voice numbers are 0-based
        int voiceNumber = -1;
        for (int x = 0; x < this.nextVoice; ++x)
        	if (p == voices[x].part && v == voices[x].voice)
        		voiceNumber = x;
        //	if not found, add it to the array
        if (voiceNumber == -1)
        {	voiceNumber = nextVoice;
        	voices[voiceNumber] = new voiceDef();
        	voices[voiceNumber].part = p;
        	voices[voiceNumber].voice = v;
        	++nextVoice;
        }
        if (voiceNumber != this.curVoice)
        	fireVoiceEvent(new Voice((byte)voiceNumber));
        curVoice = voiceNumber;
    }
    
    /**
     * Parses a <code>XMLpart.midi_instruments</code> and fires a voice or
     * instrument events
     * @param instruments is the <code>XMLpart.midiinstruments</code> string to parse
     * Can be a list of ~ separated pairs - midi-channel|InstName where InstName 
     * can be a midi-name, midi-bank, or program Element  
     */
    private void parsePartElementInstruments(int p, String instruments)
    {	if(instruments.indexOf('~') > -1)
    	{	String[] instArray = instruments.split("~");
	    	//	just do the first in the array
	    	String[] midiArray = instArray[0].split("|");
	    	if (midiArray.length > 0 && midiArray[0].length() > 0)
	    		parseVoice(p, Integer.parseInt(midiArray[0])-1);
	    	if (midiArray.length != 1)
	    		parseInstrument(midiArray[1]);
    	}
    	else
    		parseInstrument(instruments);
    }
    
    /**
     * parses <code>inst</code> and fires an Instrument Event
     * @param inst is a String that represents the instrument.  If it is a numeric
     * value, it is interpreted as a midi-bank or program.  If it is an instrument
     * name, it is looked up in the Dictionary as an instrument name.
     */
    private void parseInstrument(String inst)
    {	byte instrumentNumber;
    	try
        {	instrumentNumber = Byte.parseByte(inst);
        }
        catch (NumberFormatException e)
        {	instrumentNumber = getByteFromDictionary(inst);
        }
        trace("Instrument element: inst = ",inst);
        if (instrumentNumber > -1)
        	fireInstrumentEvent(new Instrument(instrumentNumber));
    }

    /**
     * converts beats per minute (BPM) to pulses per minute (PPM) assuming 240 pulses per second
     * In MusicXML, BPM can be fractional, so <code>BPMtoPPM</code> takes a float argument
     * @param bpm
     * @return
     */
    public static int BPMtoPPM(float bpm)
    {	//	convert BPM to PPM assuming 240 pulses per second
		return( new Float((60.f * 240.f) / bpm).intValue() );
    }

    /**
	 ** Used for diagnostic purposes.  main() makes calls to test the Pattern-to-MusicXML
	 ** parser.    
	 ** If you make any changes to this parser, run
	 ** this method ("java org.jfugue.MusicStringParser"), and make sure everything 
	 ** works correctly.
	 ** @param args not used
	 **/
	public static void main(String[] args)
	{	testMusicXmlParser();
	}
	
	private static void testMusicXmlParser()
	{	//File fileXML = new File("C:\\Documents and Settings\\Philip Sobolik\\My Documents\\"
			//					+ "Visual Studio 2005\\WebSites\\NYSSMA3\\"
				//				+ "NYSSMA-Flute-2.xml");
		File fileXML = new File("/users/epsobolik/documents/binchois.xml");
		//File fileXML = new File("/users/epsobolik/documents/SchbAvMaSample.xml");
		try {
	    	FileInputStream fisXML = new FileInputStream(fileXML);
	    	
		    //	test the XML file by displaying the first 1024 characters
			FileChannel fc = fisXML.getChannel();
			ByteBuffer buf = ByteBuffer.allocate((int)fc.size());
			fc.read(buf);
			buf.flip();
	//			while(buf.hasRemaining())
	//				System.out.print((char)buf.get());
	//			fisXML.close();
	//			System.out.print('\n');
	
			//	set up the source MusicXML file (parser)
		    MusicXmlParser MusicXMLIn = new MusicXmlParser();
	//		    MusicXmlParser.setTracing(Parser.TRACING_ON);
		
		    //	set up the target MusicString (renderer)
		    MusicStringRenderer MusicStringOut = new MusicStringRenderer();
		    
		    //	attach the render to the parser
		    MusicXMLIn.addParserListener(MusicStringOut);
		    
		    //	start the parser
		    MusicXMLIn.parse(fileXML);
		    
		    //	display the MusicString
		    Pattern p = MusicStringOut.getPattern();
		    p.insert("T60");
		    
	        System.out.println(p.toString());
			System.out.print('\n');
	        
	//		File fileMS = new File("/users/epsobolik/documents/SchbAvMaSample.jFugue");
	//   	FileOutputStream fosMS = new FileOutputStream(fileMS);
	    	
	//		String ps = p.toString();
	//		for (int c = 0; c < ps.length(); ++c)
	//			fosMS.write(ps.charAt(c));
	//		fosMS.close();

			//	play the pattern
	        Player player = new Player();
	        player.play(p);
	   
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}
	}
}