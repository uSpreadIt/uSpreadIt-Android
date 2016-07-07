package it.uspread.android.activity.message.create;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;

import it.uspread.android.R;

/**
 * Bouton de switch entre les modes d'édition du fond du message.
 */
public class BackgroundToggleButton extends ImageButton {

    /**
     * Etats du bouton
     */
    public enum State {
        WALL, IMAGE
    }

    /**
     * Ecouteur d'évenement de toggle
     */
    public interface OnToggleListener {
        void onSwitchMessageEdition();

        void onSwitchImageEdition();
    }

    /** Etat courant du bouton */
    private State state;

    /** Ecouteur d'évenement de toggle */
    private OnToggleListener onToggleListener;

    public BackgroundToggleButton(final Context context) {
        super(context);
        init();
    }

    public BackgroundToggleButton(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BackgroundToggleButton(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * @return {@link #state}
     */
    public State getState() {
        return state;
    }

    /**
     * @param state
     *         {@link #state}
     */
    public void setState(State state) {
        this.state = state;
        createDrawableState();

    }

    /**
     * @return {@link #onToggleListener}
     */
    public OnToggleListener getOnToggleListener() {
        return onToggleListener;
    }

    /**
     * @param onToggleListener
     *         {@link #onToggleListener}
     */
    public void setOnToggleListener(OnToggleListener onToggleListener) {
        this.onToggleListener = onToggleListener;
    }

    /**
     * Initialisation du composant
     */
    public void init() {
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                int next = ((state.ordinal() + 1) % State.values().length);
                setState(State.values()[next]);
                switch (state) {
                    case WALL:
                        onToggleListener.onSwitchMessageEdition();
                        break;
                    case IMAGE:
                        onToggleListener.onSwitchImageEdition();
                        break;
                }
            }
        });
        setState(State.WALL);
    }

    private void createDrawableState() {
        switch (state) {
            case IMAGE:
                setImageResource(R.drawable.action_switchbackground_image);
                break;
            case WALL:
                setImageResource(R.drawable.action_switchbackground_wall);
                break;
        }
    }

}