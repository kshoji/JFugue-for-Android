package jp.kshoji.jfuguesample.fragment;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;
import org.jfugue.player.Player;
import org.jfugue.theory.Chord;
import org.jfugue.theory.ChordProgression;
import org.jfugue.theory.Note;

/**
 * Example 04: Introduction to Chord Progressions
 */
@EFragment
public class Example04Fragment extends AbstractExampleFragment {
    private final Player player = new Player();

    @Background
    @Override
    public void start() {
        resetPlayer(player);
        ChordProgression cp = new ChordProgression("I IV V");

        Chord[] chords = cp.setKey("C").getChords();
        for (Chord chord : chords) {
            System.out.print("Chord "+chord+" has these notes: ");
            println("Chord "+chord+" has these notes: ");
            Note[] notes = chord.getNotes();
            String chordText = "";
            for (Note note : notes) {
                chordText += note+" ";
                System.out.print(note+" ");
            }
            System.out.println();
            println(chordText);
        }

        player.play(cp);
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
