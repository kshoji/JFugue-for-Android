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

package org.jfugue.pattern;

import java.util.ArrayList;
import java.util.List;

public class TrackTable implements PatternProducer
{
    private int length;
    private List<List<Pattern>> tracks; 
    private PatternValidator validator;
    
    public TrackTable(int length) {
        this.length = length;
        tracks = new ArrayList<List<Pattern>>(TrackTable.NUM_TRACKS);
    }
    
    public List<Pattern> getTrack(int track) {
        return tracks.get(track);
    }
    
    public TrackTable setPatternValidator(PatternValidator pv) {
        this.validator = pv;
        return this;
    }
    
    public PatternValidator getPatternValidator() {
    	return this.validator;
    }
    
    public TrackTable put(int track, int position, PatternProducer patternProducer) {
        if (validator != null) {
            if (!validator.isValid(patternProducer)) {
                throw new RuntimeException(validator.getErrorMessage(patternProducer));
            }
        }
        List<Pattern> trackList = this.tracks.get(track);
        if (trackList == null) {
            trackList = new ArrayList<Pattern>(getLength());
            this.tracks.add(track, trackList);
        }
        trackList.add(position, patternProducer.getPattern());
        return this;
    }

    public TrackTable put(int track, int position, PatternProducer... patternProducers) {
        int counter = 0;
        for (PatternProducer producer : patternProducers) {
            this.put(track, position+counter, producer);
            counter++;
        }
        return this;
    }

    /** Puts the given pattern in the track table at every 'nth' position */
    public TrackTable putAtIntervals(int track, int nth, PatternProducer patternProducer) {
        for (int position = 0; position < this.length; position += nth) {
            this.put(track, position, patternProducer);
        }
        return this;
    }

    /** Puts the given pattern in the track table at every 'nth' position, starting with position 'first' */
    public TrackTable putAtIntervals(int track, int nth, int first, PatternProducer patternProducer) {
        for (int position = first; position < this.length; position += nth) {
            this.put(track, position, patternProducer);
        }
        return this;
    }

    /**
     * As part of JFugue's fluent API, this method returns the instance of this class. 
     * @param track
     * @param start
     * @param end
     * @param patternProducer
     * @return The instance of this class
     */
    public TrackTable put(int track, int start, int end, PatternProducer patternProducer) {
        for (int i=start; i <= end; i++) {
            put(track, i, patternProducer);
        }
        return this;
    }
    
    /**
     * Lets you specify which cells in the TrackTable should be populated with the given PatternProducer by using a String 
     * in which a period means "not in this cell" and any other character means "in this cell".
     * Example: put(1, pattern, "...XXXX..XX....XXXX..XX....");
     * 
     * @param track
     * @param periodMeansNoOtherMeansYes
     * @param patternProducer
     * @return
     */
    public TrackTable put(int track, String periodMeansNoOtherMeansYes, PatternProducer patternProducer) {
    	for (int i=0; i < periodMeansNoOtherMeansYes.length(); i++) {
    		if (periodMeansNoOtherMeansYes.charAt(i) != '.') {
    			put(track, i, patternProducer);
    		}
    	}
    	return this;
    }
    
    public int getLength() {
        return this.length;
    }
    
    @Override
    public Pattern getPattern() {
        Pattern pattern = new Pattern();
        for (List<Pattern> track : tracks) {
            for (Pattern p : track) {
                pattern.addTrack(tracks.indexOf(track), p);
            }
        }
        return pattern;
    }

    public static final int NUM_TRACKS = 16;
    public static final int RHYTHM_TRACK = 9;
}
