package Stopping;


public enum CompoundCalculationMode {

    BRAGG("Bragg-Rule");

    private final String displayed_text;

    private CompoundCalculationMode(String s)
    {
        displayed_text = s;
    }

    @Override
    public String toString()
    {
        return displayed_text;
    }

}
