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

package org.jfugue.rhythm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jfugue.midi.MidiDefaults;
import org.jfugue.pattern.Pattern;
import org.jfugue.pattern.PatternProducer;
import org.staccato.StaccatoUtil;

public class Rhythm implements PatternProducer
{
	private List<String> layers;
	private Map<Integer, List<AltLayer>> altLayers;
	private Map<Character, String> rhythmKit;
	private int length = 1;
	
	public Rhythm() {
		this(DEFAULT_RHYTHM_KIT);
	}
	
	public Rhythm(String... layers) {
		this(DEFAULT_RHYTHM_KIT, layers);
	}
	
	public Rhythm(Map<Character, String> rhythmKit) {
		layers = new ArrayList<String>();
		altLayers = new HashMap<Integer, List<AltLayer>>();
		setRhythmKit(rhythmKit);
    }

    public Rhythm(Map<Character, String> rhythmKit, String... layers) {
		this(rhythmKit);
		for (String layer :  layers) {
			this.addLayer(layer);
		}
    }

    public Rhythm setRhythmKit(Map<Character, String> rhythmKit) {
    	this.rhythmKit = rhythmKit;
    	return this;
    }
    
    public Map<Character, String> getRhythmKit() {
    	return this.rhythmKit;
    }
    
    /**
     * Adds a layer to this Rhythm, but fails silently if
     * the rhythm already has MAX_LAYERS layers.
     * @param layer
     * @return
     */
    public Rhythm addLayer(String layer) {
    	if (this.layers.size() < MidiDefaults.LAYERS) {
    		this.layers.add(layer);
    	}
    	return this;
    }
    
    public String getLayer(int layer) {
    	return this.layers.get(layer);
    }
    
    /**
     * Returns all layers that have been added with the traditional addLayer() method - but to
     * truly find out what the layer will sound like at a given segment, use getLayersForSegment(),
     * which takes alt layers into account.
     * @see getLayersForSegment
     */
    public List<String> getLayers() {
    	return this.layers;
    }
    
    /**
     * Sets all of the layers 
     */
    public Rhythm setLayers(List<String> layers) {
    	if (layers.size() > MidiDefaults.LAYERS) {
    		throw new RuntimeException("Size of the List<String> provided to Rhythm.setLayers() is greater than "+MidiDefaults.LAYERS);
    	}
    	this.layers = layers;
    	return this;
    }

    /**
     * Returns all layers, including altLayers, for the given segment
     * @see getLayers
     */
    public String[] getLayersForSegment(int segment) {
    	String[] retVal = new String[layers.size()];
    	for (int layer = 0; layer < layers.size(); layer++) {
    		List<AltLayer> altLayers = getSortedAltLayersForLayer(layer);
    		// Start with the base layer
    		retVal[layer] = getLayer(layer);
    		
    		// See if the base layer should be replaced by any of the alt layers
    		for (AltLayer altLayer : altLayers) {
    			if (altLayer.shouldProvideAltLayer(segment)) {
    				// Remember that RhythmAltLayerProvider is allowed to return null if there is nothing to add
    				String rhythmOrNull = altLayer.getAltLayer(segment);
    				if (rhythmOrNull != null) {
    					retVal[layer] = rhythmOrNull;
    				}
    			}
    		}
    	}
    	return retVal;
    }
    
    /**
     * Returns true if the number of layers is less than MAX_LAYERS, which is limited to
     * 16 by the MIDI Specification
     * @return
     */
    public boolean canAddLayer() {
    	return (this.layers.size() < MidiDefaults.LAYERS);
    }
    
    public Rhythm clone() {
    	return new Rhythm(this.rhythmKit, this.getLayers().toArray(new String[0]));
    }

    /**
     * Returns all AltLayers for the given layer; the resulting list is unsorted by z-order
     * @see getSortedAltLatersForLayer
     */
    public List<AltLayer> getAltLayersForLayer(int layer) {
    	if (altLayers.get(layer) == null) {
    		altLayers.put(layer,  new ArrayList<AltLayer>());
    	}
    	return altLayers.get(layer);
    }

    /**
     * Returns all AltLayers for the given layer sorted by each AltLayer's z-order
     */
    public List<AltLayer> getSortedAltLayersForLayer(int layer) {
		List<AltLayer> retVal = getAltLayersForLayer(layer);
    	Collections.sort(retVal, new Comparator<AltLayer>() {
			@Override
			public int compare(AltLayer altLayer1, AltLayer altLayer2) {
				if (altLayer1.zOrder < altLayer2.zOrder) return -1;
				if (altLayer1.zOrder > altLayer2.zOrder) return 1;
				return 0;
			}
		});
		return retVal;
    }

    /** 
     * Sets an alt layer that will recur every recurrence times *after* the start index is reached.
     * If the start index is 2 and the recurrence is 5, this alt layer will be used every time 
     * the segment % recurrence == start. By default, this has a Z-Order of 1.
     */
	public Rhythm addRecurringAltLayer(int layer, int start, int end, int recurrence, String rhythmString) {
		return addRecurringAltLayer(layer, start, end, recurrence, rhythmString, 1);
	}

    /** 
     * Sets an alt layer that will recur every recurrence times *after* the start index is reached.
     * If the start index is 2 and the recurrence is 5, this alt layer will be used every time 
     * the segment % recurrence == start
     */
	public Rhythm addRecurringAltLayer(int layer, int start, int end, int recurrence, String rhythmString, int zOrder) {
		getAltLayersForLayer(layer).add(new AltLayer(start, end, recurrence, rhythmString, null, zOrder));
		return this;
	}

	/**
	 * Sets an alt layer that will play between and including the start and end indices. 
	 * By default, this has a Z-Order of 2.
	 */
	public Rhythm addRangedAltLayer(int layer, int start, int end, String rhythmString) {
		return addRangedAltLayer(layer, start, end, rhythmString, 2);
	}
	
	/**
	 * Sets an alt layer that will play between and including the start and end indices. 
	 */
	public Rhythm addRangedAltLayer(int layer, int start, int end, String rhythmString, int zOrder) {
		getAltLayersForLayer(layer).add(new AltLayer(start, end, -1, rhythmString, null, zOrder));
		return this;
	}

	/**
	 * Sets an alt layer that will play one time, at the given segment.
	 * By default, this has a Z-Order of 3.
	 */
	public Rhythm addOneTimeAltLayer(int layer, int oneTime, String rhythmString) {
		return addOneTimeAltLayer(layer, oneTime, rhythmString, 3);
	}
	
	/**
	 * Sets an alt layer that will play one time, at the given segment.
	 */
	public Rhythm addOneTimeAltLayer(int layer, int oneTime, String rhythmString, int zOrder) {
		getAltLayersForLayer(layer).add(new AltLayer(oneTime, oneTime, -1, rhythmString, null, zOrder));
		return this;
	}
	
	/**
	 * Gives a RhythmAltLayerProvider, which will make its own determination about what type of 
	 * alt layer to play, and when to play it.
	 * By default, this has a Z-Order of 4.
	 * @see RhythmAltLayerProvider
	 */
	public Rhythm addAltLayerProvider(int layer, RhythmAltLayerProvider altLayerProvider) {
		return addAltLayerProvider(layer, altLayerProvider, 4);
	}
	
	/**
	 * Gives a RhythmAltLayerProvider, which will make its own determination about what type of 
	 * alt layer to play, and when to play it.
	 * @see RhythmAltLayerProvider
	 */
	public Rhythm addAltLayerProvider(int layer, RhythmAltLayerProvider altLayerProvider, int zOrder) {
		getAltLayersForLayer(layer).add(new AltLayer(0, getLength(), -1, null, altLayerProvider, zOrder));
		return this;
	}
	    
    /**
     * Combines rhythms into multiple layers. If there are
     * more than MAX_LAYERS layers in the provided rhythms, 
     * only the first MAX_LAYERS are used (for example, if you
     * pass five rhythms that each have four layers, the combined
     * rhythm will only contain the layers from the first four rhythms).
     * This method also ensures that the Rhythm Kit for each of the
     * provided Rhythms is added to the return value's Rhythm Kit.
     * @param rhythms the rhythms to combine
     * @return the combined rhythm
     */
    public static Rhythm combine(Rhythm... rhythms) {
    	Rhythm retVal = new Rhythm();
    	for (Rhythm rhythm : rhythms) {
    		// Add the rhythm's Rhythm Kit to the return value's rhythm kit 
    		retVal.getRhythmKit().putAll(rhythm.getRhythmKit());
    		
    		// Add the rhythm data
    		for (String layer : rhythm.getLayers()) {
    			if (retVal.canAddLayer()) {
    				retVal.addLayer(layer);
    			} else {
    				return retVal;
    			}
    		}
    		
    		// Add the alt layer into
    		for (int key : rhythm.altLayers.keySet()) {
    			retVal.getAltLayersForLayer(key).addAll(rhythm.getAltLayersForLayer(key));
    		}
    		
    		// Figure out the length of the new rhythm
    		if (retVal.getLength() < rhythm.getLength()) {
    			retVal.setLength(rhythm.getLength());
    		}
    	}
    	
    	return retVal;
    }
    
    /**
     * Sets the length of the rhythm, which is the number of times that a single
     * pattern is repeated. For example, creating a layer of "S...S...S...O..." and
     * a length of 3 would result in a Rhythm pattern of "S...S...S...O...S...S...S...O...S...S...S...O..."
     */
    public Rhythm setLength(int length) {
    	this.length = length;
    	return this;
    }
    
    public int getLength() {
    	return this.length;
    }

    /**
     * Uses the RhythmKit to translate the given rhythm into a Staccato music string.
     * @see getPattern
     */
    public String getStaccatoStringForRhythm(String rhythm) {
    	StringBuilder buddy = new StringBuilder();
		for (char ch : rhythm.toCharArray()) {
			if (rhythmKit.get(ch) != null) {
    			buddy.append(rhythmKit.get(ch));
    			buddy.append(" ");
			} else {
				throw new RuntimeException("The character '"+ch+"' used in the rhythm layer \""+rhythm+"\" is not associated with a Staccato music string in the RhythmKit "+rhythmKit);
			}
		}
		return buddy.toString().trim();
    }
    
    @Override
    public Pattern getPattern() {
    	StringBuilder buddy = new StringBuilder();
    	buddy.append(StaccatoUtil.createTrackElement((byte)9));
    	buddy.append(" ");
    	
    	for (int segment=0; segment < getLength(); segment++) {
        	byte layerCounter = 0;
	    	for (String layer : getLayersForSegment(segment)) {
	        	buddy.append(StaccatoUtil.createLayerElement(layerCounter));
	        	buddy.append(" ");
	        	layerCounter++;
	        	buddy.append(getStaccatoStringForRhythm(layer));
	        	buddy.append(" ");
	    	}
    	}
    	return new Pattern(buddy.toString().trim());
    }

    /** 
     * Returns the full rhythm, including alt layers, but not translated into Staccato music strings by looking up rhythm entries into the RhythmKit
     * @return
     */
    public String[] getRhythm() {
    	// Create the full rhythm for each layer and each segment
    	StringBuilder[] builders = new StringBuilder[this.layers.size()];
    	for (int i=0; i < layers.size(); i++) {
    		builders[i] = new StringBuilder();
    		for (int segment=0; segment < getLength(); segment++) {
    			builders[i].append(getLayersForSegment(segment)[i]);
    		}
    	}
    	
    	// Get strings from the builders
    	String[] retVal = new String[this.layers.size()];
    	for (int i=0; i < layers.size(); i++) {
    		retVal[i] = builders[i].toString();
    	}

    	return retVal;
    }
    
    class AltLayer {
    	public String rhythmString;
    	public RhythmAltLayerProvider altLayerProvider;
    	public int startIndex;
    	public int endIndex;
    	public int recurrence;
    	public int zOrder;
    	
    	public AltLayer(int start, int end, int recurrence, String rhythmString, RhythmAltLayerProvider altLayerProvider, int zOrder) {
    		this.startIndex = start;
    		this.endIndex = end;
    		this.recurrence = recurrence;
    		this.rhythmString = rhythmString;
    		this.altLayerProvider = altLayerProvider;
    		this.zOrder = zOrder;
    	}

    	/** 
    	 * Indicates whether this alt layer should be provided for the given segment
    	 */
    	public boolean shouldProvideAltLayer(int segment) {
    		// ALways return true if there is an AltLayerProvider
    		if (altLayerProvider != null) {
    			return true;
    		}
    		
    		// Check if we're in the right range of start and end indexes, and check the recurrence
    		if ((segment >= startIndex) && (segment <= endIndex)) {
    			if (recurrence == -1) return true;
    			if ((recurrence != -1) && (segment % (recurrence) == startIndex)) return true;
    		}
    		
    		return false;
    	}
    	
    	/** 
    	 * Returns this alt layer, assuming that shouldProvideAltLayer is true
    	 */
    	public String getAltLayer(int segment) {
    		if (altLayerProvider != null) {
    			return altLayerProvider.provideAltLayer(segment);
    		} else {
    			return this.rhythmString;
    		}
    	}
    }
    
    public static final Map<Character, String> DEFAULT_RHYTHM_KIT = new HashMap<Character, String>() {{
        put('.', "Ri");
        put('O', "[BASS_DRUM]i");
        put('o', "Rs [BASS_DRUM]s");
        put('S', "[ACOUSTIC_SNARE]i");
        put('s', "Rs [ACOUSTIC_SNARE]s");
        put('^', "[PEDAL_HI_HAT]i");
        put('`', "[PEDAL_HI_HAT]s Rs");
        put('*', "[CRASH_CYMBAL_1]i");
        put('+', "[CRASH_CYMBAL_1]s Rs");
        put('X', "[HAND_CLAP]i");
        put('x', "Rs [HAND_CLAP]s");
    }};
}

