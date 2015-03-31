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

import nu.xom.Attribute;
import nu.xom.DocType;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;

import org.jfugue.parser.ParserListener;
import org.jfugue.theory.Chord;
import org.jfugue.theory.Note;

/**
 * This class is used to build a MusicXML file) given a 
 * MIDI Sequence, Music String, etc. 
 *
 *@author E.Philip Sobolik
 */
public class MusicXmlParserListener implements ParserListener
{	private Element	root;			//	top-level node of entire MusicXML Document
	private Element elCurMeasure;	//	notes, etc. are added to this measure
	private Element elPartList;		//	may need to add score-parts to this
	private Element elCurScorePart;	//	may need to add instruments to this
	private Element elCurPart;		//	current 'voice' add measures to this
	private static final int MUSICXMLDIVISIONS = 4;	//	4 divisions per quarter note
	private static final double WHOLE = 1024.0;
	private static final double QUARTER = 256.0;
    
    public MusicXmlParserListener() {
    	root = new Element("score-partwise");
    
    	Element elID = new Element("identification");
    	Element elCreator = new Element("creator");
    	elCreator.addAttribute(new Attribute("type", "software"));
    	elCreator.appendChild("JFugue MusicXMLRenderer");
    	elID.appendChild(elCreator);
    	root.appendChild(elID);
    
    	//	add an empty score-part list here (before any parts are added)
    	//	score-parts are added to this as they are generated
    	elPartList = new Element("part-list");
    	root.appendChild(elPartList);
    }	

    /**
     * creates the internal <code>Document</code> with the top-level 
     * <code>Element</code> and then creates the MusicXML file (as a 
     * string) from the internal <code>Document</code>
     * @return the completed MusicXML file as a String
     */
    public String getMusicXMLString() {	
    	return getMusicXMLDoc().toXML();
    }
    
    /**
     * creates the internal <code>Document</code> with the top-level 
     * <code>Element</code>. 
     * @return the completed MusicXML file as a <code>Document</code>
     */
    public Document getMusicXMLDoc() {
//       finishCurrentVoice();

    	//	remove empty measures
		Elements elDocParts = root.getChildElements("part");
		for (int xP = 0; xP < elDocParts.size(); ++xP)
		{	Element elDocPart = elDocParts.get(xP);
			Elements elPartMeasures = elDocPart.getChildElements("measure");
			for (int xM = 0; xM < elPartMeasures.size(); ++xM)
				if (elPartMeasures.get(xM).getChildCount() < 1)
					elDocPart.removeChild(xM);
		}
		//	create the Document
		Document xomDoc = new Document(root);
		DocType docType = new DocType("score-partwise",
						"-//Recordare//DTD MusicXML 1.1 Partwise//EN",
						"http://www.musicxml.org/dtds/partwise.dtd");
		xomDoc.insertChild(docType, 0);
		return xomDoc;
    }	

//    public void doFirstMeasure(boolean bAddDefaults)
//    {
//    	if (elCurPart == null)
//	    	newVoice(new Voice((byte)0));
//    	if (elCurMeasure == null)
//    	{	elCurMeasure = new Element("measure");
//			elCurMeasure.addAttribute(new Attribute("number", Integer.toString(1)));
//    		
//    		//	assemble attributes element
//    		Element elAttributes = new Element("attributes");
//    		if (bAddDefaults)
//    		{	//	divisions - 4 per beat
//	    		Element elDivisions = new Element("divisions");
//	    		elDivisions.appendChild(Integer.toString(MUSICXMLDIVISIONS));
//	    		elAttributes.appendChild(elDivisions);
//	    		//	beats - 1 beat per measure
//	    		Element elTime = new Element("time");
//	    		Element elBeats = new Element("beats");
//	    		elBeats.appendChild(Integer.toString(4));
//	    		elTime.appendChild(elBeats);
//	    		Element elBeatType = new Element("beat-type");
//	    		elBeatType.appendChild(Integer.toString(4));
//	    		elTime.appendChild(elBeatType);
//	    		elAttributes.appendChild(elTime);
//    		}
//    		if (bAddDefaults)
//    		{	//	Clef - assumed to be treble clef
//	    		Element elClef = new Element("clef");
//	    		Element elSign = new Element("sign");
//	    		elSign.appendChild("G");
//	    		Element elLine = new Element("line");
//	    		elLine.appendChild("2");
//	    		elClef.appendChild(elSign);
//	    		elClef.appendChild(elLine);
//	    		elAttributes.appendChild(elClef);
//    		}
//    		//	add the attributes to the measure
//    		if (elAttributes.getChildCount() > 0)
//    			elCurMeasure.appendChild(elAttributes);
//    		
//       		//	key signature
//    		if (bAddDefaults)
//    			doKeySig(new KeySignature((byte)0, (byte)0));	//	C major
//    		if (bAddDefaults)
//    			doTempo(new Tempo(120));	//	120 BMP default
//     	}
//    }	//	doFirstMeasure
//    
//    private void finishCurrentVoice()
//    {
//    	String sCurPartID = (elCurPart == null)
//								? null
//								: elCurPart.getAttribute("id").getValue();
//    	boolean bCurVoiceExists = false;
//    	Elements elParts = root.getChildElements("part");
//    	Element elExistingCurPart = null;
//
//    	for (int x = 0; x < elParts.size(); ++x)
//    	{	Element elP = elParts.get(x);
//    		String sPID = elP.getAttribute("id").getValue();
//    		
//    		if (sPID.compareTo(sCurPartID) == 0)
//    		{	bCurVoiceExists = true;
//    			elExistingCurPart = elP;
//    		}
//    	}
//    	
//    	//	finish the current measure
//    	if (elCurPart != null)
//    	{	finishCurrentMeasure();
//	    	if (bCurVoiceExists == true)
//				root.replaceChild(elExistingCurPart, elCurPart);
//	    	else
//    			root.appendChild(elCurPart);
//    	}
//    }	//	finishCurrentVoice
//
//    private void newVoice(Voice voice)
//    {//	add a part to the part list
//    	elCurScorePart = new Element("score-part");
//    	Attribute atPart = new Attribute("id", voice.getMusicString());
//    	elCurScorePart.addAttribute(atPart);
//    	//	empty part name - Finale ignores it and Sibelius gets it wrong
//    	elCurScorePart.appendChild(new Element("part-name"));
//    	Element elPL = root.getFirstChildElement("part-list");
//    	elPL.appendChild(elCurScorePart);
//    	
//    	//	start a new part - note that the score-part and the part have the 
//    	//	same id attribute
//    	elCurPart = new Element("part");
//    	Attribute atPart2 = new Attribute(atPart);
//    	elCurPart.addAttribute(atPart2);
//    	elCurMeasure = null;
//    	doFirstMeasure(true);
//    }	//	newVoice
//    
//    public void instrumentEvent(Instrument instrument)
//    {
//    	Element elInstrName = new Element("instrument-name");
//    	elInstrName.appendChild(instrument.getInstrumentName());
//
//    	Element elInstrument = new Element("score-instrument");
//    	elInstrument.addAttribute(new Attribute("id", Byte.toString(instrument.getInstrument())));
//    	elInstrument.appendChild(elInstrName);
//    }
//    
//    public void tempoEvent(Tempo tempo)
//    {	doTempo(tempo);
//    }
//    
//    private void doTempo(Tempo tempo)
//    {	Element elDirection = new Element("direction");
//		elDirection.addAttribute(new Attribute("placement", "above"));
//		Element elDirectionType = new Element("direction-type");
//		Element elMetronome = new Element("metronome");
//		Element elBeatUnit = new Element("beat-unit");
//		//	assume quarter note beat unit
//		elBeatUnit.appendChild("quarter");
//		Element elPerMinute = new Element("per-minute");
//		Integer iBPM = new Float(PPMtoBPM(tempo.getTempo())).intValue();
//		elPerMinute.appendChild(iBPM.toString());
//		//	assemble all the pieces
//		elMetronome.appendChild(elBeatUnit);
//		elMetronome.appendChild(elPerMinute);
//		elDirectionType.appendChild(elMetronome);
//		elDirection.appendChild(elDirectionType);
//		//	attach the whole thing to the current measure
//		if (elCurMeasure == null)
//			doFirstMeasure(true);
//		elCurMeasure.appendChild(elDirection);
//    }	//	doTempo
//
//    public void layerEvent(Layer layer)
//    {
////    	        pattern.add(layer.getMusicString());
//    }
//
//    public void timeEvent(Time time)
//    {
// //       pattern.add(time.getMusicString());
//    }
//
//    public void systemExclusiveEvent(SystemExclusive sysex)
//    {	
//    	// NOP
//    }
//
//    public void keySignatureEvent(KeySignature keySig)
//    {	doKeySig(keySig);
//    }
//    
//    private void doKeySig(KeySignature keySig)
//    {	Element elKey = new Element("key");
//    	//	build the key element
//    	Element elFifths = new Element("fifths");
//    	elFifths.appendChild(Byte.toString(keySig.getKeySig()));
//    	elKey.appendChild(elFifths);
//    	Element elMode = new Element("mode");
//    	elMode.appendChild((keySig.getScale() == 1 ? "minor" : "major"));
//    	elKey.appendChild(elMode);
//    	//	add the key to the attributes element of the current measure
//		if (elCurMeasure == null)
//			doFirstMeasure(true);
//    	Element elAttributes = elCurMeasure.getFirstChildElement("attributes");
//    	boolean bNewAttributes = (elAttributes == null);
//    	if (bNewAttributes == true)
//    		elAttributes = new Element("attributes");
//    	elAttributes.appendChild(elKey);
//    	if (bNewAttributes == true)
//    		elCurMeasure.appendChild(elAttributes);
//    }	//	doKeySig
//
//    public void measureEvent(Measure measure)
//    {	// first measure stuff
//    	if (elCurMeasure == null)
//    		doFirstMeasure(false);
//    	else
//    	{  	//	add the current measure to the part
//    		finishCurrentMeasure();
//    		newMeasure();
//    	}
//    }	//	measureEvent
//
//    private void finishCurrentMeasure()
//    {
//    	//	if the part exists, replace it with the new one
//    	//	otherwise, add the new one
//    	{	if (elCurMeasure.getParent() == null)
//    			elCurPart.appendChild(elCurMeasure);
//    		else
//    		{	int sCurMNum = Integer.parseInt(elCurMeasure.getAttributeValue("number"));
//    			Elements elMeasures = elCurPart.getChildElements("measure");
//    			for (int x = 0; x < elMeasures.size(); ++x)
//    			{	Element elM = elMeasures.get(x);
//    				int sMNum = Integer.parseInt(elM.getAttributeValue("number"));
//    				if (sMNum == sCurMNum)
//    					elCurPart.replaceChild(elM, elCurMeasure);
//    			}
//    		}
//    	}
//    }	//	finishCurrentMeasure
//    
//    private void newMeasure()
//    {	Integer nextNumber = 1;
//    	boolean bNewMeasure = true;
//    	// 	if there aren't any notes in the measure, 
//		//	continue to use the current measure
//    	Elements elMeasures = elCurPart.getChildElements("measure");
//    	Element elLastMeasure = null;
//		if (elMeasures.size() > 0)
//		{	elLastMeasure = elMeasures.get(elMeasures.size()-1);
//			//	get the new measure number from the last one
//			Attribute elNumber = elLastMeasure.getAttribute("number");
//			if (elLastMeasure.getChildElements("note").size() < 1)
//				bNewMeasure = false;
//			else
//				nextNumber = Integer.parseInt(elNumber.getValue()) + 1;
//    	}
//		else
//		{	//	first measure may not have been added yet
//			bNewMeasure = (elCurMeasure.getChildElements("note").size() > 0);
//		}
//    	if (bNewMeasure)
//    	{	//	start the new measure
//			elCurMeasure = new Element("measure");
//			
//			//	add the new measure number
//			elCurMeasure.addAttribute(new Attribute("number", 
//										Integer.toString(nextNumber)));
//    	}
//    	//	else continue using the same elCurMeasure
//    }	//	newMeasure
//    
//    public void controllerEvent(Controller controller)
//    {
////    	        pattern.add(controller.getMusicString());
//    }
//
//    public void channelPressureEvent(ChannelPressure channelPressure)
//    {
////    	        pattern.add(channelPressure.getMusicString());
//    }
//
//    public void polyphonicPressureEvent(PolyphonicPressure polyphonicPressure)
//    {
////    	        pattern.add(polyphonicPressure.getMusicString());
//    }
//
//    public void pitchBendEvent(PitchBend pitchBend)
//    {
////    	        pattern.add(pitchBend.getMusicString());
//    }
//
//    public void noteEvent(Note note) 
//    {	doNote(note, false);
//    }
//    
//    private void doNote(Note note, boolean bChord)
//    {
//    	Element elNote = new Element("note");
//    	
//    	if (bChord)
//    		elNote.appendChild(new Element("chord"));
//    	
//    	//	rest
//    	if (note.isRest())
//    	{	Element elRest = new Element("rest");
//    		elNote.appendChild(elRest);
//    	}
//    	else
//    	{	//	pitch
//    		Element elPitch = new Element("pitch");
//    		//	step - note letter name without sharp or flat
//    		Element elStep = new Element("step");
//    		String sPitch = Note.NOTES[note.getValue() % 12];
//    		int iAlter = 0;
//    		if (sPitch.length() > 1)
//    		{	iAlter = sPitch.contains("#") ? 1 : -1;
//    			sPitch = sPitch.substring(0,1);
//    		}
//    		elStep.appendChild(sPitch);
//    		elPitch.appendChild(elStep);
//    		//	alter - -1 = flat, 1 = sharp
//    		if (iAlter != 0)
//    		{	Element elAlter = new Element("alter");
//    			elAlter.appendChild(Integer.toString(iAlter));
//    			elPitch.appendChild(elAlter);
//    		}
//    		//	octave
//    		Element elOctave = new Element("octave");
//    		elOctave.appendChild(Integer.toString(note.getValue() / 12));
//    		elPitch.appendChild(elOctave);
//    		
//    		elNote.appendChild(elPitch);
//    	}
//    	//	duration
//    	Element elDuration = new Element("duration");
//    	double dDuration = note.getDecimalDuration();
//    	int iXMLDuration = (int)
//    		((dDuration * WHOLE * MUSICXMLDIVISIONS) / QUARTER); 
//    	elDuration.appendChild(Integer.toString(iXMLDuration));
//    	elNote.appendChild(elDuration);
//    	//	tie start/stop
//    	boolean bDoNotation = false;
//    	if (note.isStartOfTie())
//    	{	Element elTie = new Element("tie");
//    		Attribute atType = new Attribute("type", "start");
//    		elTie.addAttribute(atType);
//    		elNote.appendChild(elTie);
//    		bDoNotation = true;
//    	}
//    	else if (note.isEndOfTie())
//    	{	Element elTie = new Element("tie");
//			Attribute atType = new Attribute("type", "stop");
//			elTie.addAttribute(atType);
//			elNote.appendChild(elTie);
//    		bDoNotation = true;
//		}
//    	//	duration type
//    	String sDuration;
//    	boolean bDotted = false;
//    	if (dDuration == 1.0)
//    		sDuration = "whole";
//        else if (dDuration == 0.75)
//        {	sDuration = "half";
//        	bDotted = true;
//        }
//        else if (dDuration == 0.5)
//        	sDuration = "half";
//        else if (dDuration == 0.375)
//        {	sDuration = "quarter";
//        	bDotted = true;
//        }
//        else if (dDuration == 0.25)
//        	sDuration = "quarter";
//        else if (dDuration == 0.1875)
//        {	sDuration = "eighth";
//        	bDotted = true;
//        }
//        else if (dDuration == 0.125)
//        	sDuration = "eighth";
//        else if (dDuration == 0.09375)
//        {	sDuration = "16th";
//        	bDotted = true;
//        }
//        else if (dDuration == 0.0625)
//        	sDuration = "16th";
//        else if (dDuration == 0.046875)
//        {	sDuration = "32nd";
//        	bDotted = true;
//        }
//        else if (dDuration == 0.03125)
//        	sDuration = "32nd";
//        else if (dDuration == 0.0234375)
//        {	sDuration = "64th";
//        	bDotted = true;
//        }
//        else if (dDuration == 0.015625)
//        	sDuration = "64th";
//        else if (dDuration == 0.01171875)
//        {	sDuration = "128th";
//        	bDotted = true;
//        }
//        else if (dDuration == 0.0078125)
//        	sDuration = "128th";
//        else sDuration = "/" + Double.toString(dDuration);
//    	Element elType = new Element("type");
//    	elType.appendChild(sDuration);
//    	elNote.appendChild(elType);
//    	//	dotted
//    	if (bDotted)
//    	{	Element elDot = new Element("dot");
//    		elNote.appendChild(elDot);
//    	}
//    	//	notations
//    	if (bDoNotation)
//    	{	Element elNotations = new Element("notations");
//    		if (note.isStartOfTie())
//    		{	Element elTied = new Element("tied");
//    			Attribute atStart = new Attribute("type", "start");
//    			elTied.addAttribute(atStart);
//    			elNotations.appendChild(elTied);
//    		}
//    		else if (note.isEndOfTie())
//    		{	Element elTied = new Element("tied");
//				Attribute atStart = new Attribute("type", "stop");
//				elTied.addAttribute(atStart);
//				elNotations.appendChild(elTied);
//			}
//    		elNote.appendChild(elNotations);
//    	}
//    	if (elCurMeasure == null)
//    		doFirstMeasure(true);
//    	elCurMeasure.appendChild(elNote);
//    }	//	doNote
//
//    public void sequentialNoteEvent(Note note)
//    {	
//    }
//
//    public void parallelNoteEvent(Note note) 
//    {  	doNote(note, true);
//    }
//    
//    /**
//     * converts pulses per minute (PPM) to beats per minute (BPM) assuming 240 pulses per second
//     * In MusicXML, BPM can be fractional, so <code>PPMtoBPM</code> returns a float
//     * @param ppm
//     * @return bpm
//     */
//    public static float PPMtoBPM(int ppm)
//    {	//	convert PPM to BPM assuming 240 pulses per second
//		return( new Float((60.f * 240.f) / ppm) );
//    }

    
    
    
    
    
    
    
    
	@Override
	public void beforeParsingStarts() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterParsingFinished() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTrackChanged(byte track) {
//    	String sReqVoice = voice.getMusicString();
//    	String sCurPartID = (elCurPart == null)
//    							? null
//    							: elCurPart.getAttribute("id").getValue();
//    	
//    	//	if current voice is the same as the requested one, do nothing
//    	if (sCurPartID != null)
//    		if (sReqVoice.compareTo(sCurPartID) == 0)
//    			return;
//    	
//    	//	check if the requested voice already exists
//    	boolean bNewVoiceExists = false;
//    	Elements elParts = root.getChildElements("part");
//    	Element elExistingNewPart = null;
//    	for (int x = 0; x < elParts.size(); ++x)
//    	{	Element elP = elParts.get(x);
//    		String sPID = elP.getAttribute("id").getValue();
//    		
//    		if (sPID.compareTo(sReqVoice) == 0)
//    		{	bNewVoiceExists = true;
//    			elExistingNewPart = elP;
//    		}
//    	}
//    	
//    	finishCurrentVoice();
//    
//    	//	start the new part
//    	//	if the new part exists, set the working part to the existing one
//    	//	otherwise, start a new part
//    	if (bNewVoiceExists)
//    		elCurPart = elExistingNewPart;
//    	else
//    		newVoice(voice);
//    	
//    	//	start the first/next measure of the working part
//    	//	note:  doesn't start a new measure if there
//    	//			aren't any notes in the current measure
//    	newMeasure();
	}

	@Override
	public void onLayerChanged(byte layer) {
	}

	@Override
	public void onInstrumentParsed(byte instrument) {
	}

	@Override
	public void onTempoChanged(int tempoBPM) {
	}

	@Override
	public void onKeySignatureParsed(byte key, byte scale) {
	}

	@Override
	public void onTimeSignatureParsed(byte numerator, byte denominator) {
	}

	@Override
	public void onBarLineParsed(long id) {
	}

	@Override
	public void onTrackBeatTimeBookmarked(String timeBookmarkId) {
	}

	@Override
	public void onTrackBeatTimeBookmarkRequested(String timeBookmarkId) {
	}

	@Override
	public void onTrackBeatTimeRequested(double time) {
	}

	@Override
	public void onPitchWheelParsed(byte lsb, byte msb) {
	}

	@Override
	public void onChannelPressureParsed(byte pressure) {
	}

	@Override
	public void onPolyphonicPressureParsed(byte key, byte pressure) {
	}

	@Override
	public void onSystemExclusiveParsed(byte... bytes) {
	}

	@Override
	public void onControllerEventParsed(byte controller, byte value) {
	}

	@Override
	public void onLyricParsed(String lyric) {
	}

	@Override
	public void onMarkerParsed(String marker) {
	}

	@Override
	public void onFunctionParsed(String id, Object message) {
	}

	@Override
	public void onNoteParsed(Note note) {
	}

	@Override
	public void onChordParsed(Chord chord) {
	}
}
