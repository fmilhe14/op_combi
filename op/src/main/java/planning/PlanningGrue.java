package planning;

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

    public PlanningGrue(int dateFinJournee, int nbGrues, int longueurQuai, Solver solver){

        this.planningGrue = new IntVar[dateFinJournee][nbGrues];

        for(int i = 0; i < dateFinJournee; i++){
            for(int j = 0; j < nbGrues; j++){

                this.planningGrue[i][j] = VariableFactory.bounded("position_grue_"+j+"_a_t_"+i, 0, longueurQuai, solver);
            }
        }

        contraintesGruesNePeuventPasSeCroiser(dateFinJournee, nbGrues);

    }

    private void contraintesGruesNePeuventPasSeCroiser(int dateFinJournee, int nbGrues){

        for(int i = 0; i<dateFinJournee; i++){
            for(int j = 0; j<nbGrues; j++){

                IntConstraintFactory.arithm(this.planningGrue[i][j], "<=", this.planningGrue[i][j+1]);
            }
        }
    }
}
