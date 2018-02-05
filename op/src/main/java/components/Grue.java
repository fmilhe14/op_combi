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
    private IntVar position;
    private int temps;
    private IntVar ouvrier;

    public Grue(int id, int capacite, int nbOuvrier, int longueurQuai, int temps, Solver solver){

        this.id = id;
        this.capacite = capacite;
        this.temps = temps;

        this.position = VariableFactory.bounded("position_grue_"+id+"_à_t_"+temps, 0, longueurQuai, solver);
        this.ouvrier = VariableFactory.bounded("ouvrier_sur_la_grue_"+id, 0, nbOuvrier, solver);

    }
}
