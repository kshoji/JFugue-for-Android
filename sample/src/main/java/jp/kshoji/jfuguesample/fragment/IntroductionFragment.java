package jp.kshoji.jfuguesample.fragment;

import android.app.Activity;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.View;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;

import jp.kshoji.jfuguesample.R;

/**
 * Show usage of this app
 */
@EFragment(R.layout.fragment_intro)
public class IntroductionFragment extends Fragment {

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        final FloatingActionButton actionButton = (FloatingActionButton) activity.findViewById(R.id.fab);
        actionButton.setVisibility(View.GONE);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        final FloatingActionButton actionButton = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        actionButton.setVisibility(View.VISIBLE);
    }

    @Click(R.id.showMenuButton)
    void onShowMenuButton() {
        ((Toolbar) getActivity().findViewById(R.id.toolbar)).showOverflowMenu();
    }

    @Click(R.id.showDrawerButton)
    void onShowDrawerButton() {
        final DrawerLayout drawer = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
        drawer.openDrawer(GravityCompat.START);
    }
}
