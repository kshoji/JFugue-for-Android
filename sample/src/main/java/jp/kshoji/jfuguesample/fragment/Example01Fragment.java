package jp.kshoji.jfuguesample.fragment;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;
import org.jfugue.player.Player;

import jp.kshoji.javax.sound.midi.MidiSystem;
import jp.kshoji.javax.sound.midi.MidiUnavailableException;

/**
 * Example 01: Playing multiple voices, multiple instruments, rests, chords, and durations
 */
@EFragment
public class Example01Fragment extends AbstractExampleFragment {

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
        Player player = new Player();
        player.play("V0 I[Piano] Eq Ch. | Eq Ch. | Dq Eq Dq Cq   V1 I[Flute] Rw | Rw | GmajQQQ CmajQ");
    }
}
