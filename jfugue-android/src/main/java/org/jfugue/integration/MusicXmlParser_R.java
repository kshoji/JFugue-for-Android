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
import java.io.Reader;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.jfugue.midi.MidiDictionary;
import org.jfugue.parser.Parser;
import org.jfugue.theory.Chord;
import org.jfugue.theory.Note;
import org.staccato.DefaultNoteSettingsManager;

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
 * @author Richard Lavoie (Major rewriting)
 * 
 */
public final class MusicXmlParser_R extends Parser {

	private static class MidiInstrument {
		
		private String id;
		private String channel;
		private String name;
		private String bank;
		private byte program;
		private String unpitched;

		public MidiInstrument(String id, String channel, String name , String bank, byte program, String unpitched) {
			this.id = id;
			this.channel = channel;
			this.name = name;
			this.bank = bank;
			this.program = program;
			this.unpitched = unpitched;
		}
	}
	
	/**
	 * holds a MusicXML part-list entry
	 */
	private static class PartContext { 
		public String id;
		public String name;
		public MidiInstrument[] instruments;
		// TODO : Find a good default, do we actually need a default ?
		private byte currentVolume = 90;
		public byte voice;
		
		public PartContext(String id, String name) {
			this.id = id;
			this.name = name;
			instruments = new MidiInstrument[16];
		}
	};

	private static class VoiceDefinition {
		public VoiceDefinition(int part, int voice) {
			this.part = part;
			this.voice = voice;
		}
		int part;
		int voice;
	}
	
	private static class KeySignature {

		private final byte key;
		private final byte scale;
		
		public KeySignature(byte key, byte scale) {
			this.key = key;
			this.scale = scale;
		}
		
		public byte getKey() {
			return key;
		}
		
		public byte getScale() {
			return scale;
		}
		
		@Override
		public boolean equals(Object o) {
			if (o instanceof KeySignature) {
				KeySignature other = (KeySignature) o;
				return other.key == key && other.scale == scale; 
			}
			return false;
		}
	}
	
	private Builder xomBuilder;
	private Document xomDoc;

	private byte curVelocity = DefaultNoteSettingsManager.getInstance().getDefaultOnVelocity();
	private byte beatsPerMeasure;
	private byte divisionsPerBeat;
	private int currentVoice;
	private byte currentLayer;

	private KeySignature keySignature = new KeySignature((byte) 0, (byte) 0);

	// next available voice # for a new voice
	private byte nextVoice; 
	private VoiceDefinition[] voices;
	private PartContext currentPart; 

	private static final Comparator<String> CHORD_COMPARATOR = new Comparator<String>() {
		@Override
		public int compare(String s1, String s2) {
			int result = compareLength(s1, s2);
			if (result == 0) {
				result = s1.compareTo(s2);
			}
			return result;
		}

		/**
		 * Compare two strings and the bigger of the two is deemed to come
		 * first in order
		 */
		private int compareLength(String s1, String s2) {
			if (s1.length() < s2.length()) {
				return 1;
			} else if (s1.length() > s2.length()) {
				return -1;
			} else {
				return 0;
			}
		}
	}; 
	
	public static Map<String, String> XMLtoJFchordMap;
	static {
		// @formatter:off
		XMLtoJFchordMap = new TreeMap<String, String>(CHORD_COMPARATOR);
		// Major Chords
		XMLtoJFchordMap.put("major", "MAJ");
		XMLtoJFchordMap.put("major-sixth", "MAJ6");
		XMLtoJFchordMap.put("major-seventh", "MAJ7");
		XMLtoJFchordMap.put("major-ninth", "MAJ9");
		XMLtoJFchordMap.put("major-13th", "MAJ13");

		// Minor Chords
		XMLtoJFchordMap.put("minor", "MIN");
		XMLtoJFchordMap.put("minor-sixth", "MIN6");
		XMLtoJFchordMap.put("minor-seventh", "MIN7");
		XMLtoJFchordMap.put("minor-ninth", "MIN9");
		XMLtoJFchordMap.put("minor-11th", "MIN11");
		XMLtoJFchordMap.put("major-minor", "MINMAJ7");

		// Dominant Chords
		XMLtoJFchordMap.put("dominant", "DOM7");
		XMLtoJFchordMap.put("dominant-11th", "DOM7%11");
		XMLtoJFchordMap.put("dominant-ninth", "DOM9");
		XMLtoJFchordMap.put("dominant-13th", "DOM13");

		// Augmented Chords
		XMLtoJFchordMap.put("augmented", "AUG");
		XMLtoJFchordMap.put("augmented-seventh", "AUG7");

		// Diminished Chords
		XMLtoJFchordMap.put("diminished", "DIM");
		XMLtoJFchordMap.put("diminished-seventh", "DIM7");

		// Suspended Chords
		XMLtoJFchordMap.put("suspended-fourth", "SUS4");
		XMLtoJFchordMap.put("suspended-second", "SUS2");

		// @formatter:on
	}

	// CONSTRUCTOR
	public MusicXmlParser_R() throws ParserConfigurationException {
		xomBuilder = new Builder();

		// Set up MusicXML default values
		beatsPerMeasure = 1;
		divisionsPerBeat = 1;
		currentVoice = -1;
		nextVoice = 0;
		voices = new VoiceDefinition[32];
	}

	public void parse(String musicXmlString) throws ValidityException,
			ParsingException, IOException {
		// URI is null when parsing a String as it's coming from somewhere else
		parse(xomBuilder.build(musicXmlString, (String) null)); 
	}

	public void parse(File inputFile) throws ValidityException,
			ParsingException, IOException {
		parse(xomBuilder.build(inputFile));
	}

	public void parse(FileInputStream inputStream) throws ValidityException,
			ParsingException, IOException {
		parse(xomBuilder.build(inputStream));
	}

	public void parse(Reader reader) throws ValidityException,
			ParsingException, IOException {
		parse(xomBuilder.build(reader));
	}

	private void parse(Document document) {
		xomDoc = document;
		parse();
	}
	
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
		fireBeforeParsingStarts();
		Element root = xomDoc.getRootElement();
		if (root.getQualifiedName().equalsIgnoreCase("score-timewise")) {
			parseTimeWise(root);

		} else if (root.getQualifiedName().equalsIgnoreCase("score-partwise")) {
			parsePartWise(root);
		}
		// else Error document could not be parsed.
	}
	
	private void parsePartWise(Element root) {
		Element partlist = root.getFirstChildElement("part-list"); 
		Elements parts = partlist.getChildElements();
		Map<String, PartContext> partHeaders = parsePartList(parts);
		parts = root.getChildElements("part");
		for (int childId = 0; childId < parts.size(); childId++) {
			Element partElement =  parts.get(childId);
			String partId = partElement.getAttribute("id").getValue();
			switchPart(partHeaders, partId, childId);
			Elements measures = partElement.getChildElements("measure");
			for (int measure = 0; measure < measures.size(); measure++) {
				parseMusicData(childId, partId, partHeaders, measures.get(measure));
				fireBarLineParsed(0);
			}
		}
	}

	private void parseTimeWise(Element root) {
		Element partlist = root.getFirstChildElement("part-list"); 
		Elements scoreParts = partlist.getChildElements();
		Map<String, PartContext> partHeaders = parsePartList(scoreParts);
		Elements measures = root.getChildElements("measure");
		for (int measureIndex = 0; measureIndex < measures.size(); measureIndex++) {
			Element measureElement =  measures.get(measureIndex);
			Elements parts = measureElement.getChildElements("part");
			for (int partIndex = 0; partIndex < parts.size(); partIndex++) {
				Element partElement = parts.get(partIndex);
				String partId = partElement.getAttribute("id").getValue();
				switchPart(partHeaders, partId, measureIndex);
				parseMusicData(partIndex, partId, partHeaders, partElement);
				fireBarLineParsed(0);
			}
		}
	}

	private Map<String, PartContext> parsePartList(Elements parts) {
		Map<String, PartContext> partHeaders = new HashMap<String, PartContext>();
		for (int p = 0; p < parts.size(); ++p) {
			PartContext header = parsePartHeader(parts.get(p)); 
			if (header != null) {
				partHeaders.put(header.id, header);
			}
		}
		return partHeaders;

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
	private PartContext parsePartHeader(Element part) {
		// I added the following check to satisfy a MusicXML file that contained
		// a part-group, but I am not convinced that this is the proper way to 
		// handle such an element.
		// - dmkoelle, 2 MAR 2011
		// part-group is a notational convention and can be ignored - JWitzgall

		if (part.getLocalName().equals("part-group")) {
			return null;
		}

		PartContext partHeader = new PartContext(part.getAttribute("id").getValue(), part.getFirstChildElement("part-name").getValue());
		
		// midi-instruments
		Elements midiInsts = part.getChildElements("midi-instrument");
		for (int x = 0; x < midiInsts.size(); ++x) { 
			Element midi_instrument = midiInsts.get(x);
			String instrumentId = midi_instrument.getAttribute("id").getValue();
			String channel = getStringValueOrNull(midi_instrument.getFirstChildElement("midi-channel"));
			String name = getStringValueOrNull(midi_instrument.getFirstChildElement("midi-name"));
			String bank = getStringValueOrNull(midi_instrument.getFirstChildElement("midi-bank"));
			byte program = getByteValueOrDefault(midi_instrument.getFirstChildElement("midi-program"), (byte) 0);
			String unpitched = getStringValueOrNull(midi_instrument.getFirstChildElement("midi-unpitched"));
			partHeader.instruments[x] = new MidiInstrument(instrumentId, channel, name , bank, program, unpitched);
		}
		
		return partHeader;
	}

	/**
	 * Returns the value if the object is not null, null otherwise.
	 * @param element Element to return the String value of
	 * @return String value of the element or not null, null otherwise
	 */
	private String getStringValueOrNull(Element element) {
		return element == null ? null : element.getValue();
	}

	/**
	 * Parses music data in under either a measure for timewise score or under a part for partwise scores.
	 *  
	 * @param partIndex Index of the part
	 * @param partId
	 * @param partHeaders
	 * @param musicDataRoot
	 */
	private void parseMusicData(int partIndex, String partId, Map<String, PartContext> partHeaders, Element musicDataRoot) {
		Element attributes = musicDataRoot.getFirstChildElement("attributes");
		if (attributes != null) { 				
			KeySignature ks = parseKeySignature(attributes);
			if (!keySignature.equals(ks)) {
				keySignature = ks;
				fireKeySignatureParsed(keySignature.getKey(), keySignature.getScale());
			}
			
			//Time-Signature
			this.divisionsPerBeat = getByteValueOrDefault(attributes.getFirstChildElement("divisions"), this.divisionsPerBeat);
			this.beatsPerMeasure = getByteValueOrDefault( getRecursiveFirstChildElement(attributes, "time", "beats"), this.beatsPerMeasure);
		}
		
		Elements childs = musicDataRoot.getChildElements();
		for (int i = 0; i < childs.size(); i++) {
			Element el = childs.get(i);
			if (el.getLocalName().equals("harmony")) {
				parseGuitarChord(el);	
			} else if (el.getLocalName().equals("note")) {
				parseNote(partIndex, el, partId, partHeaders);
			} else if (el.getLocalName().equals("direction")) {
				Element sound = el.getFirstChildElement("sound");
				if (sound != null) {
					String value = sound.getAttributeValue("dynamics");
					if (value != null) {
						currentPart.currentVolume = Byte.parseByte(value);
					}
					value = sound.getAttributeValue("tempo");
					if (value != null) {
						for (MidiInstrument mi : currentPart.instruments) {
							System.out.println(mi);
						}
						fireTempoChanged(Integer.parseInt(value));
					}
				}
			}
		}
	}

	private void switchPart(Map<String, PartContext> partHeaders, String partString, int partId) {
		currentPart = partHeaders.get(partString);
		// assigns a jfugue voice to the part

		if (currentPart.voice >= 0) {
			fireTrackChanged(currentPart.voice);
		} else {
			
			// if there are no midi instruments for the part ie
			// the midi-instruments string length is 0
			// TODO : can that even be possible ?
			if (currentPart.instruments[0] == null) { 
				parseVoice(partId, Integer.parseInt(currentPart.id));
				// then pass the name of the part to the Instrument parser
				parseInstrumentNameAndFireChange(currentPart.name); 
				currentLayer = 0;
				fireLayerChanged(currentLayer);
			} else {
				// TODO : channel ?? really ?? We need to validate this.
				if (currentPart.instruments[0].channel != null) {
					parseVoice(partId, Integer.parseInt(currentPart.instruments[0].channel));
				}
				parseInstrumentAndFireChange(currentPart.instruments[0]); 
				
				currentLayer = 0;
				fireLayerChanged(currentLayer);
			}
		}
	}

	private KeySignature parseKeySignature(Element attributes) {
		// scale 0 = minor, 1 = major
		byte key = keySignature.getKey(), scale = keySignature.getScale();
		Element attr = attributes.getFirstChildElement("key"); 
		if (attr != null) {
			key = getByteValueOrDefault(attr.getFirstChildElement("fifths"), key);
			Element eMode = attr.getFirstChildElement("mode");
			if (eMode != null) {
				String mode = eMode.getValue();
				if (mode.equalsIgnoreCase("major")) {
					scale = 0;
				} else if (mode.equalsIgnoreCase("minor")) {
					scale = 1;
				} else {
					throw new RuntimeException("Error in key signature: " + mode);
				}
			} else {
				scale = 0;
			}
		}
		return new KeySignature(key, scale);
	}

	private Element getRecursiveFirstChildElement(Element element, String... childs) {
		Element el = element;
		for (String c : childs) {
			if (el == null) {
				return null;
			} 
			el = el.getFirstChildElement(c);
		}
		return el;
	}

	private byte getByteValueOrDefault(Element element, byte defaultValue) {
		if (element != null) {
			int value = Integer.parseInt(element.getValue());
			return (byte) value;
			//return (byte) (value - 1);
		}
		return defaultValue;
	}

	private void parseGuitarChord(Element harmony) {
		StringBuilder chordString = new StringBuilder(" ");
		appendToChord(chordString, harmony, "root");
		Element chord_kind = harmony.getFirstChildElement("kind");
		if (chord_kind != null) {
			String chord_kind_str = XMLtoJFchordMap.get(chord_kind.getValue());
			if (chord_kind_str != null) {
				chordString.append(chord_kind_str);
			}
		}
		Element chord_inv = harmony.getFirstChildElement("inversion");
		if (chord_inv != null) {
			Integer inv_value = Integer.parseInt(chord_inv.getValue());
			for (Integer i = 0; i < inv_value; i++) {
				chordString.append("^");
			}
		}
		appendToChord(chordString, harmony, "bass");
		fireChordParsed(new Chord(chordString.toString()));
	}

	private void appendToChord(StringBuilder chordString, Element harmony,
			String base) {
		Element chord_root = harmony.getFirstChildElement(base);
		if (chord_root != null) {
			Element chord_root_step = chord_root.getFirstChildElement(base + "-step");
			if (chord_root_step != null) {
				chordString.append(chord_root_step.getValue());
			}
			Element chord_root_alter = chord_root.getFirstChildElement(base + "-alter");
			if (chord_root_alter != null) {
				if (chord_root_alter.getValue().equals("-1")) {
					chordString.append("b");
				}
				if (chord_root_alter.getValue().equals("+1")) {
					chordString.append("#");
				}
			}
		}

		
	}

	/**
	 * parses MusicXML note Element
	 * 
	 * @param noteElement
	 *            is the note Element to parse
	 */

	private void parseNote(int p, Element noteElement, String partId,
			Map<String, PartContext> partHeaders) {
		Note newNote = new Note();
		newNote.setFirstNote(true);
		
		boolean isRest = false;
		boolean isStartOfTie = false;
		boolean isEndOfTie = false;
		byte noteNumber = 0;
		byte octaveNumber = 0;
		double decimalDuration;

		// skip grace notes
		// TODO : why do we skip grace notes ?
		if (noteElement.getFirstChildElement("grace") != null) {
			return;
		}
		Element voice = noteElement.getFirstChildElement("voice");
		// TODO : !newNote.isHarmonicNote() is always true ...
		if (voice != null && !newNote.isHarmonicNote()) {
			if ((Byte.parseByte(voice.getValue()) - 1) != currentLayer) {
				currentLayer = Byte.parseByte(voice.getValue());
				currentLayer = (byte) (currentLayer - 1);
				fireLayerChanged(currentLayer);
			}
		}
		
		enhanceFromChord(noteElement, newNote);

		Elements noteEls = noteElement.getChildElements();
		// See if note is part of a chord
		for (int i = 0; i < noteEls.size(); i++) {
			Element element = noteEls.get(i); 
			String tagName = element.getQualifiedName(); 
			if (tagName.equals("instrument")) {
				PartContext header = partHeaders.get(partId);
				MidiInstrument[] instruments = header.instruments; 
				for (int y = 0; y < instruments.length; ++y) {
					MidiInstrument ins = instruments[y]; 
					if (ins != null && ins.id.equals(element.getAttributeValue("id"))) {
						parseVoice(p, findInstrument(ins));
						parseInstrumentAndFireChange(ins);
					}
				}
			} else if (tagName.equals("unpitched")) {
				// To Determine if Note is Percussive
				newNote.setPercussionNote(true);
				Element display_note = element.getFirstChildElement("display-step");
				if (display_note != null) {
					noteNumber = getNoteNumber(display_note.getValue().charAt(0));
				}

				Element display_octave = element.getFirstChildElement("display-octave");
				if (display_octave != null) {
					Byte octave_byte = new Byte(display_octave.getValue());
					noteNumber += octave_byte * 12;
				}
			} else if (tagName.equals("pitch")) {
				String sStep = element.getFirstChildElement("step").getValue();
				noteNumber = getNoteNumber(sStep.charAt(0));
				Element alter = element.getFirstChildElement("alter");
				if (alter != null) {
					noteNumber += Integer.parseInt(alter.getValue());
					if (noteNumber > 11) {
						noteNumber = 0;
					} else if (noteNumber < 0) {
						noteNumber = 11;
					}
				}
				
				octaveNumber = getByteValueOrDefault(element.getFirstChildElement("octave"), octaveNumber);

				// Compute the actual note number, based on octave and note
				int intNoteNumber = ((octaveNumber) * 12) + noteNumber;
				if (intNoteNumber > 127) {
					throw new RuntimeException("Note value " + intNoteNumber + " is larger than 127");
				}
				noteNumber = (byte) intNoteNumber;
			} else if (tagName.equals("rest")) {
				isRest = true;
			}

		}

		// duration
		Element element_duration = noteElement.getFirstChildElement("duration");
		double durationValue = Double.parseDouble(element_duration.getValue());
		decimalDuration = durationValue / (divisionsPerBeat * beatsPerMeasure);

		// Tied Note
		Element notations = noteElement.getFirstChildElement("notations");
		if (notations != null) {
			Element tied = notations.getFirstChildElement("tied");
			if (tied != null) {
				String tiedValue = tied.getAttributeValue("type");
				if (tiedValue.equalsIgnoreCase("start")) {
					isStartOfTie = true;
				} else if (tiedValue.equalsIgnoreCase("stop")) {
					isEndOfTie = true;
				}
			}
		}

		byte attackVelocity = currentPart.currentVolume;
		byte decayVelocity = this.curVelocity;

		// Set up the note
		if (isRest) {
			newNote.setRest(true);
			newNote.setDuration(decimalDuration);

			// turn off sound for rest notes
			newNote.setOnVelocity((byte) 0); 
			newNote.setOffVelocity((byte) 0);
		} else {
			newNote.setValue(noteNumber);
			newNote.setDuration(decimalDuration);
			newNote.setStartOfTie(isStartOfTie);
			newNote.setEndOfTie(isEndOfTie);
			newNote.setOnVelocity(attackVelocity);
			newNote.setOffVelocity(decayVelocity);
		}

		fireNoteParsed(newNote);
		// Add Lyric
		Element lyric = noteElement.getFirstChildElement("lyric");
		if (lyric != null) {
			Element lyric_text_element = lyric.getFirstChildElement("text");
			if (lyric_text_element != null) {
				fireLyricParsed(lyric_text_element.getValue());
			}
		}

	}

	/**
	 * Converts a step to it's note value.
	 * @param step 
	 * 		Note step which is one of A,B,C,D,E,F or G
	 * @return 
	 * 		Note number between 0 and 11 associated to it's MIDI equivalent note number
	 */
	private byte getNoteNumber(char step) {
		byte note = 0;
		switch (step) {
		case 'C':
			note = 0;
			break;
		case 'D':
			note = 2;
			break;
		case 'E':
			note = 4;
			break;
		case 'F':
			note = 5;
			break;
		case 'G':
			note = 7;
			break;
		case 'A':
			note = 9;
			break;
		case 'B':
			note = 11;
			break;
		}
		return note;
	}

	private int findInstrument(MidiInstrument instrument) {
		if (instrument.name != null) {
			return MidiDictionary.INSTRUMENT_STRING_TO_BYTE.get(instrument.name);
		}
		return 0;
	}

	private void enhanceFromChord(Element noteElement, Note note) {
		Elements note_elements = noteElement.getChildElements();
		for (int i = 0; i < note_elements.size(); i++) {
			Element element = note_elements.get(i); 
			String tagName = element.getQualifiedName(); 
			if (tagName.equals("chord")) {
				note.setHarmonicNote(true);
				note.setFirstNote(false);
			}
		}
		
	}

	/**
	 * Parses a voice and fires a voice element
	 * 
	 * @param voice
	 *            is the voice number 1 - 16
	 * @throws JFugueException
	 *             if there is a problem parsing the element
	 */
	private void parseVoice(int part, int voice) {
		// This needs to be reworked as it probably should be stored in PartContext.
		if (voice == 10) {
			// TODO : Why does 10 fires a change right away ... ?
			fireTrackChanged((byte) voice);
		} else {
			// scroll through voiceDef objects looking for this particular
			// combination of p v
			// XML part ID's are 1-based, JFugue voice numbers are 0-based
			byte voiceNumber = -1;
			
			for (byte x = 0; x < this.nextVoice; ++x) {
				// class variable voices is an array of voiceDef objects. These
				// objects match a part index to a voice index.
				if (part == voices[x].part && voice == voices[x].voice) { 
					voiceNumber = x;
					break;
				}
			}
			// if Voice not found, add a new voiceDef to the array
			if (voiceNumber == -1) {
				voiceNumber = nextVoice;
				voices[voiceNumber] = new VoiceDefinition(part, voice);
				++nextVoice;
			}
			if (voiceNumber != this.currentVoice) {
				fireTrackChanged(voiceNumber);
			}
			currentVoice = voiceNumber;
		}
	}

	/**
	 * parses <code>inst</code> and fires an Instrument Event
	 * 
	 * @param name
	 *            is a String that represents the instrument. If it is a numeric
	 *            value, it is interpreted as a midi-bank or program. If it is
	 *            an instrument name, it is looked up in the Dictionary as an
	 *            instrument name.
	 */
	private void parseInstrumentNameAndFireChange(String name) {
		byte instrumentNumber;
		try {
			// if the inst string is a number ie the midi number
			instrumentNumber = Byte.parseByte(name);
		} catch (NumberFormatException e) {
			// otherwise map the midi_name to its byte code
			Object value = MidiDictionary.INSTRUMENT_STRING_TO_BYTE.get(name); 
			instrumentNumber = (value == null) ? -1 : (Byte) value;
		}
		if (instrumentNumber > -1) {
			fireInstrumentParsed(instrumentNumber);
		} else 
			throw new RuntimeException();
	}
	
	private void parseInstrumentAndFireChange(MidiInstrument instrument) {
		if (instrument.program >= 0) {
			fireInstrumentParsed(instrument.program);
		} else if (instrument.name != null) {
			parseInstrumentNameAndFireChange(instrument.name);
		} else { 
			throw new RuntimeException("Couldn't determine the instrument. Possibly and unhandled case. Please report with the musicXML data.");
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