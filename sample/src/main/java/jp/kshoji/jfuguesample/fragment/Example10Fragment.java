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
import org.jfugue.midi.MidiFileManager;
import org.jfugue.pattern.Pattern;

import java.io.File;
import java.io.IOException;

import jp.kshoji.javax.sound.midi.InvalidMidiDataException;
import jp.kshoji.jfuguesample.R;

/**
 * Example 10: See the Contents of a MIDI File in Human-Readable and Machine-Parseable Staccato Format
 */
@EFragment
public class Example10Fragment extends AbstractExampleFragment {

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
            if (midiFilepath != null) {
                Pattern pattern = MidiFileManager.loadPatternFromMidi(new File(midiFilepath));
                System.out.println(pattern);
                println(pattern);
            } else {
                Pattern pattern = MidiFileManager.loadPatternFromMidi(getResources().getAssets().open("smfs/fugue-c-major.mid"));
                System.out.println(pattern);
                println(pattern);
            }
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

    @Override
    public void stop() {

    }
}
