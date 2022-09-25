package fr.umontpellier.iut.rails;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Tunnel extends Route {
    public Tunnel(Ville ville1, Ville ville2, int longueur, CouleurWagon couleur) {
        super(ville1, ville2, longueur, couleur);
    }

    @Override
    public String toString() {
        return "[" + super.toString() + "]";
    }

    @Override
    public void capturerParJoueur(Joueur j) {
        //Capture de route normale
        int longueur = getLongueur();
        CouleurWagon couleur = getCouleur();
        for (CouleurWagon c : j.choisirCarteWagon(couleur, longueur, false)) j.poserCarteWagon(c);

        //Détermination de la couleur utilisée.
        CouleurWagon couleurUtilisee = CouleurWagon.GRIS;
        if (Collections.frequency(j.getCartesWagonPosees(), CouleurWagon.LOCOMOTIVE) == j.getCartesWagonPosees().size()) couleurUtilisee = CouleurWagon.LOCOMOTIVE;
        else {
            int i = 0;
            List<CouleurWagon> cartesWagonPosees = j.getCartesWagonPosees();
            while(i < cartesWagonPosees.size() && couleurUtilisee == CouleurWagon.GRIS) {
                if (cartesWagonPosees.get(i) != CouleurWagon.LOCOMOTIVE) couleurUtilisee = cartesWagonPosees.get(i);
                else i++;
            }
            if (couleurUtilisee == CouleurWagon.GRIS) couleurUtilisee = CouleurWagon.LOCOMOTIVE;
        }

        //Détermination du nombres de cartes supplémentaires à défausser.
        int nbCartesSupplementaires = 0;
        for (int i = 0; i < 3; i++) {
            CouleurWagon cartePiochee = j.getJeu().piocherCarteWagon();
            j.log(String.format("Carte piochée: %s.", cartePiochee.toLog()));
            if (cartePiochee == (couleur != CouleurWagon.GRIS ? couleur : couleurUtilisee) || cartePiochee == CouleurWagon.LOCOMOTIVE) nbCartesSupplementaires++;
            j.getJeu().defausserCarteWagon(cartePiochee);
        }
        j.log(String.format("%s: Vous devez défausser %d carte(s) wagon supplémentaire(s).", toLog(), nbCartesSupplementaires));

        //Choix des cartes supplémentaires à défausser.
        if (nbCartesSupplementaires > 0) {
            ArrayList<CouleurWagon> cartesSupp = j.choisirCarteWagon(couleurUtilisee, nbCartesSupplementaires, true);
            if (cartesSupp.isEmpty()) {
                while (!j.getCartesWagonPosees().isEmpty()) j.ajouterCarteWagon(j.getCartesWagonPosees().remove(0));
                j.log(String.format("%s: Abandon de la capture du tunnel %s.", j.toLog(), toLog()));
            } else {
                for (CouleurWagon c : cartesSupp) j.getJeu().defausserCarteWagon(c);
                for (CouleurWagon c : j.getCartesWagonPosees()) j.getJeu().defausserCarteWagon(c);
                j.getCartesWagonPosees().clear();
                j.poserWagon(longueur);
                switch (longueur) {
                    case 1 -> j.mettreAJourScore(1);
                    case 2 -> j.mettreAJourScore(2);
                    case 3 -> j.mettreAJourScore(4);
                    case 4 -> j.mettreAJourScore(7);
                    case 6 -> j.mettreAJourScore(15);
                    case 8 -> j.mettreAJourScore(21);
                }
                setProprietaire(j);
                j.log(String.format("%s a capturé le tunnel %s.", j.toLog(), toLog()));
            }
        } else {
            for (CouleurWagon c : j.getCartesWagonPosees()) j.getJeu().defausserCarteWagon(c);
            j.getCartesWagonPosees().clear();
            j.poserWagon(longueur);
            switch (longueur) {
                case 1 -> j.mettreAJourScore(1);
                case 2 -> j.mettreAJourScore(2);
                case 3 -> j.mettreAJourScore(4);
                case 4 -> j.mettreAJourScore(7);
                case 6 -> j.mettreAJourScore(15);
                case 8 -> j.mettreAJourScore(21);
            }
            setProprietaire(j);
            j.log(String.format("%s a capturé le tunnel %s.", j.toLog(), toLog()));
        }
    }
}
