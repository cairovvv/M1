package com.mycompany.sportstrainingacademy;

public class Athletes {
    private int id, age;
    private String fName, lName, sport;
    private double bmi;

    public Athletes(int id, String f, String l, int age, String s, double bmi) {
        this.id = id; this.fName = f; this.lName = l; this.age = age; this.sport = s; this.bmi = bmi;
    }

    @Override
    public String toString() {
        return String.format("ID: %d | %s %s | Sport: %s | BMI: %.1f", id, fName, lName, sport, bmi);
    }
}
