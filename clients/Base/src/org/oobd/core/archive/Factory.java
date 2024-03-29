/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.oobd.core.archive;

import java.io.File;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.oobd.core.Core;

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
            for (int i = 0; i < files.length; i++) {
                actFile = null;
                // split file name into name and extension
                Logger.getLogger(Factory.class.getName()).log(Level.CONFIG,
                        "File to be evaluated: {0}", files[i].getName());
                String name[] = files[i].getName().split("\\.");
                // only class names without $ are taken
                if (name.length > 1) {
                    String ext = name[name.length - 1];
                    if (ext.equalsIgnoreCase("pgp")
                            || ext.equalsIgnoreCase("lbc")
                            || ext.equalsIgnoreCase("epd")
                            || ext.equalsIgnoreCase("epa")) {
                        actFile = getArchive(files[i].getPath());
                    }
                }
                if (actFile != null) {
                    if (actFile.bind(path
                            + "/"
                            + files[i].getName())) {
                        res.add(actFile);
                        Logger.getLogger(Factory.class.getName()).log(Level.INFO, "Archive found : {0} with ID {1} ", new Object[]{actFile.getFileName(), actFile.getID()});

                    }
                }
            }
        }
        return res;
    }

    public static Archive getArchive(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            file = new File(filePath + ".pgp");
        }
        if (file.exists()) {
            String name[] = file.getName().split("\\.");
            if (name.length > 1) {
                if (name[name.length - 1].equalsIgnoreCase("pgp")) {
                    return new FileHandlerPGP(Core.getSingleInstance());
                } else if (name[name.length - 1].equalsIgnoreCase("epd") || name[name.length - 1].equalsIgnoreCase("epa")) {
                    return new FileHandlerEpa(Core.getSingleInstance());
                } else {
                    return new FileHandlerPlain(Core.getSingleInstance());
                }
            } else {
                return new FileHandlerPlain(Core.getSingleInstance());
            }

        }
        return null;
    }
}
