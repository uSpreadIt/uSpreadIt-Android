package it.uspread.android.message;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.TextView;

import it.uspread.android.R;
import it.uspread.android.USpreadItApplication;
import it.uspread.android.data.Message;
import it.uspread.android.data.type.BackgroundType;

/**
 * Vue permettant la visualisation d'un message.<br/>
 * Un message affiche un texte dans un cadre de forme carré avec une couleur ou une image de fond spécifié
 *
 * @author Lone Décosterd,
 */
public class MessageView extends TextView {

    /** Indique si le message est visualisé dans une liste */
    private boolean viewedInList = false;

    /** Id du message représenté par cette vue */
    private long messageId;

    public MessageView(final Context context) {
        super(context);
        init();
    }

    public MessageView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MessageView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * Initialisation du composant
     */
    public void init() {
        setSingleLine(false);
        setMinLines(1);
        setMaxLines(MessageRender.MAX_LINE);
        final int dim = (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, MessageRender.PADDING_DIP, getResources().getDisplayMetrics()) / TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, MessageRender.TEXT_SIZE_RATIO_DIP, getResources().getDisplayMetrics()));
        setPadding(dim, dim, dim, dim);
        setGravity(Gravity.CENTER);
    }

    /**
     * @return {@link #messageId}
     */
    public long getMessageId() {
        return messageId;
    }

    /**
     * @param viewedInList
     *         {@link #viewedInList}
     */
    public void setViewedInList(final boolean viewedInList) {
        this.viewedInList = viewedInList;
    }

    /**
     * @param message Le message à afficher
     */
    public void setMessage(final Message message) {
        messageId = message.getId();
        setTextColor(Color.parseColor(message.getTextColor()));
        setText(message.getText());
        if (message.getBackgroundType() == BackgroundType.IMAGE) {
            if (message.getBackgroundImage() != null) {
                setBackgroundDrawable(new BitmapDrawable(getResources(), message.getBackgroundImage()));
            } else {
                // Récupérer l'image en chache RAM si présente
                Bitmap image = USpreadItApplication.getInstance().getImageCache().getImageFromCacheRAM(message.getId());
                // Sinon demander de la charger depuis le cache SD ou le Web
                if (image == null) {
                    USpreadItApplication.getInstance().getImageCache().loadImageFromCacheSDorWeb(message.getId(), this);
                }

                // Si pas déjà en mémoire on applique un fond spécial indiquant que l'image sera chargé
                if (image == null) {
                    setBackgroundDrawable(getResources().getDrawable(R.drawable.image_notfound));
                }
                // Sinon on peut sans attendre l'afficher
                else {
                    setBackgroundDrawable(new BitmapDrawable(getResources(), image));
                }
            }
        } else {
            setBackgroundColor(Color.parseColor(message.getBackgroundColor()));
        }

    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        final int widthAvailable = MeasureSpec.getSize(widthMeasureSpec);
        final int heightAvailable = MeasureSpec.getSize(heightMeasureSpec);

        // Le composant doit être carré et adapter sa taille suivant le ratio de l'écran
        final int size = MessageRender.calculateSquareSize(widthAvailable, heightAvailable, viewedInList, getContext());

        final int finalMeasureSpec = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);
        super.onMeasure(finalMeasureSpec, finalMeasureSpec);

        // Adapter la taille du texte en fonction de la taille de la zone.
        setTextSize(size / TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, MessageRender.TEXT_SIZE_RATIO_DIP, getResources().getDisplayMetrics()));
    }
}