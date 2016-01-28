package jp.kshoji.jfuguesample.fragment;

import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.jfugue.midi.MidiParser;
import org.jfugue.parser.ParserListenerAdapter;
import org.jfugue.theory.Note;

import java.io.File;
import java.io.IOException;

import jp.kshoji.javax.sound.midi.InvalidMidiDataException;
import jp.kshoji.javax.sound.midi.MidiSystem;
import jp.kshoji.jfuguesample.R;

/**
 * Example 12: Create a Listener to Find Out About Music
 */
@EFragment
public class Example12Fragment extends AbstractExampleFragment {

    private String midiFilepath = null;

    @ViewById
    LinearLayout chooseFileLayout;

    @ViewById
    TextView filenameText;

    @AfterViews
    void afterViews() {
        chooseFileLayout.setVisibility(View.VISIBLE);
    }

    @Click(R.id.chooseFileButton)
    void onChooseFileClicked() {
        selectMidiFile();
    }

    @Override
    protected void onMidiFileSelected(final String filepath) {
        super.onMidiFileSelected(filepath);
        midiFilepath = filepath;

        final String filename = filepath.substring(filepath.lastIndexOf("/") + 1);
        filenameText.setText(filename);
        Toast.makeText(getActivity(), "MIDI File selected: " + filename, Toast.LENGTH_SHORT).show();;
    }

    @Background
    @Override
    public void start() {
        try {
            MidiParser parser = new MidiParser(); // Remember, you can use any Parser!
            MyParserListener listener = new MyParserListener();
            parser.addParserListener(listener);
            if (midiFilepath != null) {
                parser.parse(MidiSystem.getSequence(new File(midiFilepath)));
            } else {
                parser.parse(MidiSystem.getSequence(getResources().getAssets().open("smfs/fugue-c-major.mid")));
            }

            println("There are " + listener.counter + " 'C' notes in this music.");
        } catch (IOException | InvalidMidiDataException | NullPointerException e) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            Log.e(getClass().getSimpleName(), e.getMessage(), e);
        }
    }

    class MyParserListener extends ParserListenerAdapter {
        public int counter;

        @Override
        public void onNoteParsed(Note note) {
            // A "C" note is in the 0th position of an octave
            if (note.getPositionInOctave() == 0) {
                counter++;
            }
        }
    }

    @Override
    public void stop() {

    }
}
