package com.ruthelde.Stopping;

import com.google.gson.Gson;

import javax.swing.*;
import java.io.File;
import java.io.FileReader;

public class StoppingParaFile {

    public StoppingCorrectionEntry[] data;

    public StoppingParaFile(StoppingCorrectionEntry[] data){

        this.data = data;
    }

    public static StoppingParaFile load(String fileName){

        StoppingParaFile result = null;

        if(fileName != null && fileName != "") {

            File file = new File(fileName);

            if (file.exists()) {

                Gson gson = new Gson();

                try {
                    FileReader fr = new FileReader(file);
                    result = gson.fromJson(fr, StoppingParaFile.class);
                    System.out.println("Stopping data successfully imported from " + file.getName());
                } catch (Exception ex) {
                    System.out.println("Error loading stopping data.");
                    ex.printStackTrace();
                }
            } else {
                JOptionPane.showMessageDialog(null, "StoppingData.json not found!");
            }
        }

        return result;
    }

    public StoppingParaFile getDeepCopy(){

        StoppingCorrectionEntry[] new_data = new StoppingCorrectionEntry[data.length];

        for (int i=0; i<data.length; i++){
            new_data[i] = data[i].getDeepCopy();
        }

        return new StoppingParaFile(new_data);
    }

}
