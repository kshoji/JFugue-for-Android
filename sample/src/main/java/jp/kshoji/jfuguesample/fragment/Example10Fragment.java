package jp.kshoji.jfuguesample.fragment;

import android.util.Log;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;
import org.jfugue.midi.MidiFileManager;
import org.jfugue.pattern.Pattern;

import java.io.File;
import java.io.IOException;

import jp.kshoji.javax.sound.midi.InvalidMidiDataException;

/**
 * Example 10: See the Contents of a MIDI File in Human-Readable and Machine-Parseable Staccato Format
 */
@EFragment
public class Example10Fragment extends AbstractExampleFragment {

    @Override
    public void stop() {

    }

    @Background
    @Override
    public void start() {
        try {
           Pattern pattern = MidiFileManager.loadPatternFromMidi(new File("PUT YOUR MIDI FILENAME HERE"));
           System.out.println(pattern);
        } catch (IOException | InvalidMidiDataException e) {
            Log.e(getClass().getSimpleName(), e.getMessage(), e);
        }
    }
}
