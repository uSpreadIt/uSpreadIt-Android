package it.uspread.android.activity.message.create;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.util.Random;

import it.uspread.android.R;
import it.uspread.android.activity.USpreadItActivity;
import it.uspread.android.activity.misc.KeyboardUtils;
import it.uspread.android.activity.misc.OnGestureListener;
import it.uspread.android.activity.misc.OnSoftKeyboardVisibilityListener;
import it.uspread.android.data.Message;
import it.uspread.android.data.Status;
import it.uspread.android.data.criteria.StatusCriteria;
import it.uspread.android.data.type.BackgroundColor;
import it.uspread.android.data.type.BackgroundType;
import it.uspread.android.data.type.MessageType;
import it.uspread.android.data.type.TextColor;
import it.uspread.android.message.MessageViewEditable;
import it.uspread.android.task.Task;
import it.uspread.android.task.TaskLoadImageDisk;
import it.uspread.android.task.TaskSendMessage;
import it.uspread.android.task.TaskUserStatus;

/**
 * Activité
 * <ul>
 * <li>Création d'un nouveau message</li>
 * </ul>
 *
 * @author Lone Décosterd,
 */
public class MessageCreationActivity extends USpreadItActivity implements View.OnClickListener, BackgroundToggleButton.OnToggleListener, OnSoftKeyboardVisibilityListener {

    /** Clé de l'objet sauvegardé dans le contexte */
    private static final String CONTEXT_MESSAGE_CREATION = "message_creation";
    /** Clé du mode d'édition du message sauvegardé dans le contexte */
    private static final String CONTEXT_MESSAGE_CREATION_MODE_MESSAGE = "message_creation_mode";
    /** Clé de l'image source sauvegardé dans le contexte */
    private static final String CONTEXT_MESSAGE_CREATION_IMAGE_SRC = "message_creation_image_src";
    /** Clé de la valeur de flou actuel sauvegardé dans le contexte */
    private static final String CONTEXT_MESSAGE_CREATION_IMAGE_BLUR = "message_creation_image_blur";
    /** Clé de la valeur de luminosité actuel sauvegardé dans le contexte */
    private static final String CONTEXT_MESSAGE_CREATION_IMAGE_BRIGHTNESS = "message_creation_image_brightness";

    /** Code de résultat d'activité de capture d'image */
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    /** Code de résultat d'activité de séléction d'image existante */
    private static final int REQUEST_IMAGE_PICK = 2;

    /** Le message en construction */
    private Message newMessage;

    /** La zone de message */
    private MessageViewEditable messageView;

    /** Le fichier ou est enregistré la photo prise */
    private Uri imageUri;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_creation);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        KeyboardUtils.registerHideSoftKeyBoard(this, findViewById(R.id.activity_message_creation), this);

        messageView = (MessageViewEditable) findViewById(R.id.message_render);

        findViewById(R.id.action_textColor).setOnClickListener(this);
        findViewById(R.id.action_backgroundColor).setOnClickListener(this);
        findViewById(R.id.action_camera).setOnClickListener(this);
        findViewById(R.id.action_pickImage).setOnClickListener(this);
        findViewById(R.id.action_deleteImage).setOnClickListener(this);
        ((BackgroundToggleButton) findViewById(R.id.action_backgroundSwitch)).setOnToggleListener(this);
        messageView.setOnSoftKeyboardVisibilityListener(this);

        boolean modeMessage = true;
        if (savedInstanceState != null) {
            newMessage = (Message) savedInstanceState.get(CONTEXT_MESSAGE_CREATION);
            modeMessage = savedInstanceState.getBoolean(CONTEXT_MESSAGE_CREATION_MODE_MESSAGE);
            messageView.setImageSrc((Bitmap) savedInstanceState.get(CONTEXT_MESSAGE_CREATION_IMAGE_SRC));
            messageView.setBlurRadius(savedInstanceState.getInt(CONTEXT_MESSAGE_CREATION_IMAGE_BLUR));
            messageView.setBrightnessValue(savedInstanceState.getInt(CONTEXT_MESSAGE_CREATION_IMAGE_BRIGHTNESS));
        } else {
            newMessage = new Message();
            newMessage.setMessageType(MessageType.WRITED);

            // Lancer la vérification du quota de messages
            checkQuota();
        }
        messageView.setMessage(newMessage);

        imageUri = Uri.fromFile(new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "messageCreationImage.jpg"));

        // Au lancement le mode d'édition "message" doit être actif
        if (modeMessage) {
            onSwitchMessageEdition();
        } else {
            onSwitchImageEdition();
        }
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(CONTEXT_MESSAGE_CREATION, newMessage);
        outState.putBoolean(CONTEXT_MESSAGE_CREATION_MODE_MESSAGE, findViewById(R.id.action_camera).getVisibility() == View.GONE);
        outState.putParcelable(CONTEXT_MESSAGE_CREATION_IMAGE_SRC, messageView.getImageSrc());
        outState.putInt(CONTEXT_MESSAGE_CREATION_IMAGE_BLUR, messageView.getBlurRadius());
        outState.putInt(CONTEXT_MESSAGE_CREATION_IMAGE_BRIGHTNESS, messageView.getBrightnessValue());
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_message_creation, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        KeyboardUtils.hideSoftKeyboard(this, findViewById(R.id.activity_message_creation));
        switch (item.getItemId()) {
            case R.id.action_send:
                actionSend();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Lorsque l'écran est (ré)affiché s'assurer d'avoir un comportement d'affichage propre au fait que le clavier virtuel n'est pas visible
        onSoftKeyboardHide();
    }

    @Override
    protected void onDestroy() {
        if (imageUri != null) {
            final File file = new File(imageUri.getPath());
            if (file.exists()) {
                file.delete();
            }
        }
        super.onDestroy();
    }

    @Override
    public void onSwitchMessageEdition() {
        findViewById(R.id.action_textColor).setVisibility(View.VISIBLE);
        findViewById(R.id.action_backgroundColor).setVisibility(newMessage.getBackgroundImage() == null ? View.VISIBLE : View.GONE);
        findViewById(R.id.action_camera).setVisibility(View.GONE);
        findViewById(R.id.action_pickImage).setVisibility(View.GONE);
        findViewById(R.id.action_deleteImage).setVisibility(View.GONE);
        messageView.setOnTouchListener(new MessageSwipeListener());
    }

    @Override
    public void onSwitchImageEdition() {
        findViewById(R.id.action_textColor).setVisibility(View.GONE);
        findViewById(R.id.action_backgroundColor).setVisibility(View.GONE);
        findViewById(R.id.action_camera).setVisibility(View.VISIBLE);
        findViewById(R.id.action_pickImage).setVisibility(View.VISIBLE);
        findViewById(R.id.action_deleteImage).setVisibility(newMessage.getBackgroundImage() != null ? View.VISIBLE : View.GONE);
        messageView.setOnTouchListener(new ImageSwipeListener());
    }

    @Override
    public void onSoftKeyboardShow() {
        // Le composant de message à reçus le focus donc on masque les boutons d'éditions du message
        findViewById(R.id.bottomBar_message_creation).setVisibility(View.GONE);
    }

    @Override
    public void onSoftKeyboardHide() {
        // On affiche les boutons d'éditions du message et on enlève le focus au composant de message afin que le focus soit de pair avec l'apparition du clavier virtuel
        findViewById(R.id.bottomBar_message_creation).setVisibility(View.VISIBLE);
        messageView.clearFocus();
    }

    /**
     * Gesture listener pour le mode mur
     */
    private class MessageSwipeListener extends OnGestureListener {

        public MessageSwipeListener() {
            super(MessageCreationActivity.this);
        }

        @Override
        public boolean onTouch(final View v, final MotionEvent event) {
            // Si le composant de message à le focus cela signifie qu'on est en cours d'édition du texte du message on ne traite ni intercepte l'événement
            if (messageView.hasFocus()) {
                return false;
            }
            // Sinon traiter et intercepter l'événement ou bien donner le focus si on fait juste un click sur le composant du message
            else {
                final boolean result = super.onTouch(v, event);
                if (!result && MotionEvent.ACTION_UP == event.getAction()) {
                    messageView.requestFocus();
                }
                return result;
            }
        }

        @Override
        public void onSwipeLeft() {
            if (newMessage.getBackgroundType() == BackgroundType.PLAIN) {
                messageView.setBackgroundColor(BackgroundColor.getNextColor(newMessage.getBackgroundColor()).getHtmlColor());
            }
        }

        @Override
        public void onSwipeRight() {
            if (newMessage.getBackgroundType() == BackgroundType.PLAIN) {
                messageView.setBackgroundColor(BackgroundColor.getPreviousColor(newMessage.getBackgroundColor()).getHtmlColor());
            }
        }

        @Override
        public void onSwipeBottom() {
            messageView.setTextColor(TextColor.getNextColor(newMessage.getTextColor()).getHtmlColor());
        }

        @Override
        public void onSwipeTop() {
            messageView.setTextColor(TextColor.getPreviousColor(newMessage.getTextColor()).getHtmlColor());
        }
    }

    /**
     * Gesture listener pour le mode image
     */
    private class ImageSwipeListener extends OnGestureListener {

        public ImageSwipeListener() {
            super(MessageCreationActivity.this);
        }

        @Override
        public boolean onTouch(final View v, final MotionEvent event) {
            // Si le composant de message à le focus cela signifie qu'on est en cours d'édition du texte du message on ne traite ni intercepte l'événement
            if (messageView.hasFocus()) {
                return false;
            }
            // Sinon traiter et intercepter l'événement ou bien donner le focus
            else {
                final boolean result = super.onTouch(v, event);
                if (!result && MotionEvent.ACTION_UP == event.getAction()) {
                    messageView.requestFocus();
                }
                return result;
            }
        }


        @Override
        public void onSwipeLeft() {
            messageView.lessBlurOnImage();
        }

        @Override
        public void onSwipeRight() {
            messageView.moreBlurOnImage();
        }

        @Override
        public void onSwipeBottom() {
            messageView.moreBrightnessOnImage();
        }

        @Override
        public void onSwipeTop() {
            messageView.lessBrightnessOnImage();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.action_textColor: {
                messageView.setTextColor(randomColor());
                break;
            }
            case R.id.action_backgroundColor: {
                if (newMessage.getBackgroundType() == BackgroundType.PLAIN) {
                    messageView.setBackgroundColor(randomColor());
                }
                break;
            }
            case R.id.action_camera: {
                dispatchTakePhotoIntent();
                break;
            }
            case R.id.action_pickImage: {
                dispatchPickImageIntent();
                break;
            }
            case R.id.action_deleteImage: {
                messageView.clearImage();
                findViewById(R.id.action_deleteImage).setVisibility(View.GONE);
                break;
            }
        }
    }

    /**
     * Permet d'obtenir une couleur aléatoire mais pas trop foncé ni trop terne
     *
     * @return Couleur au format HTML
     */
    private String randomColor() {
        final Random random = new Random(System.currentTimeMillis());
        final float hue = (random.nextFloat() * 1000) % 360;
        final float saturation = random.nextFloat() % 0.8f + 0.2f;//1.0 for brilliant, 0.0 for dull
        final float luminance = random.nextFloat() % 0.6f + 0.4f; //1.0 for brighter, 0.0 for black
        return "#" + Integer.toHexString(Color.HSVToColor(new float[]{hue, saturation, luminance})).substring(2);
    }

    /**
     * Vérification de l'autorisation de créer un message
     */
    private void checkQuota() {
        new TaskUserStatus().execute(new StatusCriteria(true));
    }

    /**
     * Action d'envoi du nouveau Message
     */
    private void actionSend() {
        new TaskSendMessage().execute(newMessage);
    }

    @Override
    public void onReceiveTaskResultUpdateUI(final Task.TaskResult taskResult) {
        if (taskResult.success) {
            if (taskResult.resultCode == TaskUserStatus.RESULT_CODE) {
                final Status status = (Status) taskResult.resultData;
                // Si le quota est atteint en informer l'utilisateur et fermer l'activité
                if (status.isQuotaReached()) {
                    Toast.makeText(this, getResources().getString(R.string.exception_quota), Toast.LENGTH_SHORT).show();
                    setResult(Activity.RESULT_CANCELED);
                    finish();
                }
            } else if (taskResult.resultCode == TaskSendMessage.RESULT_CODE) {
                Toast.makeText(this, getResources().getString(R.string.toast_send), Toast.LENGTH_SHORT).show();
                setResult(Activity.RESULT_OK);
                finish();
            } else if (taskResult.resultCode == TaskLoadImageDisk.RESULT_CODE) {
                messageView.clearImage();
                messageView.setBackground((Bitmap) taskResult.resultData);
            }
        }
    }

    /**
     * Demande au device de prendre une photo avec son application d'appareil photo
     */
    private void dispatchTakePhotoIntent() {
        final Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        if (takePhotoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePhotoIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(this, "No Photo Application available", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Demande au device de séléctionner une image
     */
    private void dispatchPickImageIntent() {
        final Intent pickImageIntent = new Intent(Intent.ACTION_PICK);
        pickImageIntent.setType("image/*");
        startActivityForResult(pickImageIntent, REQUEST_IMAGE_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                findViewById(R.id.action_deleteImage).setVisibility(View.VISIBLE);
                findViewById(R.id.action_backgroundColor).setVisibility(View.GONE);
                new TaskLoadImageDisk().execute(imageUri);
            } else if (requestCode == REQUEST_IMAGE_PICK) {
                findViewById(R.id.action_deleteImage).setVisibility(View.VISIBLE);
                findViewById(R.id.action_backgroundColor).setVisibility(View.GONE);
                final Uri selectedImage = data.getData();
                new TaskLoadImageDisk().execute(selectedImage);
            }
        }
    }
}
