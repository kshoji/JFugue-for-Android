package jp.kshoji.jfuguesample.fragment;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;
import org.jfugue.pattern.Pattern;
import org.jfugue.player.Player;
import org.jfugue.theory.ChordProgression;

import jp.kshoji.javax.sound.midi.MidiSystem;
import jp.kshoji.javax.sound.midi.MidiUnavailableException;

/**
 * Example 06: Twelve-Bar Blues in Two Lines of Code
 */
@EFragment
public class Example06Fragment extends AbstractExampleFragment {

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
        Pattern pattern = new ChordProgression("I IV V")
                .distribute("7%6")
                .allChordsAs("$0 $0 $0 $0 $1 $1 $0 $0 $2 $1 $0 $0")
                .eachChordAs("$0ia100 $1ia80 $2ia80 $3ia80 $4ia100 $3ia80 $2ia80 $1ia80")
                .getPattern()
                .setInstrument("Acoustic_Bass")
                .setTempo(100);
        new Player().play(pattern);
    }
}
