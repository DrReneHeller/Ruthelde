package com.ruthelde.DataFileReader;

public enum FileType {

     ONE_COLUMN_ASCII("ASCII_ONE"), TWO_COLUMN_ASCII("ASCII_TWO"), IBC_RBS("IBC-RBS"), IBC_3MV_SINGLE("IBC-3MV Single"),
     IBC_3MV_MULTI("IBC-3MV Multiple"), IMEC("Imec"), IBA_SIM("IBA Simulation File");

    private final String displayed_text;

    private FileType(String s)
    {
        displayed_text = s;
    }

    @Override
    public String toString()
    {
        return displayed_text;
    }

}
