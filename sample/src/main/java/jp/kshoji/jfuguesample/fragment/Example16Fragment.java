package jp.kshoji.jfuguesample.fragment;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;
import org.jfugue.pattern.Pattern;
import org.jfugue.player.Player;
import org.staccato.ReplacementMapPreprocessor;
import org.staccato.maps.SolfegeReplacementMap;

/**
 * Example 16: Use "Replacement Maps" to Play Solfege
 */
@EFragment
public class Example16Fragment extends AbstractExampleFragment {
    private final Player player = new Player();

    @Background
    @Override
    public void start() {
        resetPlayer(player);
        ReplacementMapPreprocessor rmp = ReplacementMapPreprocessor.getInstance();
        rmp.setReplacementMap(new SolfegeReplacementMap()).setRequireAngleBrackets(false);
        player.play(new Pattern("do re mi fa so la ti do")); // This will play "C D E F G A B"

        // This next example brings back the brackets so durations can be added
        rmp.setRequireAngleBrackets(true);
        player.play(new Pattern("<Do>q <Re>q <Mi>h | <Mi>q <Fa>q <So>h | <So>q <Fa>q <Mi>h | <Mi>q <Re>q <Do>h"));
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
