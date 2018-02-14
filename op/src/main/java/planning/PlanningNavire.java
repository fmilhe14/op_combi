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

            //Si dans la liste des positions d'un navire, sa valeur est à 0, alors on ne le met pas dans le planning à t
            contrainteSiUnNavireNestPasPositionneATAlorsIlNestPasDansLePlanningAT(t);
        }

        //Place les navires sur le planning, aux bonnes positions
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

                    }
                }
            }
        }
    }

    public IntVar[][] getPlanningNavire() {
        return planningNavire;
    }

    public void setPlanningNavire(IntVar[][] planningNavire) {
        this.planningNavire = planningNavire;
    }

    public Navire[] getNavires() {
        return navires;
    }

    public void setNavires(Navire[] navires) {
        this.navires = navires;
    }

    public int getLongueurQuai() {
        return longueurQuai;
    }

    public void setLongueurQuai(int longueurQuai) {
        this.longueurQuai = longueurQuai;
    }

    public int getDureeDUneJournee() {
        return dureeDUneJournee;
    }

    public void setDureeDUneJournee(int dureeDUneJournee) {
        this.dureeDUneJournee = dureeDUneJournee;
    }

    public Solver getSolver() {
        return solver;
    }

    public void setSolver(Solver solver) {
        this.solver = solver;
    }
}
