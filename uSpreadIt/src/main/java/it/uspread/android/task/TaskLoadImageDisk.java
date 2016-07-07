package it.uspread.android.task;

import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.IOException;

import it.uspread.android.USpreadItApplication;
import it.uspread.android.remote.exception.USpreadItException;

/**
 * Tache de chargement d'une image du disque
 */
public class TaskLoadImageDisk extends Task<Uri, Void, Bitmap> {

    /** Code de retour de la tâche */
    public static final int RESULT_CODE = Task.provideResultCode();

    /**
     * Constructeur.
     */
    public TaskLoadImageDisk() {
        super(RESULT_CODE, true);
    }


    @Override
    protected Bitmap executeTask(final Uri... params) throws USpreadItException {
        try {
            return MediaStore.Images.Media.getBitmap(USpreadItApplication.getInstance().getContentResolver(), params[0]);
        } catch (IOException e) {
            Log.e(getClass().getName(), "Error when reading image " + params[0].getPath());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onFinalizeTask(final Bitmap bitmap) {
        // Rien à faire hors de l'UI
    }
}
