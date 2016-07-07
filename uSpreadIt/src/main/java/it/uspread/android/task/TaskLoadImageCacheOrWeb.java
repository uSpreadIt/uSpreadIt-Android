package it.uspread.android.task;

import android.graphics.Bitmap;
import android.util.Log;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import it.uspread.android.BuildConfig;
import it.uspread.android.USpreadItApplication;
import it.uspread.android.message.MessageView;
import it.uspread.android.remote.USpreadItServer;
import it.uspread.android.remote.exception.USpreadItException;

/**
 * Tache de chargement de l'image depuis le cache ou à défaut le serveur
 */
public class TaskLoadImageCacheOrWeb extends Task<Void, Void, Bitmap> {

    /** Code de retour de la tâche */
    public static final int RESULT_CODE = Task.provideResultCode();

    /** Liste des tâches en cours d'éxécution pour une vue */
    private static final Set<TaskLoadImageCacheOrWeb> listRunningImageTask = Collections.synchronizedSet(new HashSet<TaskLoadImageCacheOrWeb>());

    /** Id du message */
    private long messsageId;

    /** Composant d'affichage du message */
    private MessageView messageView;

    /** Indique si l'image trouvé est issu du cache SD */
    private boolean imageComeFromCache;

    /**
     * Constructeur.
     *
     * @param messsageId
     *         {@link #messsageId}
     * @param messageView
     *         {@link #messageView}
     */
    public TaskLoadImageCacheOrWeb(final long messsageId, final MessageView messageView) {
        super(RESULT_CODE, "image" + TASK_IDPART_SEPARATOR + messsageId);
        this.messsageId = messsageId;
        this.messageView = messageView;
    }

    public MessageView getMessageView() {
        return messageView;
    }

    /**
     * Vérifie que la tâche peut s'éxécuter
     *
     * @return Vrai si la vue est toujours destiné a afficher le même message (Dans les adapter une vue est réutilisé et non reconstruite)
     */
    private synchronized boolean checkTaskCanExecute() {
        synchronized (listRunningImageTask) {
            for (TaskLoadImageCacheOrWeb task : listRunningImageTask) {
                if (task.getMessageView().getMessageId() != messsageId) {
                    return false;
                }
            }
        }
        return true;
        // Le test d'unicité du lancement de la tache en fonction de l'id du message est déjà géré par la superclasse
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (!checkTaskCanExecute()) {
            cancel(false);
        } else {
            listRunningImageTask.add(this);
        }
    }

    @Override
    protected void onPostExecute(final Bitmap image) {
        super.onPostExecute(image);
        listRunningImageTask.remove(this);
    }

    @Override
    protected void onCancelled(final Bitmap image) {
        super.onCancelled(image);
        listRunningImageTask.remove(this);
    }

    @Override
    protected Bitmap executeTask(final Void... params) throws USpreadItException {
        Bitmap image = USpreadItApplication.getInstance().getImageCache().getImageFromCacheSD(messsageId);

        imageComeFromCache = true;
        // Si l'image n'est pas trouvé dans le cache SD : la redemander au serveur
        if (image == null) {
            if (BuildConfig.DEBUG) {
                Log.d(getClass().getName(), "Image load from Web " + messsageId);
            }
            image = USpreadItServer.getInstance().getMessageImage(messsageId).getBackgroundImage();
            imageComeFromCache = false;
        }
        return image;
    }

    @Override
    protected void onFinalizeTask(final Bitmap image) {
        // Ajouter l'image au cache
        USpreadItApplication.getInstance().getImageCache().addImageToCache(messsageId, image, imageComeFromCache);
    }
}
