package planning;

import components.Navire;
import lombok.Getter;
import lombok.Setter;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.constraints.nary.cnf.LogOp;
import org.chocosolver.solver.constraints.set.SetConstraintsFactory;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.VariableFactory;

@Getter
@Setter
public class PlanningNavire {

    //TODO faire le lien entre les setvar et les colonnes
    //TODO lien entre le planning navire et le planning grue

    private IntVar[][] planningNavire;
    private SetVar[] naviresPresentsAT;
    private Navire[] navires; //Tous les navires de la journée, ordonnés par ID

    private int longueurQuai;
    private int dateFinJournee;

    private Solver solver;

    public PlanningNavire(int longueurQuai, int dateFinJournee, Solver solver) {

        this.longueurQuai = longueurQuai;
        this.dateFinJournee = dateFinJournee;
        this.solver = solver;
        int n = navires.length;

        this.naviresPresentsAT = new SetVar[dateFinJournee];

        int[] taillesDesBateaux = new int[n];
        for (int i = 0; i < n; i++) taillesDesBateaux[i] = navires[i].getLongueur();

        this.planningNavire = new IntVar[dateFinJournee][longueurQuai];
        int[] enveloppeNaviresAT = new int[n + 1];

        for (int i = 0; i < n + 1; i++) enveloppeNaviresAT[i] = i;

        for (int i = 0; i < dateFinJournee; i++) {
            for (int j = 0; j < longueurQuai; j++) {

                this.planningNavire[i][j] = VariableFactory.bounded("navire_present_a_la_position_" + i + "_a_t_" + j, 0, n + 1, this.solver);
            }

            this.naviresPresentsAT[i] = VariableFactory.set("navires_presents_a_t_" + i, enveloppeNaviresAT, new int[]{},
                    this.solver);

            contraintTailleQuaiSuperieureACelleDesBateauxDessusAT(i, n, taillesDesBateaux);
        }

        contraintesEspaceOccupeParLeNavireDansLePlanning();
        contrainteSurLeTempsOuLeNavireResteAQuai();
    }

    //TODO la vitesse c'est pas encore bon
    private void contrainteSurLeTempsOuLeNavireResteAQuai() {

        for (int i = 0; i < dateFinJournee; i++) {

            for (int j = 0; j < this.navires.length; j++) {

                BoolVar dateArriveeATPourLeNavireJ = IntConstraintFactory.arithm(this.navires[j].getXDateArrivee(), "=", i).reif();

                for (int k = 0; k < longueurQuai; k++) {

                    BoolVar bateauPlaceEnK = IntConstraintFactory.arithm(this.navires[j].getPositionDebut(), "=", k).reif();

                    BoolVar c = IntConstraintFactory.arithm(dateArriveeATPourLeNavireJ, "=", bateauPlaceEnK).reif();

                    for (int l = i; l < dateFinJournee; l++) {

                        BoolVar bateauPasEncoreDecharge = IntConstraintFactory.arithm(this.navires[j].getTempsResteAQuai(), "+", this.navires[j].getXDateArrivee(), "<=", l).reif();

                        BoolVar c1 = IntConstraintFactory.arithm(c, "=", bateauPasEncoreDecharge).reif();
                        BoolVar c2 = IntConstraintFactory.arithm(this.planningNavire[l][k], "=", this.navires[j].getId()).reif();

                        LogOp.implies(c1, c2);
                    }
                }
            }
        }
    }

    private void contraintesEspaceOccupeParLeNavireDansLePlanning() {

        for (int i = 0; i < dateFinJournee; i++) {

            for (int j = 0; j < navires.length; j++) {

                int navireId = this.navires[j].getId();

                BoolVar navirePresentAT = SetConstraintsFactory.member(VariableFactory.fixed(navireId, solver), this.naviresPresentsAT[i]).reif();

                for (int l = 0; l < longueurQuai; l++) {

                    BoolVar positionDebutNavireEnL = IntConstraintFactory.arithm(this.planningNavire[i][l], "=", navireId).reif();
                    LogOp.implies(positionDebutNavireEnL, navirePresentAT);

                    int tailleNavire = this.navires[i].getLongueur();

                    if(l>0){

                        BoolVar pasDeNavireLaPositionDAvant= IntConstraintFactory.arithm(this.planningNavire[i][l-1], "=", 0).reif();
                        IntConstraintFactory.arithm(positionDebutNavireEnL, "=", pasDeNavireLaPositionDAvant);

                        if(l>1) {

                            BoolVar pasDeNavireDeuxPositionsAvant = IntConstraintFactory.arithm(this.planningNavire[i][l-2], "=", 0).reif();
                            IntConstraintFactory.arithm(positionDebutNavireEnL, "=", pasDeNavireDeuxPositionsAvant);
                        }

                    }

                    if(l<longueurQuai-1){

                        BoolVar pasDeNavireLaPositionDApres= IntConstraintFactory.arithm(this.planningNavire[i][l+1], "=", 0).reif();
                        IntConstraintFactory.arithm(positionDebutNavireEnL, "=", pasDeNavireLaPositionDApres);

                        if(l < longueurQuai-2){

                            BoolVar pasDeNavireDeuxPositionsApres = IntConstraintFactory.arithm(this.planningNavire[i][l+2], "=", 0).reif();
                            IntConstraintFactory.arithm(positionDebutNavireEnL, "=", pasDeNavireDeuxPositionsApres);
                        }
                    }

                    for (int k = 0; k < tailleNavire; k++) {

                        BoolVar navireDansLePlanning = IntConstraintFactory.arithm(this.planningNavire[i][l + k], "=", navireId).reif();
                        IntConstraintFactory.arithm(positionDebutNavireEnL, "=", navireDansLePlanning);
                    }
                }
            }
        }
    }

    private void contraintTailleQuaiSuperieureACelleDesBateauxDessusAT(int t, int n, int[] taillesDesBateaux) {

        IntVar sizeMax = VariableFactory.bounded("size_max_present_a_t" + t, 0, this.longueurQuai - 2 * (n - 1), solver);

        this.solver.post(SetConstraintsFactory.sum(this.naviresPresentsAT[t], taillesDesBateaux
                , 0, sizeMax, false));
    }
}
