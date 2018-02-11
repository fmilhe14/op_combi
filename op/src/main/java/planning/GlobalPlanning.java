package planning;

import components.Navire;
import lombok.Getter;
import lombok.Setter;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.constraints.nary.cnf.LogOp;
import org.chocosolver.solver.constraints.set.SetConstraintsFactory;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.VariableFactory;


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

        contraintesPositionGruePositionNavire();
    }

    private void contraintesPositionGruePositionNavire() {

        for (int i = 0; i < this.planningNavire.getDateFinJournee(); i++) {

            SetVar naviresPresentsAT = this.planningNavire.getNaviresPresentsAT()[i];

            for(int j = 0; j < this.planningNavire.getNavires().length; j++){

                Navire navire =  this.planningNavire.getNavires()[j];

                BoolVar navireJPresentAT = SetConstraintsFactory.member(VariableFactory.fixed(j, solver), naviresPresentsAT).reif();

                for(int k = 0; k < this.planningGrue.getNbGrues(); k++){

                    BoolVar grueTravailleSurNavireJ = SetConstraintsFactory.member(VariableFactory.fixed(k, solver),
                            navire.getGruesPresentes()).reif();


                    BoolVar grueDansLeBonIntervalleBorneDroite = IntConstraintFactory.arithm(this.planningGrue.getPlanningGrue()[i][k], "-",
                            navire.getPositionDebut(), "<=", navire.getLongueur()).reif();
                    BoolVar grueDansLeBonIntervalleBorneGauche = IntConstraintFactory.arithm(this.planningGrue.getPlanningGrue()[i][k], ">=",
                            navire.getPositionDebut()).reif();

                    BoolVar navirePresentATEtGrueTravailleSurJ = IntConstraintFactory.arithm(navireJPresentAT, "=", grueTravailleSurNavireJ).reif();

                    LogOp.implies(navirePresentATEtGrueTravailleSurJ, grueDansLeBonIntervalleBorneDroite);
                    LogOp.implies(navirePresentATEtGrueTravailleSurJ, grueDansLeBonIntervalleBorneGauche);


                }
            }
        }
    }

}
