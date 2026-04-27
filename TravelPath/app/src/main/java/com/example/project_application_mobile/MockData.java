package com.example.project_application_mobile;

import java.util.ArrayList;
import java.util.List;

public class MockData {

    public static List<Lieu> genererFauxLieux() {
        List<Lieu> lieux = new ArrayList<>();

        lieux.add(new Lieu("M1", "Musée du Louvre", "Musées", 17.0, 180, "Facile", false, 48.8606, 2.3376,"10:00-18:00"));
        lieux.add(new Lieu("M2", "Musée d'Orsay", "Musées", 16.0, 150, "Facile", false, 48.8599, 2.3265,"10:00-18:00"));
        lieux.add(new Lieu("M3", "Centre Pompidou", "Musées", 15.0, 120, "Facile", false, 48.8606, 2.3522,"10:00-18:00"));

        lieux.add(new Lieu("MO1", "Tour Eiffel", "Monuments", 28.3, 120, "Medium", true, 48.8584, 2.2945,"10:00-18:00"));
        lieux.add(new Lieu("MO2", "Arc de Triomphe", "Monuments", 13.0, 60, "Medium", true, 48.8738, 2.2950,"10:00-18:00"));
        lieux.add(new Lieu("MO3", "Panthéon", "Monuments", 11.5, 90, "Facile", false, 48.8462, 2.3459,"10:00-18:00"));

        lieux.add(new Lieu("C1", "Catacombes de Paris", "Sites culturels", 29.0, 90, "Medium", false, 48.8338, 2.3324,"10:00-18:00"));
        lieux.add(new Lieu("C2", "Sainte-Chapelle", "Sites culturels", 11.5, 60, "Facile", false, 48.8554, 2.3450,"10:00-18:00"));
        lieux.add(new Lieu("C3", "Opéra Garnier", "Sites culturels", 14.0, 90, "Facile", false, 48.8719, 2.3316,"10:00-18:00"));

        lieux.add(new Lieu("P1", "Jardin du Luxembourg", "Parcs", 0.0, 120, "Facile", true, 48.8462, 2.3371,"10:00-18:00"));
        lieux.add(new Lieu("P2", "Parc des Buttes-Chaumont", "Parcs", 0.0, 150, "Medium", true, 48.8769, 2.3811,"10:00-18:00"));
        lieux.add(new Lieu("P3", "Bois de Boulogne", "Parcs", 0.0, 240, "Facile", true, 48.8624, 2.2492,"10:00-18:00"));

        lieux.add(new Lieu("PA1", "Disneyland Paris", "Parcs d'attractions", 90.0, 480, "Medium", true, 48.8672, 2.7836,"10:00-18:00"));
        lieux.add(new Lieu("PA2", "Parc Astérix", "Parcs d'attractions", 55.0, 420, "Medium", true, 49.1342, 2.5714,"10:00-18:00"));

        lieux.add(new Lieu("R1", "Bouillon Chartier", "Restauration", 15.0, 60, "Facile", false, 48.8718, 2.3423,"10:00-18:00"));
        lieux.add(new Lieu("R2", "Le Train Bleu", "Restauration", 65.0, 90, "Facile", false, 48.8443, 2.3734,"10:00-18:00"));
        lieux.add(new Lieu("R3", "L'As du Fallafel", "Restauration", 10.0, 45, "Facile", false, 48.8573, 2.3588,"10:00-18:00"));

        lieux.add(new Lieu("B1", "Rooftop Montmartre", "Bars", 12.0, 90, "Facile", true, 48.8867, 2.3431,"10:00-18:00"));
        lieux.add(new Lieu("B2", "Le Comptoir Général", "Bars", 15.0, 120, "Facile", false, 48.8722, 2.3655,"10:00-18:00"));

        lieux.add(new Lieu("S1", "Galeries Lafayette", "Boutiques", 0.0, 150, "Facile", false, 48.8735, 2.3325,"10:00-18:00"));
        lieux.add(new Lieu("S2", "Champs-Élysées", "Boutiques", 0.0, 180, "Medium", true, 48.8698, 2.3075,"10:00-18:00"));

        lieux.add(new Lieu("W1", "Forêt de Fontainebleau", "Randonnées", 0.0, 300, "Sportif", true, 48.3970, 2.6860,"10:00-18:00"));
        lieux.add(new Lieu("W2", "Mont Valérien", "Randonnées", 0.0, 180, "Difficile", true, 48.8719, 2.2132,"10:00-18:00"));
        lieux.add(new Lieu("W3", "Coulée Verte", "Randonnées", 0.0, 150, "Medium", true, 48.8465, 2.3769,"10:00-18:00"));

        return lieux;
    }
}