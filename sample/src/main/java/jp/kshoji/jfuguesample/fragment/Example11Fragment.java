package jp.kshoji.jfuguesample.fragment;

import android.util.Log;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;
import org.jfugue.midi.MidiParser;
import org.jfugue.pattern.Pattern;
import org.jfugue.player.Player;
import org.staccato.StaccatoParserListener;

import java.io.File;
import java.io.IOException;

import jp.kshoji.javax.sound.midi.InvalidMidiDataException;
import jp.kshoji.javax.sound.midi.MidiSystem;
import jp.kshoji.javax.sound.midi.MidiUnavailableException;

/**
 * Example 11: Connecting Any Parser to Any ParserListener
 */
@EFragment
public class Example11Fragment extends AbstractExampleFragment {

    @Override
    public void stop() {
        try {
            MidiSystem.getSequencer().stop();
        } catch (final MidiUnavailableException ignored) {
        }
    }

    @Background
    @Override
    public void start() {
        try {
            MidiParser parser = new MidiParser();
            StaccatoParserListener listener = new StaccatoParserListener();
            parser.addParserListener(listener);
            parser.parse(MidiSystem.getSequence(new File("PUT A MIDI FILE HERE")));
            Pattern staccatoPattern = listener.getPattern();
            System.out.println(staccatoPattern);

            Player player = new Player();
            player.play(staccatoPattern);
        } catch (IOException | InvalidMidiDataException e) {
            Log.e(getClass().getSimpleName(), e.getMessage(), e);
        }
    }
}
