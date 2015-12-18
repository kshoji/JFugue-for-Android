package jp.kshoji.jfuguesample.fragment;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;
import org.jfugue.pattern.Pattern;
import org.jfugue.realtime.RealtimePlayer;
import org.jfugue.theory.Note;

import java.util.Random;
import java.util.Scanner;

import jp.kshoji.javax.sound.midi.MidiSystem;
import jp.kshoji.javax.sound.midi.MidiUnavailableException;

/**
 * Example 13: Play Music in Realtime
 */
@EFragment
public class Example13Fragment extends AbstractExampleFragment {

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
        try {
            RealtimePlayer player = new RealtimePlayer();
            Random random = new Random();
            Scanner scanner = new Scanner(System.in);
            boolean quit = false;
            while (quit == false) {
                System.out.print("Enter a '+C' to start a note, "+
                        "'-C' to stop a note, 'i' for a random instrument, " +
                        "'p' for a pattern, or 'q' to quit: ");
                String entry = scanner.next();
                if (entry.startsWith("+")) {
                    player.startNote(new Note(entry.substring(1)));
                }
                else if (entry.startsWith("-")) {
                    player.stopNote(new Note(entry.substring(1)));
                }
                else if (entry.equalsIgnoreCase("i")) {
                    player.changeInstrument(random.nextInt(128));
                }
                else if (entry.equalsIgnoreCase("p")) {
                    player.play(PATTERNS[random.nextInt(PATTERNS.length)]);
                }
                else if (entry.equalsIgnoreCase("q")) {
                    quit = true;
                }
            }
            scanner.close();
            player.close();
        } catch (MidiUnavailableException e) {

        }
    }

    private static Pattern[] PATTERNS = new Pattern[] {
            new Pattern("Cmajq Dmajq Emajq"),
            new Pattern("V0 Ei Gi Di Ci  V1 Gi Ci Fi Ei"),
            new Pattern("V0 Cmajq V1 Gmajq")
    };
}
