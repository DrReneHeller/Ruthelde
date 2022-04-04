package IBA.CalculationSetup;

public enum StragglingMode {

    NONE("None"), BOHR("Bohr"), CHU("Chu");

    private final String displayed_text;

    private StragglingMode(String s)
    {
        displayed_text = s;
    }

    @Override
    public String toString()
    {
        return displayed_text;
    }

}
