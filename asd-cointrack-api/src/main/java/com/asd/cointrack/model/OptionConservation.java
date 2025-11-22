package com.asd.cointrack.model;

public enum OptionConservation {

    FDC("Fior di conio"),
    qFDC("Quasi fior di conio"),
    SPL("Splendido"),
    qSPL("Quasi splendido"),
    BB("Bellissimo"),
    qBB("Quasi bellissimo"),
    MB("Molto bello"),
    B("Bello"),
    D("Discreto"),
    ILLEGIBILE("Illegibile");
    
    public final String wording;
    
    OptionConservation(String wording) {
        this.wording = wording;
    }

}
