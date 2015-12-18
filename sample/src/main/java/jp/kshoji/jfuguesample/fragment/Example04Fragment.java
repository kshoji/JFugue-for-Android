package jp.kshoji.jfuguesample.fragment;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;
import org.jfugue.player.Player;
import org.jfugue.theory.Chord;
import org.jfugue.theory.ChordProgression;
import org.jfugue.theory.Note;

import jp.kshoji.javax.sound.midi.MidiSystem;
import jp.kshoji.javax.sound.midi.MidiUnavailableException;

/**
 * Example 04: Introduction to Chord Progressions
 */
@EFragment
public class Example04Fragment extends AbstractExampleFragment {

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
        ChordProgression cp = new ChordProgression("I IV V");

        Chord[] chords = cp.setKey("C").getChords();
        for (Chord chord : chords) {
            System.out.print("Chord "+chord+" has these notes: ");
            Note[] notes = chord.getNotes();
            for (Note note : notes) {
                System.out.print(note+" ");
            }
            System.out.println();
        }

        Player player = new Player();
        player.play(cp);
    }
}
