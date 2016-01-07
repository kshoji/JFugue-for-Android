package jp.kshoji.jfuguesample.fragment;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;
import org.jfugue.player.Player;
import org.staccato.ReplacementMapPreprocessor;
import org.staccato.maps.CarnaticReplacementMap;

/**
 * Example 15: Use "Replacement Maps" to Create Carnatic Music
 */
@EFragment
public class Example15Fragment extends AbstractExampleFragment {
    private final Player player = new Player();

    @Background
    @Override
    public void start() {
        resetPlayer(player);
        ReplacementMapPreprocessor.getInstance().setReplacementMap(new CarnaticReplacementMap());

        player.play("<S> <R1> <R2> <R3> <R4>");
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
