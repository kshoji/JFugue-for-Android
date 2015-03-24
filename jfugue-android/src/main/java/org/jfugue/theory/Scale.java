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

package org.jfugue.theory;


/** 
 * A scale is a sequence of notes.
 * MAJOR and MINOR are two examples of scales.
 * A scale in a particular key, such as C Major or A Minor, can provide the notes of the scale
 */
public class Scale {
    private Intervals intervals;
    private String name;
    private byte majorMinorIndicator;
    
    public Scale(String intervalString) {
        this(new Intervals(intervalString));
    }
    
    public Scale(String intervalString, String name) {
        this(new Intervals(intervalString), name);
    }
    
    public Scale(Intervals pattern) {
       this.intervals = pattern; 
    }

    public Scale(Intervals pattern, String name) {
        this.intervals = pattern; 
        this.name = name;
    }

    public Scale setName(String name) {
    	this.name = name;
    	return this;
    }
    
    public String getName() {
    	return this.name;
    }
    
    public Intervals getIntervals() {
        return this.intervals;
    }
    
    public Scale setMajorOrMinorIndicator(byte indicator) {
    	this.majorMinorIndicator = indicator;
    	return this;
    }
    
    public byte getMajorOrMinorIndicator() {
    	return this.majorMinorIndicator;
    }
    
    // Returns +1 for MAJOR or -1 for MINOR
    public int getDisposition() {
    	return (this.majorMinorIndicator == MAJOR_INDICATOR ? 1 : -1);
    }
    
    @Override
    public String toString() {
    	if (this.majorMinorIndicator == MAJOR_INDICATOR) {
    		return "maj";
    	} else if (this.majorMinorIndicator == MINOR_INDICATOR) {
    		return "min";
    	} else {
    		return this.name;
    	}
    }
    
    @Override
    public boolean equals(Object o) {
    	if ((o == null) || (!(o instanceof Scale))) return false;
    	return (((Scale)o).intervals.equals(this.intervals));
    }

    @Override
    public int hashCode() {
    	return this.intervals.hashCode();
    }
    
	public static final Scale MAJOR = new Scale(new Intervals("1 2 3 4 5 6 7")).setMajorOrMinorIndicator(Scale.MAJOR_INDICATOR);
    public static final Scale MINOR = new Scale(new Intervals("1 2 b3 4 5 b6 b7")).setMajorOrMinorIndicator(Scale.MINOR_INDICATOR);
	public static final Scale CIRCLE_OF_FIFTHS = new Scale(new Intervals("1 2 3b 4 5 6 7b"));
	
	public static final byte MAJOR_INDICATOR = 1;
	public static final byte MINOR_INDICATOR = -1;
}
