package components;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;

@Getter
@Setter
@EqualsAndHashCode
public class Grue {

    private int id;
    private int capacite;
    private int dureeDUneJournee;
    private IntVar[] positions;
    private Ouvrier[] ouvriers;

    public Grue(int id, int capacite, int longueurQuai, int dureeDUneJournee, Solver solver) {

        this.id = id;
        this.capacite = capacite;
        this.dureeDUneJournee = dureeDUneJournee;
        this.positions = new IntVar[dureeDUneJournee];

        for (int t = 0; t < this.dureeDUneJournee; t++) {

            this.positions[t] = VariableFactory.bounded("position_grue_" + id, 0, longueurQuai, solver);
        }


    }
}
