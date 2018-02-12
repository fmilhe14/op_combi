package planning;

import components.Grue;
import components.Navire;
import lombok.Getter;
import lombok.Setter;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.trace.Chatterbox;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;

@Getter
@Setter
public class PlanningNavire {

    private IntVar[][] planningNavire;

    private Navire[] navires; //Tous les navires de la journée, ordonnés par ID
    private int longueurQuai;
    private int dureeDUneJournee;

    private Solver solver;

    public PlanningNavire(int longueurQuai, int dureeDUneJournee, Navire[] navires, Solver solver) {

        this.longueurQuai = longueurQuai;
        this.dureeDUneJournee = dureeDUneJournee;
        this.solver = solver;
        this.navires = navires;
        int n = this.navires.length;

        int[] taillesDesBateaux = new int[n];
        for (int i = 0; i < n; i++) taillesDesBateaux[i] = navires[i].getLongueur();

        this.planningNavire = new IntVar[dureeDUneJournee][longueurQuai];

        int[] enveloppeNaviresAT = new int[n];

        for (int i = 0; i < n; i++) enveloppeNaviresAT[i] = this.navires[i].getId();

        for (int t = 0; t < dureeDUneJournee; t++) {
            for (int j = 0; j < longueurQuai; j++) {

                this.planningNavire[t][j] = VariableFactory.bounded("navire_present_a_la_position_" + j + "_a_t_" + t, 0, n, this.solver);
            }

            contrainteSiUnNavireNestPasPositionneATAlorsIlNestPasDansLePlanningAT(t);
        }

        contrainteLiantPlanningEtNavires();
    }

    private void contrainteSiUnNavireNestPasPositionneATAlorsIlNestPasDansLePlanningAT(int t) {

        for (int nav = 0; nav < this.navires.length; nav++) {

            Navire navire = this.navires[nav];
            BoolVar navirePasPositionneEnT = IntConstraintFactory.arithm(navire.getPositionsDuNavire()[t], "=", 0).reif();

            for (int pos = 0; pos < longueurQuai; pos++) {

                solver.post(IntConstraintFactory.arithm(navirePasPositionneEnT, "<=",
                        IntConstraintFactory.arithm(this.planningNavire[t][pos], "!=", navire.getId()).reif()));
            }
        }

    }

    private void contrainteLiantPlanningEtNavires() {

        for (int t = 0; t < this.dureeDUneJournee; t++) {

            for (int pos = 0; pos < longueurQuai; pos++) {

                for (Navire navire : this.navires) {

                    BoolVar navirePositionneEnPositionAT = IntConstraintFactory.arithm(navire.getPositionsDuNavire()[t], "=", pos + 1).reif();
                    BoolVar navireApparaitEnPositionDansLePlanning = IntConstraintFactory.arithm(this.planningNavire[t][pos], "="
                            , navire.getId()).reif();

                    solver.post(IntConstraintFactory.arithm(navirePositionneEnPositionAT, "<=", navireApparaitEnPositionDansLePlanning));

                    for (int pos1 = 0; pos1 < longueurQuai; pos1++) {

                        BoolVar pos1InferieurAPosPlusTailleBateau = IntConstraintFactory.arithm(VariableFactory.fixed(pos, solver), "+", VariableFactory.fixed(navire.getLongueur(), solver),
                                ">", pos1).reif();
                        BoolVar pos1SuperieurAPos = IntConstraintFactory.arithm(VariableFactory.fixed(pos, solver), "<=", pos1).reif();
                        BoolVar pos1DansLeBonIntervalle = IntConstraintFactory.arithm(pos1InferieurAPosPlusTailleBateau, "+", pos1SuperieurAPos, "=", 2).reif();
                        BoolVar navireEnPosEtPos1DansLeBonIntervalle = IntConstraintFactory.arithm(pos1DansLeBonIntervalle, "+", navirePositionneEnPositionAT, "=", 2).reif();
                        BoolVar planningATEnPos1VautNavireId = IntConstraintFactory.arithm(this.planningNavire[t][pos1], "=", this.planningNavire[t][pos]).reif();

                        solver.post(IntConstraintFactory.arithm(navireEnPosEtPos1DansLeBonIntervalle, "<=", planningATEnPos1VautNavireId));

                        BoolVar pos1PasDansLeBonIntervalle = IntConstraintFactory.arithm(pos1DansLeBonIntervalle, "=", 0).reif();
                        BoolVar navireEnPosEtPos1PasDansLeBonIntervalle = IntConstraintFactory.arithm(pos1PasDansLeBonIntervalle, "+", navirePositionneEnPositionAT, "=", 2).reif();
                        BoolVar planningEnPos1DifferentDePos = IntConstraintFactory.arithm(this.planningNavire[t][pos1], "!=", this.planningNavire[t][pos]).reif();

                        solver.post(IntConstraintFactory.arithm(navireEnPosEtPos1PasDansLeBonIntervalle, "<=", planningEnPos1DifferentDePos));


                        if(pos1 > 0){

                            BoolVar planningPos1Moins1EgalA0 = IntConstraintFactory.arithm(this.planningNavire[t][pos1-1], "=", 0).reif();
                       //     solver.post(IntConstraintFactory.arithm(navireEnPosEtPos1PasDansLeBonIntervalle, "<=", planningPos1Moins1EgalA0));

                            if(pos1 > 1){

                                BoolVar planningPos1Moins2EgalA0 = IntConstraintFactory.arithm(this.planningNavire[t][pos1-2], "=", 0).reif();
                        //        solver.post(IntConstraintFactory.arithm(navireEnPosEtPos1PasDansLeBonIntervalle, "<=", planningPos1Moins2EgalA0));
                            }
                        }
                    }
                }
            }
        }
    }

    public static void main(String[] args) {

        Solver solver = new Solver("");


        Navire navire = new Navire(1, 5, 2, 10, 1, 1, 3,
                new Grue[]{new Grue(0, 5, 10, 3, solver),
                        new Grue(1, 10, 10, 3, solver)}, solver);

        Navire navire1 = new Navire(2, 15, 3, 10, 1, 1, 3,
                new Grue[]{new Grue(0, 5, 10, 3, solver),
                        new Grue(1, 10, 10, 3, solver)}, solver);


        PlanningNavire planningNavire = new PlanningNavire(10, 3, new Navire[]{navire, navire1}, solver);

        solver.findSolution();
        Chatterbox.printStatistics(solver);
        solver.getVars();
    }
}
