package com.ruthelde.IBA.CalculationSetup;

public enum ChargeFractionMode {

    NONE("None"), FIXED("Fixed"), LINEAR("Linear");

    private final String displayed_text;

    private ChargeFractionMode(String s)
    {
        displayed_text = s;
    }

    @Override
    public String toString()
    {
        return displayed_text;
    }

}
