package planning;

import components.Grue;
import components.Navire;
import lombok.Getter;
import lombok.Setter;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.trace.Chatterbox;


@Getter
@Setter
public class GlobalPlanning {

    private PlanningGrue planningGrue;
    private PlanningNavire planningNavire;
    private Solver solver;

    public GlobalPlanning(PlanningNavire planningNavire, PlanningGrue planningGrue, Solver solver) {

        this.planningGrue = planningGrue;
        this.planningNavire = planningNavire;
        this.solver = solver;

    }


    public static void main(String[] args) {

        //Planning 8h, Quai de Longueur 12, 12 Navires, 7 Grues : 30secondes
        //instanceLongueurQuai12DureeJournee8H8Grues12Navires();

        //Planning 8h, Quai de Longueur 13 : OutOfMemoryError: Java heap space
        //instanceLongueurQuai13DureeJournee8H8Grues12Navires();

        //Planning : 1s
        instanceLongueur12JourneeDe8H6GruesEtUnSeulNavire();

    }

    public PlanningGrue getPlanningGrue() {
		return planningGrue;
	}


	public void setPlanningGrue(PlanningGrue planningGrue) {
		this.planningGrue = planningGrue;
	}


	public PlanningNavire getPlanningNavire() {
		return planningNavire;
	}


	public void setPlanningNavire(PlanningNavire planningNavire) {
		this.planningNavire = planningNavire;
	}


	public Solver getSolver() {
		return solver;
	}


	public void setSolver(Solver solver) {
		this.solver = solver;
	}


	private static void instanceLongueurQuai12DureeJournee8H8Grues12Navires(){

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

    private static void instanceLongueurQuai13DureeJournee8H8Grues12Navires(){

        Solver solver = new Solver("");

        int longueurQuai = 13;
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
    
    private static void instanceLongueur12JourneeDe8H6GruesEtUnSeulNavire(){

        Solver solver = new Solver("");

        int longueurQuai = 12;
        int dureeDuneJournee = 32;

        Grue[] grues = new Grue[]{new Grue(0, 5, longueurQuai, dureeDuneJournee, solver),
                new Grue(1, 10, longueurQuai, dureeDuneJournee, solver),
                new Grue(2, 20, longueurQuai, dureeDuneJournee, solver),
                new Grue(3, 15, longueurQuai, dureeDuneJournee, solver),
                new Grue(4, 20, longueurQuai, dureeDuneJournee, solver),
                new Grue(5, 25, longueurQuai, dureeDuneJournee, solver),
                new Grue(6, 30, longueurQuai, dureeDuneJournee, solver),
                new Grue(6, 15, longueurQuai, dureeDuneJournee, solver)
        };

        Navire navire = new Navire(1, 2500, 8, longueurQuai, 1, 1, dureeDuneJournee,
                grues, solver);

 

        Navire[] navires = new Navire[]{navire};

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
