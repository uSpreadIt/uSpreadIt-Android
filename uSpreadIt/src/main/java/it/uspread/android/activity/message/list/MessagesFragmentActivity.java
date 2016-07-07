package it.uspread.android.activity.message.list;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import it.uspread.android.R;
import it.uspread.android.activity.USpreadItFragment;
import it.uspread.android.activity.message.create.MessageCreationActivity;
import it.uspread.android.activity.message.list.received.ReceivedMessagesFragment;
import it.uspread.android.activity.message.list.spread.SpreadMessagesFragment;
import it.uspread.android.activity.message.list.writed.WritedMessagesFragment;
import it.uspread.android.task.Task;

/**
 * Activité de consultation des messages.<br/>
 * C'est l'activité de lancement de l'application  elle remplace donc par l'activité de login si nécessaire.
 * <ul>
 * <li>Messages reçus</li>
 * <li>Messages écrits</li>
 * <li>Messages propagés</li>
 * </ul>
 *
 * @author Lone Décosterd,
 */
public class MessagesFragmentActivity extends USpreadItFragment {

    /** Onglet des messages reçus */
    private final static int TAB_INDEX_RECEIVED = 0;
    /** Onglet des messages écrits */
    private final static int TAB_INDEX_USER = 1;
    /** Onglet des messages propagés */
    private final static int TAB_INDEX_SPREAD = 2;

    /** Page des messages reçus */
    public ReceivedMessagesFragment receivedMessagesFragment;
    /** Page des messages écrits */
    public WritedMessagesFragment writedMessagesFragment;
    /** Page des messages propagés */
    public SpreadMessagesFragment spreadMessagesFragment;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.activity_messages, container, false);

        // Mise en place des 3 fragments de l'activité qui seront accessible par une barre d'onglet
        ViewPager viewPager = (ViewPager) view;
        viewPager.setAdapter(new FragmentStatePagerAdapter(getFragmentManager()) {
            @Override
            public Fragment getItem(final int position) {
                // Cette méthode est appelé pour la création du fragment : ne sera pas rappelé ensuite
                switch (position) {
                    case TAB_INDEX_RECEIVED:
                        receivedMessagesFragment = new ReceivedMessagesFragment();
                        return receivedMessagesFragment;
                    case TAB_INDEX_USER:
                        writedMessagesFragment = new WritedMessagesFragment();
                        return writedMessagesFragment;
                    case TAB_INDEX_SPREAD:
                        spreadMessagesFragment = new SpreadMessagesFragment();
                        return spreadMessagesFragment;
                }
                return null;
            }

            @Override
            public int getCount() {
                return 3;
            }

            public CharSequence getPageTitle(int position) {
                switch (position) {
                    case TAB_INDEX_RECEIVED:
                        return getResources().getString(R.string.activity_fragment_receivedMessages);
                    case TAB_INDEX_USER:
                        return getResources().getString(R.string.activity_fragment_writedMessages);
                    case TAB_INDEX_SPREAD:
                        return getResources().getString(R.string.activity_fragment_spreadMessages);
                }
                return super.getPageTitle(position);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(getResources().getString(R.string.activity_messages));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_messages, menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            // Création d'un nouveau message
            case R.id.action_new:
                final Intent intent = new Intent(getActivity(), MessageCreationActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onReceiveTaskResultUpdateUI(final Task.TaskResult taskResult) {

    }
}
