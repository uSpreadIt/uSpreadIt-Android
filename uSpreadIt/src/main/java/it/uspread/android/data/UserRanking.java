package it.uspread.android.data;

/**
 * Les informations de classement d'un utilisateur.
 * <ul>
 * <li>Un nom d'utilisateur</li>
 * <li>Ses informations de classement</li>
 * </ul>
 *
 * @author Lone Décosterd,
 */
public class UserRanking {

    private String username;

    // TODO représentation ultra temporaire car le ranking devra être représenté par période, etc, cette conception interne ne peut convenir plus tard

    /** Le classement de l'utilisateur */
    private int ranking;

    /**
     * @param username
     *         {@link #username}
     * @param ranking
     *         {@link #ranking}
     */
    public UserRanking(final String username, final int ranking) {
        this.username = username;
        this.ranking = ranking;
    }

    /**
     * @return {@link #username}
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username
     *         {@link #username}
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return {@link #ranking}
     */
    public int getRanking() {
        return ranking;
    }

    /**
     * @param ranking
     *         {@link #ranking}
     */
    public void setRanking(final int ranking) {
        this.ranking = ranking;
    }
}
