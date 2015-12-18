package jp.kshoji.jfuguesample.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import jp.kshoji.jfuguesample.R;

/**
 * Abstract Fragment for examples
 *
 * http://www.jfugue.org/examples.html
 */
public abstract class AbstractExampleFragment extends Fragment {

    private int getExampleId() {
        return Integer.parseInt(getClass().getSimpleName().replaceFirst("Example", "").replaceFirst("Fragment_", ""));
    }

    private String getAssetFile(String filename) {
        try {
            InputStream inputStream = getResources().getAssets().open(filename);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            int count;
            final byte[] data = new byte[1024];
            while ((count = inputStream.read(data)) >= 0) {
                baos.write(data, 0, count);
            }
            return new String(baos.toByteArray());
        } catch (IOException e) {

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

        final View fragmentView = inflater.inflate(R.layout.fragment_example, container, false);
        ((TextView)fragmentView.findViewById(R.id.title)).setText(getAssetFile(String.format("titles/%02d.txt", getExampleId())));
        ((TextView)fragmentView.findViewById(R.id.description)).setText(getAssetFile(String.format("descriptions/%02d.txt", getExampleId())));
        ((TextView)fragmentView.findViewById(R.id.code)).setText(getAssetFile(String.format("codes/%02d.txt", getExampleId())));

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
}
