package it.uspread.android.data;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import it.uspread.android.data.type.BackgroundColor;
import it.uspread.android.data.type.BackgroundType;
import it.uspread.android.data.type.MessageType;
import it.uspread.android.data.type.TextColor;
import it.uspread.android.remote.exception.USpreadItException;

/**
 * Un message : texte diffusé dans le réseau d'utilisateurs de l'application.<br>
 * Ce texte s'affiche dans un cadre de proportion précisément défini.<br>
 * On a :
 * <ul>
 * <li>Un nombre de propagation effectué</li>
 * <li>Une date de création, réception, propagation</li>
 * <li>Un contenu texte pouvant inclure un lien web (Le nombre de caractère maximum est limité)</li>
 * <li>Une couleur de texte</li>
 * <li>Un motif et couleur de fond <b>OU</b> une image de fond :<br>
 * Tout deux remplissent entièremet le cadre</li>
 * </ul>
 * <br>
 * Le fait d'implémenter Parcelable est un prérequis pour permettre de conserver un message lorsque dans le cycle de vie d'une ativité android elle est détruite puis reconstruite.
 *
 * @author Lone Décosterd,
 */
public class Message implements Parcelable, Serializable {

    /** Formattage utilisé pour les dates */
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");

    /** Nécessaire pour la sérialisation interne Android de l'objet */
    public static final Parcelable.Creator<Message> CREATOR = new Parcelable.Creator<Message>() {
        @Override
        public Message createFromParcel(final Parcel source) {
            return new Message(source);
        }

        @Override
        public Message[] newArray(final int size) {
            return new Message[size];
        }
    };

    /** Type du message pour l'utilisateur (Usage client uniquement) */
    private MessageType messageType = MessageType.WRITED;

    /** L'identifiant technique du message */
    private long id;
    /** Le nombre de fois que le message a été propagé */
    private long nbSpread;
    /** Date de création */
    private Date dateCreation;
    /** Date de réception */
    private Date dateReception;
    /** Date de propagation */
    private Date dateSpread;
    /** Le texte associé au message */
    private String text;
    /** La couleur du texte (format HTML #XXXXXX) */
    private String textColor = TextColor.BLACK.getHtmlColor();
    /** Type de fond de cadre utilisé (ne doit pas être null) */
    private BackgroundType backgroundType = BackgroundType.PLAIN;
    /** La couleur du fond du cadre (format HTML #XXXXXX) */
    private String backgroundColor = BackgroundColor.YELLOW.getHtmlColor();
    /** Image de fond éventuelle (Une fois mis en cache les messages auront cet attribut à null car stocké dans un cache différents) */
    private Bitmap backgroundImage = null;

    /** La liste de lien hypertexte détécté dans le texte du message (Usage client uniquement) */
    private List<String> listLink = new ArrayList<>(2);

    /**
     * Construction d'un message vierge
     */
    public Message() {
    }

    /**
     * Construction d'un message avec texte. Les autres paramètres restent par défaut.
     *
     * @param text
     *         {@link #text}
     */
    public Message(final String text) {
        this.text = text;
    }

    /**
     * Construction d'un message à partir des informations fourni en JSON
     *
     * @param json
     *         message au format JSON
     * @param messageType
     *         type de message. Si null on recherche le type dans le contenu JSON qui doit alors le contenir
     */
    public Message(final JSONObject json, final MessageType messageType) throws USpreadItException {
        try {
            if (messageType != null) {
                this.messageType = messageType;
            } else {
                this.messageType = MessageType.valueOf(json.getString("messageType"));
            }
            this.id = json.getLong("id");
            if (!json.isNull("nbSpread")) {
                this.nbSpread = json.getLong("nbSpread");
            }
            if (!json.isNull("created")) {
                try {
                    this.dateCreation = DATE_FORMAT.parse(json.getString("created"));
                } catch (ParseException e) {
                    this.dateCreation = null;
                }
            }
            if (!json.isNull("received")) {
                try {
                    this.dateReception = DATE_FORMAT.parse(json.getString("received"));
                } catch (ParseException e) {
                    this.dateReception = null;
                }
            }
            if (!json.isNull("spread")) {
                try {
                    this.dateSpread = DATE_FORMAT.parse(json.getString("spread"));
                } catch (ParseException e) {
                    this.dateSpread = null;
                }
            }
            if (!json.isNull("txt")) {
                this.text = json.getString("txt");
            }
            if (!json.isNull("txtColor")) {
                this.textColor = "#" + json.getString("txtColor");
            }
            if (!json.isNull("bgType")) {
                this.backgroundType = BackgroundType.valueOf(json.getString("bgType"));
            }
            if (!json.isNull("bgColor")) {
                this.backgroundColor = "#" + json.getString("bgColor");
            }
            if (!json.isNull("img")) {
                byte[] imageByte = Base64.decode(json.getString("img"), Base64.NO_WRAP);
                this.backgroundImage = BitmapFactory.decodeByteArray(imageByte, 0, imageByte.length);
            }
        } catch (JSONException e) {
            throw new USpreadItException(USpreadItException.Type.OTHERS, e.getMessage(), e);
        }
    }

    /**
     * Construction d'un message à partir d'un Parcel
     *
     * @param source
     *         parcel
     */
    public Message(final Parcel source) {
        messageType = MessageType.valueOf(source.readString());
        id = source.readLong();
        nbSpread = source.readLong();
        String date;
        try {
            date = source.readString();
            if (!"NO_DATE".equals(date)) {
                dateCreation = DATE_FORMAT.parse(date);
            }
        } catch (ParseException e) {
            dateCreation = null;
        }
        try {
            date = source.readString();
            if (!"NO_DATE".equals(date)) {
                dateReception = DATE_FORMAT.parse(date);
            }
        } catch (ParseException e) {
            dateReception = null;
        }
        try {
            date = source.readString();
            if (!"NO_DATE".equals(date)) {
                dateSpread = DATE_FORMAT.parse(date);
            }
        } catch (ParseException e) {
            dateSpread = null;
        }
        text = source.readString();
        textColor = source.readString();
        backgroundType = BackgroundType.valueOf(source.readString());
        if (backgroundType == BackgroundType.PLAIN) {
            backgroundColor = source.readString();
        } else if (backgroundType == BackgroundType.IMAGE) {
            if (source.readString().equals("IMAGE_DIRECTLY_ATTACHED")) {
                backgroundImage = Bitmap.CREATOR.createFromParcel(source);
            }
        }
        source.readList(listLink, String.class.getClassLoader());
    }

    /**
     * Créer une liste contenant ce message
     *
     * @param message
     *         le message
     * @return une liste de message
     */
    public static List<Message> inList(final Message message) {
        final List<Message> list = new ArrayList<>();
        list.add(message);
        return list;
    }

    /**
     * @return {@link #messageType}
     */
    public MessageType getMessageType() {
        return messageType;
    }

    /**
     * @param messageType
     *         {@link #messageType}
     */
    public void setMessageType(final MessageType messageType) {
        this.messageType = messageType;
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
     * @return {@link #nbSpread}
     */
    public long getNbSpread() {
        return nbSpread;
    }

    /**
     * @param nbSpread
     *         {@link #nbSpread}
     */
    public void setNbSpread(final long nbSpread) {
        this.nbSpread = nbSpread;
    }

    /**
     * @return {@link #dateCreation}
     */
    public Date getDateCreation() {
        return dateCreation;
    }

    /**
     * @param dateCreation
     *         {@link #dateCreation}
     */
    public void setDateCreation(final Date dateCreation) {
        this.dateCreation = dateCreation;
    }

    /**
     * @return {@link #dateReception}
     */
    public Date getDateReception() {
        return dateReception;
    }

    /**
     * @param dateReception
     *         {@link #dateReception}
     */
    public void setDateReception(final Date dateReception) {
        this.dateReception = dateReception;
    }

    /**
     * @return {@link #dateSpread}
     */
    public Date getDateSpread() {
        return dateSpread;
    }

    /**
     * @param dateSpread
     *         {@link #dateSpread}
     */
    public void setDateSpread(final Date dateSpread) {
        this.dateSpread = dateSpread;
    }

    /**
     * @return {@link #text}
     */
    public String getText() {
        return text;
    }

    /**
     * @param text
     *         {@link #text}
     */
    public void setText(final String text) {
        this.text = text;
    }

    /**
     * @return {@link #textColor}
     */
    public String getTextColor() {
        return textColor;
    }

    /**
     * @param textColor
     *         {@link #textColor} non null
     */
    public void setTextColor(final String textColor) {
        this.textColor = textColor;
    }

    /**
     * @return {@link #backgroundType}
     */
    public BackgroundType getBackgroundType() {
        return backgroundType;
    }

    /**
     * @param backgroundType
     *         {@link #backgroundType} non null
     */
    public void setBackgroundType(final BackgroundType backgroundType) {
        this.backgroundType = backgroundType;
    }

    /**
     * @return {@link #backgroundColor}
     */
    public String getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * @param backgroundColor
     *         {@link #backgroundColor}
     */
    public void setBackgroundColor(final String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    /**
     * @return {@link #backgroundImage}
     */
    public Bitmap getBackgroundImage() {
        return backgroundImage;
    }

    /**
     * @param backgroundImage
     *         {@link #backgroundImage}
     */
    public void setBackgroundImage(final Bitmap backgroundImage) {
        this.backgroundImage = backgroundImage;
    }

    /**
     * @return {@link #listLink}
     */
    public List<String> getListLink() {
        return listLink;
    }

    /**
     * @param listLink
     *         {@link #listLink}
     */
    public void setListLink(final List<String> listLink) {
        this.listLink = listLink;
    }

    /**
     * Indique si le texte contient des liens hypertexte
     *
     * @return Vrai si un lien hyperTexte
     */
    public boolean hasLink() {
        return !listLink.isEmpty();
    }

    /**
     * Mise à jour des attributs issu de la création du message par le serveur
     *
     * @param message
     *         les valeurs de création du message
     */
    public void updateCreationValue(final Message message) {
        setId(message.getId());
        setDateCreation(message.getDateCreation());
    }

    /**
     * Mise à jour des attributs dynamique avec les nouvelles valeurs données
     *
     * @param message
     *         les valeurs dynamique du message
     */
    public void updateDynamicValue(final Message message) {
        if (message.getId() == id) {
            setNbSpread(message.getNbSpread());
        }
    }

    /**
     * Transforme l'objet en JSON.<br>
     * Ce limite aux attibuts qui sont destiné à être envoyé sur le serveur
     *
     * @return les propriété de l'objet en JSON
     */
    public JSONObject toCreationJSON() throws USpreadItException {
        final JSONObject json = new JSONObject();
        try {
            json.put("txt", getText());
            json.put("txtColor", getTextColor().replace("#", ""));
            json.put("bgType", getBackgroundType().name());
            if (getBackgroundType() == BackgroundType.PLAIN) {
                json.put("bgColor", getBackgroundColor().replace("#", ""));
            } else if (getBackgroundType() == BackgroundType.IMAGE) {
                ByteArrayOutputStream blob = new ByteArrayOutputStream();
                backgroundImage.compress(Bitmap.CompressFormat.JPEG, 80 , blob);
                json.put("img", Base64.encodeToString(blob.toByteArray(), Base64.NO_WRAP));
            }
        } catch (JSONException e) {
            throw new USpreadItException(USpreadItException.Type.OTHERS, e.getMessage(), e);
        }
        return json;
    }

    /**
     * Transforme l'objet en JSON<br>
     * Ce limite aux attibuts qui sont destiné au cache des messages
     *
     * @return les propriété de l'objet en JSON
     */
    public JSONObject toJSONMessageCache() throws USpreadItException {
        final JSONObject json = new JSONObject();
        try {
            json.put("messageType", getMessageType().name());
            json.put("id", getId());
            json.put("nbSpread", getNbSpread());
            if (getDateCreation() != null) {
                json.put("created", DATE_FORMAT.format(getDateCreation()));
            }
            if (getDateReception() != null) {
                json.put("received", DATE_FORMAT.format(getDateReception()));
            }
            if (getDateSpread() != null) {
                json.put("spread", DATE_FORMAT.format(getDateSpread()));
            }
            json.put("txt", getText());
            json.put("txtColor", getTextColor().replace("#", ""));
            json.put("bgType", getBackgroundType().name());
            if (getBackgroundType() == BackgroundType.PLAIN) {
                json.put("bgColor", getBackgroundColor().replace("#", ""));
            }
        } catch (JSONException e) {
            throw new USpreadItException(USpreadItException.Type.OTHERS, e.getMessage(), e);
        }
        return json;
    }

    /**
     * Transforme l'objet en JSON.<br>
     * Ce limite aux attibuts qui sont destiné au cache des images
     *
     * @return les propriété de l'objet en JSON
     */
    public JSONObject toJSONImageCache() throws USpreadItException {
        final JSONObject json = new JSONObject();
        try {
            json.put("id", getId());
            if (getBackgroundType() == BackgroundType.IMAGE) {
                ByteArrayOutputStream blob = new ByteArrayOutputStream();
                backgroundImage.compress(Bitmap.CompressFormat.JPEG, 80, blob);
                json.put("img", Base64.encodeToString(blob.toByteArray(), Base64.NO_WRAP));
            }
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
        dest.writeString(messageType.name());
        dest.writeLong(id);
        dest.writeLong(nbSpread);
        dest.writeString(dateCreation != null ? DATE_FORMAT.format(dateCreation) : "NO_DATE");
        dest.writeString(dateReception != null ? DATE_FORMAT.format(dateReception) : "NO_DATE");
        dest.writeString(dateSpread != null ? DATE_FORMAT.format(dateSpread) : "NO_DATE");
        dest.writeString(text);
        dest.writeString(textColor);
        dest.writeString(backgroundType.name());
        if (backgroundType == BackgroundType.PLAIN) {
            dest.writeString(backgroundColor);
        } else if (backgroundType == BackgroundType.IMAGE) {
            if (backgroundImage == null) {
                dest.writeString("NO_IMAGE_LOADED");
            } else {
                dest.writeString("IMAGE_DIRECTLY_ATTACHED");
                backgroundImage.writeToParcel(dest, 0);
            }
        }
        dest.writeList(listLink);
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
        if (obj instanceof Message) {
            Message other = (Message) obj;
            // return Objects.equals(this.id, other.id);
            return this.id == other.id;
        }
        return false;
    }
}
