package it.uspread.android.activity.misc;

/**
 * Callback pour indiquer l'apparition et la disparaition du clavier virtuel
 */
public interface OnSoftKeyboardVisibilityListener {
    /**
     * Indique que le clavier virtuel apparait
     */
    void onSoftKeyboardShow();

    /**
     * Indique que le clavier virtuel est masqué
     */
    void onSoftKeyboardHide();
}
