package palikka.utilities.exercise;

import java.io.File;
import palikka.utilities.http.FileUploaderAsync;
import palikka.utilities.http.IUploadListener;
import java.io.IOException;
import palikka.data.Exercise;
import palikka.settings.PluginSettings;
import palikka.settings.Settings;
import palikka.utilities.FolderHelper;
import palikka.utilities.zip.Zipper;

/**
 * This class is used to send a single exercise to the server for review.
 * @author knordman
 */
public class ExerciseUploader implements IUploadListener {

    private FileUploaderAsync uploader;
    private IExerciseUploadListener listener;
    private Exercise exercise;

    /**
     * Constructor
     * @param exercise The exercise we want to send
     * @param uploadListener Interface that listens to this ExerciseUploader
     * @throws NullPointerException When fails to create an uploader
     */
    public ExerciseUploader(Exercise exercise, IExerciseUploadListener uploadListener) throws NullPointerException {

        this.uploader = new FileUploaderAsync(exercise.getReturnAddress(), this);
        this.uploader.setTimeout(PluginSettings.getSettings().getExerciseUploadTimeout());
        this.listener = uploadListener;
        this.exercise = exercise;

    }

    /**
     * The method sends whatever exercise was given at the time the object
     * was contructed.
     */
    public void sendExercise() {
        Settings settings = PluginSettings.getSettings();

        if (!settings.isValid()) {
            listener.ExerciseUploadFailed("Student id not set. Check preferences before sending.");
            return;
        }

        try {
            File path = FolderHelper.searchSrcFolder(exercise);

            if (path == null) {
                listener.ExerciseUploadFailed("Couldn't locate source folder. Unable to send exercise");
                return;
            }

            Zipper zipper = new Zipper();
            byte[] fileContent = zipper.zip(path);

            uploader.AddFile(fileContent, "exercise.zip", "exercise_return[tmp_file]");
            uploader.AddStringKeyValuePart("exercise_return[student_id]", settings.getStudentID());


            uploader.send("Sending exercise " + exercise.getName());
        } catch (IOException ioex) {  //Return the occurring IOException instead
            listener.ExerciseUploadFailed(ioex.getMessage());
        }
    }

    /**
     * Called when an upload is complete
     * @param source 
     */
    @Override
    public void uploadCompleted(FileUploaderAsync source) {
        listener.ExerciseUploadCompleted(this.exercise, source.getResponse());
    }

    /**
     * Called by the uploader when an upload fails
     * @param source 
     */
    @Override
    public void uploadFailed(FileUploaderAsync source) {
        listener.ExerciseUploadFailed(this.uploader.getErrorMsg());
    }
}