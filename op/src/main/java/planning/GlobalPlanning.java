package planning;

import lombok.Getter;
import lombok.Setter;
import org.chocosolver.solver.Solver;


@Getter
@Setter
public class GlobalPlanning {

    private PlanningGrue planningGrue;
    private PlanningNavire planningNavire;
    private Solver solver;

    public GlobalPlanning(PlanningNavire planningNavire, PlanningGrue planningGrue, Solver solver) {

        this.planningGrue = planningGrue;
        this.planningNavire = planningNavire;
        this.solver = solver;

    }
}
