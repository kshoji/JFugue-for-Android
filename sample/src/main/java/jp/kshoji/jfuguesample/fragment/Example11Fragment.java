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
import org.jfugue.pattern.Pattern;
import org.jfugue.player.Player;
import org.staccato.StaccatoParserListener;

import java.io.File;
import java.io.IOException;

import jp.kshoji.javax.sound.midi.InvalidMidiDataException;
import jp.kshoji.javax.sound.midi.MidiSystem;
import jp.kshoji.jfuguesample.R;

/**
 * Example 11: Connecting Any Parser to Any ParserListener
 */
@EFragment
public class Example11Fragment extends AbstractExampleFragment {
    private final Player player = new Player();

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
        resetPlayer(player);
        try {
            MidiParser parser = new MidiParser();
            StaccatoParserListener listener = new StaccatoParserListener();
            parser.addParserListener(listener);
            if (midiFilepath != null) {
                parser.parse(MidiSystem.getSequence(new File(midiFilepath)));
            } else {
                parser.parse(MidiSystem.getSequence(getResources().getAssets().open("smfs/fugue-c-major.mid")));
            }
            Pattern staccatoPattern = listener.getPattern();
            System.out.println(staccatoPattern);
            println(staccatoPattern);

            player.play(staccatoPattern);
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
        resetPlayer(player);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        resetPlayer(player);
    }
}
