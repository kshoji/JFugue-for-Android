package jp.kshoji.jfuguesample.fragment;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.jfugue.player.Player;
import org.jfugue.player.SequencerManager;

import jp.kshoji.javax.sound.midi.InvalidMidiDataException;
import jp.kshoji.javax.sound.midi.MidiEvent;
import jp.kshoji.javax.sound.midi.MidiSystem;
import jp.kshoji.javax.sound.midi.MidiUnavailableException;
import jp.kshoji.javax.sound.midi.Sequence;
import jp.kshoji.javax.sound.midi.Sequencer;
import jp.kshoji.javax.sound.midi.ShortMessage;
import jp.kshoji.javax.sound.midi.Track;
import jp.kshoji.jfuguesample.R;
import jp.kshoji.jfuguesample.util.AssetUtils;

/**
 * Abstract Fragment for examples
 *
 * http://www.jfugue.org/examples.html
 */
public abstract class AbstractExampleFragment extends Fragment {
    protected static final int FILE_OPEN = 0x0000C36B;

    private ListView logView;
    private ArrayAdapter<String> logViewAdapter;

    protected int fragmentLayoutId = R.layout.fragment_example;

    private int getExampleId() {
        return Integer.parseInt(getClass().getSimpleName().replaceFirst("Example", "").replaceFirst("Fragment_", ""));
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        final View fragmentView = inflater.inflate(fragmentLayoutId, container, false);

        // set title text
        final String titleText = AssetUtils.getAssetFileAsString(getActivity(), String.format("titles/%02d.txt", getExampleId()));
        if (TextUtils.isEmpty(titleText)) {
            fragmentView.findViewById(R.id.title).setVisibility(View.GONE);
        } else {
            fragmentView.findViewById(R.id.title).setVisibility(View.VISIBLE);
            ((TextView)fragmentView.findViewById(R.id.title)).setText(titleText);
        }

        // set description text
        final String descriptionText = AssetUtils.getAssetFileAsString(getActivity(), String.format("descriptions/%02d.txt", getExampleId()));
        if (TextUtils.isEmpty(descriptionText)) {
            fragmentView.findViewById(R.id.description).setVisibility(View.GONE);
        } else {
            fragmentView.findViewById(R.id.description).setVisibility(View.VISIBLE);
            ((TextView) fragmentView.findViewById(R.id.description)).setText(descriptionText);
        }

        // set code text
        final String codeText = AssetUtils.getAssetFileAsString(getActivity(), String.format("codes/%02d.txt", getExampleId()));
        if (TextUtils.isEmpty(codeText)) {
            fragmentView.findViewById(R.id.code).setVisibility(View.GONE);
        } else {
            fragmentView.findViewById(R.id.code).setVisibility(View.VISIBLE);
            ((TextView) fragmentView.findViewById(R.id.code)).setText(codeText);
        }

        logView = (ListView) fragmentView.findViewById(R.id.listView);
        if (logView != null) {
            logViewAdapter = new ArrayAdapter<>(getActivity(), R.layout.list_item_log, R.id.textView);
            logView.setAdapter(logViewAdapter);
        }

        return fragmentView;
    }

    /**
     * Do something to start playing
     */
    public abstract void start();

    /**
     * Do something to stop playing
     */
    public abstract void stop();

    /**
     * Print messages to logView
     *
     * @param message the message
     */
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

    /**
     * Select a file with using Intent
     */
    protected final void selectMidiFile() {
        final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/midi");
        startActivityForResult(intent, FILE_OPEN);
    }

    /**
     * Get the filepath from data Uri
     * @param uri the Uri
     * @return filepath
     */
    private String getMidiFilepath(final Uri uri){
        final ContentResolver contentResolver = getActivity().getContentResolver();

        final Cursor uriCursor = contentResolver.query(uri, null, null, null, null);
        if (uriCursor != null) {
            uriCursor.moveToFirst();
            final String cursorString = uriCursor.getString(0);
            final String documentId = cursorString.substring(cursorString.lastIndexOf(":") + 1);
            uriCursor.close();

            final Cursor pathCursor = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Audio.Media._ID + " = ? ", new String[]{documentId}, null);
            if (pathCursor != null) {
                pathCursor.moveToFirst();
                final String path = pathCursor.getString(pathCursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                pathCursor.close();
                return path;
            }
        }

        return null;
    }

    protected void onMidiFileSelected(final String filepath) {
        // implement on the subclasses
    }

    @Override
    public final void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_OPEN && resultCode == Activity.RESULT_OK) {
            final String midiFilePath = getMidiFilepath(Uri.parse(data.getDataString()));

            if (midiFilePath != null) {
                onMidiFileSelected(midiFilePath);
            }
        }
    }
}
