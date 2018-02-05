package components;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.VariableFactory;

@Getter
@Setter
@EqualsAndHashCode
public class Navire {

    private int id;
    private int nbConteneurs;

    private IntVar xDateArrivee;
    private int dateDeDepartPrevue;
    private int coutDeRetard;

    private IntVar positionDebut;
    private int longueur;
    private SetVar grues;

    private IntVar tempsPasseAQuai;

    public Navire(int id, int nbConteneurs, int dateDeDepartPrevue, int coutDeRetard, int longueur, int longueurDuQuai, int dateFinJournee,
                  Solver solver, int nbGrues){

        this.id = id;
        this.nbConteneurs = nbConteneurs;
        this.dateDeDepartPrevue = dateDeDepartPrevue;
        this.coutDeRetard = coutDeRetard;
        this.longueur = longueur;

        this.positionDebut = VariableFactory.bounded("position_navire_"+id, 0, longueurDuQuai-1, solver);
        this.xDateArrivee = VariableFactory.bounded("date_arrivee_navire_"+id, 0, dateFinJournee, solver);

        int[] enveloppeGrues = new int[nbGrues];

        for(int i = 0; i < nbGrues; i++) enveloppeGrues[i] = i;

        this.grues = VariableFactory.set("grues_pour_navire_"+id, enveloppeGrues, new int[]{},
                solver);


    }
}

