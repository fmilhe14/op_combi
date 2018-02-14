package planning;


import components.Grue;
import components.Navire;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.constraints.set.SetConstraintsFactory;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.VariableFactory;

public class PlanningGrue {

    private IntVar[][] planningGrue;
    private Navire[] navires;
    private Grue[] grues;
    private Solver solver;

    private int dureeDUneJournee;
    private int nbGrues;
    private int longueurQuai;

    public PlanningGrue(int dureeDUneJournee, int longueurQuai, Solver solver, Navire[] navires) {

        this.solver = solver;
        this.longueurQuai = longueurQuai;
        this.dureeDUneJournee = dureeDUneJournee;
        this.navires = navires;
        this.grues = this.navires[0].getGrues();
        this.nbGrues = this.grues.length;

        this.planningGrue = initialiserPlanningGrues(dureeDUneJournee, nbGrues);

        //Les grues ne peuvent pas se croiser
        contraintesGruesNePeuventPasSeCroiser();

        //Une grue ne peut travailler
        contrainteUneGrueNePeutTravaillerQueSurUnNavireALaFois();

        //Apres le depart d'un navire, la grue reste à sa position et ne travaille pas pendant 15min
        contrainteUneGrueFaitUnePauseApresAvoirDechargeUnNavire();
    }

    private IntVar[][] initialiserPlanningGrues(int dateFinJournee, int nbGrues) {

        IntVar[][] planning = new IntVar[dateFinJournee][nbGrues];

        for (int t = 0; t < dateFinJournee; t++) {
            for (int j = 0; j < nbGrues; j++) {

                planning[t][j] = this.grues[j].getPositions()[t];
            }
        }

        return planning;
    }

    private void contraintesGruesNePeuventPasSeCroiser() {

        if (nbGrues > 1) {

            for (int t = 0; t < this.dureeDUneJournee; t++) {

                for (int i = 0; i < nbGrues - 1; i++) {

                    solver.post(IntConstraintFactory.arithm(this.grues[i].getPositions()[t], "<", this.grues[i + 1].getPositions()[t]));
                }
            }
        }
    }

    private void contrainteUneGrueNePeutTravaillerQueSurUnNavireALaFois() {

        for (int t = 0; t < this.dureeDUneJournee; t++) {

            SetVar[] setVars = new SetVar[this.navires.length];

            for (int j = 0; j < this.navires.length; j++) {

                setVars[j] = this.navires[j].getGruesPresentes()[t];
            }

            solver.post(SetConstraintsFactory.all_disjoint(setVars));
        }
    }

    private void contrainteUneGrueFaitUnePauseApresAvoirDechargeUnNavire() {

        for (int t = 1; t < this.dureeDUneJournee; t++) {

            for (Navire navire : this.navires) {

                BoolVar navirePartAT = IntConstraintFactory.arithm(navire.getDateArrivee(), "+", navire.getTempsResteAQuai(), "=", t + 1).reif();

                for (Grue grue : this.grues) {

                    BoolVar grueTravaillaitSurNavireATMoins1 = SetConstraintsFactory.member(VariableFactory.fixed(grue.getId(), solver),
                            navire.getGruesPresentes()[t - 1]).reif();

                    BoolVar grueSurNavireATMoins1EtNavirePartAT = IntConstraintFactory.arithm(navirePartAT, "+", grueTravaillaitSurNavireATMoins1,
                            "=", 2).reif();

                    BoolVar grueResteALaMemePositionAT = IntConstraintFactory.arithm(grue.getPositions()[t], "=", grue.getPositions()[t - 1]).reif();

                    solver.post(IntConstraintFactory.arithm(grueSurNavireATMoins1EtNavirePartAT, "<=", grueResteALaMemePositionAT));

                }
            }
        }
    }

    public IntVar[][] getPlanningGrue() {
        return planningGrue;
    }

    public void setPlanningGrue(IntVar[][] planningGrue) {
        this.planningGrue = planningGrue;
    }

    public Navire[] getNavires() {
        return navires;
    }

    public void setNavires(Navire[] navires) {
        this.navires = navires;
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

    public int getDureeDUneJournee() {
        return dureeDUneJournee;
    }

    public void setDureeDUneJournee(int dureeDUneJournee) {
        this.dureeDUneJournee = dureeDUneJournee;
    }

    public int getNbGrues() {
        return nbGrues;
    }

    public void setNbGrues(int nbGrues) {
        this.nbGrues = nbGrues;
    }

    public int getLongueurQuai() {
        return longueurQuai;
    }

    public void setLongueurQuai(int longueurQuai) {
        this.longueurQuai = longueurQuai;
    }
}


