package jp.kshoji.jfuguesample.fragment;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;
import org.jfugue.devtools.DiagnosticParserListener;
import org.jfugue.player.Player;
import org.jfugue.temporal.TemporalPLP;
import org.staccato.StaccatoParser;

/**
 * Example 14: Anticipate Musical Events Before They Occur
 */
@EFragment
public class Example14Fragment extends AbstractExampleFragment {
    private final Player player = new Player();

    private static final String MUSIC = "C D E F G A B";
    private static final long TEMPORAL_DELAY = 500;

    @Background
    @Override
    public void start() {
        resetPlayer(player);
        // Part 1. Parse the original music
        StaccatoParser parser = new StaccatoParser();
        TemporalPLP plp = new TemporalPLP();
        parser.addParserListener(plp);
        parser.parse(MUSIC);

        // Part 2. Send the events from Part 1, and play the original music with a delay
        DiagnosticParserListener dpl = new DiagnosticParserListener(); // Or your AnimationParserListener!
        plp.addParserListener(dpl);
        player.delayPlay(TEMPORAL_DELAY, MUSIC);
        plp.parse();
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
