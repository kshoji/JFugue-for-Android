package jp.kshoji.jfuguesample.fragment;

import android.util.Log;
import android.widget.Toast;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;
import org.jfugue.midi.MidiParser;
import org.jfugue.pattern.Pattern;
import org.jfugue.player.Player;
import org.staccato.StaccatoParserListener;

import java.io.IOException;

import jp.kshoji.javax.sound.midi.InvalidMidiDataException;
import jp.kshoji.javax.sound.midi.MidiSystem;

/**
 * Example 11: Connecting Any Parser to Any ParserListener
 */
@EFragment
public class Example11Fragment extends AbstractExampleFragment {
    private final Player player = new Player();

    @Background
    @Override
    public void start() {
        resetPlayer(player);
        try {
            MidiParser parser = new MidiParser();
            StaccatoParserListener listener = new StaccatoParserListener();
            parser.addParserListener(listener);
            parser.parse(MidiSystem.getSequence(getResources().getAssets().open("smfs/fugue-c-major.mid")));// TODO PUT A MIDI FILE HERE
            Pattern staccatoPattern = listener.getPattern();
            System.out.println(staccatoPattern);
            println(staccatoPattern);

            player.play(staccatoPattern);
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
        resetPlayer(player);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        resetPlayer(player);
    }
}
