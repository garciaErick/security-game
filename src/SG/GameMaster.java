package SG;

import java.util.*;

/**
 * Security Game Tournament Simulator based of Normal Form Tournament Simulator
 * originally created by Marcus and Oscar for Fall 2015 of Risk Analysis
 * <p>
 * Note to students: Add your agents in the //add your agent(s) here section
 *
 * @author Oscar Veliz
 * @version 2016.10.20
 */
public class GameMaster {

	private static boolean verbose = false; //Set to false if you do not want the details
	private static int numGames = 1; //test with however many games you want
	private static ArrayList<GameModel> games = new ArrayList<GameModel>();

	/**
	 * Runs the tournament. Add your agent(s) to the list.
	 *
	 * @param args not using any command line arguments
	 */
	public static void main(String[] args) {
		ArrayList<Player> players = new ArrayList<Player>();
		players.add(new UniformRandom());
		players.add(new SolidRock());
		players.add(new RelentlessAssault());
		players.add(new Solidarity());
		//add your agent(s) here
		players.add(new SG.MyPlayer());

		//create games
		for (int i = 0; i < numGames; i++)
			games.add(new GameModel(i));
		computeStrategies(players);

		//compute expected payoffs

		double[] attackerPayoffs = new double[players.size()];
		double[] defenderPayoffs = new double[players.size()];
		double[][] defenderUtilities = new double[players.size()][players.size()];
		double[][] attackerUtilities = new double[players.size()][players.size()];
		int numPlayers = players.size();
		for (int d = 0; d < numPlayers; d++) {
			for (int a = 0; a < numPlayers; a++) {
				for (int game = 0; game < numGames; game++) {
					Player player1 = players.get(d);
					Player player2 = players.get(a);
					if (verbose) System.out.println("Game" + game);
					if (verbose) System.out.println(player1.getName() + " vs " + player2.getName());
					double[] payoffs = match(player1, player2, game);
					defenderPayoffs[d] += payoffs[0];
					attackerPayoffs[a] += payoffs[1];
					defenderUtilities[d][a] += payoffs[0];
					attackerUtilities[d][a] += payoffs[1];
					if (verbose) System.out.println(payoffs[0]);
					if (verbose) System.out.println(payoffs[1]);
				}
			}
		}
		String[] names = new String[numPlayers];
		for (int i = 0; i < numPlayers; i++)
			names[i] = players.get(i).getName();
		System.out.println("Defender Results");
		Analyzer defenderResults = new Analyzer(defenderUtilities, names);
		defenderResults.printResults();
		defenderResults.printAverages();
		//defenderResults.printMedians();
		defenderResults.printRegret();
		System.out.println();
		System.out.println("Attacker Results");
		Analyzer attackerResults = new Analyzer(attackerUtilities, names);
		attackerResults.printResults();
		attackerResults.printAverages();
		//attackerResults.printMedians();
		//attackerResults.printRegret();
		System.out.println("Attackers who punished the most");
		defenderResults.printMinimizer();

		System.out.println();
		System.out.println("Defender Utilities");
		printMatrix(defenderUtilities, players);
		System.out.println();
		System.out.println("Attacker Utilities");
		printMatrix(attackerUtilities, players);


		System.exit(0);//just to make sure it exits
	}

	/**
	 * Tries to execute a Player class' method by using threads for protection in case
	 * the Player subclasses crash or time out.
	 *
	 * @param pDriver The thread that will execute the player
	 */
	private static void tryPlayer(PlayerDriver pDriver) {
		int timeLimit = 2000;//2s or 2000ms
		Thread playerThread = new Thread(pDriver);
		playerThread.start();
		for (int sleep = 0; sleep < timeLimit; sleep += 10) {
			if (playerThread.isAlive())
				try {
					Thread.sleep(10);
				} catch (Exception e) {
					e.printStackTrace();
				}
			else
				return;
		}
	}

	/**
	 * Figures out every agent strategy for every game for all players within a game
	 *
	 * @param p The agents being run in the tournament
	 */
	private static void computeStrategies(ArrayList<Player> p) {
		ArrayList<PlayerDriver> drivers = new ArrayList<PlayerDriver>();
		for (int pd = 0; pd < p.size(); pd++)
			drivers.add(new PlayerDriver(PlayerState.SOLVE, p.get(pd)));
		for (int gameNumber = 0; gameNumber < numGames; gameNumber++) {
			GameModel g = new GameModel(gameNumber);//gives the agent a copy of the game
			for (int playerIndex = 0; playerIndex < p.size(); playerIndex++) {
				Player player = p.get(playerIndex);
				player.setGame(gameNumber);
				player.setGame(g);
				player.setDefender();
				tryPlayer(new PlayerDriver(PlayerState.SOLVE, player));
			}
		}
	}

	/**
	 * A single individual match between a defender and an attacker.
	 * Returns the payoffs
	 * <p>
	 * If a strategy for a player is invalid it will assign a payoff of -1337 to that player
	 *
	 * @param d          defender
	 * @param a          attacker
	 * @param gameNumber game
	 * @return the payoffs in an array of size 2 payoff[0] is the defender utility and payoff[1] is the attacker utility
	 */
	public static double[] match(Player d, Player a, int gameNumber) {
		double[] coverage = d.getStrategy(gameNumber);
		GameModel g = new GameModel(gameNumber);
		double[] payoffs = new double[2];
		if (!g.isValidCoverage(coverage)) {//invalid coverage do not even bother executing match. give -1337 to defender and 0 to attacker
			if (verbose) System.out.println(d.getName() + " has invalid coverage vector for game " + gameNumber);
			payoffs[0] = -1337;
			payoffs[1] = 0;
		} else {
			a.setAttacker();
			a.setGame(gameNumber);
			a.setC(coverage);
			tryPlayer(new PlayerDriver(PlayerState.SOLVE, a));
			int t = a.getT();
			if (!g.isValidTarget(t)) {//invalid target attacked assign 0 to defender and -1337 to attacker
				if (verbose) System.out.println(a.getName() + " has invalid target for game " + gameNumber);
				payoffs[0] = 0;
				payoffs[1] = -1337;
			} else
				payoffs = g.computePayoffs(coverage, t);
		}
		return payoffs;
	}

	/**
	 * Want to visualize the results of a tournament? Call this function.
	 *
	 * @param matrix  Payoff Matrix
	 * @param players List of Players
	 */
	public static void printMatrix(double[][] matrix, ArrayList<Player> players) {
		for (int i = 0; i < players.size(); i++)
			System.out.print(players.get(i).getName() + "\t");
		System.out.println();
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				System.out.print(matrix[i][j] + "\t\t\t\t");
			}
			System.out.print("\t" + players.get(i).getName());
			System.out.println();
		}
	}
}
