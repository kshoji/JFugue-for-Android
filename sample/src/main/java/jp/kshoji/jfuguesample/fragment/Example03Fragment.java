package jp.kshoji.jfuguesample.fragment;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;
import org.jfugue.pattern.Pattern;
import org.jfugue.player.Player;

/**
 * Example 03: Introduction to Patterns, Part 2
 */
@EFragment
public class Example03Fragment extends AbstractExampleFragment {
    private final Player player = new Player();

    @Background
    @Override
    public void start() {
        resetPlayer(player);
        Pattern p1 = new Pattern("Eq Ch. | Eq Ch. | Dq Eq Dq Cq").setVoice(0).setInstrument("Piano");
        Pattern p2 = new Pattern("Rw     | Rw     | GmajQQQ  CmajQ").setVoice(1).setInstrument("Flute");
        player.play(p1, p2);
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
