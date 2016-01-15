package jp.kshoji.jfuguesample.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.jfugue.pattern.Pattern;
import org.jfugue.player.SynthesizerManager;
import org.jfugue.realtime.RealtimePlayer;
import org.jfugue.theory.Note;

import java.util.Random;

import jp.kshoji.javax.sound.midi.MidiSystem;
import jp.kshoji.javax.sound.midi.MidiUnavailableException;
import jp.kshoji.jfuguesample.R;

/**
 * Example 13: Play Music in Realtime
 */
@EFragment
public class Example13Fragment extends AbstractExampleFragment {
    private RealtimePlayer player;
    private Random random = new Random();
    private volatile boolean quit = false;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        fragmentLayoutId = R.layout.fragment_example_13;
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @ViewById
    EditText editText;

    @Click(R.id.plusButton)
    void onPlusButton() {
        Log.i(getActivity().getLocalClassName(), "+Button clicked. player: " + player);
        if (player != null) {
            player.startNote(new Note(editText.getText().toString().substring(0, 1)));
        }
    }

    @Click(R.id.minusButton)
    void onMinusButton() {
        Log.i(getActivity().getLocalClassName(), "-Button clicked. player: " + player);
        if (player != null) {
            player.stopNote(new Note(editText.getText().toString().substring(0, 1)));
        }
    }

    @Click(R.id.iButton)
    void onIButton() {
        if (player != null) {
            player.changeInstrument(random.nextInt(128));
        }
    }

    @Click(R.id.pButton)
    void onPButton() {
        if (player != null) {
            player.play(PATTERNS[random.nextInt(PATTERNS.length)]);
        }
    }

    @Click(R.id.qButton)
    void onQButton() {
        quit = true;
    }

    @Background
    @Override
    public void start() {
        Log.i(getActivity().getLocalClassName(), "player initializing...");
        try {
            SynthesizerManager.getInstance().setSynthesizer(MidiSystem.getSynthesizer());
            player = new RealtimePlayer();
            Log.i(getActivity().getLocalClassName(), "player initialized: " + player);
        } catch (final MidiUnavailableException e) {
            Log.i(getActivity().getLocalClassName(), "player initialize failed: " + e.getMessage());
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getContext(), "RealtimePlayer is not available", Toast.LENGTH_LONG).show();
                }
            });
            return;
        }
        quit = false;
        while (quit == false);
        player.close();
        player = null;
    }

    private static Pattern[] PATTERNS = new Pattern[] {
            new Pattern("Cmajq Dmajq Emajq"),
            new Pattern("V0 Ei Gi Di Ci  V1 Gi Ci Fi Ei"),
            new Pattern("V0 Cmajq V1 Gmajq")
    };

    @Override
    public void stop() {
        quit = true;
    }
}
