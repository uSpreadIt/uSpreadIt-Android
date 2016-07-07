package it.uspread.android.message;

import android.content.Context;
import android.util.Log;
import android.util.TypedValue;

/**
 * Propriété de rendu d'un message.
 *
 * @author Lone Décosterd,
 */
public class MessageRender {

    /** Le nombre de lignes maximales */
    public static final int MAX_LINE = 12;

    /** Le nombre de caractère maximale */
    public static final int MAX_LENGTH = 240;

    /** L'écart entre les bords du cadre et le texte */
    public static final int PADDING_DIP = 200;

    /** Le ratio permettant de définir une taille de police et de bordure dépendant de la taille du carré */
    public static final int TEXT_SIZE_RATIO_DIP = 20;

    /**
     * Calcule la taille approprié pour que le message soit rendu dans un cadre carré au coueur de la zone disponible qui peut avoir des ratio très divers.
     *
     * @param widthAvailable
     *         Largeur maximale disponible pour l'affichage du message
     * @param heightAvailable
     *         Hauteur maximale disponible pour l'affichage du message
     * @param displayMessageInList
     *         Indique si le message est affiché dans une liste de message
     * @param context
     *         Contexte
     * @return taille du carré
     */
    public static int calculateSquareSize(final int widthAvailable, final int heightAvailable, final boolean displayMessageInList, final Context context) {
        // Le composant doit être carré et adapter sa taille suivant le ratio de l'écran
        final int size;
        // Les premiers cas survienne quand une des taille du layout n'est pas connu (N'est pas censé être emprunté)
        if (widthAvailable <= 0 && heightAvailable <= 0) {
            size = 600; // Taille par défaut si rien n'est spécifié
            Log.e("MESSAGE_RENDU", "Des informations manquent pour un bon rendu du message (width et height demandé par le layout)");
        } else if (widthAvailable <= 0) {
            size = heightAvailable;
        } else if (heightAvailable <= 0) {
            size = widthAvailable;
        }
        // Cas généralement attendu
        else {
            // Si le message est affiché au sein d'une liste de message on s'arrange pour qu'il reste de la place pour voir un bout du message suivant
            // (Algo nécessaire surtout pour le mode paysage autrement le ratio habituel fait que la place suffit)
            if (displayMessageInList) {
                final int minus = 12;
                if (widthAvailable < heightAvailable && (widthAvailable / (float) heightAvailable) >= 0.75) {
                    size = widthAvailable - (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, minus, context.getResources().getDisplayMetrics());
                } else if (widthAvailable > heightAvailable && (widthAvailable / (float) heightAvailable) > 1.3) {
                    size = heightAvailable - (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, minus, context.getResources().getDisplayMetrics());
                } else {
                    size = widthAvailable <= heightAvailable ? widthAvailable : heightAvailable;
                }
            } else {
                size = widthAvailable <= heightAvailable ? widthAvailable : heightAvailable;
            }
        }

        return size;
    }
}
