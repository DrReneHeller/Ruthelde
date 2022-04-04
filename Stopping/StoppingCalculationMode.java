package Stopping;


public enum StoppingCalculationMode {

    ZB("Ziegler-Biersack");

    private final String displayed_text;

    private StoppingCalculationMode(String s)
    {
        displayed_text = s;
    }

    @Override
    public String toString()
    {
        return displayed_text;
    }

}
