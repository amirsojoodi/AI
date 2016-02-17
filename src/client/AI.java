package client;

import java.util.Arrays;
import java.util.Comparator;

import client.model.Node;

/**
 * AI class. You should fill body of the method {@link #doTurn}. Do not change
 * name or modifiers of the methods or fields and do not add constructor for
 * this class. You can add as many methods or fields as you want! Use world
 * parameter to access and modify game's world! See World interface for more
 * details.
 */
public class AI {

	private static boolean firstTurn = true;

	private static boolean[] visitedList;

	private static Node[] stratedgicNodes;

	public void doTurn(World world) {
		randomDoTurn(world);
	}

	public void naiveDoTurn(World world) {

		if (firstTurn) {
			// printWorldStaticConfig(world);
			graphConfiguration(world);
			firstTurn = false;
		}
		long start = System.currentTimeMillis(); // how can we not busy waiting?
													// event e.g.

		// TODO : check remaining time
	}

	public void randomDoTurn(World world) {
		// fill this method, we've presented a stupid AI for example!
		Node[] myNodes = world.getMyNodes();
		for (Node source : myNodes) {
			Node[] neighbours = source.getNeighbours();
			if (neighbours.length > 0) {
				Node destination = neighbours[(int) (neighbours.length * Math
						.random())];
				world.moveArmy(source, destination, source.getArmyCount() / 2);
			}
		}
	}

	private void graphConfiguration(World world) {
		visitedList = new boolean[world.getMap().getNodes().length];
	}

	private Node[] getStrategicNodes(World world) {
		if (stratedgicNodes != null) {
			
			stratedgicNodes = new Node[10];
			Arrays.sort(world.getMap().getNodes(), new Comparator<Node>() {
			});
			
		}
		
		return stratedgicNodes;
	}

	private void printWorldStaticConfig(World world) {
		System.out
				.println("============= Static Config of the world: ============");
		System.out.println("EdgeBonusConstant = "
				+ world.getEdgeBonusConstant());
		System.out.println("EscapeConstant = " + world.getEscapeConstant());
		System.out.println("LowArmyBound = " + world.getLowArmyBound());
		System.out.println("LowCasualtyCoefficient = "
				+ world.getLowCasualtyCoefficient());
		System.out.println("MediumArmyBound = " + world.getMediumArmyBound());
		System.out.println("MediumCasualtyCoefficient = "
				+ world.getMediumCasualtyCoefficient());
		System.out.println("MyID = " + world.getMyID());
		System.out.println("NodeBonusConstant = "
				+ world.getNodeBonusConstant());
		System.out.println("TotalTurns = " + world.getTotalTurns());
		System.out.println("TotalTurnTime = " + world.getTotalTurnTime());
		System.out.println();
	}

	private void printWorldConfig(World world) {
		System.out.println("==================== Turn " + world.getTurnNumber()
				+ " ===================");
		System.out.println("TurnRemainingTime = "
				+ world.getTurnRemainingTime());
		System.out.println("TurnTimePassed = " + world.getTurnTimePassed());
		System.out.println();
	}

}
