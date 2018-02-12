package planning;

import components.Grue;
import components.Navire;
import lombok.Getter;
import lombok.Setter;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.constraints.set.SetConstraintsFactory;
import org.chocosolver.solver.trace.Chatterbox;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.VariableFactory;

@Getter
@Setter
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

        contraintesGruesNePeuventPasSeCroiser();
        contrainteUneGrueNePeutTravaillerQueSurUnNavireALaFois();
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

    public static void main(String[] args) {

        Solver solver = new Solver("");

        int longueurQuai = 12;
        int dureeDuneJournee = 32;

        Grue[] grues = new Grue[]{new Grue(0, 5, longueurQuai, dureeDuneJournee, solver),
                new Grue(1, 10, longueurQuai, dureeDuneJournee, solver),
                new Grue(2, 5, longueurQuai, dureeDuneJournee, solver),
                new Grue(3, 5, longueurQuai, dureeDuneJournee, solver),
                new Grue(4, 15, longueurQuai, dureeDuneJournee, solver),
                new Grue(5, 5, longueurQuai, dureeDuneJournee, solver),
                new Grue(6, 25, longueurQuai, dureeDuneJournee, solver),
                new Grue(7, 5, longueurQuai, dureeDuneJournee, solver)
        };

        Navire navire = new Navire(1, 25, 2, longueurQuai, 1, 1, dureeDuneJournee,
                grues, solver);

        Navire navire1 = new Navire(2, 35, 3, longueurQuai, 1, 1, dureeDuneJournee,
                grues, solver);

        Navire navire2 = new Navire(3, 60, 2, longueurQuai, 1, 1, dureeDuneJournee,
                grues, solver);

        Navire navire3 = new Navire(4, 50, 5, longueurQuai, 1, 1, dureeDuneJournee,
                grues, solver);

        Navire navire4 = new Navire(5, 15, 3, longueurQuai, 1, 1, dureeDuneJournee,
                grues, solver);

        Navire navire5 = new Navire(6, 30, 4, longueurQuai, 1, 1, dureeDuneJournee,
                grues, solver);

        Navire navire6 = new Navire(7, 10, 2, longueurQuai, 1, 1, dureeDuneJournee,
                grues, solver);

        Navire navire7 = new Navire(8, 25, 2, longueurQuai, 1, 1, dureeDuneJournee,
                grues, solver);

        Navire navire8 = new Navire(9, 15, 2, longueurQuai, 1, 1, dureeDuneJournee,
                grues, solver);

        Navire navire9 = new Navire(10, 5, 4, longueurQuai, 1, 1, dureeDuneJournee,
                grues, solver);

        Navire navire10 = new Navire(11, 20, 2, longueurQuai, 1, 1, dureeDuneJournee,
                grues, solver);


        Navire[] navires = new Navire[]{navire, navire1, navire2, navire3, navire4, navire5, navire6, navire7, navire8, navire9, navire10};

        PlanningNavire planningNavire = new PlanningNavire(longueurQuai, dureeDuneJournee, navires, solver);

        PlanningGrue planningGrue = new PlanningGrue(dureeDuneJournee, longueurQuai, solver, navires);

        long start = System.currentTimeMillis();
        long time;

        solver.findSolution();

        time = System.currentTimeMillis();
        System.out.println(time - start);

        Chatterbox.printStatistics(solver);
    }
}

