/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.oobd.base.archive;

import java.io.File;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author steffen
 */
public class Factory {

    public static ArrayList<Archive> getDirContent(String path) {
        ArrayList<Archive> res = new ArrayList<Archive>();
        File directory = new File(path);
        Archive actFile;
        if (directory.exists()) {
            File[] files = directory.listFiles();
            // For each file in dir...
            for (int i = 0; i
                    < files.length; i++) {
                actFile = null;
                // split file name into name and extension
                Logger.getLogger(Factory.class.getName()).log(Level.CONFIG, "File to be evaluated: {0}", files[i].getName());
                String name[] = files[i].getName().split("\\.");
                // only class names without $ are taken
                if (name.length > 1) {
                    actFile = getArchive(files[i].getPath());
                }
                if (actFile != null) {
                    if (actFile.bind(path + System.getProperty("file.separator") + files[i].getName())) {
                        res.add(actFile);
                    }
                }
            }
        }
        return res;
    }

    public static Archive getArchive(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            String name[] = file.getName().split("\\.");
            if (name.length > 1) {
                if (name[1].equals("lbc")) {
                    return new FileHandlerLBC();
                }
            }
        }
        return null;
    }
}
