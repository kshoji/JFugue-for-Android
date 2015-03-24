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

package org.jfugue.integration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Reader; //Abstract class for reading character streams. 
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger; //the Java Logger

import javax.xml.parsers.ParserConfigurationException;

import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.DocType;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Node;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.jfugue.midi.MidiDefaults;
import org.jfugue.midi.MidiDictionary;
import org.jfugue.parser.Parser;
import org.jfugue.theory.Chord;
import org.jfugue.theory.Note;
import org.staccato.DefaultNoteSettingsManager;

	
//	helper class
class XMLpart extends Object {   //should be called XMLPartHeader - holds a MusicXML part-list entry
	public String ID;
	public String part_name;
	public String score_instruments;
	public String [][] midi_instruments; // id|channel1|name1~id|channel2|name2

	public XMLpart() {
		ID = "";
		part_name = "";
		score_instruments = "";
		midi_instruments = new String[16][6];
	}
};

/**
 * voiceDef MusicString voice can be a combination of part and voice
 */

class voiceDef {
	int part;
	int voice;
}

/**
 * Parses a MusicXML file, and fires events for <code>ParserListener</code>
 * interfaces when tokens are interpreted. The <code>ParserListener</code> does
 * intelligent things with the resulting events, such as create music, draw
 * sheet music, or transform the data.
 * 
 * MusicXmlParser.parse can be called with a file name, File, InputStream, or
 * Reader
 * 
 * @author E.Philip Sobolik
 * @author David Koelle (updates for JFugue 5)
 * 
 */
public final class MusicXmlParser_J extends Parser {
	// private HashMap<String, String> dictionaryMap;
	private Builder xomBuilder;
	private Document xomDoc;
	private String[] volumes = { "pppppp", "ppppp", "pppp", "ppp", "pp", "p",
			"mp", "mf", "f", "ff", "fff", "ffff", "fffff", "ffffff" };
	// note difference between maxVolume and minVolume should be divisible by 13
	private byte minVelocity = 10;
	private byte maxVelocity = 127;
	private byte curVelocity = DefaultNoteSettingsManager.getInstance().getDefaultOnVelocity();
	private byte beats; // beats per measure
	private byte divisions; // divisions per beat
	private int curVoice; // current voice
	private byte curLayer;
        private byte[] curKey = {0, 0};
	private byte nextVoice; // next available voice # for a new voice
	private voiceDef[] voices;
    	private Logger logger = Logger.getLogger("org.jfugue");

	 private double totalMeasurePct;
	// private double lastNoteInMeasureDuration; // adjusted duration of the last note in the measure

	public static Map<String, String> XMLtoJFchordMap;
	static {
        // @formatter:off
	    XMLtoJFchordMap = new TreeMap<String, String>(new Comparator<String>() {
			@Override
			public int compare(String s1, String s2) {
				int result = compareLength(s1, s2);
				if (result == 0) { result = s1.compareTo(s2); }
				return result;
			}
			
			/** Compare two strings and the bigger of the two is deemed to come first in order */
			private int compareLength(String s1, String s2) {
				if (s1.length() < s2.length()) {
					return 1;
				} else if (s1.length() > s2.length()) {
					return -1;
				} else {
					return 0;
				}
			}
	    });
				// Major Chords
		XMLtoJFchordMap.put("major","MAJ");
		XMLtoJFchordMap.put("major-sixth","MAJ6");
		XMLtoJFchordMap.put("major-seventh","MAJ7");
		XMLtoJFchordMap.put("major-ninth","MAJ9");
		XMLtoJFchordMap.put("major-13th","MAJ13");

		// Minor Chords
		XMLtoJFchordMap.put("minor","MIN");
		XMLtoJFchordMap.put("minor-sixth","MIN6");
		XMLtoJFchordMap.put("minor-seventh","MIN7");
		XMLtoJFchordMap.put("minor-ninth","MIN9");
		XMLtoJFchordMap.put("minor-11th","MIN11");
		XMLtoJFchordMap.put("major-minor","MINMAJ7");

		// Dominant Chords
		XMLtoJFchordMap.put("dominant","DOM7");
		XMLtoJFchordMap.put("dominant-11th","DOM7%11");
		XMLtoJFchordMap.put("dominant-ninth","DOM9"); 
		XMLtoJFchordMap.put("dominant-13th","DOM13");

		// Augmented Chords
		XMLtoJFchordMap.put("augmented","AUG");
		XMLtoJFchordMap.put("augmented-seventh","AUG7");
		
		// Diminished Chords
		XMLtoJFchordMap.put("diminished","DIM");
		XMLtoJFchordMap.put("diminished-seventh","DIM7");

		// Suspended Chords
		XMLtoJFchordMap.put("suspended-fourth","SUS4");
		XMLtoJFchordMap.put("suspended-second","SUS2");
		
		// @formatter:on
	}
	
	
	
	
	//CONSTRUCTOR
	public MusicXmlParser_J() throws ParserConfigurationException {
		xomBuilder = new Builder();
		
		// Set up MusicXML default values
		beats = 1;
		divisions = 1;
		curVoice = -1;
		nextVoice = 0;
		voices = new voiceDef[15];
	}
	//Overloadings of parse method
	
	public void parse(String musicXmlString) throws ValidityException, ParsingException, IOException {
		xomDoc = xomBuilder.build(musicXmlString, (String)null);  // URI is null
		parse();
	}

	public void parse(File fileXMLin) throws ValidityException, ParsingException, IOException {
		System.out.println("attempting to build file");
                xomDoc = xomBuilder.build(fileXMLin);
		parse();
	}

	public void parse(FileInputStream fisXMLin) throws ValidityException, ParsingException, IOException {
		xomDoc = xomBuilder.build(fisXMLin);
		parse();
	}

	public void parse(Reader rXMLin) throws ValidityException, ParsingException, IOException {
		xomDoc = xomBuilder.build(rXMLin);
		parse();
	}

	// ///////////////////////////////////////////////////////////////////////
	// Tempo methods
	//

	private int tempo = MidiDefaults.DEFAULT_TEMPO_BEATS_PER_MINUTE;

	protected void setTempo(int tempo) {
		this.tempo = tempo;
	}

	protected int getTempo() {
		return this.tempo;
	}

	//
	// End Tempo methods
	// ///////////////////////////////////////////////////////////////////////

	/**
	 * Parses a MusicXML file and fires events to subscribed
	 * <code>ParserListener</code> interfaces. As the file is parsed, events are
	 * sent to <code>ParserListener</code> interfaces, which are responsible for
	 * doing something interesting with the music data.
	 * 
	 * the input is a XOM Document, which has been built previously
	 * 
	 * @throws Exception
	 *             if there is an error parsing the pattern
	 */

	public void parse() {
		DocType docType = xomDoc.getDocType();
                //System.out.print(xomDoc.getDocType().getValue().toString());
                logger.log(Level.INFO, "DocType = {0}", xomDoc.getDocType().toXML());
                logger.log(Level.INFO, "Score Partwise Check");
		Element root = xomDoc.getRootElement();
                logger.log(Level.INFO, "Root Element = {0}", xomDoc.getDocType().getRootElementName());
		if (docType.getRootElementName().compareToIgnoreCase("score-timewise") == 0){
		//RUN XSLT CONVERT TO SCORE-PARTWISE
		}
							//compareToIgnoreCase returns zero if the strings are equal, neg or positive if one is greater than the other
		if (docType.getRootElementName().compareToIgnoreCase("score-partwise") == 0) { // check if RootElementName eq "score-partwise"
                    Element partlist = root.getFirstChildElement("part-list"); //Then check for the first element named "part-list"
                    Elements parts = partlist.getChildElements(); // gets the part headers in the part-list and puts them into "parts", an ArrayList of Elements (Elements is an XOM defined ArrayList) 
                    Integer part_list_size = parts.size();
                    if (parts.size() == 1) System.out.println("part list size = 1");
                        XMLpart[] partHeaders = new XMLpart[parts.size()]; //declare an array of XMLPart helper classes. These will be entries from the MusicXML doc's part-list
			 //uses class XMLPart which should be called XMLPartHeader - holds select subset of data from a MusicXML part-list entry
			for (int p = 0; p < parts.size(); ++p) {
                                System.out.println("score part " + p);
				partHeaders[p] = new XMLpart();//declares an XMLpart
                                logger.log(Level.INFO, "Parsing a Part Header");
				parsePartHeader(parts.get(p), partHeaders[p]); //this method - see below-  fills in the XMLpart
			}
			parts = root.getChildElements("part"); // now we re-use the parts XML Element array to hold the parts themselves
			for (int p = 0; p < parts.size(); ++p) {
                                logger.log(Level.INFO, "Parsing a Part");
				parsePart(p, parts.get(p), partHeaders); //parses each part - takes as input an integer p, the corresponding part, and the whole array of XMLParts
			}
		}
		 //else Error document could not be parsed. 
	}

	/**
	 * Parses a <code>part</code> element in the <code>part-list</code> section
	 * 
	 * @param part
	 *            is the <code>part</code> element
	 * @param partHeader
	 *            is the array of <code>XMLpart</code> classes that stores the
	 *            <code>part-list</code> elements
	 */
	private void parsePartHeader(Element part, XMLpart partHeader) {     //Parse part header
		// I added the following check to satisfy a MusicXML file that contained a part-group,
		// but I am not convinced that this is the proper way to handle such an
		// element.
		// - dmkoelle, 2 MAR 2011
		// part-group is a notational convention and can be ignored - JWitzgall
		
		if (part.getLocalName().equals("part-group")) {
			return;
		}
		
		// ID									//ID
		Attribute ID = part.getAttribute("id");				//gets the part-list entry's id attribute
		partHeader.ID = ID.getValue();					//puts it into the XMLpart
		
		// part-name							// part-name
		Element partName = part.getFirstChildElement("part-name");
		partHeader.part_name = partName.getValue();
                logger.log(Level.INFO, "ID = {0}",partHeader.ID );
                logger.log(Level.INFO, "part_name = {0} ",partHeader.part_name  );
		
		// score-instruments						//score-instruments
		int x;
		Elements scoreInsts = part.getChildElements("score-instrument");
		for (x = 0; x < scoreInsts.size(); ++x) {
                        logger.log(Level.INFO, "Processing Score-instrument String" );
			partHeader.score_instruments += scoreInsts.get(x).getValue(); //builds a string of instrument names
			if (x < scoreInsts.size() - 1)
				partHeader.score_instruments += "~"; //puts these into the XMLpart score_instruments string. there is a '..' before the last substring
		}
		logger.log(Level.INFO, "Score Instruments String = {0} ", partHeader.score_instruments  );
		// midi-instruments						
		Elements midiInsts = part.getChildElements("midi-instrument");
		for (x = 0; x < midiInsts.size(); ++x) { //For each of the part's midi-instruments
			Element midi_instrument = midiInsts.get(x);
			// Get instrument id
			Attribute midi_instID = midi_instrument.getAttribute("id"); 
			String midi_instID_str = midi_instID.getValue();
			partHeader.midi_instruments[x][0] = midi_instID_str;
                        logger.log(Level.INFO, "Midi Instrument ID = {0}",partHeader.midi_instruments[x][0] );
			//Get Instrument Channel
			Element midi_channel = midi_instrument.getFirstChildElement("midi-channel"); 
			String midiChannel_str = (midi_channel == null) ? "" : midi_channel.getValue();
			if (midiChannel_str.length() > 0)  //ASK DAVID IF THESE CHECKS ARE NECESSARY?
				partHeader.midi_instruments[x][1] = midiChannel_str;
                        logger.log(Level.INFO, "Midi Instrument Channel = {0}",partHeader.midi_instruments[x][1] );
			//Get Midi Name
			Element midi_inst = midi_instrument.getFirstChildElement("midi-name");
			String midiInstName_str = (midi_inst == null) ? "" : midi_inst.getValue();
			if (midiInstName_str.length() > 0)
				partHeader.midi_instruments[x][2] = midiInstName_str;
			//Get Midi Bank
			Element midi_bank = midi_instrument.getFirstChildElement("midi-bank");
			String midi_bank_str = (midi_bank == null) ? "" : midi_bank.getValue();
			if (midi_bank_str.length() > 0)
				partHeader.midi_instruments[x][3] = midi_bank_str;
			//Get Midi Program
			Element midi_program = midi_instrument.getFirstChildElement("midi-program");
			String midi_program_str = (midi_program == null) ? "" : midi_program.getValue();
			partHeader.midi_instruments[x][4] = midi_program_str;
			//Get Midi Unpitched
			Element midi_unpitched = midi_instrument.getFirstChildElement("midi-unpitched");
			String midi_unpitched_str = (midi_unpitched == null) ? "" : midi_unpitched.getValue();
			partHeader.midi_instruments[x][5] = midi_unpitched_str;
		}
	}

	/**
	 * Parses a <code>part</code> and fires all the appropriate note events
	 * 
	 * @param part
	 *            is the entire <code>part</part>
	 * @param partHeaders
	 *            is the array of XMLpart classes that contains instrument info
	 *            for the <code>part</code>s
	 */
	private void parsePart(int p, Element part, XMLpart[] partHeaders) {  //Parse Part

		for (int x = 0; x < partHeaders.length; ++x) {              //Go thru partHeaders and get initial instrument for this part.
			if (part.getAttribute("id").getValue().equals(partHeaders[x].ID)) { 
				 // assigns a jfugue voice to the part - inputs are part index p and part header index x
				logger.log(Level.INFO, "part number: {0}", x);
                                logger.log(Level.INFO, "Midi Instruments in part = {0}", partHeaders[x].midi_instruments.length );
                                
                                if (partHeaders[x].midi_instruments[0] == null) { //if there are no midi instruments for the part ie the midi-instruments string length is 0
					parseVoice(p, x);
					parseInstrument(partHeaders[x].part_name); //then pass the name of the part to the Instrument parser
					curLayer = 0;
					fireLayerChanged(curLayer);
				} else {
					if (partHeaders[x].midi_instruments[0][1] != null) parseVoice(p, Integer.parseInt(partHeaders[x].midi_instruments[0][1]));
					logger.log(Level.INFO, "Passing {0} to parseInstrument", partHeaders[x].midi_instruments[0][4] );
                                        parseInstrument(partHeaders[x].midi_instruments[0][4]); // else parse the midi_instrument midi-name
					curLayer = 0;
					fireLayerChanged(curLayer);
				}
			} //If part id doesn't match the corresponding part headers ID, move on to next part header. 
		}

		Elements measures = part.getChildElements("measure");      //measures - load all measures in the part into Element array "measures"
		for (int m = 0; m < measures.size(); ++m) {
			Element measure = measures.get(m);
			Element attributes = measure.getFirstChildElement("attributes");
			if (attributes != null) { 								//key signature...	
				byte key = 0, scale = 0; 							// scale 0 = minor, 1 = major
				Element attr = attributes.getFirstChildElement("key"); //shouldn't this be key_signature??
				if (attr != null) {
					Element eKey = attr.getFirstChildElement("fifths");
					if (eKey != null) {
						key = Byte.parseByte(eKey.getValue());
					}
					Element eMode = attr.getFirstChildElement("mode");
					if (eMode != null) {
						String mode = eMode.getValue();
						if (mode.compareToIgnoreCase("major") == 0) {
							scale = 0;
						}
						else if (mode.compareToIgnoreCase("minor") == 0) {
							scale = 1;
						}
						else {
							throw new RuntimeException("Error in key signature: "+mode);
						}
					}
				} else {
					scale = 0; // default = major
				}
                                logger.log(Level.INFO, "Key Signature = " + key + scale );
                                if (key != curKey[0] || scale != curKey[1]) {
                                    fireKeySignatureParsed(key, scale);
                                    curKey[0] = key;
                                    curKey[1] = scale;
                                }
				// divisions and beats used to calculate duration when note type not present //Time-Signature
				
				Element element_divisions = attributes.getFirstChildElement("divisions");
				if (element_divisions != null) {
					this.divisions = Byte.valueOf(element_divisions.getValue());
				}
				Element element_time = attributes.getFirstChildElement("time");
				if (element_time != null) {
					Element element_beats = element_time.getFirstChildElement("beats");
					if (element_beats != null) {
						this.beats = Byte.valueOf(element_beats.getValue());
					}
				}
			}       logger.log(Level.INFO, "Time Signature = " + divisions + "/"+ beats );

			// Tempo
			Element direction = measure.getFirstChildElement("direction");
			if (direction != null) {
				Element directionType = direction.getFirstChildElement("direction-type");
				if (directionType != null) {
					Element metronome = directionType.getFirstChildElement("metronome");
					if (metronome != null) {
						Element beatUnit = metronome.getFirstChildElement("beat-unit");
						String sBeatUnit = beatUnit.getValue();
						if (sBeatUnit.compareToIgnoreCase("quarter") != 0) {
							throw new RuntimeException("Beat unit must be quarter: "+sBeatUnit);
						}
						Element bpm = metronome.getFirstChildElement("per-minute");
						if (bpm != null) {
							this.setTempo(BPMtoPPM(Float.parseFloat(bpm.getValue())));
							fireTempoChanged(this.getTempo());
						}
					}
				}
			}

			Elements chords = measure.getChildElements("harmony"); //load all notes into Element array 'notes'
			for (int n = 0; n < chords.size(); ++n) {
				parseChord(p, chords.get(n));
			}

			// Notes
			Elements notes = measure.getChildElements("note"); //load all notes into Element array 'notes'
			for (int n = 0; n < notes.size(); ++n) {
				parseNote(p, notes.get(n), part, partHeaders);
			}


	       	 	//Add a pad rest at the end of the bar if needed
			//	attempt to adjust for rounding errors with un-supported durations
	        	//	if the total length of all the notes doesn't equal a full measure, add a pad rest
	        	//float	minDif = (1.f / (beats * divisions));
	        	//double	padDur = (1. - totalMeasurePct);
	        	//if (padDur > minDif)  	{
			//	Note pad = new Note();
	        	//	pad.setDuration(padDur);
	        	//	pad.setRest(true);
	        	//	fireNoteParsed(pad); //this note event is just a padding rest
	        	//}
	        
			//Bar Line
			fireBarLineParsed(0);
		} // end of measure
	}

	private void parseChord(int p, Element harmony) {

		String chord_string = " ";
		Element chord_root = harmony.getFirstChildElement("root");
		if (chord_root != null) {
			Element chord_root_step = chord_root.getFirstChildElement("root-step");
			if (chord_root_step != null) chord_string = chord_root_step.getValue();
			Element chord_root_alter = chord_root.getFirstChildElement("root-alter");
			if (chord_root_alter != null)  {
				if (chord_root_alter.getValue() == "-1")chord_string = chord_string + "b";
				if (chord_root_alter.getValue() == "+1")chord_string = chord_string + "#";
			}
		}
		Element chord_kind = harmony.getFirstChildElement("kind");
		if (chord_kind != null) {
                    String chord_kind_str = XMLtoJFchordMap.get(chord_kind.getValue());
                    chord_string += (chord_kind_str == null) ? "" : chord_kind_str;
                }
		Element chord_inv = harmony.getFirstChildElement("inversion");
		if (chord_inv != null) {
			Integer inv_value = Integer.parseInt(chord_inv.getValue()); 
			for (Integer i = 0; i < inv_value; i++) {
				 chord_string = chord_string + "^";
			}
                }
		Element chord_bass = harmony.getFirstChildElement("bass");
		if (chord_bass != null) {
			Element chord_bass_step = chord_bass.getFirstChildElement("bass-step");
			if (chord_bass_step != null) chord_string = chord_bass_step.getValue();
			Element chord_bass_alter = chord_bass.getFirstChildElement("bass-alter");
			if (chord_bass_alter != null)  {
				if (chord_bass_alter.getValue() == "-1")chord_string = chord_string + "b";
				if (chord_bass_alter.getValue() == "+1")chord_string = chord_string + "#";
			}
		}
		logger.log(Level.INFO, "Chord = {0}", chord_string );
                Chord newChord = new Chord(chord_string);
		fireChordParsed(newChord);
	}


	/**
	 * parses MusicXML note Element
	 * 
	 * @param note
	 *            is the note Element to parse
	 */
        
	private void parseNote(int p, Element note, Element part,  XMLpart[] partHeaders) {
		Note newNote = new Note();
		boolean isRest = false;
		boolean isStartOfTie = false;
		boolean isEndOfTie = false;
		byte noteNumber = 0;
		byte octaveNumber = 0;
		double decimalDuration;

		// skip grace notes
		if (note.getFirstChildElement("grace") != null) {
			return;
		}
                logger.log(Level.INFO, "Parsing Note");
                Elements note_elements = note.getChildElements();
                //See if note is part of a chord
                for (int i = 0; i < note_elements.size(); i++){
                    //System.out.println (note_elements.get(i).getQualifiedName());
                    if (note_elements.get(i).getQualifiedName().equals("chord")){
                       // System.out.println("setting harmonic to true");
                        newNote.setHarmonicNote(true);
                        //if (newNote.isHarmonicNote()) System.out.println("harmonic is set");
                    } 
                    //else  newNote.setFirstNote(true);
                }
               // Element element_my_chord = note.getFirstChildElement("chord");
                //String test = element_my_chord.getQualifiedName();
               // logger.log(Level.INFO, " go back {0}", test );
                    //if (test.equalsIgnoreCase("chord")){
                        //String test = element_chord.getLocalName();
                        //logger.log(Level.INFO, " go back {0}", test );
                    //    newNote.setHarmonicNote(true); 
                   // }    
                  //  else  newNote.setFirstNote(true);
		Element note_instrument = note.getFirstChildElement("instrument");
		if (note_instrument != null) {
			for (int x = 0; x < partHeaders.length; ++x) { 
				if (part.getAttribute("ID").equals(partHeaders[x].ID)) { 		
					for (int y = 0; y < partHeaders[x].midi_instruments.length; ++y) { 
						if (partHeaders[x].midi_instruments[y][0] == note_instrument.getAttributeValue("ID").toString() ) {
                                                        logger.log(Level.INFO, "Part Headers entry: {0}", partHeaders[x].midi_instruments[y][0] );
                                                        logger.log(Level.INFO, "Instrument ID {0}", note_instrument.getAttributeValue("ID").toString() );
							logger.log(Level.INFO, "Part Headers Name: {0}", partHeaders[x].midi_instruments[y][1] );
                                                        parseVoice(p, Integer.parseInt(partHeaders[x].midi_instruments[y][0]));
							parseInstrument(partHeaders[x].midi_instruments[y][1]);
							
						}
					}
				}
			}
		}
		
                //To Determine if Note is Percussive
                Element unpitched = note.getFirstChildElement("unpitched"); 
                if (unpitched != null) {
                    newNote.setPercussionNote(true);
                    Element display_note = unpitched.getFirstChildElement("display-step");
                    Element display_octave = unpitched.getFirstChildElement("display-octave");
                    if (display_note != null ){
                        String sdisplay_note = display_note.getValue();
                        switch (sdisplay_note.charAt(0)) {
			case 'C': noteNumber = 0; break;
			case 'D': noteNumber = 2; break;
			case 'E': noteNumber = 4; break;
			case 'F': noteNumber = 5; break;
			case 'G': noteNumber = 7; break;
			case 'A': noteNumber = 9; break;
			case 'B': noteNumber = 11; break;
			}
                        
                    }
                    if (display_octave != null) {
                        Byte octave_byte = new Byte(display_octave.getValue());
                        noteNumber += octave_byte*12;
                        System.out.println("percussion Note display pitch " + display_note.getValue()+ display_octave.getValue() + "maps to notenumber value " + noteNumber);
                    } 
                }
                
                
		Element voice = note.getFirstChildElement("voice"); // voice is property element of note
		if (voice != null && !newNote.isHarmonicNote()) {
			//parseVoice(p, curVoice);
                        if (Byte.parseByte(voice.getValue()) - 1 != curLayer){
                            curLayer = Byte.parseByte(voice.getValue());
                            curLayer = (byte) (curLayer - 1);
                            //System.out.println("Layer changed to " + curLayer);
                            fireLayerChanged(curLayer);
                        }
		}

		Element pitch = note.getFirstChildElement("pitch"); //pitch
		if (pitch != null) {
			String sStep = pitch.getFirstChildElement("step").getValue();
			switch (sStep.charAt(0)) {
			case 'C': noteNumber = 0; break;
			case 'D': noteNumber = 2; break;
			case 'E': noteNumber = 4; break;
			case 'F': noteNumber = 5; break;
			case 'G': noteNumber = 7; break;
			case 'A': noteNumber = 9; break;
			case 'B': noteNumber = 11; break;
			}
			Element alter = pitch.getFirstChildElement("alter");
			if (alter != null) {
				String sAlter = alter.getValue();
				if (sAlter != null) {
					noteNumber += Integer.parseInt(sAlter);
					if (noteNumber > 11) {
						noteNumber = 0;
					}
					else if (noteNumber < 0) {
						noteNumber = 11;
					}
				}
			}
			Element octave = pitch.getFirstChildElement("octave");
                        
			if (octave != null) {
				String sOctave = octave.getValue();
                                logger.log(Level.INFO, "Octave Value: {0}", sOctave);
				if (sOctave != null) {
					octaveNumber = Byte.parseByte(sOctave);
				}
			}

			// Compute the actual note number, based on octave and note
			int intNoteNumber = ((octaveNumber) * 12) + noteNumber;
			if (intNoteNumber > 127) {
				throw new RuntimeException("Note value "+intNoteNumber+" is larger than 127");
			}
			noteNumber = (byte) intNoteNumber;
		} else {
			if (!newNote.isPercussionNote())isRest = true;
		}

		// duration
		Element element_duration = note.getFirstChildElement("duration"); 

		decimalDuration = (element_duration == null) ? beats * divisions
					: Double.parseDouble(element_duration.getValue()) / (beats * divisions);
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

		// Tempo is in PPQ (Pulses Per Quarter). Turn that into "PPW", then multiply that by durationNumber for WHQITXN notes
		double PPW = (double) this.getTempo() * 4.0; // 4 quarter notes in a whole note

		//Tied Note
		Element notations = note.getFirstChildElement("notations");
		if (notations != null) {
			Element tied = notations.getFirstChildElement("tied");
			if (tied != null) {
				Attribute tiedType = tied.getAttribute("type"); 
				String sTiedType = tiedType.getValue();
				if (sTiedType.compareToIgnoreCase("start") == 0) {
					isStartOfTie = true;
				}
				else if (sTiedType.compareToIgnoreCase("end") == 0) {
					isEndOfTie = true;
				}
                                logger.log(Level.INFO, "Is start of tie = {0}",	isStartOfTie );
                                logger.log(Level.INFO, "TiedType = {0}", sTiedType );
			}
		// Velocity
		Element dynamics = notations.getFirstChildElement("dynamics");
		if (dynamics != null) {
			Node dynamic = dynamics.getChild(0);
			if (dynamic != null) {
				for (int x = 0; x < this.volumes.length; ++x) {
					if (dynamic.getValue().compareToIgnoreCase(this.volumes[x]) == 0) {
						this.curVelocity = (byte) (((this.maxVelocity - this.minVelocity) / (this.volumes.length - 1)) * x);
					}
				}
			}
		}
		}
		byte attackVelocity = this.curVelocity;
		byte decayVelocity = this.curVelocity;

		// Set up the note
		if (isRest) {
			newNote.setRest(true);
			newNote.setDuration(decimalDuration);
			newNote.setOnVelocity((byte) 0); // turn off sound for rest notes
			newNote.setOffVelocity((byte) 0);
		} else {
			newNote.setValue(noteNumber);
			newNote.setDuration(decimalDuration);
			newNote.setStartOfTie(isStartOfTie);
			newNote.setEndOfTie(isEndOfTie);
			newNote.setOnVelocity(attackVelocity);
			newNote.setOffVelocity(decayVelocity);
		}
		
		
        
                   



		/*
		 * attempt to adjust for rounding errors in non-supported durations if
		 * (newNote.getType() == Note.FIRST) { if ((totalMeasurePct +
		 * decimalDuration) > 1.) { decimalDuration = 1. - totalMeasurePct;
		 * lastNoteInMeasureDuration = decimalDuration; totalMeasurePct = 1.; }
		 * else { float minDif = (1.f / (beats * divisions)); if (1. -
		 * (totalMeasurePct + decimalDuration) < minDif) { decimalDuration = (1.
		 * - totalMeasurePct); totalMeasurePct = 1.; } else totalMeasurePct +=
		 * decimalDuration; } } else if (totalMeasurePct == 1.) // just did a
		 * last note in measure decimalDuration = lastNoteInMeasureDuration;
		 */
		newNote.setDuration(decimalDuration);
                logger.log(Level.INFO, "note duration = {0}", newNote.getDuration());
                if (newNote.isHarmonicNote()) 
                System.out.println("this is harmonic note");
                //logger.log(Level.INFO, "This is a harmonic note");
                System.out.println(newNote.toDebugString());
		fireNoteParsed(newNote);
	//Add Lyric
		Element lyric = note.getFirstChildElement("lyric");
		if (lyric != null) {
			Element lyric_text_element = lyric.getFirstChildElement("text");
			if (lyric_text_element != null) {
				String lyric_text_string = lyric_text_element.getValue();
                                fireLyricParsed(lyric_text_string);
			}
		}
		
	}

	/**
	 * Parses a voice and fires a voice element
	 * 
	 * @param v
	 *            is the voice number 1 - 16
	 * @throws JFugueException
	 *             if there is a problem parsing the element
	 */
	private void parseVoice(int p, int v) { 				
		if (v == 10) fireTrackChanged((byte)v);
                else {
                    //scroll through voiceDef objects looking for this particular combination of p v
                    byte voiceNumber = -1;// XML part ID's are 1-based, JFugue voice numbers are 0-based
                    for (byte x = 0; x < this.nextVoice; ++x) {// class variable nextVoice has been previously initialized to zero
                            if (p == voices[x].part && v == voices[x].voice)  //class variable voices is an array of voiceDef objects. These objects match a part index to a voice index. 
                                    voiceNumber = x;
                    }
                    // if Voice not found, add a new voiceDef to the array
                    if (voiceNumber == -1) {
                            voiceNumber = nextVoice;
                            voices[voiceNumber] = new voiceDef();
                            voices[voiceNumber].part = p;
                            voices[voiceNumber].voice = v;
                            ++nextVoice;
                    }
                    if (voiceNumber != this.curVoice) {
                            fireTrackChanged(voiceNumber);
                    }
                    curVoice = voiceNumber;
                }
	}
        
	/**
	 * Parses a <code>XMLpart.midi_instruments</code> and fires a voice or
	 * instrument events
	 * 
	 * @param instruments
	 *            is the <code>XMLpart.midiinstruments</code> string to parse
	 *            Can be a list of ~ separated pairs - midi-channel|InstName
	 *            where InstName can be a midi-name, midi-bank, or program
	 *            Element
	 */
                
	//private void parsePartElementInstruments(int p, String [] instruments) { // NOT USED 
	
		/*if (instruments.indexOf('~') > -1) { //if the ~ character is present in the string there are more than one midi instrument
			String[] instArray = instruments.split("~"); //split the string on ~ and put the substrings into an array
			// just do the first in the array
			String[] midiArray = instArray[0].split("|"); //split the first midi instrument substring on | 
			if (midiArray.length > 0 && midiArray[1].length() > 0)//if the second section of the midi instrument substring (channel) is not empty
				parseVoice(p, Integer.parseInt(midiArray[1]) - 1); //run parseVoice using the channel -1 since Jfugue starts voices at 0
			if (midiArray.length > 2) //parses the midi instrument name if the name is present
				parseInstrument(midiArray[2]); //this parses a string
		} */
		//else { 						//else there must only be one instrument ie the part name
		//	if (instruments. (instruments.length - 1) == '|') {
		//		instruments = instruments.substring(0, instruments.length() - 1); //truncates instruments to the channel info
		//	}
		//	parseInstrument(instruments[0]); //parses this channel
		//}
	//}

	/**
	 * parses <code>inst</code> and fires an Instrument Event
	 * 
	 * @param inst
	 *            is a String that represents the instrument. If it is a numeric
	 *            value, it is interpreted as a midi-bank or program. If it is
	 *            an instrument name, it is looked up in the Dictionary as an
	 *            instrument name.
	 */
	
private void parseInstrument(String inst) {
		byte instrumentNumber;
		logger.log(Level.INFO, "Starting parseInstrument");
                try {
			instrumentNumber = Byte.parseByte(inst); //if the inst string is a number ie the midi number
		} catch (NumberFormatException e) {
			Object value = MidiDictionary.INSTRUMENT_STRING_TO_BYTE.get(inst); // otherwise map the midi_name to its byte code
			instrumentNumber = (value == null) ? -1 : (Byte)value;
		}
		logger.log(Level.INFO, "Instrument element: inst = {0}", inst);
                logger.log(Level.INFO, "Instrument number:  {0}", instrumentNumber);
		if (instrumentNumber > -1) {
			fireInstrumentParsed(instrumentNumber);
		}
	}

	/**
	 * converts beats per minute (BPM) to pulses per minute (PPM) assuming 240
	 * pulses per second In MusicXML, BPM can be fractional, so
	 * <code>BPMtoPPM</code> takes a float argument
	 * 
	 * @param bpm
	 * @return ppm
	 */
	public static int BPMtoPPM(float bpm) { 
		return (new Float((60.f * 240.f) / bpm).intValue());
	}
}