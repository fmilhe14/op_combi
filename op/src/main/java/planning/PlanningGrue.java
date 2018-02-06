package planning;

import components.Grue;
import lombok.Getter;
import lombok.Setter;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;

@Getter
@Setter
public class PlanningGrue {

    private IntVar[][] planningGrue;
    private Grue[] grues;
    private Solver solver;

    private int dateFinJournee;
    private int nbGrues;
    private int longueurQuai;

    public PlanningGrue(int dateFinJournee, int nbGrues, int longueurQuai, Solver solver, Grue[] grues) {

        this.solver = solver;
        this.nbGrues = nbGrues;
        this.longueurQuai = longueurQuai;
        this.dateFinJournee = dateFinJournee;
        this.grues = grues;

        this.planningGrue = initialiserPlanningGrues(dateFinJournee, nbGrues);

        contraintesGruesNePeuventPasSeCroiser(dateFinJournee, nbGrues);
 //       contrainteUnOuvrierParGrue();


    }

    private void contraintesGruesNePeuventPasSeCroiser(int dateFinJournee, int nbGrues) {

        for (int i = 0; i < dateFinJournee; i++) {
            for (int j = 0; j < nbGrues; j++) {

                IntConstraintFactory.arithm(this.planningGrue[i][j], "<=", this.planningGrue[i][j + 1]);
            }
        }
    }

    private IntVar[][] initialiserPlanningGrues(int dateFinJournee, int nbGrues) {

        IntVar[][] planning = new IntVar[dateFinJournee][nbGrues];

        for (int i = 0; i < dateFinJournee; i++) {
            for (int j = 0; j < nbGrues; j++) {

                planning[i][j] = VariableFactory.bounded("position_grue_" + j + "_a_t_" + i, 0, longueurQuai, solver);
            }
        }

        return planning;
    }

    private void contrainteUnOuvrierParGrue(){

        IntVar[] ouvriers = new IntVar[grues.length];

        for(int i = 0; i<ouvriers.length; i++){

            ouvriers[i] = this.grues[i].getOuvrier();
        }

        this.solver.post(IntConstraintFactory.alldifferent(ouvriers));
    }
}
