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

import org.jfugue.midi.MidiFileManager;
import org.jfugue.parser.ParserListener;
import org.jfugue.pattern.Pattern;
import org.jfugue.theory.Key;
import org.jfugue.theory.Note;
import org.staccato.StaccatoParser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import jp.kshoji.javax.sound.midi.InvalidMidiDataException;


/** Provides <code>Pattern</code> and MIDI analysis of the following elements:
  *  General descriptors, Pitch descriptors, Duration descriptors, Silence descriptors (rests),
  *  Interval descriptors (half-steps), Inter Onset Interval (IOI), Harmonic Descriptors (Non-diatonics), 
  *  Rhythm Descriptors (Syncopations), Normality Descriptors.
  *  
  *  @Author Grant Mehrer (gtmehrer@gmail.com)
  *  @Date September 28, 2013
  */
public class GetPatternStats {
    private List<Number> pitches = new ArrayList<Number>();
    private List<Number> intervals = new ArrayList<Number>();
    private List<Number> degreeNonDiatonic = new ArrayList<Number>();
    private List<Number> interOI = new ArrayList<Number>();
    private List<Number> durations = new ArrayList<Number>();
    private List<Number> restDurations = new ArrayList<Number>();
    private List<Byte> attacks = new ArrayList<Byte>();
    private List<Byte> decays = new ArrayList<Byte>();
    private List<TimeEvent> musicEvents= new ArrayList<TimeEvent>();
    private int rhythm, measures = 0;
    private double tickPos = 0;
    private Key key = new Key("Cmaj");
    
    
    /** Parses JFugue <code>Pattern<code> to calculate statistics
      * for all descriptors.
      * 
      * @param pattern The JFuge Pattern to be parsed.
      * @param clear True to clear previous data, false to add to previous data
      */
    public void parsePattern(Pattern pattern, Boolean clear){ 
        tickPos = 0;
        if (clear){
            this.clearLists();
        }
        StaccatoParser sp = new StaccatoParser();
        Listener l = new Listener();
        sp.addParserListener(l);
        sp.parse(pattern.toString());   
        processEvents();
    }
    
   /** Parses MIDI file to calculate statistics for all descriptors
     * 
     * @param midiFile The MIDI file to be parsed.
     * @param clear True to clear previous data, false to add to previous data
     * @throws java.io.IOException
     * @throws InvalidMidiDataException
     */
    public void parsePattern(File midiFile, Boolean clear) throws IOException, InvalidMidiDataException {
       Pattern midiPattern = new Pattern();           
       String ext = midiFile.getName().substring(midiFile.getName().lastIndexOf(".")+1, midiFile.getName().length());
       if (ext.equalsIgnoreCase("mid")){  
           Pattern fromMidi = MidiFileManager.loadPatternFromMidi(midiFile);
           midiPattern.add(fromMidi.toString());
       }  
       this.parsePattern(midiPattern, clear);
    }
    
    private void clearLists(){
        pitches.clear();
        intervals.clear();
        degreeNonDiatonic.clear();
        interOI.clear();
        durations.clear();
        restDurations.clear();
        attacks.clear();
        decays.clear();
        musicEvents.clear();
        measures = 0;
        rhythm = 0;
    }
    
    /**Parses two patterns to find average difference of all stats
     * 
     * @param Pattern Pattern 1 to be parsed
     * @param Pattern Pattern 2 to be parsed
     * @return        The average difference between patterns
     */
    public double comparePatterns(Pattern p1, Pattern p2){
        List<Number> difference = new ArrayList<Number>();
        
        GetPatternStats pa1 = new GetPatternStats();
        GetPatternStats pa2 = new GetPatternStats();
        pa1.parsePattern(p1, true);
        pa2.parsePattern(p2, true);
        
        difference.add(pa1.getGeneralStats()[0] - pa2.getGeneralStats()[0]);
        difference.add(pa1.getGeneralStats()[1] - pa2.getGeneralStats()[1]);
        difference.add(pa1.getGeneralStats()[2] - pa2.getGeneralStats()[2]);
        
        findDifference(pa1.pitches, pa2.pitches, this.pitches);
        difference.add(pa1.getPitchStats().average - pa2.getPitchStats().average);
        //difference.add(pa1.getPitchStats().range - pa2.getPitchStats().range);
        difference.add(pa1.getPitchStats().sd - pa2.getPitchStats().sd);

        findDifference(pa1.durations, pa2.durations, this.durations);
        difference.add(pa1.getDurationStats().average - pa2.getDurationStats().average);
        //difference.add(pa1.getDurationStats().range - pa2.getDurationStats().range);
        difference.add(pa1.getDurationStats().sd - pa2.getDurationStats().sd);
        
        findDifference(pa1.restDurations, pa2.restDurations, this.restDurations);
        difference.add(pa1.getRestStats().average - pa2.getRestStats().average);
        //difference.add(pa1.getRestStats().range - pa2.getRestStats().range);
        difference.add(pa1.getRestStats().sd - pa2.getRestStats().sd);
        
        findDifference(pa1.intervals, pa2.intervals, this.intervals);
        difference.add(pa1.getIntervalStats().average - pa2.getIntervalStats().average);
        //difference.add(pa1.getIntervalStats().range - pa2.getIntervalStats().range);
        difference.add(pa1.getIntervalStats().sd - pa2.getIntervalStats().sd);
        
        findDifference(pa1.interOI, pa2.interOI, this.interOI);
        difference.add(pa1.getIOIStats().average - pa2.getIOIStats().average);
        //difference.add(pa1.getIOIStats().range - pa2.getIOIStats().range);
        difference.add(pa1.getIOIStats().sd - pa2.getIOIStats().sd);
        
        findDifference(pa1.degreeNonDiatonic, pa2.degreeNonDiatonic, this.degreeNonDiatonic);
        difference.add(pa1.getHarmonicStats().average - pa2.getHarmonicStats().average);
        //difference.add(pa1.getHarmonicStats().range - pa2.getHarmonicStats().range);
        difference.add(pa1.getHarmonicStats().sd - pa2.getHarmonicStats().sd);
        
        rhythm = Math.abs(pa1.getRhythmStats() - pa2.getRhythmStats());
        difference.add(pa1.getRhythmStats() - pa2.getRhythmStats()); 
               
        return Math.abs(computeAverage(difference));
    }
    
    private void findDifference(List<Number> p1, List<Number> p2, List<Number> thisList){
        if (p1.isEmpty()){
            if (!p2.isEmpty()){
                for(Number num: p2){  
                thisList.add(num.doubleValue());
        }}}
        else if (p2.isEmpty()){
            if (!p1.isEmpty()){
                for(Number num: p1){  
                thisList.add(num.doubleValue());
        }}}
        else {
            int count = 0;
            for(Number num: p2){  
                thisList.add(num.doubleValue() - p1.get(count).doubleValue());
                ++count;
                if (count > (p1.size() -1)){break;}
        }}
    }
    
    /** Gets general statistics (Number of notes, number of rests, number of measures)
     * 
     * @return  Array index 0: N of Notes; index 1: N of rests; index 2: N of measures
     */
    public int[] getGeneralStats(){
        return new int[]{durations.size(),restDurations.size(), measures};
    }
    
    /** Gets <code>Stats</code> object containing pitch N, Average (mean - min), SD, and Range.
     * 
     * @return <code>Stats</code> object for pitch
     */
    public Stats getPitchStats(){
        return new Stats(pitches);
    }
    
    /** Gets <code>Stats</code> object containing note duration N, Average (mean - min), SD, and Range.
     * 
     * @return <code>Stats</code> object for note duration
     */
    public Stats getDurationStats(){
        return new Stats(durations);
    }
    
    /** Gets <code>Stats</code> object containing rest duration N, Average (mean - min), SD, and Range.
     *  Only silences greater than  a 16th note are evaluated.
     * 
     * @return <code>Stats</code> object for rest duration
     */
    public Stats getRestStats(){
        return new Stats(restDurations);
    }
    
    /** Gets <code>Stats</code> object containing pitch interval N, Average (mean - min), SD, and Range.
     *  Intervals are in half-steps.
     * 
     * @return <code>Stats</code> object for pitch interval
     */
    public Stats getIntervalStats(){
        return new Stats(intervals);
    }
    
    /** Gets <code>Stats</code> object containing inter-onset-interval(IOI) N, Average (mean - min), SD, and Range.
     *  Inter-onset-intervals are the number of MIDI pulses between the onset of non-rest notes.
     * 
     * @return <code>Stats</code> object for IOI
     */
    public Stats getIOIStats(){
        return new Stats(interOI);
    }
    
    /** Gets number of syncopations.
     *  Syncopations are calculated as any note that begins between beats and extends beyond the next beat.
     *  ie. C5H C5W C5H - the middle note is syncopated. 
     * 
     * @return <code>Stats</code> object for rhythm
     */
    public int getRhythmStats(){
        return rhythm;
    }
    
     /** Gets <code>Stats</code> object containing harmonics N, Average (mean - min), SD, and Range.
       *  Non-diatonic notes based off of the parsed key signature or default of Cmaj.
       *  Average is average of 0-4, as a degree of non-diatonic notes. ie. 0: ♭II, 1: ♭III (♮III for minor key), 2: ♭V, 3: ♭VI, 4: ♭VII. 
       *  SD is also computed using degree of non-diatonic notes.
       * 
       * @return  <code>Stats</code> object for harmonics
       */ 
    public Stats getHarmonicStats(){
        return new Stats(degreeNonDiatonic);
    }
   
    /** Sorts time events for chronological processing
     * 
     */
    private void sortTimeEvents(){
      Collections.sort(musicEvents, new Comparator<TimeEvent>() {
          @Override
          public int compare(TimeEvent eg1, TimeEvent eg2) {
              if (eg1.time < eg2.time){
                  return -1;
              }
              if (eg1.time > eg2.time) {
                return 1;
              }
              else {
                return 0;
              }}});
   }
    
    /** Checks pitch value of note against Key Signature to determine whether value is "non-diatonic"
     * 
     * @param note The note to be checked
     * @return True if note is non-diatonic
     */
    private boolean checkHarmonics(Note note){ 
        
        List keyScale;
        List<List> majors = new ArrayList<List>();
        List<List> minors = new ArrayList<List>();
        majors.add(Arrays.asList(0,2,4,5,7,9,11));   //C D E F G A B no flats or sharps
        majors.add(Arrays.asList(1,3,5,5,8,10,0));   //Db Eb F Gb Ab Bb C 5 flats //C# D# E# F# G# A# B# 7 sharps     
        majors.add(Arrays.asList(2,4,6,7,9,11,1 ));  //D E F# G A B C# 2 sharps
        majors.add(Arrays.asList(3,4,7,8,10,0,2));   //Eb F G Ab Bb C	D 3 flats
        majors.add(Arrays.asList(4,6,8,9,11,1,3));   //E F# G# A B C# D# 4 sharps
        majors.add(Arrays.asList(5,7,9,10,0,1,4));   //F G A Bb C D E 1 flat
        majors.add(Arrays.asList(6,8,10,11,1,3,5));  //Gb Ab Bb Cb Db Eb F 6 flats //F# G# A# B C# D# E# 6 sharps      
        majors.add(Arrays.asList(7,9,11,0,2,4,6));   //G A B C D E F# 1 sharp
        majors.add(Arrays.asList(8,10,0,1,3,5,7));   //Ab Bb C Db Eb F G 4 flats
        majors.add(Arrays.asList(9,11,1,2,4,6,8));   //A B C# D E F# G# 3 sharps
        majors.add(Arrays.asList(10,0,2,3,5,7,9));   //Bb C D Eb F G A 2 flats 
        majors.add(Arrays.asList(11,1,3,4,6,8,10));  //Cb Db Eb Fb Gb Ab Bb 7 flats //B C# D# E F# G# A# 5 sharps        
        
        minors.add(Arrays.asList(3,4,7,8,10,0,2));   //C D Eb F G Ab Bb 3 flats
        minors.add(Arrays.asList(4,6,8,9,11,1,3));   // C# D# E F# G# A B 4 sharps
        minors.add(Arrays.asList(5,7,9,10,0,1,4));   // D E F G A Bb C 1 flat
        minors.add(Arrays.asList(6,8,10,11,1,3,5));  //Eb F Gb Ab Bb Cb Db 6 flats /D# E# F# G# A# B C# 6 sharps
        minors.add(Arrays.asList(7,9,11,0,2,4,6));   // E F# G A B C D 1 sharp
        minors.add(Arrays.asList(8,10,0,1,3,5,7));   //F G Ab Bb C Db Eb 4 flats
        minors.add(Arrays.asList(9,11,1,2,4,6,8));   //F# G# A	B C# D E 3 sharps
        minors.add(Arrays.asList(10,0,2,3,5,7,9));   // G A Bb C D Eb F	2 flats
        minors.add(Arrays.asList(11,1,3,4,6,8,10));  //G# A# B C# D# E F# 5 sharps /Ab Bb Cb Db Eb Fb Gb 7 flats 
        minors.add(Arrays.asList(0,2,4,5,7,9,11));   // A B C D E F G no flats or sharps 
        minors.add(Arrays.asList(1,3,5,5,8,10,0));   //Bb C Db Eb F Gb Ab 5 flats /A# B# C# D# E# F# G# 7 sharps  
        minors.add(Arrays.asList(2,4,6,7,9,11,1 ));  //B C# D E F# G A 2 sharps 
       
        if (key.getScale().getMajorOrMinorIndicator()==0){ //0 for major
            keyScale = majors.get(reduceValue(key.getRoot().getValue()));         
        }
        else{
            keyScale = minors.get(reduceValue(key.getRoot().getValue())); 
        }
        if (!keyScale.contains(reduceValue(note.getValue()))){
            return true;
         } 
        
        return false;
    }
    
    /** Reduces a note value (0-128) to its equivalent in the lowest octave.
     * 
     * @param v The note value to be reduced
     * @return  The reduced value
     */
    private int reduceValue(int v) {
        return v % 12;
    }
    
    /** Checks the degree of non-diatonics and adds the degree to list
     * 
     * @param n The note to have it's value checked
     */
    private void checkDegree(Number n){
        Integer rootValue = reduceValue(key.getRoot().getValue());
        int noteValue = n.intValue();   
        //degrees are: 0: ♭II, 1: ♭III (♮III for minor key), 2: ♭V, 3: ♭VI, 4: ♭VII.
        if (noteValue < rootValue){
            noteValue += 12;
        }
        int difference = noteValue - rootValue;
        switch (difference){
            case 0: case 1: 
                degreeNonDiatonic.add(0);
                break;
            case 2: case 3: case 4:
                degreeNonDiatonic.add(1);
                break;
            case 5: case 6:
                degreeNonDiatonic.add(2);
                break;
            case 7: case 8:
                degreeNonDiatonic.add(3);
                break;
            case 9: case 10: case 11:
                degreeNonDiatonic.add(4);
                break;
            default:
                break;
        }
                             
    }
    
    /** Process all events in chronological order.
     *   - add pitch, duration, etc. to lists for further statistical processing
     */
    private void processEvents(){
        sortTimeEvents();
        double lastTime = 0;
        double ioi = 0.0;
        int ticks = 0;
        byte interval = 60; 
        sortTimeEvents();
        
        //Find first note event and get first stats for relative calculations
        for (TimeEvent t : musicEvents){
            if (t.getEvent() instanceof Note || t.getEvent() instanceof org.jfugue.theory.Chord){
                Note note = (Note)t.getEvent(); 
                interval = note.getValue(); //set interval to fist note value
                lastTime = t.time; //get first time 
                durations.add(note.getDuration()); //add first duration 
                ioi = (int)convertDecimalToTicks(note.getDuration());
                break;
            }
        }    
        for (TimeEvent t : musicEvents){
            Note note;
            //If event is note, collect stats
            if (t.getEvent() instanceof Note || t.getEvent() instanceof org.jfugue.theory.Chord){
                if (t.getEvent() instanceof org.jfugue.theory.Chord){
                    org.jfugue.theory.Chord c = (org.jfugue.theory.Chord)t.getEvent();
                    note = c.getRoot();
                }
                else{
                    note = (Note)t.getEvent();
                }
                int noteTicks = (int)convertDecimalToTicks(note.getDuration());
                if (!note.isRest()){
                    pitches.add(note.getValue()); 
                    attacks.add(note.getOnVelocity());
                    decays.add(note.getOffVelocity());
                    //check for non-diatonics
                    if (checkHarmonics(note)){
                        checkDegree(reduceValue(note.getValue()));
                    }
                    //check for syncopations
                    if (ticks%128 > 15 && ticks%128 < 112
                        && noteTicks > (142 - ticks%128)){ //16 TICKS IS 32ND NOTE 16+128 = 142
                        rhythm++;
                    } 
                    
                    ticks = ticks + noteTicks;
                    interval = (byte)(Math.abs(note.getValue() - interval));
                    intervals.add(interval);
                    interval = note.getValue();
                    if (lastTime != t.time) {
                        durations.add(note.getDuration()); //ADD NOTE IF IT IS NOT SYNCRONOUS 
                        interOI.add(ioi);
                        ioi = noteTicks;
                    }
                    lastTime = t.time;
                }
                else {
                    ioi = ioi + noteTicks;
                    ticks = ticks + noteTicks;
                    lastTime = t.time;
                    if (note.getDuration() > .0615){
                        restDurations.add(note.getDuration());
                    } 
               }
            }
            else if (t.getEvent() instanceof Key){
                key = (Key)t.getEvent();
            }   
        } 
        if (intervals.size() > 0){
        intervals.remove(0); //remove first interval (first note value)
        }     
    }
    
    private long convertBeatsToTicks(double time) {
        long durationInTicks = (long)(time * 512);
        return durationInTicks;   
    }
    
    private double convertDecimalToTicks(double decimalDuration){
        double wholeNoteinTicks = 512; //128*4
        return decimalDuration * wholeNoteinTicks;    
    }
        
    private double calcAverage(List<Number> list){
        //AVERAGES: computed as (mean-min)
        if (calcN(list)==0){
            return 0;
        }
        Double average;
        Double total = 0.0;
        Double min = list.get(0).doubleValue();
        for (Number n : list){
            Double d = n.doubleValue();
            total = total + d;
            if(min > d){
                min = d;
        }}
       average = (total/list.size()) - min; 
       return average; 
    }    
        
    private double calcSD(List<Number> list){
        if (calcN(list)==0){
            return 0;
        }
        double average;
        List<Number> squares = new ArrayList<Number>();
        
        average = computeAverage(list);
        for (Number n : list){       
            squares.add(Math.pow(n.doubleValue()-average, 2));
        }
        average = Math.sqrt(computeAverage(squares));
        return average;
    }    
    
    private double calcRange(List<Number> list){
        if (calcN(list)==0){
            return 0;
        }
        Double max = list.get(0).doubleValue();
        Double min = list.get(0).doubleValue();
        Double range;
        for (Number n : list){
            double d = n.doubleValue();
            if(min > d){
                min = d;
            }
            if(max < d){
                max = d;
        }}
        range = max - min;
        return range;
    }
    
    private int  calcN(List<Number> list){
        int n = list.size();
        return n;
    }

    private double computeAverage(List<Number> list){
        Double total = 0.0;
        for (Number n : list){
            total = total + n.doubleValue();
        }
       double average = (total/list.size()); 
       return average;    
    }

    @Override
    public String toString(){
        int[] general = this.getGeneralStats();
        Stats duration = this.getDurationStats();
        Stats pitch = this.getPitchStats();
        Stats ioi = this.getIOIStats();
        Stats interval = this.getIntervalStats();
        Stats silence = this.getRestStats();
        Stats harmonics = this.getHarmonicStats();
        return ("General Stats: \n Notes =  " + general[0] + "\n Silences =  " + general[1] 
            + "\nInterval Stats: n  =  " + interval.getN() + " \n Average  =  " + interval.getAverage() 
            + "\n Range  =  " + interval.getRange() + "\n SD  =  " + interval.getSD() 
            + "\nIOI Stats: n  =  " + ioi.getN() + "\n Average  =  " + ioi.getAverage()
            + "\n Range  =  " + ioi.getRange() + "\n SD = " + ioi.getSD()
            + "\nDuration Stats: n  =  " + duration.getN() + "\n Average  =  " + duration.getAverage()
            + "\n Range  =  " + duration.getRange() + "\n SD = " + duration.getSD()
            + "\nPitch Stats: n  =  " + pitch.getN() + "\n Average  =  " + pitch.getAverage()
            + "\n Range  =  " + pitch.getRange() + "\n SD = " + pitch.getSD()
            + "\nSilence Stats: n  =  " + silence.getN() + "\n Average  =  " + silence.getAverage() 
            + "\n Range  =  " + silence.getRange() + "\n SD = " + silence.getSD()
            + "\nHarmonic Stats: n  =  " + harmonics.getN() + "\n Average  =  " + harmonics.getAverage()
            + "\n Range  =  " + harmonics.getRange() + "\n SD = " + silence.getSD()
            + "\nRythm Stats: n = " + this.getRhythmStats());
        
    }
    
    private class TimeEvent<T> {
       double time; //in ticks
       T eventValue;
       
       private TimeEvent(double ticks, T event){ 
           time = ticks;
           eventValue = event;
       }
       
       private T getEvent(){
           return eventValue;
       }
       
   }
   
    public final class Stats{
       private int n;
       private double range;
       private double sd;
       private double average;
      
      private Stats(List<Number> list){
           n = calcN(list);
           range = calcRange(list);
           sd = calcSD(list);
           average = calcAverage(list);
       }
       
       private Stats(List<Number> list1, List<Number> list2){
           n = calcN(list1);
           range = calcRange(list1);
           sd = calcSD(list1);
           average = calcAverage(list2);
       }
       
       public int getN(){
           return n;
       }
       
       /**Get the population range
        * 
        * @return Population range
        */
       public double getRange(){
           return range;
       }
       
       /**Get the Population Standard Deviation
        * 
        * @return Population SD
        */
       public double getSD(){
           return sd;
       }
       
       /**Get the average (mean - min)
        * 
        * @return (mean - min)
        */
       public double getAverage(){
           return average;
       }
   }
   
   private class Listener implements ParserListener {
    
    
    @Override
    public void beforeParsingStarts() {
       // System.out.println("Begin Parse");
    }

    @Override
    public void afterParsingFinished() {   
      
    }

    @Override
    public void onTrackChanged(byte t) {
        musicEvents.add(new TimeEvent<Byte>(tickPos, t));
    }

    @Override
    public void onLayerChanged(byte layerNum) {
        musicEvents.add(new TimeEvent<Byte>(tickPos, layerNum));
        tickPos = 0;
    }

    @Override
    public void onInstrumentParsed(byte i) {
       musicEvents.add(new TimeEvent<Byte>(tickPos, i));
    }

    @Override
    public void onTempoChanged(int tBPM) {  
        musicEvents.add(new TimeEvent<Integer>(tickPos, tBPM));
    }

    @Override
    public void onKeySignatureParsed(byte keyB, byte scale) {    
        Key k;
        String maj = ("maj"), min = ("min");
        if (scale == 0){
            k = new Key(new Note(keyB).toString() + maj);
        }
        else{
            k = new Key(new Note(keyB).toString() + min);
        }
        musicEvents.add(new TimeEvent<Key>(tickPos, k));
    }

    @Override
    public void onTimeSignatureParsed(byte bDuration, byte bNumber) {
        musicEvents.add(new TimeEvent<Byte[]>(tickPos, new Byte[]{bDuration,bNumber}));
    }

    @Override
    public void onBarLineParsed(long m) {     
        musicEvents.add(new TimeEvent<Long>(tickPos, m));
        ++measures;
    }

    @Override
    public void onTrackBeatTimeBookmarked(String string) {
       // System.out.println("Time bookmarked");
    }

    @Override
    public void onTrackBeatTimeBookmarkRequested(String string) {
       // System.out.println("Time bookmark requested");
    }

    @Override
    public void onTrackBeatTimeRequested(double beats) {
       tickPos = convertBeatsToTicks(beats);
    }

    @Override
    public void onPitchWheelParsed(byte b, byte b1) {
        musicEvents.add(new TimeEvent<Byte[]>(tickPos, new Byte[]{b,b1}));
    }

    @Override
    public void onChannelPressureParsed(byte b) {
       musicEvents.add(new TimeEvent<Byte>(tickPos, b));
    }

    @Override
    public void onPolyphonicPressureParsed(byte b, byte b1) {
        musicEvents.add(new TimeEvent<Byte[]>(tickPos, new Byte[]{b,b1}));
    }

    @Override
    public void onSystemExclusiveParsed(byte... bytes) {
        //Byte[] sysEx = new Byte[bytes.length];
        //System.arraycopy(bytes, 0, sysEx, 0, bytes.length);
       // musicEvents.add(new TimeEvent<Byte[]>(tickPos, sysEx));
        /*
         * Exception in thread "AWT-EventQueue-0" java.lang.ArrayStoreException
	at java.lang.System.arraycopy(Native Method)
	at music.generator.PatternAnalyzer$Listener.onSystemExclusiveParsed(PatternAnalyzer.java:763)
	at org.jfugue.parser.ParserContext.fireSystemExclusiveParsed(ParserContext.java:253)
	at org.jfugue.functions.SysexFunction.apply(SysexFunction.java:50)
	at org.staccato.FunctionSubparser.parse(FunctionSubparser.java:53)
	at org.staccato.StaccatoParser.parse(StaccatoParser.java:98)
	at music.generator.PatternAnalyzer.parsePattern(PatternAnalyzer.java:77)
	at music.generator.PatternAnalyzer.parsePattern(PatternAnalyzer.java:106)
         */
    }

    @Override
    public void onControllerEventParsed(byte b, byte b1) {
        musicEvents.add(new TimeEvent<Byte[]>(tickPos, new Byte[]{b,b1}));
    }

    @Override
    public void onLyricParsed(String lyric) {
        musicEvents.add(new TimeEvent<String>(tickPos, lyric));
    }

    @Override
    public void onMarkerParsed(String string) {
        musicEvents.add(new TimeEvent<String>(tickPos, string));
    }

    @Override
    public void onFunctionParsed(String string, Object o) {
        musicEvents.add(new TimeEvent<Object>(tickPos, o));
    }
    
    @Override
    public void onNoteParsed(Note note) {
       musicEvents.add(new TimeEvent<Note>(tickPos, note));
       tickPos = tickPos + convertDecimalToTicks(note.getDuration()); //advance tick position

    }
    
    @Override
    public void onChordParsed(org.jfugue.theory.Chord chord) {    
        musicEvents.add(new TimeEvent<org.jfugue.theory.Chord>(tickPos, chord));
        tickPos = tickPos + convertDecimalToTicks(chord.getRoot().getDuration());
    }
    }  
}
 
   