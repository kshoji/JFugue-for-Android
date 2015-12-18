package jp.kshoji.jfuguesample.fragment;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;
import org.jfugue.player.Player;
import org.staccato.ReplacementMapPreprocessor;
import org.staccato.maps.CarnaticReplacementMap;

import jp.kshoji.javax.sound.midi.MidiSystem;
import jp.kshoji.javax.sound.midi.MidiUnavailableException;

/**
 * Example 15: Use "Replacement Maps" to Create Carnatic Music
 */
@EFragment
public class Example15Fragment extends AbstractExampleFragment {

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
        ReplacementMapPreprocessor.getInstance().setReplacementMap(new CarnaticReplacementMap());

        Player player = new Player();
        player.play("<S> <R1> <R2> <R3> <R4>");
    }
}
