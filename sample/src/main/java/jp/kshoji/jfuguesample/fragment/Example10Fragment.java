package jp.kshoji.jfuguesample.fragment;

import android.util.Log;
import android.widget.Toast;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;
import org.jfugue.midi.MidiFileManager;
import org.jfugue.pattern.Pattern;

import java.io.IOException;

import jp.kshoji.javax.sound.midi.InvalidMidiDataException;

/**
 * Example 10: See the Contents of a MIDI File in Human-Readable and Machine-Parseable Staccato Format
 */
@EFragment
public class Example10Fragment extends AbstractExampleFragment {

    @Background
    @Override
    public void start() {
        try {
            Pattern pattern = MidiFileManager.loadPatternFromMidi(getResources().getAssets().open("smfs/fugue-c-major.mid"));// TODO PUT YOUR MIDI FILENAME HERE
            System.out.println(pattern);
            println(pattern);
        } catch (IOException | InvalidMidiDataException | NullPointerException e) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            Log.e(getClass().getSimpleName(), e.getMessage(), e);
        }
    }

    @Override
    public void stop() {

    }
}
