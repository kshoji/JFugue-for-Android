package jp.kshoji.jfuguesample.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.jfugue.player.Player;
import org.jfugue.player.SequencerManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import jp.kshoji.javax.sound.midi.InvalidMidiDataException;
import jp.kshoji.javax.sound.midi.MidiEvent;
import jp.kshoji.javax.sound.midi.MidiSystem;
import jp.kshoji.javax.sound.midi.MidiUnavailableException;
import jp.kshoji.javax.sound.midi.Sequence;
import jp.kshoji.javax.sound.midi.Sequencer;
import jp.kshoji.javax.sound.midi.ShortMessage;
import jp.kshoji.javax.sound.midi.Track;
import jp.kshoji.jfuguesample.R;

/**
 * Abstract Fragment for examples
 *
 * http://www.jfugue.org/examples.html
 */
public abstract class AbstractExampleFragment extends Fragment {
    private ListView logView;
    private ArrayAdapter<String> logViewAdapter;

    protected int fragmentLayoutId = R.layout.fragment_example;

    private int getExampleId() {
        return Integer.parseInt(getClass().getSimpleName().replaceFirst("Example", "").replaceFirst("Fragment_", ""));
    }

    /**
     * Obtains asset file content as String
     * @param filename asset file name
     * @return read String
     */
    private String getAssetFileAsString(final String filename) {
        try {
            final InputStream inputStream = getResources().getAssets().open(filename);
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();

            int count;
            final byte[] data = new byte[1024];
            while ((count = inputStream.read(data)) >= 0) {
                baos.write(data, 0, count);
            }
            return new String(baos.toByteArray());
        } catch (final IOException ignored) {

        }
        return "";
    }

    protected AbstractExampleFragment() {
        super();
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        final View fragmentView = inflater.inflate(fragmentLayoutId, container, false);
        ((TextView)fragmentView.findViewById(R.id.title)).setText(getAssetFileAsString(String.format("titles/%02d.txt", getExampleId())));
        ((TextView)fragmentView.findViewById(R.id.description)).setText(getAssetFileAsString(String.format("descriptions/%02d.txt", getExampleId())));
        ((TextView)fragmentView.findViewById(R.id.code)).setText(getAssetFileAsString(String.format("codes/%02d.txt", getExampleId())));

        logView = (ListView) fragmentView.findViewById(R.id.listView);
        if (logView != null) {
            logViewAdapter = new ArrayAdapter<>(getContext(), R.layout.list_item_log, R.id.textView);
            logView.setAdapter(logViewAdapter);
        }

        return fragmentView;
    }

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public abstract void start();

    public abstract void stop();

    public final void println(@Nullable final Object message) {
        // add log to ListView
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (logView.getVisibility() != View.VISIBLE) {
                    logView.setVisibility(View.VISIBLE);
                }
                logViewAdapter.add(message == null ? "null" : message.toString());
            }
        });
    }

    /**
     * Reset the player
     *
     * @param player the player
     */
    protected final void resetPlayer(@NonNull final Player player) {
        // finish playing
        player.getManagedPlayer().finish();

        // attach new sequencer to SequencerManager
        Sequencer sequencer = null;
        try {
            sequencer = MidiSystem.getSequencer();
            SequencerManager.getInstance().setSequencer(sequencer);
        } catch (final MidiUnavailableException ignored) {
        }

        if (sequencer == null) {
            return;
        }

        // reset synthesizer status
        try {
            final Sequence sequence = new Sequence(Sequence.PPQ, 480);
            final Track track = sequence.createTrack();
            for (int channel = 0; channel < 16; channel++) {
                track.add(new MidiEvent(new ShortMessage(ShortMessage.CONTROL_CHANGE | channel, 123, 0), channel)); // all notes off
                track.add(new MidiEvent(new ShortMessage(ShortMessage.CONTROL_CHANGE | channel, 120, 0), channel + 16)); // all sounds off
                track.add(new MidiEvent(new ShortMessage(ShortMessage.CONTROL_CHANGE | channel, 121, 0), channel + 32)); // reset all controllers
                track.add(new MidiEvent(new ShortMessage(ShortMessage.PROGRAM_CHANGE | channel, 0, 0), channel + 48)); // set program to 0
            }
            track.add(new MidiEvent(new ShortMessage(ShortMessage.SYSTEM_RESET), 64));

            player.play(sequence);
        } catch (final InvalidMidiDataException ignored) {
        }
    }
}
