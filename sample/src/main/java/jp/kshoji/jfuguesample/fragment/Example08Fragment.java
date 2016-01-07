package jp.kshoji.jfuguesample.fragment;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;
import org.jfugue.player.Player;
import org.jfugue.rhythm.Rhythm;

/**
 * Example 08: Advanced Rhythms
 */
@EFragment
public class Example08Fragment extends AbstractExampleFragment {
    private final Player player = new Player();

    @Background
    @Override
    public void start() {
        resetPlayer(player);
        Rhythm rhythm = new Rhythm()
                .addLayer("O..oO...O..oOO..") // This is Layer 0
                .addLayer("..S...S...S...S.")
                .addLayer("````````````````")
                .addLayer("...............+") // This is Layer 3
                .addOneTimeAltLayer(3, 3, "...+...+...+...+") // Replace Layer 3 with this string on the 4th (count from 0) measure
                .setLength(4); // Set the length of the rhythm to 4 measures
        player.play(rhythm.getPattern().repeat(2)); // Play 2 instances of the 4-measure-long rhythm
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
