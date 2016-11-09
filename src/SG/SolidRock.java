package SG;

/**
 * Sample agent that always defends Target 0 and always attacks Target 0
 *
 * @author Oscar
 * @version 2015.10.04
 */
public class SolidRock extends Player {
	protected final String newName = "SolidRock"; //Overwrite this variable in your player subclass

	/**
	 * Your constructor should look just like this
	 */
	public SolidRock() {
		super();
		playerName = newName;
	}

	/**
	 * THIS METHOD SHOULD BE OVERRIDDEN
	 * GameMaster will call this to compute your coverage strategy.
	 *
	 * @param g The game your agent will be playing
	 * @return the coverage
	 */
	protected double[] solveGame(GameModel g) {
		double[] coverage = new double[g.getT()];
		coverage[0] = 1;//fully protect target 0
		return coverage;
	}

	/**
	 * Attacker logic
	 *
	 * @param g        the game
	 * @param coverage defender's coverage
	 * @return the target to attack
	 */
	protected int attackTarget(GameModel g, double[] coverage) {
		return 0;//always attack target 0
	}
}
