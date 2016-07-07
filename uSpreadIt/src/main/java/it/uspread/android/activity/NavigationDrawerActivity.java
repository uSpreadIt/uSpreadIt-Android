package it.uspread.android.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import it.uspread.android.R;
import it.uspread.android.USpreadItApplication;
import it.uspread.android.activity.account.LoginActivity;
import it.uspread.android.activity.account.ManageAccountFragmentActivity;
import it.uspread.android.activity.message.list.MessagesFragmentActivity;
import it.uspread.android.activity.stats.HallOfFameFragmentActivity;
import it.uspread.android.gcm.GcmIntentService;
import it.uspread.android.gcm.GcmUtils;
import it.uspread.android.task.Task;

/**
 * Activité principale de l'application qui utilise un panneau latéral de navigation pour accéder à ses fonctionnalités.<br/>
 * <br/>
 * Les fragments de l'activité devront donc s'injecter dans <code>activity_content_frame</code>.
 *
 * @author Lone Décosterd,
 */
public class NavigationDrawerActivity extends USpreadItActivity {

    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private ActionBarDrawerToggle drawerToggle;

    /** Titre actuellement affiché */
    private CharSequence toolbarTitle;

    /** Menu actuellement séléctionné */
    private int selectedMenuId;

    /** Recoit les notifications de nouveau messages */
    private BroadcastReceiver broadcastReceiverNewMessage = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final MessagesFragmentActivity messagesFragmentActivity = (MessagesFragmentActivity) getFragmentManager().findFragmentByTag(MessagesFragmentActivity.class.getName());
            if (messagesFragmentActivity != null) {
                if (!messagesFragmentActivity.receivedMessagesFragment.isDetached()) {
                    messagesFragmentActivity.receivedMessagesFragment.refreshList();
                }
            }
        }
    };

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ########## Vérification que l'utilisateur soit logué, si pas logué on interrompt l'initialisation de l'activité et lance l'activité de login à la place de celle ci.
        if (!USpreadItApplication.getInstance().getSessionManager().isLoggedIn()) {
            // Fermeture des activités courante et lancement de l'activité de login.
            final Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return;
        }

        // ########## Vérifier que le Play Service APK est disponible et demande à l'user de l'activer ou le télécharger si nécessaire
        checkPlayServicesAndRegistrationIdExistence();

        setContentView(R.layout.navigation_drawer);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        // Nécessaire d'activer le bouton de l'action bar permettant de faire un up. On va le changer afin d'avoir notre boutons d'apparition du panneau de navigation
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        drawerLayout = (DrawerLayout) findViewById(R.id.activity_navigation_drawer);
        drawerList = (ListView) findViewById(R.id.navigation_drawer);

        // Ajout d'une ombre sur le coté du panneau de navigation
        drawerLayout.setDrawerShadow(R.drawable.navigation_shadow, GravityCompat.START);

        // Initialisation des eleménts du panneau et de l'écoute des clic
        drawerList.setAdapter(new NavigationDrawerAdapter(this, createMenu()));
        drawerList.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onNavigationMenu(((NavigationDrawerModel) parent.getItemAtPosition(position)).getIdAction());
            }
        });

        // Intercation entre l'actionBar et le panneau de navigation
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(toolbarTitle);
                // Recréer les boutons d'actions
                supportInvalidateOptionsMenu();
            }

            public void onDrawerOpened(View view) {
                super.onDrawerOpened(view);
                getSupportActionBar().setTitle(getResources().getString(R.string.app_name));
                // Recréer les boutons d'actions
                supportInvalidateOptionsMenu();
            }
        };
        drawerLayout.setDrawerListener(drawerToggle);

        if (savedInstanceState == null) {
            // Affiche du fragment de démarrage en cas de premier démarrage (pas lors de la restauration)
            drawerList.setItemChecked(1, true);
            onNavigationMenu(R.string.navigation_action_messages);
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiverNewMessage, new IntentFilter(GcmIntentService.BROADCAST_ACTION_REFRESH_MESSAGES_RECEIVED));
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServicesAndRegistrationIdExistence();
    }

    /**
     * Vérification que les services Play fonctionnent et le cas échéant q'une registration Id est enregistré ou s'il faut s'enregistrer.
     */
    private void checkPlayServicesAndRegistrationIdExistence() {
        if (GcmUtils.checkPlayServices(this)) {
            final String registrationId = USpreadItApplication.getInstance().getSessionManager().getGCMRegistrationId();
            // Si pas de token de push enregistré alors en demander un
            if (registrationId == null) {
                GcmUtils.obtainNewGCMRegistrationIDInBackground(this);
            }
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        // Si le panneau de navigation est ouvert on affiche pas les menus de la toolbar
        boolean drawerOpen = drawerLayout.isDrawerOpen(drawerList);
        for (int i = 0; i < menu.size(); i++) {
            menu.getItem(i).setVisible(!drawerOpen);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void setTitle(final CharSequence title) {
        // On ne se préocuppe plus de changer le nom de l'activité : seul le titre affiché dans la toolbar nous intéresse
        getSupportActionBar().setTitle(title);
        toolbarTitle = title;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(final Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(drawerList)) {
            drawerLayout.closeDrawer(drawerList);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiverNewMessage);
        super.onDestroy();
    }

    /**
     * Définition des éléments du panneau de navigation : menu clicable et entêtes de menus.
     *
     * @return Liste des éléments
     */
    private List<NavigationDrawerModel> createMenu() {
        List<NavigationDrawerModel> listMenu = new ArrayList<>();
        listMenu.add(new NavigationDrawerModel(USpreadItApplication.getInstance().getSessionManager().getUsername()));
        listMenu.add(new NavigationDrawerModel(R.drawable.menu_messages, getResources().getString(R.string.navigation_action_messages), R.string.navigation_action_messages));
        listMenu.add(new NavigationDrawerModel(R.drawable.menu_scores, getResources().getString(R.string.navigation_action_scores), R.string.navigation_action_scores));
        listMenu.add(new NavigationDrawerModel(getResources().getString(R.string.navigation_account)));
        listMenu.add(new NavigationDrawerModel(R.drawable.menu_account, getResources().getString(R.string.navigation_action_account), R.string.navigation_action_account));
        listMenu.add(new NavigationDrawerModel(R.drawable.menu_logout, getResources().getString(R.string.navigation_action_logout), R.string.navigation_action_logout));
        return listMenu;
    }

    /**
     * Ouverture de l'activité demandé
     *
     * @param idMenu
     *         Identifiant du menu
     */
    private void onNavigationMenu(final int idMenu) {
        drawerLayout.closeDrawer(drawerList);
        // Si le menu demandé n'est pas celui déjà affiché
        if (idMenu != selectedMenuId) {
            selectedMenuId = idMenu;
            switch (idMenu) {
                case R.string.navigation_action_messages:
                    getFragmentManager().beginTransaction().replace(R.id.activity_content_frame, new MessagesFragmentActivity(), MessagesFragmentActivity.class.getName()).commit();
                    break;
                case R.string.navigation_action_scores:
                    getFragmentManager().beginTransaction().replace(R.id.activity_content_frame, new HallOfFameFragmentActivity(), HallOfFameFragmentActivity.class.getName()).commit();
                    break;
                case R.string.navigation_action_account:
                    getFragmentManager().beginTransaction().replace(R.id.activity_content_frame, new ManageAccountFragmentActivity(), ManageAccountFragmentActivity.class.getName()).commit();
                    break;
                case R.string.navigation_action_logout:
                    USpreadItApplication.getInstance().getSessionManager().logout();
                    break;
            }
        }
    }

    @Override
    public void onReceiveTaskResultUpdateUI(final Task.TaskResult taskResult) {
    }
}
