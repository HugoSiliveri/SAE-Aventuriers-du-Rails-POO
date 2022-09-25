package fr.umontpellier.iut.rails;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Ferry extends Route {
    /**
     * Nombre de locomotives qu'un joueur doit payer pour capturer le ferry
     */
    private int nbLocomotives;

    public Ferry(Ville ville1, Ville ville2, int longueur, CouleurWagon couleur, int nbLocomotives) {
        super(ville1, ville2, longueur, couleur);
        this.nbLocomotives = nbLocomotives;
    }

    @Override
    public String toString() {
        return String.format("[%s - %s (%d, %s, %d)]", getVille1(), getVille2(), getLongueur(), getCouleur(),
                nbLocomotives);
    }

    @Override
    public boolean estCapturablePar(Joueur j) {
        if (getProprietaire() == null && j.getNbWagons() >= getLongueur() && Collections.frequency(j.getCartesWagon(), CouleurWagon.LOCOMOTIVE) >= nbLocomotives) {
            ArrayList<Route> routesJoueurs = super.routesCaptureesParJoueur(j);
            int i = 0;
            while (i < routesJoueurs.size() && !routesJoueurs.get(i).estDouble(this)) i++;
            if (i < routesJoueurs.size()) return false;
            int k = 0;
            boolean cartesSuffisantes = false;
            List<CouleurWagon> cartesJoueur = j.getCartesWagon();
            while (k < cartesJoueur.size() && !cartesSuffisantes) {
                if (Collections.frequency(cartesJoueur, cartesJoueur.get(k)) >= getLongueur() - nbLocomotives)
                    cartesSuffisantes = true;
                else if (Collections.frequency(cartesJoueur, CouleurWagon.LOCOMOTIVE) >= getLongueur() - nbLocomotives - Collections.frequency(cartesJoueur, cartesJoueur.get(k)))
                    cartesSuffisantes = true;
                else k++;
            }
            return cartesSuffisantes;
        }
        return false;
    }

    @Override
    public void capturerParJoueur(Joueur j) {
        int longueur = getLongueur() - nbLocomotives;
        for (int i = 0; i < nbLocomotives; i++) {
            j.retirerCarteWagon(CouleurWagon.LOCOMOTIVE);
            j.getJeu().defausserCarteWagon(CouleurWagon.LOCOMOTIVE);
        }
        for (CouleurWagon c : j.choisirCarteWagon(CouleurWagon.GRIS, longueur, false)) j.getJeu().defausserCarteWagon(c);
        j.poserWagon(getLongueur());
        switch (getLongueur()) {
            case 1 -> j.mettreAJourScore(1);
            case 2 -> j.mettreAJourScore(2);
            case 3 -> j.mettreAJourScore(4);
            case 4 -> j.mettreAJourScore(7);
            case 6 -> j.mettreAJourScore(15);
            case 8 -> j.mettreAJourScore(21);
        }
        setProprietaire(j);
        j.log(String.format("%s a capturé le ferry %s.", j.toLog(), toLog()));
    }
}
