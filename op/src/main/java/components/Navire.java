package components;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.constraints.set.SetConstraintsFactory;
import org.chocosolver.solver.trace.Chatterbox;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.VariableFactory;

public class Navire {

    private int id;
    private int nbConteneurs;
    private int longueur;
    private int longueurDuQuai;
    private int dateDepartPrevue;
    private int coutPenalite;
    private int dureeDUneJournee;

    private IntVar dateArrivee;
    private IntVar[] vitesseDesGrues;
    private IntVar tempsResteAQuai;
    private IntVar[] positionsDuNavire;

    private SetVar[] gruesPresentes;

	private Grue[] grues;

    private Solver solver;

    public Navire(int id, int nbConteneurs, int longueur, int longueurDuQuai, int dateDepartPrevue, int coutPenalite, int dureeDUneJournee, Grue[] grues, Solver solver) {

        this.id = id;
        this.nbConteneurs = nbConteneurs;
        this.longueur = longueur;
        this.longueurDuQuai = longueurDuQuai;
        this.dateDepartPrevue = dateDepartPrevue;
        this.coutPenalite = coutPenalite;
        this.dureeDUneJournee = dureeDUneJournee;
        this.grues = grues;
        this.solver = solver;

        int[] enveloppeGrues = new int[grues.length];
        for (int i = 0; i < grues.length; i++) enveloppeGrues[i] = i;

        //Set des grues présentes à tout t sur le navire
        this.gruesPresentes = new SetVar[dureeDUneJournee];

        for (int t = 0; t < this.dureeDUneJournee; t++)
            this.gruesPresentes[t] = VariableFactory.set("grues_pour_navire_" + id, enveloppeGrues, new int[]{}, solver);

        this.dateArrivee = VariableFactory.bounded("date_arrivee_du_navire_" + this.id, 1, this.dureeDUneJournee, this.solver);

        //Position du navire comprise entre 0 et la longueur du quai, la position 0 correspond au planning non placé
        initialiserPositionsDuNavire();

        //Calcul de la vitesse de chargement/dechargement des grues placées sur le navire: somme des capacités des grues placées sur le navire
        initialiserVitesse();

        //Contrainte qui dit que le temps reste à quai * la vitesse de chargement/dechargement est égale au nombre de conteneurs (ne fonctionne que si nbConteneurs et vitesseDesGrues sont multiples)
        tempsResteAQuai(this.dureeDUneJournee);

        //Si le navire est à quai, il y a au moins une grue qui travaille dessus
        contrainteGruesQuandNavireAQuai();

        //Dans la liste des positions du navire : si le navire est présent, alors dans cette liste, à t, apparait la valeur de la position de debut du navire, s'il n'est pas là : 0
        contraintePositionTempsResteAQuai();

        //Position des grues qui travaillent sur le navire comprise entre [debutDuNavire, debutDuNavire + longueurNavire]
        contraintePositionGrueAT();

        //La position de début d'un navire est fonction de sa longueur (on ne peut pas le placer sur une unité, s'il ne rentre pas)
        contrainteEspaceOccupe();

        //Tous les navires doivent apparaître dans le planning
        contrainteNavireDoitPartirAvantLaFinDeLaJournee();
    }

    private void initialiserPositionsDuNavire() {

        this.positionsDuNavire = new IntVar[this.dureeDUneJournee];

        for (int t = 0; t < this.dureeDUneJournee; t++) {

            this.positionsDuNavire[t] = VariableFactory.bounded("position_du_navire_" + this.id + "_a_t_" + t,
                    0, longueurDuQuai, this.solver);
        }
    }

    private void initialiserVitesse() {

        this.vitesseDesGrues = new IntVar[dureeDUneJournee];

        int[] vitesseDeChaqueGrue = new int[this.grues.length];
        int vitesseMax = 0;

        for (int i = 0; i < this.grues.length; i++) {

            vitesseDeChaqueGrue[i] = this.grues[i].getCapacite();
            vitesseMax += grues[i].getCapacite();
        }


        for (int t = 0; t < this.dureeDUneJournee; t++) {

            this.vitesseDesGrues[t] = VariableFactory.bounded("vitesse_de_chargement_ou_dechargement_sur_le_navire_" + this.id, 0, vitesseMax, solver);

            BoolVar navirePasPositionne = IntConstraintFactory.arithm(this.getPositionsDuNavire()[t], "=", 0).reif();
            BoolVar vitesseNulle = IntConstraintFactory.arithm(this.vitesseDesGrues[t], "=", 0).reif();

            solver.post(IntConstraintFactory.arithm(navirePasPositionne, "<=", vitesseNulle));

            BoolVar vitesse = SetConstraintsFactory.sum(this.gruesPresentes[t], vitesseDeChaqueGrue
                    , 0, vitesseDesGrues[t], false).reif();

            solver.post(IntConstraintFactory.arithm(navirePasPositionne.not(), "<=", vitesse));


        }
    }

    private void contraintePositionTempsResteAQuai() {

        for (int t = 0; t < this.dureeDUneJournee; t++) {

            BoolVar navireArriveAT = IntConstraintFactory.arithm(this.dateArrivee, "=", t + 1).reif();
            BoolVar positionDifferenteDe0 = IntConstraintFactory.arithm(this.positionsDuNavire[t], ">", 0).reif();

            solver.post(IntConstraintFactory.arithm(navireArriveAT, "<=", positionDifferenteDe0));

            for (int t1 = 0; t1 < this.dureeDUneJournee; t1++) {

                BoolVar t1InferieurATPlusTempsResteAQuai = IntConstraintFactory.arithm(VariableFactory.fixed(t, solver), "+", tempsResteAQuai,
                        ">", t1).reif();
                BoolVar t1SuperieurAT = IntConstraintFactory.arithm(VariableFactory.fixed(t, solver), "<=", t1).reif();

                BoolVar t1DansLeBonIntervalle = IntConstraintFactory.arithm(t1InferieurATPlusTempsResteAQuai, "+", t1SuperieurAT, "=", 2).reif();

                BoolVar navireArriveEnTEtT1DansLeBonInvervalle = IntConstraintFactory.arithm(t1DansLeBonIntervalle, "+", navireArriveAT, "=", 2).reif();

                BoolVar positionNavireEnT1 = IntConstraintFactory.arithm(this.positionsDuNavire[t1], "=", this.positionsDuNavire[t]).reif();

                solver.post(IntConstraintFactory.arithm(navireArriveEnTEtT1DansLeBonInvervalle, "<=", positionNavireEnT1));

                BoolVar t1PasDansLeBonIntervalle = IntConstraintFactory.arithm(t1DansLeBonIntervalle, "=", 0).reif();

                BoolVar navireArriveEnTEtT1PasDansLeBonIntervalle = IntConstraintFactory.arithm(t1PasDansLeBonIntervalle, "+", navireArriveAT, "=", 2).reif();
                BoolVar positionNavireEnT1EgaleA0 = IntConstraintFactory.arithm(this.positionsDuNavire[t1], "=", 0).reif();

                solver.post(IntConstraintFactory.arithm(navireArriveEnTEtT1PasDansLeBonIntervalle, "<=", positionNavireEnT1EgaleA0));


            }
        }
    }

    private void contrainteEspaceOccupe() {

        for (int t = 0; t < this.dureeDUneJournee; t++) {

            solver.post(IntConstraintFactory.arithm(this.positionsDuNavire[t], "+",
                    VariableFactory.fixed(this.longueur, solver), "<=",
                    this.longueurDuQuai + 1));
        }
    }

    private void tempsResteAQuai(int dateFinJournee) {

        this.tempsResteAQuai = VariableFactory.bounded("temps_reste_a_quai_pour_le_navire_" + id, 1, dateFinJournee, solver);

        for (int t = 0; t < dureeDUneJournee; t++) {

            BoolVar tEgalDateArrivee = IntConstraintFactory.arithm(this.dateArrivee, "=", t + 1).reif();
            BoolVar c = IntConstraintFactory.times(this.tempsResteAQuai, this.vitesseDesGrues[t], VariableFactory.fixed(this.nbConteneurs, solver)).reif();
            solver.post(IntConstraintFactory.arithm(tEgalDateArrivee, "<=", c));


        }
    }

    private void contrainteGruesQuandNavireAQuai() {

        for (int t = 0; t < this.dureeDUneJournee; t++) {

            BoolVar navireAQuai = IntConstraintFactory.arithm(this.positionsDuNavire[t], ">", 0).reif();
            BoolVar setGruesPasVide = SetConstraintsFactory.notEmpty(this.gruesPresentes[t]).reif();

            solver.post(IntConstraintFactory.arithm(navireAQuai, "<=", setGruesPasVide));
            solver.post(IntConstraintFactory.arithm(navireAQuai.not(), "<=", setGruesPasVide.not()));
        }
    }

    private void contraintePositionGrueAT() {

        for (int t = 0; t < this.dureeDUneJournee; t++) {

            BoolVar navirePositionne = IntConstraintFactory.arithm(this.positionsDuNavire[t], ">", 0).reif();

            for (Grue grue : this.grues) {

                BoolVar gruePresenteSurLeNavire = SetConstraintsFactory.member(VariableFactory.fixed(grue.getId(), solver), this.gruesPresentes[t]).reif();

                BoolVar navirePositionneEtGruePresenteSurLeNavire = IntConstraintFactory.arithm(navirePositionne, "+", gruePresenteSurLeNavire, "=", 2).reif();

                BoolVar positionGrueSuperieurOuEgaleAuDebutNavire = IntConstraintFactory.arithm(positionsDuNavire[t], "<=", grue.getPositions()[t]).reif();
                BoolVar positionGrueInferieureOuEgaleAuDebutNavirePlusSaLongueur =
                        IntConstraintFactory.arithm(grue.getPositions()[t], "-", positionsDuNavire[t],
                                "<=", longueur).reif();


                BoolVar positionGrueDansLeBonIntervalle = IntConstraintFactory.arithm(positionGrueSuperieurOuEgaleAuDebutNavire, "+",
                        positionGrueInferieureOuEgaleAuDebutNavirePlusSaLongueur, "=", 2).reif();

                solver.post(IntConstraintFactory.arithm(navirePositionneEtGruePresenteSurLeNavire, "<=", positionGrueDansLeBonIntervalle));
            }
        }
    }

    private void contrainteNavireDoitPartirAvantLaFinDeLaJournee() {

        solver.post(IntConstraintFactory.arithm(dateArrivee, "+", tempsResteAQuai, "<=", dureeDUneJournee));
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNbConteneurs() {
        return nbConteneurs;
    }

    public void setNbConteneurs(int nbConteneurs) {
        this.nbConteneurs = nbConteneurs;
    }

    public int getLongueur() {
        return longueur;
    }

    public void setLongueur(int longueur) {
        this.longueur = longueur;
    }

    public int getLongueurDuQuai() {
        return longueurDuQuai;
    }

    public void setLongueurDuQuai(int longueurDuQuai) {
        this.longueurDuQuai = longueurDuQuai;
    }

    public int getDateDepartPrevue() {
        return dateDepartPrevue;
    }

    public void setDateDepartPrevue(int dateDepartPrevue) {
        this.dateDepartPrevue = dateDepartPrevue;
    }

    public int getCoutPenalite() {
        return coutPenalite;
    }

    public void setCoutPenalite(int coutPenalite) {
        this.coutPenalite = coutPenalite;
    }

    public int getDureeDUneJournee() {
        return dureeDUneJournee;
    }

    public void setDureeDUneJournee(int dureeDUneJournee) {
        this.dureeDUneJournee = dureeDUneJournee;
    }

    public IntVar getDateArrivee() {
        return dateArrivee;
    }

    public void setDateArrivee(IntVar dateArrivee) {
        this.dateArrivee = dateArrivee;
    }

    public IntVar[] getVitesseDesGrues() {
        return vitesseDesGrues;
    }

    public void setVitesseDesGrues(IntVar[] vitesseDesGrues) {
        this.vitesseDesGrues = vitesseDesGrues;
    }

    public IntVar getTempsResteAQuai() {
        return tempsResteAQuai;
    }

    public void setTempsResteAQuai(IntVar tempsResteAQuai) {
        this.tempsResteAQuai = tempsResteAQuai;
    }

    public IntVar[] getPositionsDuNavire() {
        return positionsDuNavire;
    }

    public void setPositionsDuNavire(IntVar[] positionsDuNavire) {
        this.positionsDuNavire = positionsDuNavire;
    }

    public SetVar[] getGruesPresentes() {
        return gruesPresentes;
    }

    public void setGruesPresentes(SetVar[] gruesPresentes) {
        this.gruesPresentes = gruesPresentes;
    }

    public Grue[] getGrues() {
        return grues;
    }

    public void setGrues(Grue[] grues) {
        this.grues = grues;
    }

    public Solver getSolver() {
        return solver;
    }

    public void setSolver(Solver solver) {
        this.solver = solver;
    }
}
