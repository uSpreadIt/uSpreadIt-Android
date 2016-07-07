package it.uspread.android.message;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import it.uspread.android.activity.misc.ImageModifier;
import it.uspread.android.activity.misc.OnSoftKeyboardVisibilityListener;
import it.uspread.android.data.Message;
import it.uspread.android.data.type.BackgroundColor;
import it.uspread.android.data.type.BackgroundType;

/**
 * Vue représentant l'édition d'un message.<br/>
 * Un message affiche un texte dans un cadre de forme carré avec une couleur ou une image de fond spécifié
 *
 * @author Lone Décosterd,
 */
public class MessageViewEditable extends EditText {

    /** Le message : modèle du composant */
    private Message message;

    /** Callback sur les début/fin d'édition (A relier au fait que le clavier soit visible ou pas pour déduire ces états de début et fin d'édition) */
    private OnSoftKeyboardVisibilityListener onSoftKeyboardVisibilityListener;

    /** Image source */
    private Bitmap imageSrc;

    /** Valeur de flou a appliquer */
    private int blurRadius = 0;

    /** Valeur de luminosité a appliquer */
    private int brightnessValue = 0;

    /** Utilitaire de modification de l'image */
    private ImageModifier imageModifier;

    public MessageViewEditable(final Context context) {
        super(context);
        init(context);
    }

    public MessageViewEditable(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MessageViewEditable(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    /**
     * @param onSoftKeyboardVisibilityListener
     *         {@link #onSoftKeyboardVisibilityListener}
     */
    public void setOnSoftKeyboardVisibilityListener(final OnSoftKeyboardVisibilityListener onSoftKeyboardVisibilityListener) {
        this.onSoftKeyboardVisibilityListener = onSoftKeyboardVisibilityListener;
    }

    /**
     * Initialisation du composant
     */
    public void init(final Context context) {
        setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        final int dim = (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, MessageRender.PADDING_DIP, getResources().getDisplayMetrics()) / TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, MessageRender.TEXT_SIZE_RATIO_DIP, getResources().getDisplayMetrics()));
        setPadding(dim, dim, dim, dim);
        setGravity(Gravity.CENTER);

        // Limite du nombre de caractères
        setFilters(new InputFilter[]{new InputFilter.LengthFilter(MessageRender.MAX_LENGTH)});

        // Mise en place de la limite du nombre de ligne
        addTextChangedListener(new TextWatcher() {
            private String text;
            private int beforeCursorPosition;

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                text = s.toString();
                beforeCursorPosition = start;
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            public void afterTextChanged(final Editable s) {
                if (getLineCount() > MessageRender.MAX_LINE) {
                    removeTextChangedListener(this);
                    setText(text);
                    setSelection(beforeCursorPosition);
                    addTextChangedListener(this);
                }
                if (message != null) {
                    message.setText(getText().toString());
                }
            }
        });

        // Désactiver la séléction/copy
        setCustomSelectionActionModeCallback(new ActionMode.Callback() {
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            public void onDestroyActionMode(ActionMode mode) {
            }

            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                return false;
            }
        });

        imageModifier = new ImageModifier();
    }

    @Override
    protected void onFocusChanged(final boolean focused, final int direction, final Rect previouslyFocusedRect) {
        if (focused) {
            if (onSoftKeyboardVisibilityListener != null) {
                // Qaund le composant prend le focus le clavier est selon toute logique affiché
                onSoftKeyboardVisibilityListener.onSoftKeyboardShow();
            }
        }
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
    }

    @Override
    public boolean onKeyPreIme(final int keyCode, final KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (onSoftKeyboardVisibilityListener != null) {
                // Si la touche de masquage du clavier est utilisé
                onSoftKeyboardVisibilityListener.onSoftKeyboardHide();
            }
        }
        return super.onKeyPreIme(keyCode, event);
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        final int widthAvailable = MeasureSpec.getSize(widthMeasureSpec);
        final int heightAvailable = MeasureSpec.getSize(heightMeasureSpec);

        // Le composant doit être carré et adapter sa taille suivant le ratio de l'écran
        final int size = MessageRender.calculateSquareSize(widthAvailable, heightAvailable, false, getContext());

        final int finalMeasureSpec = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);
        super.onMeasure(finalMeasureSpec, finalMeasureSpec);

        // Adapter la taille du texte en fonction de la taille de la zone.
        setTextSize(size / TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, MessageRender.TEXT_SIZE_RATIO_DIP, getResources().getDisplayMetrics()));
    }

    /**
     * @return {@link #message}
     */
    public Message getMessage() {
        return message;
    }

    /**
     * @param message
     *         {@link #message}
     */
    public void setMessage(final Message message) {
        this.message = message;
        setTextColor(Color.parseColor(message.getTextColor()));
        setText(message.getText());
        if (message.getBackgroundType() == BackgroundType.IMAGE) {
            setBackgroundDrawable(new BitmapDrawable(getResources(), message.getBackgroundImage()));
        } else {
            setBackgroundColor(Color.parseColor(message.getBackgroundColor()));
        }
    }

    /**
     * @return {@link #imageSrc}
     */
    public Bitmap getImageSrc() {
        return imageSrc;
    }

    /**
     * @param imageSrc
     *         {@link #imageSrc}
     */
    public void setImageSrc(final Bitmap imageSrc) {
        this.imageSrc = imageSrc;
    }

    /**
     * @return {@link #blurRadius}
     */
    public int getBlurRadius() {
        return blurRadius;
    }

    /**
     * @param blurRadius
     *         {@link #blurRadius}
     */
    public void setBlurRadius(final int blurRadius) {
        this.blurRadius = blurRadius;
    }

    /**
     * @return {@link #brightnessValue}
     */
    public int getBrightnessValue() {
        return brightnessValue;
    }

    /**
     * @param brightnessValue
     *         {@link #brightnessValue}
     */
    public void setBrightnessValue(final int brightnessValue) {
        this.brightnessValue = brightnessValue;
    }

    /**
     * Change la couleur de fond
     *
     * @param htmlColor
     *         couleur au format html
     */
    public void setBackgroundColor(final String htmlColor) {
        message.setBackgroundColor(htmlColor);
        setBackgroundColor(Color.parseColor(message.getBackgroundColor()));
    }

    /**
     * Change la couleur de texte
     *
     * @param htmlColor
     *         couleur au format html
     */
    public void setTextColor(final String htmlColor) {
        message.setTextColor(htmlColor);
        setTextColor(Color.parseColor(message.getTextColor()));
    }

    /**
     * Change l'image de fond
     *
     * @param image
     *         image
     */
    public void setBackground(final Bitmap image) {
        imageSrc = imageModifier.cropImage(image);
        message.setBackgroundType(BackgroundType.IMAGE);
        message.setBackgroundImage(imageSrc);
        checkValidity(message);
        setBackgroundDrawable(new BitmapDrawable(getResources(), imageSrc));
    }

    /**
     * Applique les effet demandé sur l'image.<br>
     * <ul>
     * <li>Effet de flou</li>
     * <li>Effet de luminosité</li>
     * </ul>
     */
    private void applyEffect() {
        // Copier l'image source si des effets sont a appliquer
        final boolean doEffect = blurRadius > 0 || brightnessValue != 0;

        if (doEffect) {
            imageModifier.applyEffectOnImage(this, blurRadius, brightnessValue);
        } else {
            imageModifier.cancelProcessingEffect();
            message.setBackgroundImage(imageSrc);
            setBackgroundDrawable(new BitmapDrawable(getResources(), imageSrc));
        }
    }

    /**
     * Retirer l'image de fond
     */
    public void clearImage() {
        imageSrc = null;
        blurRadius = 0;
        brightnessValue = 0;
        message.setBackgroundType(BackgroundType.PLAIN);
        checkValidity(message);
        setBackgroundColor(message.getBackgroundColor());
    }

    /**
     * Traite la cohérance des valeurs de certains attributs
     */
    public void checkValidity(final Message message) {
        // Pas d'image pour un background de type PLAIN
        if (message.getBackgroundType() == BackgroundType.PLAIN) {
            message.setBackgroundImage(null);
            // Si passage changement de type de background alors réinit la couleur de fond (indispensable si on vient du type IMAGE)
            if (message.getBackgroundColor() == null) {
                message.setBackgroundColor(BackgroundColor.YELLOW.getHtmlColor());
            }
        }
        // Pas de couleur de fond pour un background de type IMAGE
        else if (message.getBackgroundType() == BackgroundType.IMAGE) {
            message.setBackgroundColor(null);
        }
    }

    /**
     * Ajoute plus de flou à l'image
     */
    public void moreBlurOnImage() {
        if (imageSrc != null) {
            blurRadius++;
            if (blurRadius > 25) {
                blurRadius = 25;
            }
            applyEffect();
        }
    }

    /**
     * Diminue le flou de l'image
     */
    public void lessBlurOnImage() {
        if (imageSrc != null) {
            blurRadius--;
            if (blurRadius < 0) {
                blurRadius = 0;
            }
            applyEffect();
        }
    }

    /**
     * Ajoute plus de luminosité à l'image
     */
    public void moreBrightnessOnImage() {
        if (imageSrc != null) {
            brightnessValue += 10;
            if (brightnessValue > 200) {
                brightnessValue = 200;
            }
            applyEffect();
        }
    }

    /**
     * Diminue la luminosité de l'image
     */
    public void lessBrightnessOnImage() {
        if (imageSrc != null) {
            brightnessValue -= 10;
            if (brightnessValue < -200) {
                brightnessValue = -200;
            }
            applyEffect();
        }
    }
}