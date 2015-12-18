package jp.kshoji.jfuguesample.fragment;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;
import org.jfugue.pattern.Pattern;
import org.jfugue.player.Player;
import org.staccato.ReplacementMapPreprocessor;
import org.staccato.maps.SolfegeReplacementMap;

import jp.kshoji.javax.sound.midi.MidiSystem;
import jp.kshoji.javax.sound.midi.MidiUnavailableException;

/**
 * Example 16: Use "Replacement Maps" to Play Solfege
 */
@EFragment
public class Example16Fragment extends AbstractExampleFragment {

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
        ReplacementMapPreprocessor rmp = ReplacementMapPreprocessor.getInstance();
        rmp.setReplacementMap(new SolfegeReplacementMap()).setRequireAngleBrackets(false);
        Player player = new Player();
        player.play(new Pattern("do re mi fa so la ti do")); // This will play "C D E F G A B"

        // This next example brings back the brackets so durations can be added
        rmp.setRequireAngleBrackets(true);
        player.play(new Pattern("<Do>q <Re>q <Mi>h | <Mi>q <Fa>q <So>h | <So>q <Fa>q <Mi>h | <Mi>q <Re>q <Do>h"));
    }
}
