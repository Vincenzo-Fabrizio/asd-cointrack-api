package com.asd.cointrack.model;

public enum NumismaticRarity {
    
    R5("Unica"),
    R4("Estremamente rara"),
    R3("Rarissima"),
    R2("Molto rara"),
    R("Rara"),
    NC("Non comune"),
    C("Comune");
    
    public final String wording;

    NumismaticRarity(String wording) {
        this.wording = wording;
    }

}
