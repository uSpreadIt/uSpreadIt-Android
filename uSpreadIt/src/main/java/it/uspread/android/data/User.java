package it.uspread.android.data;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import it.uspread.android.remote.exception.USpreadItException;

/**
 * Un utilisateur.
 * <ul>
 * <li>Un email</li>
 * <li>Un mot de passe</li>
 * <li>Un nom d'utilisateur</li>
 * </ul>
 * <br>
 * Le fait d'implémenter Parcelable est un prérequis pour permettre de conserver un utilisateur lorsque dans le cycle de vie d'une ativité android elle est détruite puis
 * reconstruite
 *
 * @author Lone Décosterd,
 */
public class User implements Parcelable {

    /** Nécessaire pour la sérialisation interne Android de l'objet */
    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        @Override
        public User createFromParcel(final Parcel source) {
            return new User(source);
        }

        @Override
        public User[] newArray(final int size) {
            return new User[size];
        }
    };

    /** L'identidiant technique de l'utilisateur */
    private long id;
    /** Le nom d'utilisateur */
    private String username;
    /** Le mot de passe (Très Confidentiel : Renseigné que lors de la création d'un nouveau compte ou lors du login) */
    private String password;
    /** L'email */
    private String email;

    /**
     * Construction d'un user vierge
     */
    private User() {
    }

    /**
     * Création d'un User.
     *
     * @param username
     *         {@link #username}
     * @param password
     *         {@link #password}
     */
    public User(final String username, final String password) {
        this(username, password, null);
    }

    /**
     * Création d'un User.
     *
     * @param username
     *         {@link #username}
     * @param password
     *         {@link #password}
     * @param email
     *         {@link #email}
     */
    public User(final String username, final String password, final String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }

    /**
     * Construction d'un user à partir des informations fourni en JSON
     *
     * @param json
     *         utilisateur au format JSON
     */
    public User(JSONObject json) throws USpreadItException {
        try {
            this.id = json.getLong("id");
            this.username = json.getString("usr");
            this.email = json.getString("email");
        } catch (JSONException e) {
            throw new USpreadItException(USpreadItException.Type.OTHERS, e.getMessage(), e);
        }
    }

    /**
     * Construction d'un user à partir d'un Parcel
     *
     * @param source
     *         parcel
     */
    public User(final Parcel source) {
        id = source.readLong();
        username = source.readString();
        password = source.readString();
        email = source.readString();
    }

    /**
     * @return {@link #id}
     */
    public long getId() {
        return id;
    }

    /**
     * @param id
     *         {@link #id}
     */
    public void setId(final long id) {
        this.id = id;
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
    public void setUsername(final String username) {
        this.username = username;
    }

    /**
     * @return {@link #password}
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password
     *         {@link #password}
     */
    public void setPassword(final String password) {
        this.password = password;
    }

    /**
     * @return {@link #email}
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email
     *         {@link #email}
     */
    public void setEmail(final String email) {
        this.email = email;
    }

    /**
     * Transforme l'objet en JSON. (Ce limite aux attibuts qui sont destiné à être envoyé sur le serveur)
     *
     * @return les propriété de l'objet en JSON
     */
    public JSONObject toJSONCreation() throws USpreadItException {
        final JSONObject json = new JSONObject();
        try {
            json.put("usr", getUsername());
            json.put("pass", getPassword());
            json.put("email", getEmail());
            json.put("lang", "FR");//FIXME
        } catch (JSONException e) {
            throw new USpreadItException(USpreadItException.Type.OTHERS, e.getMessage(), e);
        }
        return json;
    }

    /**
     * Transforme l'objet en JSON. (Ce limite aux attibuts qui sont destiné à être envoyé sur le serveur)
     *
     * @return les propriété de l'objet en JSON
     */
    public JSONObject toJSONLogin() throws USpreadItException {
        final JSONObject json = new JSONObject();
        try {
            json.put("usr", getUsername());
            json.put("pass", getPassword());
        } catch (JSONException e) {
            throw new USpreadItException(USpreadItException.Type.OTHERS, e.getMessage(), e);
        }
        return json;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeLong(id);
        dest.writeString(username);
        dest.writeString(password);
        dest.writeString(email);
    }

    @Override
    public int hashCode() {
        //return Objects.hash(this.id);
        int hash = 5;
        hash += 89 + ((Long) this.id).hashCode();
        return hash;

    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj instanceof User) {
            User other = (User) obj;
            // return Objects.equals(this.id, other.id);
            return this.id == other.id;
        }
        return false;
    }
}
