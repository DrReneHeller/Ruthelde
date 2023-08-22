package com.ruthelde.IBA.Kinematics;

import com.ruthelde.Globals.Globals;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.LinkedList;

public class CrossSectionData {

    public int Z1, Z2;
    public double M1, M2;

    private double theta;
    public double E_start, E_end;

    public String name;

    public boolean rr; // true --> unit = rr , false --> unit mb/sr

    public double energies[], crossSections[];

    public CrossSectionData(){
    }

    public boolean loadFromFile(){

        String currentLine, temp;
        BufferedReader inputBuffer;
        LinkedList<Double> energyList = new LinkedList<>();
        LinkedList<Double> sigmaList  = new LinkedList<>();
        boolean foundStartOfData = false;
        boolean foundUnit = false;
        boolean status = false;

        final JFileChooser fc;
        if (Globals.lastFolder != null) fc = new JFileChooser(Globals.lastFolder);
        else fc = new JFileChooser();
        int returnVal = fc.showOpenDialog(null);

        File file = null;
        if (returnVal == JFileChooser.APPROVE_OPTION){
            file = fc.getSelectedFile();
            Globals.lastFolder = fc.getSelectedFile().getParent();
        }

        try {
            if (file != null && file.exists() && file.isFile() && file.canRead()) {

                inputBuffer = new BufferedReader(new FileReader(file));

                name = file.getName();

                while ((currentLine = inputBuffer.readLine()) != null) {

                    if (!foundStartOfData) {

                        //Parse M1 & M2
                        if (currentLine.contains("Masses:")) {

                            temp = currentLine.substring(7);
                            M1 = Double.parseDouble(temp.split(",")[0]);
                            M2 = Double.parseDouble(temp.split(",")[1]);
                        }

                        //Parse Z1 & Z2
                        if (currentLine.contains("Zeds:")) {

                            temp = currentLine.substring(5);
                            Z1 = (int)Double.parseDouble(temp.split(",")[0]);
                            Z2 = (int)Double.parseDouble(temp.split(",")[1]);
                        }

                        //Parse theta
                        if (currentLine.contains("Theta:")) {

                            temp = currentLine.substring(6);
                            theta = Double.parseDouble(temp);
                        }

                        //Parse unit
                        if (currentLine.contains("Units:")) {

                            if (currentLine.contains("rr:"));{
                                foundUnit = true;
                                rr = true;
                            }
                            if (currentLine.contains("mb:")){
                                foundUnit = true;
                                rr = false;
                            }
                        }

                        if (currentLine.contains("Data:")) foundStartOfData = true;
                    }
                    else {

                        if (currentLine.contains("EndData")){
                            break;

                        }
                        else if (currentLine.contains(",")) {

                            energyList.add(Double.parseDouble(currentLine.split(",")[0]));
                            sigmaList.add(Double.parseDouble(currentLine.split(",")[2]));

                        } else {

                            energyList.add(Double.parseDouble(currentLine.split(" ")[0]));
                            sigmaList.add(Double.parseDouble(currentLine.split(" ")[2]));
                        }
                    }
                }

                inputBuffer.close();
                status = true;

                if (!foundStartOfData)  {
                    System.out.println("No cross section data found in file.");
                    status = false;
                }

                if (!foundUnit)  {
                    System.out.println("Unknown unit: only rr and mb/sr are supported yet.");
                    status = false;
                }

            } else {

                System.out.println("Error, file does not exist.");
            }
        } catch (Exception ex) {

            System.out.println("Error reading R33 file.");
            ex.printStackTrace();
        }

        if (status){

            energies      = new double[energyList.size()];
            crossSections = new double[energyList.size()];

            for (int i= 0; i < energyList.size(); i++) {

                energies[i]      = energyList.get(i);
                crossSections[i] = sigmaList.get(i);
            }

            E_start = energies[0];
            E_end = energies[energies.length-1];

            System.out.print("Parsed cross section data successfully: ");
            System.out.println(" Z1=" + Z1 + ", M1=" + M1 + ", Z2=" + Z2 + ", M2=" + M2 + ", Theta=" + theta);
            System.out.println("Energy range: " + E_start + "keV ... " + E_end + "keV");
            if (rr) System.out.println("Unit: rr"); else System.out.println("Unit: mb/sr");

            //for (int i=0; i<energies.length; i++){
            //    System.out.println("" + energies[i] + ", " + crossSections[i]);
            //}
        }

        return status;
    }
}
