package SG;

import java.util.Arrays;

/**
 * @author Erick Garcia <egarcia87@miners.utep.edu>
 * @author Kimberly kato
 * @author Luis Casillas
 *
 * Defender: Minimize Regret
 * Attacker: Maximize Payoff
 */
public class MyPlayer extends Player {
	protected final String newName = "MyPlayer";

	public MyPlayer() {
		super();
		playerName = newName;
	}

	/**
	 * Minmax - Minimize regret
	 * 1) Get Number of Resources
	 * 2) Find out largest values of the Attacker
	 * 3) Find out in which of these values he gets a higher payoff
	 * 4) Check if the higher value is uncovered
	 * If so cover it
	 * 4) Check if he wins and I cover it
	 * A) Minimize his maximum payoffs
	 * B) Minimize our regret
	 **/
	protected double[] solveGame(GameModel g) {
		int resources = g.getM();
		double[] coverage = new double[g.getT()];
		int[] coveredTargetValues = g.getPayoffs()[0];
		int[] uncoveredTargetValues = g.getPayoffs()[1];
		int[] coveredDefenderValues = g.getPayoffs()[2];
		int[] uncoveredDefenderValues = g.getPayoffs()[3];

		int payoffTableSize = coveredTargetValues.length;

		int[] maxTargetValuesIndexes = new int[payoffTableSize]; //Choose the maximum between covered or uncovered
		Pair[] targetMaxPayoffsSorted = new Pair[payoffTableSize]; //Pair of index values

		for (int i = 0; i < payoffTableSize; i++) {
			maxTargetValuesIndexes[i] = coveredTargetValues[i] >= uncoveredTargetValues[i] ? 0 : 1; //UAC or UAU
			targetMaxPayoffsSorted[i] = maxTargetValuesIndexes[i] == 0 ? new Pair(i, coveredTargetValues[i]) : new Pair(i, uncoveredTargetValues[i]);
		}
		Arrays.sort(targetMaxPayoffsSorted); //Sorted MaxPayoffs by their value remembering their indexes

		int spotsCovered = 0;
		for (Pair p : targetMaxPayoffsSorted) {
			if (maxTargetValuesIndexes[p.index] == 0) { //Uncovered value is greater than covered
				coverage[p.index] = 1;
				spotsCovered++;
			}
			if (spotsCovered <= resources)
				break;
		}
		return coverage;
	}

	/**
	 * Maximize our payoff
	 * Check the defenders coverage and see if he has a higher payoff
	 * If it has a higher payoff ignore those indexes in the next step, else include them
	 * Check our highest value
	 * A) Check if we win on that value, else choose second highest
	 * check if there are duplicates which minimize the defenders payoff
	 * B) check if we win and store the defender's regret
	 * choose maximum defender's regret
	 * <p>
	 * 2nd
	 * Check our highest value between UAU and UAC
	 * sort from highest to lowest
	 * compare utilities of UAU and UAC
	 * choose highest value with lowest regret
	 */
	protected int attackTarget(GameModel g, double[] coverage) {
		int[] coveredTargetValues = g.getPayoffs()[0];
		int[] uncoveredTargetValues = g.getPayoffs()[1];
		int[] coveredDefenderValues = g.getPayoffs()[2];
		int[] uncoveredDefenderValues = g.getPayoffs()[3];

		Pair[] uncoveredTargetValuesSorted = fillPair(uncoveredTargetValues);
		Pair[] coveredTargetValuesSorted = fillPair(coveredTargetValues);
		Arrays.sort(uncoveredTargetValuesSorted);
		Arrays.sort(coveredTargetValuesSorted);

		int indexOfMaxUncovered = 0, indexOfMaxCovered = 0;
		for (Pair p : uncoveredTargetValuesSorted) {
			if (coverage[p.index] == 0 && p.value >= uncoveredDefenderValues[p.index]) {
				indexOfMaxUncovered = p.index;
				break;
			}
		}
		for (Pair p : coveredTargetValuesSorted) {
			if (coverage[p.index] == 1 && p.value >= coveredDefenderValues[p.index]) {
				indexOfMaxCovered = p.index;
				break;
			}
		}
		return uncoveredTargetValues[indexOfMaxUncovered] >= coveredTargetValues[indexOfMaxCovered] ? indexOfMaxUncovered : indexOfMaxCovered;
	}

	/**
	 * Class used to sort values and remember their index
	 */
	private class Pair implements Comparable<Pair> {
		final int index;
		final int value;

		Pair(int index, int value) {
			this.index = index;
			this.value = value;
		}

		@Override
		public int compareTo(Pair other) {
			//multiplied to -1 to sort in descending order
			return -1 * Integer.valueOf(this.value).compareTo(other.value);
		}
	}

	private Pair[] fillPair(int[] array) {
		Pair[] pair = new Pair[array.length];
		for (int i = 0; i < array.length; i++) pair[i] = new Pair(i, array[i]);
		return pair;
	}
}
