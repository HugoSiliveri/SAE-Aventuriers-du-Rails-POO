package fr.umontpellier.iut.rails;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class Route {
    /**
     * Première extrémité
     */
    private Ville ville1;
    /**
     * Deuxième extrémité
     */
    private Ville ville2;
    /**
     * Nombre de segments
     */
    private int longueur;
    /**
     * CouleurWagon pour capturer la route (éventuellement GRIS, mais pas LOCOMOTIVE)
     */
    private CouleurWagon couleur;
    /**
     * Joueur qui a capturé la route (`null` si la route est encore à prendre)
     */
    private Joueur proprietaire;
    /**
     * Nom unique de la route. Ce nom est nécessaire pour résoudre l'ambiguïté entre les routes doubles
     * (voir la classe Plateau pour plus de clarté)
     */
    private String nom;

    public Route(Ville ville1, Ville ville2, int longueur, CouleurWagon couleur) {
        this.ville1 = ville1;
        this.ville2 = ville2;
        this.longueur = longueur;
        this.couleur = couleur;
        nom = ville1.getNom() + " - " + ville2.getNom();
        proprietaire = null;
    }

    public Ville getVille1() {
        return ville1;
    }

    public Ville getVille2() {
        return ville2;
    }

    public int getLongueur() {
        return longueur;
    }

    public CouleurWagon getCouleur() {
        return couleur;
    }

    public Joueur getProprietaire() {
        return proprietaire;
    }

    public void setProprietaire(Joueur proprietaire) {
        this.proprietaire = proprietaire;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String toLog() {
        return String.format("<span class=\"route\">%s - %s</span>", ville1.getNom(), ville2.getNom());
    }

    @Override
    public String toString() {
        return String.format("[%s - %s (%d, %s)]", ville1, ville2, longueur, couleur);
    }

    /**
     * @return un objet simple représentant les informations de la route
     */
    public Object asPOJO() {
        HashMap<String, Object> data = new HashMap<>();
        data.put("nom", getNom());
        if (proprietaire != null) {
            data.put("proprietaire", proprietaire.getCouleur());
        }
        return data;
    }

    public boolean estCapturablePar(Joueur j) {
        if (proprietaire == null && j.getNbWagons() >= longueur) {
            int i = 0;
            ArrayList<Route> routesJoueurs = routesCaptureesParJoueur(j);
            while (i < routesJoueurs.size() && !routesJoueurs.get(i).estDouble(this)) i++;
            if (i < routesJoueurs.size()) return false;
            if (couleur != CouleurWagon.GRIS) {
                int nbCartesBonneCouleur = Collections.frequency(j.getCartesWagon(), couleur);
                if (nbCartesBonneCouleur >= longueur) return true;
                else return Collections.frequency(j.getCartesWagon(), CouleurWagon.LOCOMOTIVE) >= longueur - nbCartesBonneCouleur;
            }
            else {
                int k = 0;
                boolean cartesSuffisantes = false;
                List<CouleurWagon> cartesJoueur = j.getCartesWagon();
                while (k < cartesJoueur.size() && !cartesSuffisantes) {
                    if (Collections.frequency(cartesJoueur, cartesJoueur.get(k)) >= longueur) cartesSuffisantes = true;
                    else if(Collections.frequency(cartesJoueur, CouleurWagon.LOCOMOTIVE) >= longueur - Collections.frequency(cartesJoueur, cartesJoueur.get(k))) cartesSuffisantes = true;
                    else k++;
                }
                return cartesSuffisantes;
            }
        }
        return false;
    }

    protected boolean estDouble(Route r) {
        return ((this.ville1.equals(r.ville1) || this.ville1.equals(r.ville2))
                && (this.ville2.equals(r.ville2) || this.ville2.equals(r.ville1))
                && !this.nom.equals(r.nom));
    }

    protected ArrayList<Route> routesCaptureesParJoueur(Joueur j) {
        ArrayList<Route> routes = new ArrayList<>();
        for (Route route : j.getJeu().getRoutes()) {
            if (route.proprietaire != null && route.proprietaire.equals(j)) routes.add(route);
        }
        return routes;
    }

    /**
     * Action de capturer une route simple lors d'un tour du joueur.
     *
     * @param j - Le joueur qui capture la route.
     */
    public void capturerParJoueur(Joueur j) {
        for (CouleurWagon c : j.choisirCarteWagon(couleur, longueur, false)) j.getJeu().defausserCarteWagon(c);
        j.poserWagon(longueur);
        switch (longueur) {
            case 1 -> j.mettreAJourScore(1);
            case 2 -> j.mettreAJourScore(2);
            case 3 -> j.mettreAJourScore(4);
            case 4 -> j.mettreAJourScore(7);
            case 6 -> j.mettreAJourScore(15);
            case 8 -> j.mettreAJourScore(21);
        }
        proprietaire = j;
        j.log(String.format("%s a capturé la route %s.", j.toLog(), toLog()));
    }
}
