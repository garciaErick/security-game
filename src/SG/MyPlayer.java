package SG;

import java.util.Arrays;

/**
 * Sample agent that always attacks the highest valued target, and
 * fully covers the highest valued covered targets.
 *
 * @author Jesus Molina
 */
public class MyPlayer extends Player {
	protected final String newName = "MyPlayer";

	/**
	 * Your constructor should look just like this
	 */
	public MyPlayer() {
		super();
		playerName = newName;
	}

	class Pair implements Comparable<Pair> {
		final int index;
		final int value;

		Pair(int index, int value) {
			this.index = index;
			this.value = value;
		}

		@Override
		public int compareTo(Pair other) {
			//multiplied to -1 as the author need descending sort order
			return -1 * Integer.valueOf(this.value).compareTo(other.value);
		}
	}

	protected double[] solveGame(GameModel g) {
		int resources = g.getM(); // To know how many targets to protect
		double[] coverage = new double[g.getT()];
		int[][] payoffs = g.getPayoffs();
		int[] attMaxPayoffIndexes = new int[payoffs[0].length];
		Pair[] attMaxPayoffs = new Pair[payoffs[0].length]; //Pair of index values

		for (int i = 0; i < payoffs[0].length; i++) {
			attMaxPayoffIndexes[i] = payoffs[0][i] >= payoffs[1][i] ? 0 : 1; //UAU or UAC
			attMaxPayoffs[i] = new Pair(i, payoffs[attMaxPayoffIndexes[i]][i]);
		}
		Arrays.sort(attMaxPayoffs); //Sorted MaxPayoffs by their value remembering their indexes

		int spotsCovered = 0;
		for (Pair p : attMaxPayoffs) {
			if (attMaxPayoffIndexes[p.index] == 0) {
				coverage[p.index] = 1;
				spotsCovered++;
			}
			if (spotsCovered <= resources)
				break;
		}
		return coverage;
		/*
		 * TODO: minMax
		 * 1) Get Number of Resources: get.(M)
		 * 2) Find out largest values of Attacker
		 * 3) Find out in which of these values he gets a higher payoff
		 * 4) Check if he wins and I cover it
		 *  A) Minimize his maximum payoffs
		 *  B) Minimize our regret
		 */
	}

	protected double[] attackTarget(GameModel g) {
		/*
		 * TODO:
		 * Check the defensors coverage and see if he has a higher payoff
		 * If it has a higher payoff ignore those indexes in the next step, else include them
		 * Check our highest value
		 * A) Check if we win on that value, else choose second highest
		 * check if there are duplicates which minimize the defenders payoff
		 * B) check if we win and store the defender's regret
		 * choose maximum defender's regret
		 */
	}

	/**
	 * returns true if val is in arr.
	 *
	 * @param arr the array.
	 * @param val the value.
	 * @return true if val is in arr.
	 */
	public boolean contains(int[] arr, int val) {
		for (int v : arr)
			if (v == val)
				return true;
		return false;
	}

	/**
	 * Attack the highest target either covered or not.
	 *
	 * @param g        the game
	 * @param coverage defender's coverage
	 * @return the target to attack
	 */
	protected int attackTarget(GameModel g, double[] coverage) {
		int[] uncoveredTargetValues = g.getPayoffs()[1]; // 1 is for attacker uncovered utilities

		int[] coveredTargetValues = g.getPayoffs()[0]; // 0 is for attacker covered utilities
		/** Obtain the index of the highest value target. */
		int maxIndex = 0;
		double maxValue = Double.MIN_VALUE;
		for (int i = 0; i < coverage.length; i++) {
			// Check uncovered values if uncovered.
			if (coverage[i] == 0) {
				if (uncoveredTargetValues[i] > maxValue) {
					maxValue = uncoveredTargetValues[i];
					maxIndex = i;
				}
			}
			// Check covered values if covered.
			else {
				double targetValue = coveredTargetValues[i] * coverage[i];
				if (targetValue > maxValue) {
					maxValue = targetValue;
					maxIndex = i;
				}
			}
		}
		return maxIndex;
	}
}
