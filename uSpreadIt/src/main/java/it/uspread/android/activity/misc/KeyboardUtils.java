package it.uspread.android.activity.misc;

import android.app.Activity;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * Classes utilitaires pour le clavier
 *
 * @author Lone Décosterd,
 */
public class KeyboardUtils {

    /**
     * Masque immédiatement le clavier.
     *
     * @param context
     *         contexte
     * @param view
     *         view
     */
    public static void hideSoftKeyboard(final Context context, final View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * Enregistre sur toute la hiérarchie de la vue qui sont pas des EditText un événement premettant de cacher le clavier s'il est actif
     *
     * @param context
     *         contexte
     * @param view
     *         view
     * @param onSoftKeyboardVisibilityListener
     *         callback pour le hide du clavier virtuel. Peut être null
     */
    public static void registerHideSoftKeyBoard(final Context context, final View view, final OnSoftKeyboardVisibilityListener onSoftKeyboardVisibilityListener) {
        //Pour les éléments qui ne nécessite pas de saisie cacher le clavier suite à un simple touché
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(final View v, final MotionEvent event) {
                    hideSoftKeyboard(context, view);
                    if (onSoftKeyboardVisibilityListener != null) {
                        onSoftKeyboardVisibilityListener.onSoftKeyboardHide();
                    }
                    return false;
                }
            });
        }

        //Récursion si conteneur de layout
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                registerHideSoftKeyBoard(context, innerView, onSoftKeyboardVisibilityListener);
            }
        }
    }
}
