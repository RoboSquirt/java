package main.adapters.view;

import model.plate.PlateModel;
import model.plate.objects.PlateSpecifications;
import model.serialization.SaveType;
import model.serialization.SerializationModel;
import model.tasks.basictasks.IExecuteTask;

import java.awt.geom.Point2D;

/**
 * Created by Christian on 6/12/2014.
 */
public class View2SerializationAdapter {

    private SerializationModel serializationModel;

    /**
     * Sets up model references necessary for this adapter.
     *
     * @param serializationModel - reference to the serialization model
     */
    public View2SerializationAdapter(SerializationModel serializationModel) {
        this.serializationModel = serializationModel;
    }
    /**
     * Tells plate model to save given plate specifications.
     *
     * @param nickname   - name to later refer to this specification as
     * @param plateSpecs - object encompassing all datasheet information
     */
    public boolean saveSpecs(String nickname, PlateSpecifications plateSpecs) {
        return serializationModel.savePlate(nickname, plateSpecs);
    }

    /**
     * Loads the previously saved PlateSpecifications with the given filename.
     *
     * @param nickname - name of the file specifications are actually saved in
     */
    public PlateSpecifications loadPlateSpecs(String nickname) {
        return serializationModel.loadPlate(nickname);
    }

    /**
     * Load workflow from a file.
     *
     * @param filename
     */
    public void loadWorkflow(String filename) {
        serializationModel.loadWorkflow(filename);
    }

    /**
     * Save workflow to a file.
     *
     * @param filename
     */
    public void saveWorkflow(String filename) {
        serializationModel.saveWorkflow(filename);
    }

    /**
     * Deletes previously saved data that matches the given filename and type.
     *
     * @param filename - name of the data to delete
     * @param type
     */
    public void deleteData(String filename, SaveType type) {
        serializationModel.deleteData(filename, type);
    }

    /**
     * Gets all data of a certain type that are saved.
     *
     * @param type
     * @return Iterable of each file name
     */
    public Iterable<String> updateDataList(SaveType type) {
        return serializationModel.updateDataList(type, false);
    }

    /**
     * Saves incoming task to the file system. Name is inferred from the task to save.
     *
     * @param task
     */
    public void saveExecutionTask(IExecuteTask task) {
        serializationModel.saveTask(task);
    }

    /**
     * Load a task that has previously been saved to a text file.
     *
     * @param filename
     * @return
     */
    public IExecuteTask loadTask(String filename) {
        return serializationModel.loadTask(filename);
    }

    /* Get the frame bounds from saved file. */
    public Point2D getSavedBounds() { return serializationModel.getSavedBounds(); }
}
