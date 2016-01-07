package jp.kshoji.jfuguesample.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentLayoutId = R.layout.fragment_example_13;
        final View view = super.onCreateView(inflater, container, savedInstanceState);
        if (view != null) {
            view.findViewById(R.id.plusButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    Log.i(getActivity().getLocalClassName(), "+Button clicked. player: " + player);
                    if (player != null) {
                        final EditText editText = (EditText) view.findViewById(R.id.editText);
                        player.startNote(new Note(editText.getText().toString().substring(0, 1)));
                    }
                }
            });
            view.findViewById(R.id.minusButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    Log.i(getActivity().getLocalClassName(), "-Button clicked. player: " + player);
                    if (player != null) {
                        final EditText editText = (EditText) view.findViewById(R.id.editText);
                        player.stopNote(new Note(editText.getText().toString().substring(0, 1)));
                    }
                }
            });
            view.findViewById(R.id.iButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    Log.i(getActivity().getLocalClassName(), "iButton clicked. player: " + player);
                    if (player != null) {
                        player.changeInstrument(random.nextInt(128));
                    }
                }
            });
            view.findViewById(R.id.pButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    Log.i(getActivity().getLocalClassName(), "pButton clicked. player: " + player);
                    if (player != null) {
                        player.play(PATTERNS[random.nextInt(PATTERNS.length)]);
                    }
                }
            });
            view.findViewById(R.id.qButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    Log.i(getActivity().getLocalClassName(), "qButton clicked.");
                    quit = true;
                }
            });
        }

        return view;
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
