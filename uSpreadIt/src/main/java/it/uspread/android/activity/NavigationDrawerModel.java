package it.uspread.android.activity;

/**
 * Modèle du panneau de navigation.
 *
 * @author Lone Décosterd,
 */
public class NavigationDrawerModel {

    /** Resource portant l'icone du menu */
    private final int icon;
    /** Titre du menu ou de l'entête */
    private final String title;

    /** Identifiant de l'action du menu */
    private final int idAction;

    /** Indique si l'élements est une simple entête servant à séparer des menus */
    private boolean groupHeader = false;

    /**
     * Constructeur pour une entête.
     *
     * @param title
     *         {@link #title}
     */
    public NavigationDrawerModel(final String title) {
        this(-1, title, -1);
        groupHeader = true;
    }

    /**
     * Constructeur pour un menu
     *
     * @param icon
     *         {@link #icon}
     * @param title
     *         {@link #title}
     */
    public NavigationDrawerModel(final int icon, String title, final int idAction) {
        this.icon = icon;
        this.title = title;
        this.idAction = idAction;
    }

    /**
     * @return {@link #icon}
     */
    public int getIcon() {
        return icon;
    }

    /**
     * @return {@link #title}
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return {@link #groupHeader}
     */
    public boolean isGroupHeader() {
        return groupHeader;
    }

    /**
     * @return {@link #idAction}
     */
    public int getIdAction() {
        return idAction;
    }

}
