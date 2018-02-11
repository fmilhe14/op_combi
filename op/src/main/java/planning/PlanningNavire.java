package planning;

import components.Grue;
import components.Navire;
import lombok.Getter;
import lombok.Setter;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.constraints.nary.cnf.LogOp;
import org.chocosolver.solver.constraints.set.SetConstraintsFactory;
import org.chocosolver.solver.trace.Chatterbox;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.VariableFactory;

@Getter
@Setter
public class PlanningNavire {

    private IntVar[][] planningNavire;
    private SetVar[] naviresPresentsAT;

    private Navire[] navires; //Tous les navires de la journée, ordonnés par ID
    private int longueurQuai;
    private int dateFinJournee;

    private Solver solver;

    public PlanningNavire(int longueurQuai, int dateFinJournee, Navire[] navires, Solver solver) {

        this.longueurQuai = longueurQuai;
        this.dateFinJournee = dateFinJournee;
        this.solver = solver;
        this.navires = navires;
        int n = this.navires.length;

        this.naviresPresentsAT = new SetVar[dateFinJournee];

        int[] taillesDesBateaux = new int[n];
        for (int i = 0; i < n; i++) taillesDesBateaux[i] = navires[i].getLongueur();

        this.planningNavire = new IntVar[dateFinJournee][longueurQuai];

        int[] enveloppeNaviresAT = new int[n];

        for (int i = 0; i < n; i++) enveloppeNaviresAT[i] = this.navires[i].getId();

        for (int i = 0; i < dateFinJournee; i++) {
            for (int j = 0; j < longueurQuai; j++) {

                this.planningNavire[i][j] = VariableFactory.bounded("navire_present_a_la_position_" + j + "_a_t_" + i, 0, n - 1, this.solver);
            }

            this.naviresPresentsAT[i] = VariableFactory.set("navires_presents_a_t_" + i, enveloppeNaviresAT, new int[]{},
                    this.solver);

            solver.post(SetConstraintsFactory.member(VariableFactory.fixed(0, solver), this.naviresPresentsAT[i]));

            navirePresentAT(i);
        }

        //    contraintesEspaceOccupeParLeNavireDansLePlanning();
        //       containteTantQuUnNavireNestPasPasseIlNestPasDansLePlanning();
        //  contrainteSiUnNavireEstDejaPasseIlNePeutPlusRepasser();
        contrainteSurLeTempsOuLeNavireResteAQuai();

    }


    //TODO la vitesse c'est pas encore bon
    private void contrainteSurLeTempsOuLeNavireResteAQuai() {

        for (int i = 0; i < dateFinJournee; i++) {

            for (int j = 1; j < this.navires.length; j++) {

                Navire navire = this.navires[j];

                BoolVar dateArriveeATPourLeNavireJ = IntConstraintFactory.arithm(navire.getXDateArrivee(), "=", i).reif();

                solver.post(IntConstraintFactory.arithm(dateArriveeATPourLeNavireJ, "<=", SetConstraintsFactory.member(VariableFactory.fixed(navire.getId(), solver),
                        this.naviresPresentsAT[i]).reif()));

                for (int k = 0; k < longueurQuai - navire.getLongueur(); k++) {

                    BoolVar bateauPlaceEnK = IntConstraintFactory.arithm(navire.getPositionDebut(), "=", k).reif();
                    BoolVar bateauArriveEnIEtPositionDebutEnK = IntConstraintFactory.arithm(dateArriveeATPourLeNavireJ, "+", bateauPlaceEnK,
                            "=", 2).reif();

                    solver.post(IntConstraintFactory.arithm(bateauArriveEnIEtPositionDebutEnK, "<=",
                            IntConstraintFactory.arithm(this.planningNavire[i][k], "=", navire.getId()).reif()));

                    for (int a = 0; a < navire.getLongueur(); a++) {

                        solver.post(IntConstraintFactory.arithm(bateauArriveEnIEtPositionDebutEnK, "<=",
                                IntConstraintFactory.arithm(this.planningNavire[i][k], "=", navire.getId()).reif()));
                    }

                    for (int l = i + 1; l < dateFinJournee; l++) {

                        BoolVar bateauPasEncoreDecharge = IntConstraintFactory.arithm(navire.getTempsResteAQuai(), "+",
                                navire.getXDateArrivee(), ">", l).reif();

                        BoolVar c3 = SetConstraintsFactory.member(VariableFactory.fixed(navire.getId(), solver), this.naviresPresentsAT[l]).reif();
                        solver.post(IntConstraintFactory.arithm(bateauPasEncoreDecharge, "=", c3));

                        BoolVar bateauArriveEnIEtPositionDebutEnKPasEncoreDechargeEnL = IntConstraintFactory
                                .arithm(bateauArriveEnIEtPositionDebutEnK, "+", bateauPasEncoreDecharge, "=", 2).reif();

                        for (int m = 1; m < navire.getLongueur(); m++) {

                            BoolVar c2 = IntConstraintFactory.arithm(this.planningNavire[l][m + k], "=", navire.getId()).reif();

                            solver.post(IntConstraintFactory.arithm(bateauArriveEnIEtPositionDebutEnKPasEncoreDechargeEnL, "<=", c2
                            ));
                            }
                    }
                }
            }
        }
    }

    //TODO les +2 et -2 apres le bateau
    private void contraintesEspaceOccupeParLeNavireDansLePlanning() {

        for (int t = 0; t < dateFinJournee; t++) {

            for (int j = 1; j < navires.length; j++) {

                Navire navire = this.navires[j];

                BoolVar navirePresentAT = SetConstraintsFactory.member(VariableFactory.fixed(navire.getId(), solver), this.naviresPresentsAT[t]).reif();

                for (int l = 0; l < longueurQuai; l++) {

                    BoolVar positionDebutEnL = IntConstraintFactory.arithm(navire.getPositionDebut(), "=", l).reif();
                    BoolVar positionDebutEnLAT = IntConstraintFactory.arithm(navirePresentAT, "=", positionDebutEnL).reif();

                    for (int k = l + 1; k < longueurQuai; k++) {

                        BoolVar kEntreLaPositionDebutEtLaFinDuNavire =
                                IntConstraintFactory.arithm(VariableFactory.fixed(l, solver),
                                        "+",
                                        VariableFactory.fixed(navire.getLongueur(), solver),
                                        ">",
                                        k
                                ).reif();

                        BoolVar b = IntConstraintFactory.arithm(positionDebutEnLAT, "+", kEntreLaPositionDebutEtLaFinDuNavire, "=", 2).reif();

                        solver.post(IntConstraintFactory.arithm(b, "=", IntConstraintFactory.arithm(this.planningNavire[t][k], "=", navire.getId())
                                .reif()));


                    }

                }
            }
        }
    }

    private void containteTantQuUnNavireNestPasPasseIlNestPasDansLePlanning() {

        for (int t = 0; t < dateFinJournee; t++) {

            for (int j = 1; j < this.getNavires().length; j++) {

                Navire navire = this.getNavires()[j];

                BoolVar navirePasPasse = IntConstraintFactory.arithm(navire.getXDateArrivee(), "<", t).reif();

                for (int position = 0; position < longueurQuai; position++) {

                    BoolVar navirePasDansLePlanningATEnPosition = IntConstraintFactory.arithm(this.planningNavire[t][position], "!=", navire.getId()).reif();

                    LogOp.implies(navirePasPasse, navirePasDansLePlanningATEnPosition);
                }
            }
        }
    }

    private void contrainteSiUnNavireEstDejaPasseIlNePeutPlusRepasser() {

        for (int t = 0; t < dateFinJournee; t++) {

            for (int j = 1; j < this.getNavires().length; j++) {

                Navire navire = this.getNavires()[j];

                BoolVar navireDejaPasse = IntConstraintFactory.arithm(navire.getXDateArrivee(), "+", navire.getTempsResteAQuai(), "<=", t).reif();

                BoolVar navireNePeutPlusPasser = SetConstraintsFactory.not_member(VariableFactory.fixed(navire.getId(), solver), this.naviresPresentsAT[t]).reif();

                solver.post(IntConstraintFactory.arithm(navireDejaPasse, "=", navireNePeutPlusPasser));

                for (int i = 0; i < this.longueurQuai; i++) {

                    BoolVar positionIDoitEtreDifferentDuNavire = IntConstraintFactory.arithm(this.planningNavire[t][i], "!=", navire.getId()).reif();
                    LogOp.implies(navireDejaPasse, positionIDoitEtreDifferentDuNavire);
                }

            }
        }

    }

    private void navirePresentAT(int t) {

        for (int k = 0; k < this.longueurQuai; k++) {

            this.solver.post(SetConstraintsFactory.member(this.planningNavire[t][k], this.naviresPresentsAT[t]));
        }
    }

    private void contraintTailleQuaiSuperieureACelleDesBateauxDessusAT(int t, int n, int[] taillesDesBateaux) {

        IntVar sizeMax = VariableFactory.bounded("size_max_present_a_t" + t, 0, this.longueurQuai - 2 * (n - 1), solver);

        this.solver.post(SetConstraintsFactory.sum(this.naviresPresentsAT[t], taillesDesBateaux
                , 0, sizeMax, false));
    }

    private void espaceOccupe(){

            for(int indexDate = 0 ; indexDate < dateFinJournee; indexDate ++){
                for(int indexPosition = 0; indexPosition < longueurQuai ; indexPosition ++){
                    for(int indexNavire = 1; indexNavire < this.navires.length; indexNavire ++){

                    Navire navire = this.navires[indexNavire];

                    BoolVar positionDebutNavireEnIndexPosition = IntConstraintFactory.arithm(navire.getPositionDebut(), "=", indexPosition).reif();

                    for(int taille = 1; taille < navire.getLongueur() && taille + indexPosition < longueurQuai; taille ++){

                     solver.post(IntConstraintFactory.arithm(positionDebutNavireEnIndexPosition, "="
                             , IntConstraintFactory.arithm(this.planningNavire[indexDate][indexPosition + taille], "=", indexNavire).reif()));

                    }

                    if(indexPosition > 0){

                        for(int positionDecroissante = indexPosition - 1; positionDecroissante >= 0; positionDecroissante --) {

                            solver.post(IntConstraintFactory.arithm(positionDebutNavireEnIndexPosition,
                                    "=",IntConstraintFactory.arithm(this.planningNavire[indexDate][positionDecroissante],
                                            "!=", indexNavire).reif()));

                        }

                    }

                    if(indexPosition < longueurQuai){

                        for(int positionCroissante = indexPosition + navire.getLongueur() ; positionCroissante < longueurQuai; positionCroissante ++) {

                            solver.post(IntConstraintFactory.arithm(positionDebutNavireEnIndexPosition,
                                    "=",IntConstraintFactory.arithm(this.planningNavire[indexDate][positionCroissante],
                                    "!=", indexNavire).reif()));

                        }

                    }
                }
            }

        }
    }

    public static void main(String[] args) {

        Solver solver = new Solver("");


        Navire navire0 = new Navire(0, 0, 0, 0, 0, 5, 3, solver,
                new Grue[]{new Grue(0, 1, 0, 5, solver)});

        navire0.setXDateArrivee(VariableFactory.fixed(0, solver));
        navire0.setTempsResteAQuai(VariableFactory.fixed(3, solver));


        Navire navire = new Navire(1, 1, 1, 1, 1, 5, 3, solver,
                new Grue[]{new Grue(0, 5, 0, 5, solver)});


        PlanningNavire planningNavire = new PlanningNavire(5, 3, new Navire[]{navire0, navire}, solver);

        solver.findSolution();
        Chatterbox.printStatistics(solver);
        solver.getVars();
    }
}
