package jp.kshoji.jfuguesample.fragment;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;
import org.jfugue.player.Player;
import org.jfugue.theory.ChordProgression;

import jp.kshoji.javax.sound.midi.MidiSystem;
import jp.kshoji.javax.sound.midi.MidiUnavailableException;

/**
 * Example 05: Advanced Chord Progressions
 */
@EFragment
public class Example05Fragment extends AbstractExampleFragment {

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
        ChordProgression cp = new ChordProgression("I IV V");

        Player player = new Player();
        player.play(cp.eachChordAs("$0q $1q $2q Rq"));

        player.play(cp.allChordsAs("$0q $0q $0q $0q $1q $1q $2q $0q"));

        player.play(cp.allChordsAs("$0 $0 $0 $0 $1 $1 $2 $0").eachChordAs("V0 $0s $1s $2s Rs V1 $_q"));
    }
}
