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

package org.jfugue.midi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.kshoji.javax.sound.midi.MetaMessage;
import jp.kshoji.javax.sound.midi.MidiEvent;
import jp.kshoji.javax.sound.midi.MidiMessage;
import jp.kshoji.javax.sound.midi.Receiver;
import jp.kshoji.javax.sound.midi.Sequence;
import jp.kshoji.javax.sound.midi.Track;

public class MidiTools {
    /** 
     * Returns a Map where the keys are MIDI ticks and the values are Lists of MidiMessages
     * that are declared for that tick.
     */
    public static final Map<Long, List<MidiMessage>> sortMessagesByTick(Sequence sequence) {
        Map<Long, List<MidiMessage>> sortedMessages = new HashMap<Long, List<MidiMessage>>();
        
        for (Track track : sequence.getTracks()) {
            for (int i = 0; i < track.size(); i++) { 
                MidiEvent event = track.get(i);
                List<MidiMessage> messagesAtTick = null;
                if (sortedMessages.containsKey(event.getTick())) {
                    messagesAtTick = sortedMessages.get(event.getTick());
                } else {
                    messagesAtTick = new ArrayList<MidiMessage>();
                    sortedMessages.put(event.getTick(), messagesAtTick);
                }
                messagesAtTick.add(event.getMessage());
            }
        }
        
        return sortedMessages;
    }
    
    /**
     * Returns the largest key for the given Map. While this can be used for any Map,
     * it is included here specifically to find the greatest tick in a Map<Long, List<MidiMessageWithTrack>>
     */
    public static <K extends Comparable<K>, V> K getLargestKey(Map<K, V> map) {
        K currentLargestKey = null;
        for (K key : map.keySet()) {
            if ((currentLargestKey == null) || (key.compareTo(currentLargestKey) > 0)) {
                currentLargestKey = key;
            }
        }
        return currentLargestKey;
    }

    private static int calculateTicksPerSecondFromMidiSetTempoMessageData(byte[] data, float sequenceResolution) {
        // The "Set Tempo" MIDI Message sets the tempo of a sequence in microseconds per quarter note
        int microsecondsPerQuarterNote = ((data[0] & 0xff) << 16) | ((data[1] & 0xff) << 8) | (data[2] & 0xff);
        int bpm = (int)(60000000.0D / microsecondsPerQuarterNote);
        return (int)(sequenceResolution * bpm / 60.0D);
    }

    private static int calculateTime(long deltaTick, int ticksPerSecond) {
        return (int)(deltaTick * 1000.0D / ticksPerSecond);
    }
    
    public static void sendSortedMidiMessagesToReceiver(Map<Long, List<MidiMessage>> sortedMidiMessages, float sequenceDivisionType, int sequenceResolution, Receiver receiver) {
        int bpm = MidiDefaults.DEFAULT_TEMPO_BEATS_PER_MINUTE;
        int ticksPerSecond;
        long prevTick = 0L;
        long msTime = 0L; 
        long largestTick = getLargestKey(sortedMidiMessages);

        if (sequenceDivisionType == Sequence.PPQ) {
            ticksPerSecond = (int)(sequenceResolution * bpm / 60.0D);
        } else {
            double framesPerSecond = 
                    (sequenceDivisionType == Sequence.SMPTE_24 ? 24
                      : (sequenceDivisionType == Sequence.SMPTE_25 ? 25
                        : (sequenceDivisionType == Sequence.SMPTE_30 ? 30
                          : (sequenceDivisionType == Sequence.SMPTE_30DROP ? 29.97 : 24))));
            ticksPerSecond = (int)(sequenceResolution * framesPerSecond);
        }
        
        for (long tick = 0; tick <= largestTick; tick++) {
            if (sortedMidiMessages.containsKey(tick)) {
                msTime = calculateTime(tick - prevTick, ticksPerSecond);
                List<MidiMessage> messages = sortedMidiMessages.get(tick);
                for (MidiMessage message : messages) {
                    if ((message instanceof MetaMessage) && (sequenceDivisionType == Sequence.PPQ) && (((MetaMessage)message).getType() == MidiDefaults.SET_TEMPO_MESSAGE_TYPE)) {
                        ticksPerSecond = calculateTicksPerSecondFromMidiSetTempoMessageData(((MetaMessage)message).getData(), sequenceResolution);
                        msTime = calculateTime(tick - prevTick, ticksPerSecond);
                    } else {
                        receiver.send(message, msTime);
                    }
                }
                try {
                    Thread.sleep(msTime); 
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
                prevTick = tick;
            }
        }
    }

    /**
     * Convenience method for a commonly-used idiom
     */
    public static void sendSequenceToReceiver(Sequence sequence, Receiver receiver) {
        sendSortedMidiMessagesToReceiver(sortMessagesByTick(sequence), sequence.getDivisionType(), sequence.getResolution(), receiver);
    }

    public static byte getLSB(int value) {
    	return (byte)(value & 0x7F);
    }
    
    public static byte getMSB(int value) {
    	return (byte)((value >> 7) & 0x7F);    	
    }
}

