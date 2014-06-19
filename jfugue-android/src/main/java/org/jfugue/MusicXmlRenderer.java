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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

import nu.xom.Attribute;
import nu.xom.DocType;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Serializer;

/**
 * This class is used to build a MusicXML file) given a 
 * MIDI Sequence, Music String, etc. 
 *
 *@author E.Philip Sobolik
 */
public class MusicXmlRenderer implements ParserListener
{	private Element	root;			//	top-level node of entire MusicXML Document
	private Element elCurMeasure;	//	notes, etc. are added to this measure
	private Element elPartList;		//	may need to add score-parts to this
	private Element elCurScorePart;	//	may need to add instruments to this
	private Element elCurPart;		//	current 'voice' add measures to this
	private static final int MUSICXMLDIVISIONS = 4;	//	4 divisions per quarter note
	private static final double WHOLE = 1024.0;
	private static final double QUARTER = 256.0;
    
    public MusicXmlRenderer()
    {	root = new Element("score-partwise");
    
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
    }	//	MusicXmlRenderer

    /**
     * creates the internal <code>Document</code> with the top-level 
     * <code>Element</code> and then creates the MusicXML file (as a 
     * string) from the internal <code>Document</code>
     * @return the completed MusicXML file as a String
     */
    public String getMusicXMLString()
    {	Document xomDoc = getMusicXMLDoc();
    	return xomDoc.toXML();
    }
    
    /**
     * creates the internal <code>Document</code> with the top-level 
     * <code>Element</code>. 
     * @return the completed MusicXML file as a <code>Document</code>
     */
    public Document getMusicXMLDoc()
    {   finishCurrentVoice();

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
    }	//	GetMusicXMLDoc

    public void doFirstMeasure(boolean bAddDefaults)
    {
    	if (elCurPart == null)
	    	newVoice(new Voice((byte)0));
    	if (elCurMeasure == null)
    	{	elCurMeasure = new Element("measure");
			elCurMeasure.addAttribute(new Attribute("number", Integer.toString(1)));
    		
    		//	assemble attributes element
    		Element elAttributes = new Element("attributes");
    		if (bAddDefaults)
    		{	//	divisions - 4 per beat
	    		Element elDivisions = new Element("divisions");
	    		elDivisions.appendChild(Integer.toString(MUSICXMLDIVISIONS));
	    		elAttributes.appendChild(elDivisions);
	    		//	beats - 1 beat per measure
	    		Element elTime = new Element("time");
	    		Element elBeats = new Element("beats");
	    		elBeats.appendChild(Integer.toString(4));
	    		elTime.appendChild(elBeats);
	    		Element elBeatType = new Element("beat-type");
	    		elBeatType.appendChild(Integer.toString(4));
	    		elTime.appendChild(elBeatType);
	    		elAttributes.appendChild(elTime);
    		}
    		if (bAddDefaults)
    		{	//	Clef - assumed to be treble clef
	    		Element elClef = new Element("clef");
	    		Element elSign = new Element("sign");
	    		elSign.appendChild("G");
	    		Element elLine = new Element("line");
	    		elLine.appendChild("2");
	    		elClef.appendChild(elSign);
	    		elClef.appendChild(elLine);
	    		elAttributes.appendChild(elClef);
    		}
    		//	add the attributes to the measure
    		if (elAttributes.getChildCount() > 0)
    			elCurMeasure.appendChild(elAttributes);
    		
       		//	key signature
    		if (bAddDefaults)
    			doKeySig(new KeySignature((byte)0, (byte)0));	//	C major
    		if (bAddDefaults)
    			doTempo(new Tempo(120));	//	120 BMP default
     	}
    }	//	doFirstMeasure
    
    public void voiceEvent(Voice voice)
    {	
    	String sReqVoice = voice.getMusicString();
    	String sCurPartID = (elCurPart == null)
    							? null
    							: elCurPart.getAttribute("id").getValue();
    	
    	//	if current voice is the same as the requested one, do nothing
    	if (sCurPartID != null)
    		if (sReqVoice.compareTo(sCurPartID) == 0)
    			return;
    	
    	//	check if the requested voice already exists
    	boolean bNewVoiceExists = false;
    	Elements elParts = root.getChildElements("part");
    	Element elExistingNewPart = null;
    	for (int x = 0; x < elParts.size(); ++x)
    	{	Element elP = elParts.get(x);
    		String sPID = elP.getAttribute("id").getValue();
    		
    		if (sPID.compareTo(sReqVoice) == 0)
    		{	bNewVoiceExists = true;
    			elExistingNewPart = elP;
    		}
    	}
    	
    	finishCurrentVoice();
    
    	//	start the new part
    	//	if the new part exists, set the working part to the existing one
    	//	otherwise, start a new part
    	if (bNewVoiceExists)
    		elCurPart = elExistingNewPart;
    	else
    		newVoice(voice);
    	
    	//	start the first/next measure of the working part
    	//	note:  doesn't start a new measure if there
    	//			aren't any notes in the current measure
    	newMeasure();
    }	//	voiceEvent
    
    private void finishCurrentVoice()
    {
    	String sCurPartID = (elCurPart == null)
								? null
								: elCurPart.getAttribute("id").getValue();
    	boolean bCurVoiceExists = false;
    	Elements elParts = root.getChildElements("part");
    	Element elExistingCurPart = null;

    	for (int x = 0; x < elParts.size(); ++x)
    	{	Element elP = elParts.get(x);
    		String sPID = elP.getAttribute("id").getValue();
    		
    		if (sPID.compareTo(sCurPartID) == 0)
    		{	bCurVoiceExists = true;
    			elExistingCurPart = elP;
    		}
    	}
    	
    	//	finish the current measure
    	if (elCurPart != null)
    	{	finishCurrentMeasure();
	    	if (bCurVoiceExists == true)
				root.replaceChild(elExistingCurPart, elCurPart);
	    	else
    			root.appendChild(elCurPart);
    	}
    }	//	finishCurrentVoice

    private void newVoice(Voice voice)
    {//	add a part to the part list
    	elCurScorePart = new Element("score-part");
    	Attribute atPart = new Attribute("id", voice.getMusicString());
    	elCurScorePart.addAttribute(atPart);
    	//	empty part name - Finale ignores it and Sibelius gets it wrong
    	elCurScorePart.appendChild(new Element("part-name"));
    	Element elPL = root.getFirstChildElement("part-list");
    	elPL.appendChild(elCurScorePart);
    	
    	//	start a new part - note that the score-part and the part have the 
    	//	same id attribute
    	elCurPart = new Element("part");
    	Attribute atPart2 = new Attribute(atPart);
    	elCurPart.addAttribute(atPart2);
    	elCurMeasure = null;
    	doFirstMeasure(true);
    }	//	newVoice
    
    public void instrumentEvent(Instrument instrument)
    {
    	Element elInstrName = new Element("instrument-name");
    	elInstrName.appendChild(instrument.getInstrumentName());

    	Element elInstrument = new Element("score-instrument");
    	elInstrument.addAttribute(new Attribute("id", Byte.toString(instrument.getInstrument())));
    	elInstrument.appendChild(elInstrName);
    }
    
    public void tempoEvent(Tempo tempo)
    {	doTempo(tempo);
    }
    
    private void doTempo(Tempo tempo)
    {	Element elDirection = new Element("direction");
		elDirection.addAttribute(new Attribute("placement", "above"));
		Element elDirectionType = new Element("direction-type");
		Element elMetronome = new Element("metronome");
		Element elBeatUnit = new Element("beat-unit");
		//	assume quarter note beat unit
		elBeatUnit.appendChild("quarter");
		Element elPerMinute = new Element("per-minute");
		Integer iBPM = new Float(PPMtoBPM(tempo.getTempo())).intValue();
		elPerMinute.appendChild(iBPM.toString());
		//	assemble all the pieces
		elMetronome.appendChild(elBeatUnit);
		elMetronome.appendChild(elPerMinute);
		elDirectionType.appendChild(elMetronome);
		elDirection.appendChild(elDirectionType);
		//	attach the whole thing to the current measure
		if (elCurMeasure == null)
			doFirstMeasure(true);
		elCurMeasure.appendChild(elDirection);
    }	//	doTempo

    public void layerEvent(Layer layer)
    {
//    	        pattern.add(layer.getMusicString());
    }

    public void timeEvent(Time time)
    {
 //       pattern.add(time.getMusicString());
    }

    public void keySignatureEvent(KeySignature keySig)
    {	doKeySig(keySig);
    }
    
    private void doKeySig(KeySignature keySig)
    {	Element elKey = new Element("key");
    	//	build the key element
    	Element elFifths = new Element("fifths");
    	elFifths.appendChild(Byte.toString(keySig.getKeySig()));
    	elKey.appendChild(elFifths);
    	Element elMode = new Element("mode");
    	elMode.appendChild((keySig.getScale() == 1 ? "minor" : "major"));
    	elKey.appendChild(elMode);
    	//	add the key to the attributes element of the current measure
		if (elCurMeasure == null)
			doFirstMeasure(true);
    	Element elAttributes = elCurMeasure.getFirstChildElement("attributes");
    	boolean bNewAttributes = (elAttributes == null);
    	if (bNewAttributes == true)
    		elAttributes = new Element("attributes");
    	elAttributes.appendChild(elKey);
    	if (bNewAttributes == true)
    		elCurMeasure.appendChild(elAttributes);
    }	//	doKeySig

    public void measureEvent(Measure measure)
    {	// first measure stuff
    	if (elCurMeasure == null)
    		doFirstMeasure(false);
    	else
    	{  	//	add the current measure to the part
    		finishCurrentMeasure();
    		newMeasure();
    	}
    }	//	measureEvent

    private void finishCurrentMeasure()
    {
    	//	if the part exists, replace it with the new one
    	//	otherwise, add the new one
    	{	if (elCurMeasure.getParent() == null)
    			elCurPart.appendChild(elCurMeasure);
    		else
    		{	int sCurMNum = Integer.parseInt(elCurMeasure.getAttributeValue("number"));
    			Elements elMeasures = elCurPart.getChildElements("measure");
    			for (int x = 0; x < elMeasures.size(); ++x)
    			{	Element elM = elMeasures.get(x);
    				int sMNum = Integer.parseInt(elM.getAttributeValue("number"));
    				if (sMNum == sCurMNum)
    					elCurPart.replaceChild(elM, elCurMeasure);
    			}
    		}
    	}
    }	//	finishCurrentMeasure
    
    private void newMeasure()
    {	Integer nextNumber = 1;
    	boolean bNewMeasure = true;
    	// 	if there aren't any notes in the measure, 
		//	continue to use the current measure
    	Elements elMeasures = elCurPart.getChildElements("measure");
    	Element elLastMeasure = null;
		if (elMeasures.size() > 0)
		{	elLastMeasure = elMeasures.get(elMeasures.size()-1);
			//	get the new measure number from the last one
			Attribute elNumber = elLastMeasure.getAttribute("number");
			if (elLastMeasure.getChildElements("note").size() < 1)
				bNewMeasure = false;
			else
				nextNumber = Integer.parseInt(elNumber.getValue()) + 1;
    	}
		else
		{	//	first measure may not have been added yet
			bNewMeasure = (elCurMeasure.getChildElements("note").size() > 0);
		}
    	if (bNewMeasure)
    	{	//	start the new measure
			elCurMeasure = new Element("measure");
			
			//	add the new measure number
			elCurMeasure.addAttribute(new Attribute("number", 
										Integer.toString(nextNumber)));
    	}
    	//	else continue using the same elCurMeasure
    }	//	newMeasure
    
    public void controllerEvent(Controller controller)
    {
//    	        pattern.add(controller.getMusicString());
    }

    public void channelPressureEvent(ChannelPressure channelPressure)
    {
//    	        pattern.add(channelPressure.getMusicString());
    }

    public void polyphonicPressureEvent(PolyphonicPressure polyphonicPressure)
    {
//    	        pattern.add(polyphonicPressure.getMusicString());
    }

    public void pitchBendEvent(PitchBend pitchBend)
    {
//    	        pattern.add(pitchBend.getMusicString());
    }

    public void noteEvent(Note note) 
    {	doNote(note, false);
    }
    
    private void doNote(Note note, boolean bChord)
    {
    	Element elNote = new Element("note");
    	
    	if (bChord)
    		elNote.appendChild(new Element("chord"));
    	
    	//	rest
    	if (note.isRest())
    	{	Element elRest = new Element("rest");
    		elNote.appendChild(elRest);
    	}
    	else
    	{	//	pitch
    		Element elPitch = new Element("pitch");
    		//	step - note letter name without sharp or flat
    		Element elStep = new Element("step");
    		String sPitch = Note.NOTES[note.getValue() % 12];
    		int iAlter = 0;
    		if (sPitch.length() > 1)
    		{	iAlter = sPitch.contains("#") ? 1 : -1;
    			sPitch = sPitch.substring(0,1);
    		}
    		elStep.appendChild(sPitch);
    		elPitch.appendChild(elStep);
    		//	alter - -1 = flat, 1 = sharp
    		if (iAlter != 0)
    		{	Element elAlter = new Element("alter");
    			elAlter.appendChild(Integer.toString(iAlter));
    			elPitch.appendChild(elAlter);
    		}
    		//	octave
    		Element elOctave = new Element("octave");
    		elOctave.appendChild(Integer.toString(note.getValue() / 12));
    		elPitch.appendChild(elOctave);
    		
    		elNote.appendChild(elPitch);
    	}
    	//	duration
    	Element elDuration = new Element("duration");
    	double dDuration = note.getDecimalDuration();
    	int iXMLDuration = (int)
    		((dDuration * WHOLE * MUSICXMLDIVISIONS) / QUARTER); 
    	elDuration.appendChild(Integer.toString(iXMLDuration));
    	elNote.appendChild(elDuration);
    	//	tie start/stop
    	boolean bDoNotation = false;
    	if (note.isStartOfTie())
    	{	Element elTie = new Element("tie");
    		Attribute atType = new Attribute("type", "start");
    		elTie.addAttribute(atType);
    		elNote.appendChild(elTie);
    		bDoNotation = true;
    	}
    	else if (note.isEndOfTie())
    	{	Element elTie = new Element("tie");
			Attribute atType = new Attribute("type", "stop");
			elTie.addAttribute(atType);
			elNote.appendChild(elTie);
    		bDoNotation = true;
		}
    	//	duration type
    	String sDuration;
    	boolean bDotted = false;
    	if (dDuration == 1.0)
    		sDuration = "whole";
        else if (dDuration == 0.75)
        {	sDuration = "half";
        	bDotted = true;
        }
        else if (dDuration == 0.5)
        	sDuration = "half";
        else if (dDuration == 0.375)
        {	sDuration = "quarter";
        	bDotted = true;
        }
        else if (dDuration == 0.25)
        	sDuration = "quarter";
        else if (dDuration == 0.1875)
        {	sDuration = "eighth";
        	bDotted = true;
        }
        else if (dDuration == 0.125)
        	sDuration = "eighth";
        else if (dDuration == 0.09375)
        {	sDuration = "16th";
        	bDotted = true;
        }
        else if (dDuration == 0.0625)
        	sDuration = "16th";
        else if (dDuration == 0.046875)
        {	sDuration = "32nd";
        	bDotted = true;
        }
        else if (dDuration == 0.03125)
        	sDuration = "32nd";
        else if (dDuration == 0.0234375)
        {	sDuration = "64th";
        	bDotted = true;
        }
        else if (dDuration == 0.015625)
        	sDuration = "64th";
        else if (dDuration == 0.01171875)
        {	sDuration = "128th";
        	bDotted = true;
        }
        else if (dDuration == 0.0078125)
        	sDuration = "128th";
        else sDuration = "/" + Double.toString(dDuration);
    	Element elType = new Element("type");
    	elType.appendChild(sDuration);
    	elNote.appendChild(elType);
    	//	dotted
    	if (bDotted)
    	{	Element elDot = new Element("dot");
    		elNote.appendChild(elDot);
    	}
    	//	notations
    	if (bDoNotation)
    	{	Element elNotations = new Element("notations");
    		if (note.isStartOfTie())
    		{	Element elTied = new Element("tied");
    			Attribute atStart = new Attribute("type", "start");
    			elTied.addAttribute(atStart);
    			elNotations.appendChild(elTied);
    		}
    		else if (note.isEndOfTie())
    		{	Element elTied = new Element("tied");
				Attribute atStart = new Attribute("type", "stop");
				elTied.addAttribute(atStart);
				elNotations.appendChild(elTied);
			}
    		elNote.appendChild(elNotations);
    	}
    	if (elCurMeasure == null)
    		doFirstMeasure(true);
    	elCurMeasure.appendChild(elNote);
    }	//	doNote

    public void sequentialNoteEvent(Note note)
    {	
    }

    public void parallelNoteEvent(Note note) 
    {  	doNote(note, true);
    }
    
    /**
     * converts pulses per minute (PPM) to beats per minute (BPM) assuming 240 pulses per second
     * In MusicXML, BPM can be fractional, so <code>PPMtoBPM</code> returns a float
     * @param ppm
     * @return
     */
    public static float PPMtoBPM(int ppm)
    {	//	convert PPM to BPM assuming 240 pulses per second
		return( new Float((60.f * 240.f) / ppm) );
    }


    /**
	 ** Used for diagnostic purposes.  main() makes calls to test the Pattern-to-MusicXML
	 ** renderer.    
	 ** @param args not used
	 **/
	public static void main(String[] args)
	{	
//		FrereJacquesRound();
//		Entertainer();
		metronome(120);
	}
	
	private static void FrereJacquesRound()
	{	File fileXML = new File("C:\\Documents and Settings\\Philip Sobolik\\My Documents\\"
								+ "Visual Studio 2005\\WebSites\\NYSSMA3\\"
								+ "FrereJacquesRound.xml");
	    try {
	    	FileOutputStream fosXML = new FileOutputStream(fileXML, false);
	    	

			//	set up the source MusicXML file (parser)
	    	MusicXmlRenderer MusicXmlOut = new MusicXmlRenderer();
//		    MusicXmlParser.setTracing(Parser.TRACING_ON);
		
		    //	set up the target MusicString (renderer)
		    MusicStringParser MusicStringIn = new MusicStringParser();
		    
		    //	attach the render to the parser
		    MusicStringIn.addParserListener(MusicXmlOut);
		    
		    // "Frere Jacques"
		    Pattern pattern1 = new Pattern("C5q D5q E5q C5q |");

		    // "Dormez-vous?"
		    Pattern pattern2 = new Pattern("E5q F5q G5h |");

		    // "Sonnez les matines"
		    Pattern pattern3 = new Pattern("G5i A5i G5i F5i E5q C5q |");

		    // "Ding ding dong"
		    Pattern pattern4 = new Pattern("C5q G4q C5h |");

		    // Put it all together
		    Pattern song = new Pattern();
		    song.add(pattern1, 2); // Adds 'pattern1' to 'song' twice
		    song.add(pattern2, 2); // Adds 'pattern2' to 'song' twice
		    song.add(pattern3, 2); // Adds 'pattern3' to 'song' twice
		    song.add(pattern4, 2); // Adds 'pattern4' to 'song' twice
//		    MusicStringIn.parse(new Pattern("T160 I[Cello] "+
//		    	      "G3q G3q G3q Eb3q Bb3i G3q Eb3q Bb3i G3h"));
//		    MusicStringIn.parse(song);	

		    // "Frere Jacques" as a round
		    Pattern doubleMeasureRest = new Pattern("Rw | Rw |");

		    // Create the first voice
		    Pattern round1 = new Pattern("V0");
		    round1.add(song);
		    round1.add(doubleMeasureRest, 2);

		    // Create the second voice
		    Pattern round2 = new Pattern("V1");
		    round2.add(doubleMeasureRest);
		    round2.add(song);
		    round2.add(doubleMeasureRest);

		    // Create the third voice
		    Pattern round3 = new Pattern("V2");
		    round3.add(doubleMeasureRest, 2);
		    round3.add(song);

		    // Put the voices together
		    Pattern roundSong = new Pattern();
		    roundSong.add(round1);
		    roundSong.add(round2);
		    roundSong.add(round3);
		    
		    Player player = new Player();
		    player.play(roundSong);
		    System.out.println(roundSong.toString());
		    
		    //	start the parser
		    MusicStringIn.parse(roundSong);	
		    
		    //	write the MusicXML file unformatted
/*		    String p = MusicXmlOut.getMusicXMLString();
			fosXML.write(p.getBytes());

		    //	display the MusicXML file as an unformatted string
	        System.out.println(p);
			System.out.print('\n');
*/
		    //	write the MusicXML file formatted
		    Serializer ser = new Serializer(fosXML, "UTF-8");
		    ser.setIndent(4);
		    ser.write(MusicXmlOut.getMusicXMLDoc());
		    
		    fosXML.flush();
			fosXML.close();
	        
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void Entertainer()
	{	File fileSrc = new File("F:\\WIN\\JFugue\\org\\jfugue\\extras\\"
								+ "entertainer.jfugue");
		File fileXML = new File("C:\\Documents and Settings\\Philip Sobolik\\My Documents\\"
				+ "Visual Studio 2005\\WebSites\\NYSSMA3\\"
				+ "Entertainer.xml");
	    try {
	    	FileOutputStream fosXML = new FileOutputStream(fileXML, false);

			//	set up the source MusicXML file (parser)
	    	MusicXmlRenderer MusicXmlOut = new MusicXmlRenderer();
//		    MusicXmlParser.setTracing(Parser.TRACING_ON);
		
		    //	set up the target MusicString (renderer)
		    MusicStringParser MusicStringIn = new MusicStringParser();
		    
		    //	attach the render to the parser
		    MusicStringIn.addParserListener(MusicXmlOut);
		    
	    	//	read the song from the file
		    BufferedReader brSrc = new BufferedReader(new FileReader(fileSrc));
	    	LineNumberReader lnrSrc = new LineNumberReader(brSrc);
	    	
	    	Pattern song = new Pattern();
	    	for (String s = lnrSrc.readLine(); s != null; s = lnrSrc.readLine())
	    	{	if (s.length() > 0)
	    			if (s.charAt(0) != '#')
	    				song.add(s);
	    	}
	    	lnrSrc.close();
	    	
	    	//	play the song
//		    Player player = new Player();
//		    player.play(song);
		    System.out.println(song.toString());
		    
		    //	start the parser
		    MusicStringIn.parse(song);	
		    
		    //	write the MusicXML file unformatted
/*		    String p = MusicXmlOut.getMusicXMLString();
			fosXML.write(p.getBytes());

		    //	display the MusicXML file as an unformatted string
	        System.out.println(p);
			System.out.print('\n');
*/
		    //	write the MusicXML file formatted
		    Serializer ser = new Serializer(fosXML, "UTF-8");
		    ser.setIndent(4);
		    ser.write(MusicXmlOut.getMusicXMLDoc());
		    
		    fosXML.flush();
			fosXML.close();
	        
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
				e.printStackTrace();
		}
	}

	private static void metronome(int bpm)
	{
		Pattern p = new Pattern("T" + Integer.toString((60 * 240) / bpm));
		p.add("A4q", bpm);	//	should play for 1 minute
		Player pl = new Player();
		pl.play(p);
	}
	
}
