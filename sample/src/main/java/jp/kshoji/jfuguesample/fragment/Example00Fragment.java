package jp.kshoji.jfuguesample.fragment;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;
import org.jfugue.player.Player;

import jp.kshoji.javax.sound.midi.MidiSystem;
import jp.kshoji.javax.sound.midi.MidiUnavailableException;

/**
 * Example 00: "Hello, World" in JFugue
 */
@EFragment
public class Example00Fragment extends AbstractExampleFragment {

    @Override
    public void stop() {
        try {
            MidiSystem.getSequencer().stop();
        } catch (final MidiUnavailableException | NullPointerException ignored) {
        }
    }

    @Background
    @Override
    public void start() {
        Player player = new Player();
        player.play("C D E F G A B");
    }
}
