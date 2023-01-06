package com.ruthelde.IBA;

import com.ruthelde.Target.Element;
import com.ruthelde.Target.Isotope;
import com.ruthelde.Target.Layer;
import com.ruthelde.Target.Target;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.LinkedList;

public class IDF_Converter {

    public static void write_To_IDF_File(DataFile dataFile, File outputFile, double[] simulatedSpectrum){

        StringBuilder sb = new StringBuilder();

        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "\n");
        sb.append("<idf xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://idf.schemas.itn.pt\" xmlns:simnra=\"http://www.simnra.com/simnra\">" + "\n\n");

        sb.append("\t<attributes>\n" +
                "\t\t<idfversion>1.01</idfversion>\n" +
                "\t\t<filename>test_simnra_file.xnra</filename>\n" +
                "\t\t<createtime>2023-01-05 10:35:15</createtime>\n" + //TODO: Implement current date + time
                "\t\t<simnra:simnraversionnr>7.00</simnra:simnraversionnr>\n" +
                "\t\t<simnra:filetype>xnra</simnra:filetype>\n" +
                "\t\t<simnra:xnraversionnr>1.1</simnra:xnraversionnr>\n" +
                "\t</attributes>\n\n");

        sb.append("\t<sample>\n\n");

            sb.append("\t\t<structure>\n\n");
            sb.append("\t\t\t<layeredstructure>\n\n");

                int nLayers = dataFile.target.getLayerList().size();
                sb.append("\t\t\t\t<nlayers>" + nLayers + "</nlayers>\n\n");

                sb.append("\t\t\t\t<layers>\n\n");

                    for (Layer layer : dataFile.target.getLayerList()){

                        layer.normalizeElements();

                        sb.append("\t\t\t\t\t<layer>\n\n");

                        sb.append("\t\t\t\t\t\t<layerthickness units=\"1e15at/cm2\">");

                        sb.append(String.format("%e", layer.getArealDensity()).replace(",","."));

                        sb.append("</layerthickness>\n\n");

                        sb.append("\t\t\t\t\t\t<layerelements>\n\n");

                        for (Element element : layer.getElementList()){

                            sb.append("\t\t\t\t\t\t\t<layerelement>\n");
                            sb.append("\t\t\t\t\t\t\t\t<name>");
                            sb.append(element.getName());
                            sb.append("</name>\n");

                            sb.append("\t\t\t\t\t\t\t\t<concentration units=\"fraction\">");
                            sb.append(String.format("%e", element.getRatio()).replace(",","."));
                            sb.append("</concentration>\n");
                            sb.append("\t\t\t\t\t\t\t</layerelement>\n\n");

                            //TODO: Check how to tread elements with none-natural isotope distribution
                        }

                        sb.append("\t\t\t\t\t\t</layerelements>\n\n");

                        sb.append("\t\t\t\t\t</layer>\n\n");
                    }

                sb.append("\t\t\t\t</layers>\n\n");

            sb.append("\t\t\t</layeredstructure>\n\n");
            sb.append("\t\t</structure>\n\n");

            sb.append("\t\t<spectra>\n\n");
            sb.append("\t\t\t<spectrum>\n\n");

                sb.append("\t\t\t\t<beam>\n\n");

                sb.append("\t\t\t\t\t<beamparticle>");
                int prZ = dataFile.experimentalSetup.getProjectile().getZ();
                int M = (int) (Math.round(dataFile.experimentalSetup.getProjectile().getM()));
                sb.append(M);
                Element pr = new Element();
                pr.setAtomicNumber(prZ);
                sb.append(pr.getName());
                sb.append("</beamparticle>\n");
                String prjName = M + pr.getName();

                sb.append("\t\t\t\t\t<beamZ>" + prZ + "</beamZ>\n");

                sb.append("\t\t\t\t\t<beammass units=\"amu\">");
                sb.append(String.format("%e", dataFile.experimentalSetup.getProjectile().getM()).replace(",","."));
                sb.append("</beammass>\n");

                sb.append("\t\t\t\t\t<beamenergy units=\"keV\">");
                sb.append(String.format("%e", dataFile.experimentalSetup.getProjectile().getE()).replace(",","."));
                sb.append("</beamenergy>\n");

                sb.append("\t\t\t\t\t<beamenergyspread mode=\"FWHM\" units=\"keV\">");
                sb.append(String.format("%e", dataFile.experimentalSetup.getDeltaE0()).replace(",","."));
                sb.append("</beamenergyspread>\n");

                sb.append("\t\t\t\t\t<beamfluence units=\"#particles\">");
                double particles = 1E-6 * dataFile.experimentalSetup.getCharge() * 6.241509075E+18;
                sb.append(String.format("%e", particles).replace(",","."));
                sb.append("</beamfluence>\n");

                sb.append("\t\t\t\t\t<beamangularspread units=\"degree\" mode=\"FWHM\">0</beamangularspread>\n");
                sb.append("\t\t\t\t\t<simnra:beamenergyspreadlow mode=\"FWHM\" units=\"keV\"> 0.00000000000000E+0000</simnra:beamenergyspreadlow>\n");
                sb.append("\t\t\t\t\t<simnra:beamenergyspreadhigh mode=\"FWHM\" units=\"keV\"> 0.00000000000000E+0000</simnra:beamenergyspreadhigh>\n\n");

                sb.append("\t\t\t\t</beam>\n\n");

                sb.append("\t\t\t\t<geometry>\n\n");

                    sb.append("\t\t\t\t\t<geometrytype>IBM</geometrytype>\n");

                    sb.append("\t\t\t\t\t<incidenceangle units=\"degree\">");
                    sb.append(String.format("%e", dataFile.experimentalSetup.getAlpha()).replace(",","."));
                    sb.append("</incidenceangle>\n");

                    sb.append("\t\t\t\t\t<scatteringangle units=\"degree\">");
                    sb.append(String.format("%e", dataFile.experimentalSetup.getTheta()).replace(",","."));
                    sb.append("</scatteringangle>\n");

                    sb.append("\t\t\t\t\t<exitangle units=\"degree\">");
                    sb.append(String.format("%e", dataFile.experimentalSetup.getBeta()).replace(",","."));
                    sb.append("</exitangle>\n\n");

                    sb.append("\t\t\t\t\t<spot>\n");

                        sb.append("\t\t\t\t\t\t<shape>rectangular</shape>\n");
                        sb.append("\t\t\t\t\t\t<l1 units=\"mm\"> 0.00000000000000E+0000</l1>\n");
                        sb.append("\t\t\t\t\t\t<l2 units=\"mm\"> 0.00000000000000E+0000</l2>\n");

                    sb.append("\t\t\t\t\t</spot>\n\n");

                sb.append("\t\t\t\t</geometry>\n\n");

                sb.append("\t\t\t\t<detection>\n\n");

                    sb.append("\t\t\t\t\t<detector>\n\n");

                        sb.append("\t\t\t\t\t\t<detectortype>SSB</detectortype>\n");

                        sb.append("\t\t\t\t\t\t<solidangle units=\"msr\">");
                        sb.append(String.format("%e", dataFile.detectorSetup.getSolidAngle()).replace(",","."));
                        sb.append("</solidangle>\n");
                        sb.append("\t\t\t\t\t\t<detectorshape>\n");
                        sb.append("\t\t\t\t\t\t\t<shape>rectangular</shape>\n");
                        sb.append("\t\t\t\t\t\t\t<l1 units=\"mm\"> 0.00000000000000E+0000</l1>\n");
                        sb.append("\t\t\t\t\t\t\t<l2 units=\"mm\"> 0.00000000000000E+0000</l2>\n");
                        sb.append("\t\t\t\t\t\t</detectorshape>\n");
                        sb.append("\t\t\t\t\t\t<distancedetectortosample units=\"mm\"> 0.00000000000000E+0000</distancedetectortosample>\n\n");

                    sb.append("\t\t\t\t\t</detector>\n\n");

                    sb.append("\t\t\t\t\t<electronics>\n\n");

                        sb.append("\t\t\t\t\t\t<amplifier>\n");

                            sb.append("\t\t\t\t\t\t\t<pulseshape>Gaussian</pulseshape>\n");
                            sb.append("\t\t\t\t\t\t\t<shapingtime units=\"us\"> 0.00000000000000E+0000</shapingtime>\n");
                            sb.append("\t\t\t\t\t\t\t<pur>off</pur>\n");
                            sb.append("\t\t\t\t\t\t\t<purtime units=\"us\"> 4.00000000000000E-0001</purtime>\n");

                        sb.append("\t\t\t\t\t\t</amplifier>\n\n");

                    sb.append("\t\t\t\t\t</electronics>\n\n");

                sb.append("\t\t\t\t</detection>\n\n");

                sb.append("\t\t\t\t<calibrations>\n\n");

                    sb.append("\t\t\t\t\t<detectorresolutions>\n");
                        sb.append("\t\t\t\t\t\t<detectorresolution>\n");
                            sb.append("\t\t\t\t\t\t\t<resolutionparameters>\n");
                                sb.append("\t\t\t\t\t\t\t\t<resolutionparameter units=\"keV\" mode=\"FWHM\">");
                                sb.append(String.format("%e", dataFile.detectorSetup.getResolution()).replace(",","."));
                                sb.append("</resolutionparameter>\n");
                            sb.append("\t\t\t\t\t\t\t</resolutionparameters>\n");
                        sb.append("\t\t\t\t\t\t</detectorresolution>\n");
                    sb.append("\t\t\t\t\t</detectorresolutions>\n\n");

                sb.append("\t\t\t\t\t<energycalibrations>\n");
                    sb.append("\t\t\t\t\t\t<energycalibration>\n");
                        sb.append("\t\t\t\t\t\t\t<calibrationmode>energy</calibrationmode>\n");
                        sb.append("\t\t\t\t\t\t\t<calibrationparameters>\n");
                            sb.append("\t\t\t\t\t\t\t\t<calibrationparameter units=\"keV\">");
                            sb.append(String.format("%e", dataFile.detectorSetup.getCalibration().getOffset()).replace(",","."));
                            sb.append("</calibrationparameter>\n");
                            sb.append("\t\t\t\t\t\t\t\t<calibrationparameter units=\"keV/channel\">");
                            sb.append(String.format("%e", dataFile.detectorSetup.getCalibration().getFactor()).replace(",","."));
                            sb.append("</calibrationparameter>\n");
                            sb.append("\t\t\t\t\t\t\t\t<calibrationparameter units=\"keV/channel^2\">");
                            sb.append(String.format("%e", 0.0d).replace(",","."));
                            sb.append("</calibrationparameter>\n");
                        sb.append("\t\t\t\t\t\t\t</calibrationparameters>\n");
                    sb.append("\t\t\t\t\t\t</energycalibration>\n");
                sb.append("\t\t\t\t\t</energycalibrations>\n\n");

                sb.append("\t\t\t\t</calibrations>\n\n");

                sb.append("\t\t\t\t<reactions>\n");
                    sb.append("\t\t\t\t\t<technique>RBS</technique>\n");
                sb.append("\t\t\t\t</reactions>\n\n");

                sb.append("\t\t\t\t<data>\n\n");

                    sb.append("\t\t\t\t\t<datamode>simple</datamode>\n");
                    sb.append("\t\t\t\t\t<channelmode>left</channelmode>\n\n");

                    sb.append("\t\t\t\t\t<simpledata>\n\n");

                        sb.append("\t\t\t\t\t\t<xaxis>\n");
                            sb.append("\t\t\t\t\t\t\t<axisname>channel</axisname>\n");
                            sb.append("\t\t\t\t\t\t\t<axisunit>#</axisunit>\n");
                        sb.append("\t\t\t\t\t\t</xaxis>\n\n");

                        sb.append("\t\t\t\t\t\t<yaxis>\n");
                            sb.append("\t\t\t\t\t\t\t<axisname>yield</axisname>\n");
                            sb.append("\t\t\t\t\t\t\t<axisunit>counts</axisunit>\n");
                        sb.append("\t\t\t\t\t\t</yaxis>\n\n");

                        sb.append("\t\t\t\t\t\t<x>\n");
                            sb.append("\t\t\t\t\t\t\t");
                            int legnth = dataFile.experimentalSpectrum.length;
                            for (int i=0; i<legnth-1; i++){
                                sb.append(i + "\t");
                            }
                            sb.append((legnth-1) + "\n");
                        sb.append("\t\t\t\t\t\t</x>\n\n");

                        sb.append("\t\t\t\t\t\t<y>\n");
                            sb.append("\t\t\t\t\t\t\t");
                            for (int i=0; i<legnth-1; i++){
                                sb.append(String.format("%e", dataFile.experimentalSpectrum[i]).replace(",",".") + "\t");
                            }
                            sb.append(String.format("%e", dataFile.experimentalSpectrum[legnth-1]).replace(",",".") + "\n");
                        sb.append("\t\t\t\t\t\t</y>\n\n");

                    sb.append("\t\t\t\t\t</simpledata>\n\n");

                    sb.append("\t\t\t\t\t<simnra:graphics>\n\n");

                        sb.append("\t\t\t\t\t\t<simnra:legend>Experimental</simnra:legend>\n");
                        sb.append("\t\t\t\t\t\t<simnra:linecolor>red</simnra:linecolor>\n");
                        sb.append("\t\t\t\t\t\t<simnra:active>true</simnra:active>\n\n");

                        sb.append("\t\t\t\t\t\t<simnra:symbol>\n");
                            sb.append("\t\t\t\t\t\t\t<simnra:shape>circle</simnra:shape>\n");
                            sb.append("\t\t\t\t\t\t\t<simnra:visible>true</simnra:visible>\n");
                        sb.append("\t\t\t\t\t\t</simnra:symbol>\n\n");

                    sb.append("\t\t\t\t\t</simnra:graphics>\n\n");

                sb.append("\t\t\t\t</data>\n\n");

                sb.append("\t\t\t\t<process>\n\n");

                    sb.append("\t\t\t\t\t<simulations>\n\n");

                        sb.append("\t\t\t\t\t\t<simulation>\n\n");

                            sb.append("\t\t\t\t\t\t\t<physics>\n\n");

                                sb.append("\t\t\t\t\t\t\t\t<crosssections>\n\n");

                                LinkedList<IsotopeData> isotopeList = generateIsotopeList(dataFile.target);

                                for (IsotopeData isotopeData : isotopeList){
                                    sb.append("\t\t\t\t\t\t\t\t\t<crosssection>\n");

                                        sb.append("\t\t\t\t\t\t\t\t\t\t<crosssectionframe>lab</crosssectionframe>\n");
                                        sb.append("\t\t\t\t\t\t\t\t\t\t<crosssectiontype>differential</crosssectiontype>\n");
                                        sb.append("\t\t\t\t\t\t\t\t\t\t<energyminimum units=\"keV\"> 1.00000000000000E-0003</energyminimum>\n");
                                        sb.append("\t\t\t\t\t\t\t\t\t\t<energymaximum units=\"keV\"> 1.00000000000000E+0006</energymaximum>\n");

                                        sb.append("\t\t\t\t\t\t\t\t\t\t<reaction>\n");

                                            sb.append("\t\t\t\t\t\t\t\t\t\t\t<initialtargetparticle>");
                                            sb.append(isotopeData.name);
                                            sb.append("</initialtargetparticle>\n");

                                            sb.append("\t\t\t\t\t\t\t\t\t\t\t<incidentparticle>");
                                            sb.append(prjName);
                                            sb.append("</incidentparticle>\n");

                                            sb.append("\t\t\t\t\t\t\t\t\t\t\t<exitparticle>");
                                            sb.append(prjName);
                                            sb.append("</exitparticle>\n");

                                            sb.append("\t\t\t\t\t\t\t\t\t\t\t<finaltargetparticle>");
                                            sb.append(isotopeData.name);
                                            sb.append("</finaltargetparticle>\n");

                                            sb.append("\t\t\t\t\t\t\t\t\t\t\t<reactionQ units=\"keV\"> 0.00000000000000E+0000</reactionQ>\n");

                                        sb.append("\t\t\t\t\t\t\t\t\t\t</reaction>\n");

                                        sb.append("\t\t\t\t\t\t\t\t\t\t<crosssectionoverride>\n");
                                            sb.append("\t\t\t\t\t\t\t\t\t\t\t<Rutherford>true</Rutherford>\n");
                                        sb.append("\t\t\t\t\t\t\t\t\t\t</crosssectionoverride>\n");

                                    sb.append("\t\t\t\t\t\t\t\t\t</crosssection>\n\n");
                                }

                                sb.append("\t\t\t\t\t\t\t\t</crosssections>\n\n");

                            sb.append("\t\t\t\t\t\t\t</physics>\n\n");

                            sb.append("\t\t\t\t\t\t\t<simulationtype>total</simulationtype>\n\n");
                            sb.append("\t\t\t\t\t\t\t<datamode>simple</datamode>\n\n");
                            sb.append("\t\t\t\t\t\t\t<channelmode>left</channelmode>\n\n");

                            sb.append("\t\t\t\t\t\t\t<simpledata>\n\n");

                            sb.append("\t\t\t\t\t\t\t\t<xaxis>\n");
                            sb.append("\t\t\t\t\t\t\t\t\t<axisname>channel</axisname>\n");
                            sb.append("\t\t\t\t\t\t\t\t\t<axisunit>#</axisunit>\n");
                            sb.append("\t\t\t\t\t\t\t\t</xaxis>\n\n");

                            sb.append("\t\t\t\t\t\t\t\t<yaxis>\n");
                            sb.append("\t\t\t\t\t\t\t\t\t<axisname>yield</axisname>\n");
                            sb.append("\t\t\t\t\t\t\t\t\t<axisunit>counts</axisunit>\n");
                            sb.append("\t\t\t\t\t\t\t\t</yaxis>\n\n");

                            sb.append("\t\t\t\t\t\t\t\t<x>\n");
                            sb.append("\t\t\t\t\t\t\t\t\t");
                            legnth = simulatedSpectrum.length;
                            for (int i=0; i<legnth-1; i++){
                                sb.append(i + "\t");
                            }
                            sb.append((legnth-1) + "\n");
                            sb.append("\t\t\t\t\t\t\t\t</x>\n\n");

                            sb.append("\t\t\t\t\t\t\t\t<y>\n");
                            sb.append("\t\t\t\t\t\t\t\t\t");
                            for (int i=0; i<legnth-1; i++){
                                sb.append(String.format("%e", simulatedSpectrum[i]).replace(",",".") + "\t");
                            }
                            sb.append(String.format("%e", simulatedSpectrum[legnth-1]).replace(",",".") + "\n");
                            sb.append("\t\t\t\t\t\t\t\t</y>\n\n");

                            sb.append("\t\t\t\t\t\t\t</simpledata>\n\n");

                            sb.append("\t\t\t\t\t\t\t<simnra:graphics>\n\n");

                                sb.append("\t\t\t\t\t\t\t\t<simnra:legend>Simulated</simnra:legend>\n");
                                sb.append("\t\t\t\t\t\t\t\t<simnra:linecolor>blue</simnra:linecolor>\n");
                                sb.append("\t\t\t\t\t\t\t\t<simnra:active>true</simnra:active>\n\n");

                                sb.append("\t\t\t\t\t\t\t\t<simnra:symbol>\n");
                                    sb.append("\t\t\t\t\t\t\t\t\t<simnra:shape>rectangle</simnra:shape>\n");
                                    sb.append("\t\t\t\t\t\t\t\t\t<simnra:visible>false</simnra:visible>\n");
                                sb.append("\t\t\t\t\t\t\t\t</simnra:symbol>\n\n");

                            sb.append("\t\t\t\t\t\t\t</simnra:graphics>\n\n");

                        sb.append("\t\t\t\t\t\t</simulation>\n\n");

                    sb.append("\t\t\t\t\t</simulations>\n\n");

                sb.append("\t\t\t\t</process>\n\n");

                sb.append("\t\t\t\t<simnra:scaling>\n\n");

                    sb.append("\t\t\t\t\t<simnra:auto>true</simnra:auto>\n\n");

                sb.append("\t\t\t\t</simnra:scaling>\n\n");

                sb.append("\t\t\t\t<simnra:subspectra>\n\n");

                    sb.append("\t\t\t\t\t<simnra:elementspectra>false</simnra:elementspectra>\n\n");
                    sb.append("\t\t\t\t\t<simnra:isotopespectra>false</simnra:isotopespectra>\n\n");

                sb.append("\t\t\t\t</simnra:subspectra>\n\n");

                sb.append("\t\t\t\t<simnra:legendoutsideofchart>true</simnra:legendoutsideofchart>\n\n");

            sb.append("\t\t\t</spectrum>\n\n");

            sb.append("\t\t</spectra>\n\n");

        sb.append("\t</sample>\n\n");

        sb.append("</idf>");

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
            writer.write(sb.toString());
            writer.close();
        } catch (Exception ex) {
            System.out.println("Error writing IDF file: " + ex.getMessage());
        }

    }

    private static LinkedList<IsotopeData> generateIsotopeList(Target target) {

        LinkedList<IsotopeData> isotopeList = new LinkedList<>();

        boolean addIt;

        for (Layer layer : target.getLayerList()) {
            for (Element element : layer.getElementList()) {
                int Z = element.getAtomicNumber();
                for (Isotope isotope : element.getIsotopeList()) {
                    double M = isotope.getMass();
                    double c = layer.getIsotopeContribution(Z, M);
                    addIt = true;
                    for (IsotopeData isotopeData : isotopeList) {
                        if (isotopeData.Z == Z && isotopeData.M == M) {
                            addIt = false;
                            break;
                        }
                    }
                    if (addIt) {
                        isotopeList.add(new IsotopeData(Z,M));
                    }
                }
            }
        }
        return isotopeList;
    }
}

class IsotopeData{

    public int Z;
    public double M;
    public String name;

    public IsotopeData(int Z, double M){

        this.M = M;
        Element element = new Element();
        element.setAtomicNumber(Z);
        int m = (int) Math.round(M);
        name = m + element.getName();
    }

}
