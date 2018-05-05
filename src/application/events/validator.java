package application.events;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

/**
 *
 * @author Usuario
 */
public class validator {

    /**
     * In order to update this application must have READ,WRITE AND CREATE
     * permissions on the current folder
     */
    public static boolean checkPermissions(String path) {
        boolean respuesta = false;
        //Check for permission to Create
        File sample;
        try {
            sample = new File(Paths.get(path + File.separator + "empty123123124122354345436.txt").toString());
            /*
			 * Create and delete a dummy file in order to check file
			 * permissions. Maybe there is a safer way for this check.
             */
            sample.createNewFile();
            respuesta = sample.canRead() && sample.canWrite();
            sample.delete();
        } catch (IOException e) {
            //Error message shown to user. Operation is aborted
            respuesta = false;
        } finally {
            return respuesta;
        }
    }
}
