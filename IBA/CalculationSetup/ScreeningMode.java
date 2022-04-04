package IBA.CalculationSetup;

public enum ScreeningMode {

    NONE("None"), LECUYER("L'Ecuyer"), ANDERSON("Anderson");

    private final String displayed_text;

    private ScreeningMode(String s)
    {
        displayed_text = s;
    }

    @Override
    public String toString()
    {
        return displayed_text;
    }

}
