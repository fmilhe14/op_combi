package planning;

import components.Grue;
import components.Navire;
import components.Navire2;
import lombok.Getter;
import lombok.Setter;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.VariableFactory;

@Getter
@Setter
public class PlanningGrue {

    private IntVar[][] planningGrue;
    private Navire2[] navires;
    private Grue[] grues;
    private Solver solver;

    private int dateFinJournee;
    private int nbGrues;
    private int longueurQuai;

    public PlanningGrue(int dateFinJournee, int nbGrues, int longueurQuai, Solver solver, Navire2[] navires) {

        this.solver = solver;
        this.nbGrues = nbGrues;
        this.longueurQuai = longueurQuai;
        this.dateFinJournee = dateFinJournee;
        this.navires = navires;
        this.grues = this.navires[0].getGrues();

        this.planningGrue = initialiserPlanningGrues(dateFinJournee, nbGrues);

        contraintesGruesNePeuventPasSeCroiser();
        contrainteUneGrueNePeutTravaillerQueSurUnNavireALaFois();

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

    private void contraintesGruesNePeuventPasSeCroiser() {

        if(nbGrues > 1) {

            for(int i = 0; i < nbGrues - 1; i++){

                IntConstraintFactory.arithm(this.grues[i].getPosition(), "<" , this.grues[i+1].getPosition());

                }
        }
    }

    private void contrainteUneGrueNePeutTravaillerQueSurUnNavireALaFois(){

    //    SetVar[] = new SetVar[navires.length]

    }
}
