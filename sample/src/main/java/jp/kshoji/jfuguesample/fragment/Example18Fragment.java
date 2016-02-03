package jp.kshoji.jfuguesample.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import org.androidannotations.annotations.AfterTextChange;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.jfugue.player.Player;

import java.io.File;
import java.io.IOException;

import jp.kshoji.javax.sound.midi.MidiSystem;
import jp.kshoji.javax.sound.midi.Sequence;
import jp.kshoji.jfuguesample.R;

/**
 * Example 18: Free input
 */
@EFragment
public class Example18Fragment extends AbstractExampleFragment {
    private final Player player = new Player();

    @ViewById
    EditText editText;

    @ViewById
    EditText filenameText;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        fragmentLayoutId = R.layout.fragment_example_18;
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @AfterViews
    void afterViews() {
        final SharedPreferences preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        editText.setText(preferences.getString(getString(R.string.preference_sequence), getString(R.string.preference_sequence_default)));
    }

    @AfterTextChange(R.id.editText)
    void textChanged() {
        final SharedPreferences preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putString(getString(R.string.preference_sequence), editText.getText().toString());
        editor.apply();
    }

    @Background
    @Override
    public void start() {
        resetPlayer(player);
        player.play(editText.getText().toString());
    }

    @Click(R.id.saveButton)
    void onSaveSequence() {
        if (TextUtils.isEmpty(filenameText.getText())) {
            Toast.makeText(getActivity(), "File name is empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        final Sequence sequence = player.getSequence(editText.getText().toString());

        try {
            final File dir = getActivity().getExternalFilesDir(Environment.DIRECTORY_MUSIC);
            final File resultFile = new File(dir, filenameText.getText() + ".mid");

            MidiSystem.write(sequence, 0, resultFile);
            Toast.makeText(getActivity(), "SMF saved to: " + resultFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (final IOException e) {
            Toast.makeText(getActivity(), "SMF save failed. " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Background
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
