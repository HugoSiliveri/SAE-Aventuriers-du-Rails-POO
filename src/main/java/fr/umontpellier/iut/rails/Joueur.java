package fr.umontpellier.iut.rails;

import java.util.*;
import java.util.stream.Collectors;

public class Joueur {

    /**
     * Les couleurs possibles pour les joueurs (pour l'interface graphique)
     */
    public static enum Couleur {
        JAUNE, ROUGE, BLEU, VERT, ROSE;
    }

    /**
     * Jeu auquel le joueur est rattaché
     */
    private Jeu jeu;
    /**
     * Nom du joueur
     */
    private String nom;
    /**
     * CouleurWagon du joueur (pour représentation sur le plateau)
     */
    private Couleur couleur;
    /**
     * Nombre de gares que le joueur peut encore poser sur le plateau
     */
    private int nbGares;
    /**
     * Nombre de wagons que le joueur peut encore poser sur le plateau
     */
    private int nbWagons;
    /**
     * Liste des missions à réaliser pendant la partie
     */
    private List<Destination> destinations;
    /**
     * Liste des cartes que le joueur a en main
     */
    private List<CouleurWagon> cartesWagon;
    /**
     * Liste temporaire de cartes wagon que le joueur est en train de jouer pour
     * payer la capture d'une route ou la construction d'une gare
     */
    private List<CouleurWagon> cartesWagonPosees;
    /**
     * Score courant du joueur (somme des valeurs des routes capturées)
     */
    private int score;

    public Joueur(String nom, Jeu jeu, Joueur.Couleur couleur) {
        this.nom = nom;
        this.jeu = jeu;
        this.couleur = couleur;
        nbGares = 3;
        nbWagons = 45;
        cartesWagon = new ArrayList<>();
        cartesWagonPosees = new ArrayList<>();
        destinations = new ArrayList<>();
        score = 12; // chaque gare non utilisée vaut 4 points
    }

    public String getNom() {
        return nom;
    }

    public Couleur getCouleur() {
        return couleur;
    }

    public int getNbWagons() {
        return nbWagons;
    }

    public Jeu getJeu() {
        return jeu;
    }

    public int getNbGares() {
        return nbGares;
    }

    public int getScore() {
        return score;
    }

    public List<CouleurWagon> getCartesWagonPosees() {
        return cartesWagonPosees;
    }

    public List<CouleurWagon> getCartesWagon() {
        return cartesWagon;
    }

    public List<Destination> getDestinations() {
        return destinations;
    }

    /**
     * Attend une entrée de la part du joueur (au clavier ou sur la websocket) et
     * renvoie le choix du joueur.
     * <p>
     * Cette méthode lit les entrées du jeu ({@code Jeu.lireligne()}) jusqu'à ce
     * qu'un choix valide (un élément de {@code choix} ou de {@code boutons} ou
     * éventuellement la chaîne vide si l'utilisateur est autorisé à passer) soit
     * reçu.
     * Lorsqu'un choix valide est obtenu, il est renvoyé par la fonction.
     * <p>
     * Si l'ensemble des choix valides ({@code choix} + {@code boutons}) ne comporte
     * qu'un seul élément et que {@code canPass} est faux, l'unique choix valide est
     * automatiquement renvoyé sans lire l'entrée de l'utilisateur.
     * <p>
     * Si l'ensemble des choix est vide, la chaîne vide ("") est automatiquement
     * renvoyée par la méthode (indépendamment de la valeur de {@code canPass}).
     * <p>
     * Exemple d'utilisation pour demander à un joueur de répondre à une question
     * par "oui" ou "non" :
     * <p>
     * {@code
     * List<String> choix = Arrays.asList("Oui", "Non");
     * String input = choisir("Voulez vous faire ceci ?", choix, new ArrayList<>(), false);
     * }
     * <p>
     * <p>
     * Si par contre on voulait proposer les réponses à l'aide de boutons, on
     * pourrait utiliser :
     * <p>
     * {@code
     * List<String> boutons = Arrays.asList("1", "2", "3");
     * String input = choisir("Choisissez un nombre.", new ArrayList<>(), boutons, false);
     * }
     *
     * @param instruction message à afficher à l'écran pour indiquer au joueur la
     *                    nature du choix qui est attendu
     * @param choix       une collection de chaînes de caractères correspondant aux
     *                    choix valides attendus du joueur
     * @param boutons     une collection de chaînes de caractères correspondant aux
     *                    choix valides attendus du joueur qui doivent être
     *                    représentés par des boutons sur l'interface graphique.
     * @param peutPasser  booléen indiquant si le joueur a le droit de passer sans
     *                    faire de choix. S'il est autorisé à passer, c'est la
     *                    chaîne de caractères vide ("") qui signifie qu'il désire
     *                    passer.
     * @return le choix de l'utilisateur (un élément de {@code choix}, ou de
     * {@code boutons} ou la chaîne vide)
     */
    public String choisir(String instruction, Collection<String> choix, Collection<String> boutons,
                          boolean peutPasser) {
        // on retire les doublons de la liste des choix
        HashSet<String> choixDistincts = new HashSet<>();
        choixDistincts.addAll(choix);
        choixDistincts.addAll(boutons);

        // Aucun choix disponible
        if (choixDistincts.isEmpty()) {
            return "";
        } else {
            // Un seul choix possible (renvoyer cet unique élément)
            if (choixDistincts.size() == 1 && !peutPasser)
                return choixDistincts.iterator().next();
            else {
                String entree;
                // Lit l'entrée de l'utilisateur jusqu'à obtenir un choix valide
                while (true) {
                    jeu.prompt(instruction, boutons, peutPasser);
                    entree = jeu.lireLigne();
                    // si une réponse valide est obtenue, elle est renvoyée
                    if (choixDistincts.contains(entree) || (peutPasser && entree.equals("")))
                        return entree;
                }
            }
        }
    }

    /**
     * Affiche un message dans le log du jeu (visible sur l'interface graphique)
     *
     * @param message le message à afficher (peut contenir des balises html pour la
     *                mise en forme)
     */
    public void log(String message) {
        jeu.log(message);
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner("\n");
        joiner.add(String.format("=== %s (%d pts) ===", nom, score));
        joiner.add(String.format("  Gares: %d, Wagons: %d", nbGares, nbWagons));
        joiner.add("  Destinations: "
                + destinations.stream().map(Destination::toString).collect(Collectors.joining(", ")));
        joiner.add("  Cartes wagon: " + CouleurWagon.listToString(cartesWagon));
        return joiner.toString();
    }

    /**
     * @return une chaîne de caractères contenant le nom du joueur, avec des balises
     * HTML pour être mis en forme dans le log
     */
    public String toLog() {
        return String.format("<span class=\"joueur\">%s</span>", nom);
    }

    /**
     * Renvoie une représentation du joueur sous la forme d'un objet Java simple
     * (POJO)
     */
    public Object asPOJO() {
        HashMap<String, Object> data = new HashMap<>();
        data.put("nom", nom);
        data.put("couleur", couleur);
        data.put("score", score);
        data.put("nbGares", nbGares);
        data.put("nbWagons", nbWagons);
        data.put("estJoueurCourant", this == jeu.getJoueurCourant());
        data.put("destinations", destinations.stream().map(Destination::asPOJO).collect(Collectors.toList()));
        data.put("cartesWagon", cartesWagon.stream().sorted().map(CouleurWagon::name).collect(Collectors.toList()));
        data.put("cartesWagonPosees",
                cartesWagonPosees.stream().sorted().map(CouleurWagon::name).collect(Collectors.toList()));
        return data;
    }

    /**
     * Propose une liste de cartes destinations, parmi lesquelles le joueur doit en
     * garder un nombre minimum n.
     * <p>
     * Tant que le nombre de destinations proposées est strictement supérieur à n,
     * le joueur peut choisir une des destinations qu'il retire de la liste des
     * choix, ou passer (en renvoyant la chaîne de caractères vide).
     * <p>
     * Les destinations qui ne sont pas écartées sont ajoutées à la liste des
     * destinations du joueur. Les destinations écartées sont renvoyées par la
     * fonction.
     *
     * @param destinationsPossibles liste de destinations proposées parmi lesquelles
     *                              le joueur peut choisir d'en écarter certaines
     * @param n                     nombre minimum de destinations que le joueur
     *                              doit garder
     * @return liste des destinations qui n'ont pas été gardées par le joueur
     */
    public List<Destination> choisirDestinations(List<Destination> destinationsPossibles, int n) {
        ArrayList<String> choix = new ArrayList<>();
        ArrayList<Destination> destinationsDefaussees = new ArrayList<>();
        for (Destination destinationsPossible : destinationsPossibles) {
            choix.add(destinationsPossible.toString());
        }
        boolean stop = false;
        while (n < destinationsPossibles.size() && !stop) {
            String dest = choisir(String.format("Sélectionnez au maximum %d destinations que vous ne voulez pas garder: ", destinationsPossibles.size()-n), new ArrayList<>(), choix, true);
            if (dest.equals("")) stop = true;
            else {
                int indice = choix.indexOf(dest);
                destinationsDefaussees.add(destinationsPossibles.remove(indice));
                choix.remove(indice);
            }
        }
        for (Destination d : destinationsPossibles) log(String.format("%s: Vous avez conservé la destination %s.", toLog(), d.getNom()));
        destinations.addAll(destinationsPossibles);
        return destinationsDefaussees;
    }

    /**
     * Exécute un tour de jeu du joueur.
     * <p>
     * Cette méthode attend que le joueur choisisse une des options suivantes :
     * - le nom d'une carte wagon face visible à prendre ;
     * - le nom "GRIS" pour piocher une carte wagon face cachée s'il reste des
     * cartes à piocher dans la pile de pioche ou dans la pile de défausse ;
     * - la chaîne "destinations" pour piocher des cartes destination ;
     * - le nom d'une ville sur laquelle il peut construire une gare (ville non
     * prise par un autre joueur, le joueur a encore des gares en réserve et assez
     * de cartes wagon pour construire la gare) ;
     * - le nom d'une route que le joueur peut capturer (pas déjà capturée, assez de
     * wagons et assez de cartes wagon) ;
     * - la chaîne de caractères vide pour passer son tour
     * <p>
     * Lorsqu'un choix valide est reçu, l'action est exécutée (il est possible que
     * l'action nécessite d'autres choix de la part de l'utilisateur, comme "choisir les cartes wagon à défausser pour capturer une route" ou
     * "construire une gare", "choisir les destinations à défausser", etc.)
     */
    public void jouerTour() {
        ArrayList<String> choix = new ArrayList<>();
        for (CouleurWagon carteWagon : jeu.getCartesWagonVisibles()) {
            choix.add(carteWagon.name());
        }
        for (Route route : jeu.getRoutes()) {
            if (route.estCapturablePar(this)) {
                choix.add(route.getNom());
            }
        }
        for (Ville ville : jeu.getVilles()) {
            if (ville.estCapturableParJoueur(this)) {
                choix.add(ville.getNom());
            }
        }
        choix.add("GRIS");
        choix.add("destinations");
        String choixJoueur = choisir("Que voulez-vous faire ?", choix, new ArrayList<>(), true);
        if (!choixJoueur.equals("")) {
            switch (choixJoueur) {
                case "GRIS" -> prendreCarteWagon(CouleurWagon.GRIS);
                case "destinations" -> prendreDestinations();
                case "LOCOMOTIVE" -> prendreLocomotiveVisible();
                default -> {
                    //Choix d'une route
                    for (Route route : jeu.getRoutes()) {
                        if (route.getNom().equals(choixJoueur)) {
                            route.capturerParJoueur(this);
                            return;
                        }
                    }

                    //Choix d'une ville
                    for (Ville ville : jeu.getVilles()) {
                        if (ville.getNom().equals(choixJoueur)) {
                            construireGare(ville);
                            return;
                        }
                    }

                    //Choix d'une carte visible.
                    for (CouleurWagon carte : jeu.getCartesWagonVisibles()) {
                        if (carte.name().equals(choixJoueur)) {
                            prendreCarteWagon(carte);
                            return;
                        }
                    }
                }
            }
        } else log(String.format("%s: Vous avez passé votre tour.", toLog()));
    }

    /**
     * Action de piocher une carte wahgon face visible ou dans la pioche lors d'un tour du joueur.
     *
     * @param c - Une carte wagon parmi celles face visible ou CouleurWagon.GRIS pour piocher dans la pile.
     */
    private void prendreCarteWagon(CouleurWagon c) {
        if (c == CouleurWagon.GRIS){
            this.piocherCarteWagon();
            prendreCarteSupplementaire();
        }
        else{
            log(String.format("%s: Vous avez pris une carte wagon visible %s.", toLog(), c.toLog()));
            cartesWagon.add(c);
            jeu.retirerCarteWagonVisible(c);
            prendreCarteSupplementaire();
        }

    }

    /**
     * Action de piocher une deuxième carte dans la pioche ou face visible lors d'un tour du joueur.
     */
    private void prendreCarteSupplementaire() {
        ArrayList<String> choix = new ArrayList<>();
        for (CouleurWagon carte : jeu.getCartesWagonVisibles()) {
            if (carte != CouleurWagon.LOCOMOTIVE) {
                choix.add(carte.name());
            }
        }
        choix.add("GRIS");
        String choixJoueur = choisir("Vous pouvez piocher une autre carte hors locomotive visible.", choix, new ArrayList<>(), true);
        if (!choixJoueur.equals("")) {
            if (choixJoueur.equals("GRIS")) {
                this.piocherCarteWagon();
            } else {
                CouleurWagon carte = CouleurWagon.valueOf(choixJoueur);
                log(String.format("%s: Vous avez pris une carte wagon visible %s.", toLog(), carte.toLog()));
                cartesWagon.add(carte);
                jeu.retirerCarteWagonVisible(carte);
            }
        }
    }

    /**
     * Action de piocher une carte Locomotive face visible lors d'un tour du joueur.
     */
    private void prendreLocomotiveVisible() {
        log(String.format("%s: Vous avez pris une carte %s visible.", toLog(), CouleurWagon.LOCOMOTIVE.toLog()));
        jeu.retirerCarteWagonVisible(CouleurWagon.LOCOMOTIVE);
        cartesWagon.add(CouleurWagon.LOCOMOTIVE);
    }

    /**
     * Action de piocher des destinations lors d'un tour du joueur.
     */
    private void prendreDestinations() {
        ArrayList<Destination> choix = new ArrayList<>();
        ArrayList<Destination> defausse = new ArrayList<>();
        for (int i=0; i<3;i++){
            choix.add(jeu.piocherDestination());
        }
        for(Destination dest : choisirDestinations(choix, 1)){
            jeu.defausserDestination(dest);
        }
    }

    /**
     * Action de construire une gare sur une ville lors d'un tour du joueur.
     *
     * @param ville - Une ville qui n'a pas de propriétaire et surlaquelle on peut donc construire une gare.
     */
    private void construireGare(Ville ville) {
        switch (nbGares) {
            case 3 -> {
                for (CouleurWagon c : choisirCarteWagon(CouleurWagon.GRIS, 1, false)) jeu.defausserCarteWagon(c);
                ville.setProprietaire(this);
                nbGares--;
                score -= 4;
                jeu.log(String.format("%s a construit une gare dans la ville %s.", toLog(), ville.toLog()));
            }
            case 2 -> {
                for (CouleurWagon c : choisirCarteWagon(CouleurWagon.GRIS, 2, false)) jeu.defausserCarteWagon(c);
                ville.setProprietaire(this);
                nbGares--;
                score -= 4;
                jeu.log(String.format("%s a construit une gare dans la ville %s.", toLog(), ville.toLog()));
            }
            case 1 -> {
                for (CouleurWagon c : choisirCarteWagon(CouleurWagon.GRIS, 3, false)) jeu.defausserCarteWagon(c);
                ville.setProprietaire(this);
                nbGares--;
                score -= 4;
                jeu.log(String.format("%s a construit une gare dans la ville %s.", toLog(), ville.toLog()));
            }
        }
    }

    /**
     * Demande au joueur de sélectionner des cartes parmi les cartes qu'il possède et renvoie la liste de cartes sélectionnées.
     *
     * @param couleur - La couleur des cartes à choisir, CouleurWagon.GRIS pour n'importe quelle couleur.
     * @param nbCarte - Le nombre de cartes à choisir.
     * @param passer - Booléen si le joueur peu passer la sélection de carte.
     * @return Une ArrayList de CouleurWagon qui ont été sélectionnées par le joueur.
     */
    public ArrayList<CouleurWagon> choisirCarteWagon(CouleurWagon couleur, int nbCarte, boolean passer) {
        ArrayList<CouleurWagon> cartes = new ArrayList<>();
        ArrayList<String> choix = new ArrayList<>();
        for (int i = 0; i < nbCarte; i++) {
            choix.clear();
            if (couleur != CouleurWagon.GRIS) {
                for (CouleurWagon c : cartesWagon) {
                    if ((c == couleur && Collections.frequency(cartesWagon, c) + Collections.frequency(cartesWagon, CouleurWagon.LOCOMOTIVE) >= nbCarte-i) || c == CouleurWagon.LOCOMOTIVE) choix.add(c.name());
                }
            } else {
                for (CouleurWagon c : cartesWagon) {
                    if ((Collections.frequency(cartesWagon, c) + Collections.frequency(cartesWagon, CouleurWagon.LOCOMOTIVE) >= nbCarte-i) || c == CouleurWagon.LOCOMOTIVE) choix.add(c.name());
                }
            }
            String carteChoisie = choisir(String.format("Veuillez choisir une de vos cartes wagon %s ou une Locomotive.", couleur.toString()), choix, new ArrayList<>(), passer);
            if (!carteChoisie.equals("")) {
                cartesWagon.remove(CouleurWagon.valueOf(carteChoisie));
                cartes.add(CouleurWagon.valueOf(carteChoisie));
                if (couleur == CouleurWagon.GRIS && CouleurWagon.valueOf(carteChoisie) != CouleurWagon.LOCOMOTIVE) couleur = CouleurWagon.valueOf(carteChoisie);
            } else {
                cartesWagon.addAll(cartes);
                cartes.clear();
                return cartes;
            }
        }
        return cartes;
    }

    /**
     * Pioche une carte de la pile de cartes Wagons et l'ajoute dans les cartes Wagon du Joueur.
     */
    public void piocherCarteWagon() {
        CouleurWagon carte = jeu.piocherCarteWagon();
        if (carte != null) {
            log(String.format("%s: vous avez pioché une carte wagon %s.", toLog(), carte.toLog()));
            cartesWagon.add(carte);
        }
    }

    /**
     * Retire des jetons wagons au Joueur.
     *
     * @param n - Un nombre de Wagon inférieur ou égal à nbWagons.
     */
    public void poserWagon(int n) {
        nbWagons -= n;
    }

    /**
     * Ajoute ou enlève des points au score du Joueur.
     *
     * @param n - Le nombre de points à ajouter ou à enlever au score si n < 0.
     */
    public void mettreAJourScore(int n) {
        score += n;
    }

    /**
     * Pose une carte wagon en attente pour la capture d'un Tunnel.
     *
     * @param carte - Une carte wagon.
     */
    public void poserCarteWagon(CouleurWagon carte) {
        cartesWagonPosees.add(carte);
    }

    /**
     * Ajoute une carte wagons dans la main du Joueur.
     *
     * @param carte - Une carte wagon.
     */
    public void ajouterCarteWagon(CouleurWagon carte) {
        cartesWagon.add(carte);
    }

    /**
     * Retire une carte de la main du Joueur.
     *
     * @param carte - Une Carte Wagon de la main du Joueur.
     */
    public void retirerCarteWagon(CouleurWagon carte) {
        cartesWagon.remove(carte);
    }
}
