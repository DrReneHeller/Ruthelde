package com.ruthelde.DataFileReader;

import java.io.File;
import javax.swing.filechooser.*;

public class CFileFilter extends FileFilter {

    String description = "";
    String fileExt = "";

    public CFileFilter(String extension) {
        fileExt = extension;
    }

    public String getFileExt(){
        return fileExt;
    }

    public CFileFilter(String extension, String typeDescription) {
        fileExt = extension;
        this.description = typeDescription;
    }

    @Override
    public boolean accept(File f) {
        if (f.isDirectory())
            return true;
        return (f.getName().toLowerCase().endsWith(fileExt));
    }

    @Override
    public String getDescription() {
        return description;
    }
}
