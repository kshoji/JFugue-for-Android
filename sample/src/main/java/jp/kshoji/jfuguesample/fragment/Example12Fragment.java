package jp.kshoji.jfuguesample.fragment;

import android.util.Log;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;
import org.jfugue.midi.MidiParser;
import org.jfugue.parser.ParserListenerAdapter;
import org.jfugue.theory.Note;

import java.io.File;
import java.io.IOException;

import jp.kshoji.javax.sound.midi.InvalidMidiDataException;
import jp.kshoji.javax.sound.midi.MidiSystem;

/**
 * Example 12: Create a Listener to Find Out About Music
 */
@EFragment
public class Example12Fragment extends AbstractExampleFragment {

    @Override
    public void stop() {

    }

    @Background
    @Override
    public void start() {
        try {
            MidiParser parser = new MidiParser(); // Remember, you can use any Parser!
            MyParserListener listener = new MyParserListener();
            parser.addParserListener(listener);
            parser.parse(MidiSystem.getSequence(new File("PUT A MIDI FILE HERE")));
            System.out.println("There are " + listener.counter + " 'C' notes in this music.");
        } catch (IOException | InvalidMidiDataException e) {
            Log.e(getClass().getSimpleName(), e.getMessage(), e);
        }
    }

    class MyParserListener extends ParserListenerAdapter {
        public int counter;

        @Override
        public void onNoteParsed(Note note) {
            // A "C" note is in the 0th position of an octave
            if (note.getPositionInOctave() == 0) {
                counter++;
            }
        }
    }
}
