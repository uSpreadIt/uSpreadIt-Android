package it.uspread.android.data.criteria;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import it.uspread.android.remote.utils.HttpUtils;

/**
 * Classe encapsulant les critère de recherche pour un {@link it.uspread.android.data.User}
 *
 * @author Lone Décosterd,
 */
public class UserCriteria implements Criteria {


    /** Indique le token d'enregistrement au service GCM de cet appareil à réserver par l'user */
    private String gcmRegistrationId;

    /**
     * Constructeur
     *
     * @param gcmRegistrationId
     *         {@link #gcmRegistrationId}
     */
    public UserCriteria(final String gcmRegistrationId) {
        this.gcmRegistrationId = gcmRegistrationId;
    }

    /**
     * @return {@link #gcmRegistrationId}
     */
    public String getGcmRegistrationId() {
        return gcmRegistrationId;
    }

    /**
     * @param gcmRegistrationId
     *         {@link #gcmRegistrationId}
     */
    public void setGcmRegistrationId(final String gcmRegistrationId) {
        this.gcmRegistrationId = gcmRegistrationId;
    }

    @Override
    public boolean isEmpty() {
        return gcmRegistrationId == null;
    }

    @Override
    public String addCriteriaToUrl(final String url) {
        if (isEmpty()) {
            return url;
        }
        final StringBuilder str = new StringBuilder(20);

        try {
            // Ajout de la déclaration de paramètre si pas déjà présent
            if (!url.contains("?")) {
                str.append("?");
            } else if (!"?".equals(url.substring(url.length() - 2))) {
                str.append("&");
            }

            if (gcmRegistrationId != null) {
                str.append("pToken=").append(URLEncoder.encode(gcmRegistrationId, HttpUtils.UTF8));
                str.append("&");
                str.append("device=ANDROID");
                str.append("&");
            }
        } catch (UnsupportedEncodingException e) {
            Log.e("UserCriteria", e.getMessage());
        }

        // On enlève le & en trop
        if (!isEmpty()) {
            str.setLength(str.length() - 1);
        }
        return url + str.toString();
    }
}
