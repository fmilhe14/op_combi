package components;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.constraints.set.SetConstraintsFactory;
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

        this.grues = grues;
        int nbGrues = this.grues.length;

        this.positionDebut = VariableFactory.bounded("position_navire_" + id, 0, longueurDuQuai - 1, solver);
        this.xDateArrivee = VariableFactory.bounded("date_arrivee_navire_" + id, 0, dateFinJournee, solver);

        int[] enveloppeGrues = new int[nbGrues];

        for (int i = 0; i < nbGrues; i++) enveloppeGrues[i] = i;

        this.gruesPresentes = VariableFactory.set("grues_pour_navire_" + id, enveloppeGrues, new int[]{}, solver);

        vitesse();
        tempsResteAQuai(dateFinJournee);
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
}

