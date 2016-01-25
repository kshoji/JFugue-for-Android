package jp.kshoji.jfuguesample.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.jfugue.player.Player;

import jp.kshoji.jfuguesample.R;

/**
 * Example 18: Free input
 */
@EFragment
public class Example18Fragment extends AbstractExampleFragment {
    private final Player player = new Player();

    @ViewById
    EditText editText;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        fragmentLayoutId = R.layout.fragment_example_18;
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Background
    @Override
    public void start() {
        resetPlayer(player);
        player.play(editText.getText().toString());
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
