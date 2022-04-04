package Helper;

import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public final class Helper {

     static public String dblToDecStr(double d, int precision) {
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        DecimalFormat df = (DecimalFormat)(nf);
        df.setMinimumFractionDigits(precision);
        df.setMaximumFractionDigits(precision);
        df.setGroupingUsed(false);
        df.setMinimumIntegerDigits(1);

        return df.format(d);
    }

    static public String dblToSciStr(double d, int precision) {

        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        DecimalFormat df = (DecimalFormat)(nf);

        df.setMinimumFractionDigits(precision);
        df.setMaximumFractionDigits(precision);
        df.setPositivePrefix(" ");

        double numDigits, value;

        if (d != 0.0d) {
            numDigits = Math.floor(Math.log10(Math.abs(d)));
        } else {
            numDigits = 0;
        }

        value     = d / Math.pow(10, numDigits);

        String result = df.format(value) + "E";

        df.setMaximumFractionDigits(0);
        df.setPositivePrefix("+");
        df.setMinimumIntegerDigits(2);

        result += df.format(numDigits);

        return result;
    }

    static public File changeExtension(File file, String extension, File subFolder) {

         String filename = file.getName();

        if (filename.contains(".")) {
            filename = filename.substring(0, filename.lastIndexOf('.'));
        }
        filename +=  extension;

        File result;

        if (subFolder == null) {
            result = new File(file.getParent() + "/" + filename);
        } else {
            result = new File(file.getParent() + "/" + subFolder.getName() + "/" + filename);
        }

        return result;
    }
}
