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

public interface RhythmAltLayerProvider {
	/**
	 * Override this method to provide Rhythm with an alternate layer
	 * based on the current segment. For example, if you would like to
	 * return a new layer for every 5th segment, you might say:
	 *     <code>if (segment % 5 == 0) return "S...O...S..oO..."</code>
	 * If there is no alt layer to provide, return null. 
	 * @param segment The index into rhythm's length 
	 * @return a new alt layer, or null if no alt layer is to be provided
	 * @see org.jfugue.rhythm.Rhythm.setLength()
	 */
	public String provideAltLayer(int segment);
}
