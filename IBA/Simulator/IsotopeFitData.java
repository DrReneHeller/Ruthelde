package IBA.Simulator;

public class IsotopeFitData {

    public int Z;
    public double M;
    public double[] concentrations;
    public double[][] spectra;
    public double[] straggling; // = sigma^2 [keV^2]

    public IsotopeFitData() {

    }

    public IsotopeFitData(int Z, double M, double[] concentrations, int spectrumLength) {

        this.Z = Z;
        this.M = M;
        this.concentrations = concentrations;
        this.straggling = new double[spectrumLength];
        this.spectra = new double[concentrations.length][spectrumLength];
    }

    public IsotopeFitData getDeepCopy(){

        IsotopeFitData isotopeFitData = new IsotopeFitData();
        isotopeFitData.Z = Z;
        isotopeFitData.M = M;
        isotopeFitData.concentrations = new double[concentrations.length];
        System.arraycopy(concentrations, 0, isotopeFitData.concentrations, 0, concentrations.length);
        isotopeFitData.spectra = new double[spectra.length][spectra[0].length];
        for (int i=0; i<spectra.length; i++){
            System.arraycopy(spectra[i], 0, isotopeFitData.spectra[i], 0, spectra[0].length);
        }

        isotopeFitData.straggling = new double[straggling.length];
        System.arraycopy(straggling, 0, isotopeFitData.straggling, 0, straggling.length);

        return isotopeFitData;
    }
}
