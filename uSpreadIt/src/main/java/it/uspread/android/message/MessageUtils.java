package it.uspread.android.message;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.PopupMenu;

import java.text.DecimalFormat;
import java.util.List;
import java.util.regex.Matcher;

import it.uspread.android.R;
import it.uspread.android.data.Message;

/**
 * Outils permettant de détecter les liens hypertexte présent dans le texte d'un message
 *
 * @author Lone Décosterd,
 */
public class MessageUtils {

    /**
     * Converti le nombre de propagation dans une unité prenant peu de place. (Utilisation des unité internationale pour millier, millions et milliards et abandon du reste de la
     * division).
     *
     * @param nbSpread
     *         Le nombre de propagation
     * @return Représentation courte du nombre de propagation
     */
    public static String convertToShortNbSpread(final long nbSpread) {
        if (nbSpread < 1000) {
            return ((Long) nbSpread).toString();
        } else if (nbSpread < 1000000) {
            return (nbSpread / 1000) + "K";
        } else if (nbSpread < 1000000000) {
            return (nbSpread / 1000000) + "M";
        } else {
            return (nbSpread / 1000000000) + "G";
        }
    }

    /**
     * Affiche le nombre de propagation avec un séparateur de millier.
     *
     * @param nbSpread
     *         Le nombre de propagation
     * @return Représentation longue du nombre de propagation
     */
    public static String convertToLongNbSpread(final long nbSpread) {
        return new DecimalFormat("###,###,###,##0").format(nbSpread);
    }

    /**
     * Détecte les liens hypertexte présent dans le texte des messages. Et enrichi chaque message de ses liens.
     *
     * @param listMessage
     *         liste des message
     */
    public static void detectLinks(final List<Message> listMessage) {
        for (Message message : listMessage) {
            Matcher matcher = Patterns.WEB_URL.matcher(message.getText());
            while (matcher.find()) {
                message.getListLink().add(matcher.group());
            }
        }
    }

    /**
     * Permet d'ajouter les liens web detecté dans la popup menu et l'action lors du clic
     *
     * @param context
     *         Le contexte
     * @param popupLinks
     *         popup menu accueillant les menus
     * @param listLink
     *         Liste des liens
     */
    public static void configurePopupLinks(final Context context, final PopupMenu popupLinks, final List<String> listLink) {
        int order = 0;
        for (String link : listLink) {
            popupLinks.getMenu().add(Menu.NONE, Menu.NONE, order, context.getResources().getString(R.string.text_link) + " " + (order + 1));
            order++;
        }
        popupLinks.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(final MenuItem item) {
                String linkPressed = listLink.get(item.getOrder());
                if (!linkPressed.startsWith("http://") && !linkPressed.startsWith("https://")) {
                    linkPressed = "http://" + linkPressed; // FIXME c'est pas terrible ça. D'ailleurs réflchir à la sécurité pas qu'on puisse passer des choses qui serait pas ouverte par le navigateur
                }
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(linkPressed)));
                return true;
            }
        });
    }
}
