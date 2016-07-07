package it.uspread.android.activity.misc;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;

import it.uspread.android.USpreadItApplication;
import it.uspread.android.message.MessageViewEditable;

/**
 * Utilitaire de modifications de l'image
 */
public class ImageModifier {

    /** Taille maximale de l'image */
    private static final int IMAGE_SIZE_MAX = 512;

    /** Outillage de modification de l'image */
    private RenderScript rs;

    /** Tache de traitement de l'image */
    private TaskImageModifier taskImageModifier;

    /**
     * Tache de modification de l'image
     */
    public class TaskImageModifier extends AsyncTask<Object, Void, Bitmap> {

        final MessageViewEditable messageViewEditable;

        public TaskImageModifier(final MessageViewEditable messageViewEditable) {
            this.messageViewEditable = messageViewEditable;
        }

        @Override
        protected Bitmap doInBackground(final Object... params) {
            final Bitmap imageSrc = (Bitmap) params[0];
            final float blurRadius = (float) params[1];
            final int brightnessValue = (int) params[2];

            // Copier l'image source
            Bitmap clonedImage = imageSrc.copy(imageSrc.getConfig(), true);

            // Si un effet de flou est demandé
            if (blurRadius > 0) {
                applyBlur(clonedImage, blurRadius);
            }

            // Si un effet de luminosité est demandé
            if (brightnessValue != 0) {
                applyBrightness(clonedImage, brightnessValue);
            }
            return clonedImage;
        }

        @Override
        protected void onPostExecute(final Bitmap bitmap) {
            messageViewEditable.getMessage().setBackgroundImage(bitmap);
            messageViewEditable.setBackgroundDrawable(new BitmapDrawable(USpreadItApplication.getInstance().getResources(), bitmap));
        }
    }

    /**
     * Lance la réalisation d'un effet dur l'image
     *
     * @param messageViewEditable
     *         le composant du message
     * @param blurRadius
     *         le niveau de flou à appliquer
     * @param brightnessValue
     *         la valeur de luminosité à appliquer
     */
    public void applyEffectOnImage(final MessageViewEditable messageViewEditable, final float blurRadius, final int brightnessValue) {
        cancelProcessingEffect();
        taskImageModifier = new TaskImageModifier(messageViewEditable);
        taskImageModifier.execute(messageViewEditable.getImageSrc(), blurRadius, brightnessValue);
    }

    /**
     * Annule l'effet en cours
     */
    public void cancelProcessingEffect() {
        if (taskImageModifier != null && !taskImageModifier.isCancelled()) {
            taskImageModifier.cancel(true);
        }
    }

    /**
     * Crée une image en découpant et redimensionnant pour satisfaire aux proportion et taille requise
     *
     * @param image
     *         image
     * @return Une nouvelle image
     */
    public Bitmap cropImage(final Bitmap image) {
        // On adapte l'image au format carré et avec la taille maximale autorisé
        int imgSize = Math.min(image.getWidth(), image.getHeight());
        Bitmap croppedImage = Bitmap.createBitmap(image, (image.getWidth() - imgSize) / 2, ((image.getHeight() - imgSize) / 2), imgSize, imgSize);
        if (imgSize > IMAGE_SIZE_MAX) {
            croppedImage = Bitmap.createScaledBitmap(croppedImage, IMAGE_SIZE_MAX, IMAGE_SIZE_MAX, true);
        }
        return croppedImage;
    }

    /**
     * Applique un niveau de flou à l'image
     *
     * @param image
     *         l'image à modifier
     * @param blurRadius
     *         le niveau de flou à appliquer
     */
    public void applyBlur(final Bitmap image, final float blurRadius) {
        if (rs == null) {
            rs = RenderScript.create(USpreadItApplication.getInstance());
        }
        final Allocation input = Allocation.createFromBitmap(rs, image);
        final Allocation output = Allocation.createTyped(rs, input.getType());
        final ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        script.setRadius(blurRadius);
        script.setInput(input);
        script.forEach(output);
        output.copyTo(image);
    }

    /**
     * Applique un changement de luminosité à l'image
     *
     * @param image
     *         l'image à modifier
     * @param brightnessValue
     *         la valeur de luminosité à appliquer
     */
    public void applyBrightness(final Bitmap image, final int brightnessValue) {
        // image size
        int width = image.getWidth();
        int height = image.getHeight();
        // color information
        int A, R, G, B;
        int pixel;

        // scan through all pixels
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                // get pixel color
                pixel = image.getPixel(x, y);
                A = Color.alpha(pixel);
                R = Color.red(pixel);
                G = Color.green(pixel);
                B = Color.blue(pixel);

                // increase/decrease each channel
                R += brightnessValue;
                if (R > 255) {
                    R = 255;
                } else if (R < 0) {
                    R = 0;
                }

                G += brightnessValue;
                if (G > 255) {
                    G = 255;
                } else if (G < 0) {
                    G = 0;
                }

                B += brightnessValue;
                if (B > 255) {
                    B = 255;
                } else if (B < 0) {
                    B = 0;
                }

                // apply new pixel color to output bitmap
                image.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }
    }
}
