package jp.kshoji.jfuguesample.fragment;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;
import org.jfugue.player.Player;
import org.jfugue.rhythm.Rhythm;

/**
 * Example 07: Introduction to Rhythms
 */
@EFragment
public class Example07Fragment extends AbstractExampleFragment {
    private final Player player = new Player();

    @Background
    @Override
    public void start() {
        resetPlayer(player);
        Rhythm rhythm = new Rhythm()
                .addLayer("O..oO...O..oOO..")
                .addLayer("..S...S...S...S.")
                .addLayer("````````````````")
                .addLayer("...............+");
        player.play(rhythm.getPattern().repeat(2));
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
