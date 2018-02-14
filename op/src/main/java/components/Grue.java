package components;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;


public class Grue {

    public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getCapacite() {
		return capacite;
	}

	public void setCapacite(int capacite) {
		this.capacite = capacite;
	}

	public int getDureeDUneJournee() {
		return dureeDUneJournee;
	}

	public void setDureeDUneJournee(int dureeDUneJournee) {
		this.dureeDUneJournee = dureeDUneJournee;
	}

	public IntVar[] getPositions() {
		return positions;
	}

	public void setPositions(IntVar[] positions) {
		this.positions = positions;
	}

	public Ouvrier[] getOuvriers() {
		return ouvriers;
	}

	public void setOuvriers(Ouvrier[] ouvriers) {
		this.ouvriers = ouvriers;
	}

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

        	//Position de la grue comprise entre 0 et longueur du quai
            this.positions[t] = VariableFactory.bounded("position_grue_" + id, 0, longueurQuai, solver);
        }


    }
}
