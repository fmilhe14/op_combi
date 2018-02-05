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
public class Ouvrier {

    private int id;
    private int temps;
    private IntVar dateDebut;

    public Ouvrier(int id, int temps, int dateFinJournee, Solver solver){

        this.id = id;
        this.temps = temps;
        this.dateDebut = VariableFactory.bounded("date_debut_travail_ouvrier_"+id, 0, dateFinJournee, solver);

    }
}
