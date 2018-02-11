package components;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.constraints.set.SetConstraintsFactory;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.VariableFactory;

@Getter
@Setter
@EqualsAndHashCode
public class Navire {

    private int id;
    private int nbConteneursInitial;

    private IntVar xDateArrivee;
    private IntVar vitesseDesGrues;
    private int dateDeDepartPrevue;
    private int coutDeRetard;
    private IntVar tempsResteAQuai;

    private IntVar positionDebut;
    private int longueur;
    private SetVar gruesPresentes;
    private Grue[] grues;

    private Solver solver;

    public Navire(int id, int nbConteneursInitial, int dateDeDepartPrevue, int coutDeRetard, int longueur, int longueurDuQuai, int dateFinJournee,
                  Solver solver, Grue[] grues) {

        this.id = id;
        this.nbConteneursInitial = nbConteneursInitial;
        this.dateDeDepartPrevue = dateDeDepartPrevue;
        this.coutDeRetard = coutDeRetard;
        this.longueur = longueur;
        this.solver = solver;

        this.grues = grues;
        int nbGrues = this.grues.length;

        this.positionDebut = VariableFactory.bounded("position_navire_" + id, 0, longueurDuQuai-longueur, solver);
        this.xDateArrivee = VariableFactory.bounded("date_arrivee_navire_" + id, 0, dateFinJournee-1, solver);

        int[] enveloppeGrues = new int[nbGrues];

        for (int i = 0; i < nbGrues; i++) enveloppeGrues[i] = i;

        this.gruesPresentes = VariableFactory.set("grues_pour_navire_" + id, enveloppeGrues, new int[]{}, solver);

        vitesse();
    //    tempsResteAQuai(dateFinJournee);
    ///    navireDoitPartirAvantLaFinDeLaJournee(dateFinJournee);
        laPositionDuneGrueQuiTravailleSurUnNavireEstComprisEntreLeDebutDunNavireEtLaFinDeCeluiCi();
    }

    private void tempsResteAQuai(int dateFinJournee) {

        this.tempsResteAQuai = VariableFactory.bounded("temps_reste_a_quai_pour_le_navire_" + id, 0, dateFinJournee, solver);
        Constraint c = IntConstraintFactory.times(this.tempsResteAQuai, this.vitesseDesGrues, VariableFactory.fixed(this.nbConteneursInitial, solver));
        solver.post(c);

    }

    private void vitesse() {

        int[] vitesseDeChaqueGrue = new int[this.grues.length];
        int vitesseMax = 0;

        for (int i = 0; i < this.grues.length; i++) {

            vitesseDeChaqueGrue[i] = this.grues[i].getCapacite();
            vitesseMax += grues[i].getCapacite();
        }


        this.vitesseDesGrues = VariableFactory.bounded("vitesse_de_chargement_ou_dechargement_sur_le_navire_" + id, 0, vitesseMax, solver);

        this.solver.post(SetConstraintsFactory.sum(this.gruesPresentes, vitesseDeChaqueGrue
                , 0, vitesseDesGrues, false));
    }

    private void navireDoitPartirAvantLaFinDeLaJournee(int dateFinJournee) {

        solver.post(IntConstraintFactory.arithm(this.getXDateArrivee(), "+", this.getTempsResteAQuai(), "<", dateFinJournee));

    }

    private void laPositionDuneGrueQuiTravailleSurUnNavireEstComprisEntreLeDebutDunNavireEtLaFinDeCeluiCi() {

        for (Grue grue: this.grues) {

            BoolVar grueTravailleSurLeNavire = SetConstraintsFactory.member(VariableFactory.fixed(grue.getId(), solver), this.gruesPresentes).reif();
            BoolVar positionGrueDansLeBonIntervalBorneGauche = IntConstraintFactory.arithm(grue.getPosition(), ">=", this.getPositionDebut()).reif();
            BoolVar positionGrueDansLeBonIntervalBorneDroite = IntConstraintFactory.arithm(grue.getPosition(), "-", this.getPositionDebut(), "<=", longueur).reif();

            Constraint c = IntConstraintFactory.arithm(grueTravailleSurLeNavire, "=", positionGrueDansLeBonIntervalBorneDroite);
            Constraint c1 = IntConstraintFactory.arithm(grueTravailleSurLeNavire, "=", positionGrueDansLeBonIntervalBorneGauche);

            solver.post(c, c1);

        }
    }
}

