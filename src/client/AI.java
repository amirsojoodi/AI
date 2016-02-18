package client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Queue;

import client.model.Graph;
import client.model.Node;

/**
 * AI class. You should fill body of the method {@link #doTurn}. Do not change
 * name or modifiers of the methods or fields and do not add constructor for
 * this class. You can add as many methods or fields as you want! Use world
 * parameter to access and modify game's world! See World interface for more
 * details.
 */
@SuppressWarnings("unused")
public class AI {

	private static final int ROOT_NODE_WEIGHT = 1000;
	private static final int PROPAGATION_FACTOR = 9; // of 10
	private static final int PROPAGATION_ADD_FACTOR = 10;
	private static final int MAX_STRATEGIC_DEPTH = 1;
	private static final int MAX_DISTANCE = 10000;
	private static int numberOfNodes;
	private static boolean firstTurn = true;
	private static Node[] strategicNodes;
	private static int[] weights;
	private static int[][] allWeights;
	private static int[][] allDistances; // TODO

	private static enum GlobalState {
		strategic, expanding, allAtack, kharTuKhar, bacteryAttack
	};

	private static enum LocalState {
		strategic, ghompoz, attack, gorooz
	};

	private static GlobalState globalState;
	private static LocalState[] localStates;

	private static Node globalGoal;
	private static Node[] localGoals;

	public void doTurn(World world) {
		naiveDoTurn(world);
	}

	private void naiveDoTurn(World world) {

		globalState = null;
		globalGoal = null;
		localGoals = null;
		localStates = null;

		// set strategy (global, locals)

		// TODO check elapsed time

		if (firstTurn) {
			// printWorldStaticConfig(world);
			numberOfNodes = world.getMap().getNodes().length;
			//getStrategicNodes(world);
			//AITest.printNodesNumberOfNeighbors(strategicNodes, "strategicNodes");
			//AITest.printNodesIndex(strategicNodes, "strategicNodes");
			getWeights(world);
			getAllWeights(world);
			getAllDistances(world);
			AITest.printNodesIndex(strategicNodes, "strategicNodes");
			AITest.printWeights(weights, "weights");
			// AITest.printAllDistance(allDistances);
			firstTurn = false;
		}

/*		if (globalGoal == null || globalGoal.getOwner() != world.getMyID()) {
			globalGoal = world.getOpponentNodes()[0];
			AITest.printWeights(allWeights[globalGoal.getIndex()],
					"Weights of node:" + globalGoal.getIndex());
		}

		if (globalState != null) {
			if (globalState == GlobalState.allAtack) {
				doTurnAllAtack(world, globalGoal);
				return;
			}
		}
*/
		int[] nextMoves = new int[numberOfNodes]; // 0 is no body

		Node[] myNodes = world.getMyNodes();
		for (Node source : myNodes) {
			Node[] neighbours = source.getNeighbours();
			int[] neighboursWeight = new int[neighbours.length];

			for (int i = 0; i < neighboursWeight.length; i++) {
				neighboursWeight[i] = weights[neighbours[i].getIndex()];
			}

			// Arrays.sort(neighboursWeight);
			int i = 0;
			int firstCandidateIndex = 0;
			for (; i < neighboursWeight.length; i++) {
				Node dest = candidateNeighbor(neighbours, neighboursWeight);

				if (dest.getOwner() != world.getMyID()
						&& nextMoves[dest.getIndex()] != world.getMyID() + 1) {
					world.moveArmy(source, dest, source.getArmyCount());

					nextMoves[dest.getIndex()] = world.getMyID() + 1;
					break;
				}
				if (i == 0) {
					firstCandidateIndex = dest.getIndex();
				}
			}

			if (i == neighboursWeight.length) {
				// if going closer to the strategic node is not available
				// because of all seen neighbors, the army should go closer
				// to the front line! Ay Sir!
				world.moveArmy(source.getIndex(), firstCandidateIndex,
						source.getArmyCount());
			}
		}

	}

	private void doTurnAllAtack(World world, Node globalNode) {
		int[] nextMoves = new int[numberOfNodes]; // 0 is no body

		Node[] myNodes = world.getMyNodes();
		for (Node source : myNodes) {
			Node[] neighbours = source.getNeighbours();
			int[] neighboursWeight = new int[neighbours.length];

			for (int i = 0; i < neighboursWeight.length; i++) {
				neighboursWeight[i] = allWeights[globalNode.getIndex()][neighbours[i]
						.getIndex()];
			}

			// Arrays.sort(neighboursWeight);
			int i = 0;
			int firstCandidateIndex = 0;
			for (; i < neighboursWeight.length; i++) {
				Node dest = candidateNeighbor(neighbours, neighboursWeight);

				if (dest.getOwner() != world.getMyID()
						&& nextMoves[dest.getIndex()] != world.getMyID() + 1) {
					world.moveArmy(source, dest, source.getArmyCount());

					nextMoves[dest.getIndex()] = world.getMyID() + 1;
					break;
				}
				if (i == 0) {
					firstCandidateIndex = dest.getIndex();
				}
			}

			if (i == neighboursWeight.length) {
				// if going closer to the strategic node is not available
				// because of all seen neighbors, the army should go closer
				// to the front line! Ay Sir!
				world.moveArmy(source.getIndex(), firstCandidateIndex,
						source.getArmyCount());
			}
		}
	}

	private void getAllDistances(World world) {

		allDistances = new int[numberOfNodes][numberOfNodes];

		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				for (int i = 0; i < numberOfNodes; i++) {
					for (int j = 0; j < numberOfNodes; j++) {
						allDistances[i][j] = MAX_DISTANCE;
					}
					allDistances[i][i] = 0;
				}

				for (int i = 0; i < numberOfNodes; i++) {
					Node node = world.getMap().getNode(i);
					for (Node neighbor : node.getNeighbours()) {
						allDistances[node.getIndex()][neighbor.getIndex()] = 1;
					}
				}

				for (int k = 0; k < numberOfNodes; k++) {
					for (int i = 0; i < numberOfNodes; i++) {
						for (int j = 0; j < numberOfNodes; j++) {
							if (allDistances[i][j] > allDistances[i][k]
									+ allDistances[k][j]) {
								allDistances[i][j] = allDistances[i][k]
										+ allDistances[k][j];
							}
						}
					}
				}
			}
		});

		thread.start();
	}

	private static Node candidateNeighbor(Node[] nodes, int[] neighboursWeight) {
		int maxWeight = -1;
		int maxIndex = 0;
		for (int i = 0; i < neighboursWeight.length; i++) {
			if (maxWeight < neighboursWeight[i]) {
				maxWeight = neighboursWeight[i];
				maxIndex = i;
			}
		}
		neighboursWeight[maxIndex] = -1;
		return nodes[maxIndex];
	}

	private void randomDoTurn(World world) {
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

	synchronized private static void getWeights(World world){
		weights = new int[numberOfNodes];

		strategicNodes = figureOutStrategicNodes(world, MAX_STRATEGIC_DEPTH);

		for (int i = 0; i < strategicNodes.length; i++) {
			propagateWeight(strategicNodes[i], world, weights);
		}
	}

	private static void propagateWeight(Node rootNode, World world,
			int[] weights) {
		Queue<Node> q = new LinkedList<Node>();

		boolean[] visitedList = new boolean[numberOfNodes];
		// TODO eliminate redundant news

		q.add(rootNode);
		visitedList[rootNode.getIndex()] = true;
		weights[rootNode.getIndex()] = ROOT_NODE_WEIGHT;
		while (!q.isEmpty()) {
			Node node = (Node) q.remove();
			int parentWeight = weights[node.getIndex()];
			for (Node child : node.getNeighbours()) {
				if (visitedList[child.getIndex()] == false) {
					if (weights[child.getIndex()] == 0) {
						weights[child.getIndex()] = (parentWeight * PROPAGATION_FACTOR)
								/ (PROPAGATION_FACTOR + 1);
					} else if (weights[child.getIndex()] == ROOT_NODE_WEIGHT) {
						;
					} else {
						weights[child.getIndex()] = PROPAGATION_ADD_FACTOR
								+ Integer.max(weights[child.getIndex()],
										(parentWeight * PROPAGATION_FACTOR)
												/ (PROPAGATION_FACTOR + 1));
					}
					visitedList[child.getIndex()] = true;
					q.add(child);
				}
			}
		}
	}

	private static void getAllWeights(World world) {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				allWeights = new int[numberOfNodes][numberOfNodes];
				for (Node node : world.getMap().getNodes()) {
					propagateWeight(node, world, allWeights[node.getIndex()]);
				}
				/*
				 * for (int i = 0; i < numberOfNodes; i++) {
				 * propagateWeight(world.getMap().getNodes()[i], world,
				 * allWeights[world.getMap().getNodes()[i].getIndex()]); }
				 */

				System.out.println("\n---AllWeights computed!---");
			}
		});

		thread.start();
	}

	private static Node[] figureOutStrategicNodes(World world, int maxDepth) {
		
		Graph map = world.getMap();
		Integer[] expansePower = new Integer[numberOfNodes];
		for (Node node : map.getNodes()) {
			// System.out.println("index: "+node.getIndex()+", "+node.getNeighbours().length);
			expansePower[node.getIndex()] = BFS(node, maxDepth);
		}
		ArrayList<Node> strategicPoints = new ArrayList<>();
		int maxPower = 0;
		for (int i = 0; i < expansePower.length; i++) {
			if (expansePower[i] > maxPower) {
				strategicPoints.clear();
				maxPower = expansePower[i];
			}
			if (expansePower[i] == maxPower) {
				strategicPoints.add(map.getNode(i));
			}
		}

		Node[] lastStrategic = new Node[strategicPoints.size()];
		lastStrategic = strategicPoints.toArray(lastStrategic);
		
		return lastStrategic;
	}

	private static int BFS(Node root, int maxDepth) {
		boolean[] visitedList = new boolean[numberOfNodes];
		int numOfEdges = 0;
		int depth = 0;
		Node node = null;

		Queue<Node> Q = new LinkedList<Node>();
		Queue<Integer> D = new LinkedList<Integer>();

		Q.add(root);
		D.add(0);
		visitedList[root.getIndex()] = true;

		while (!Q.isEmpty()) {
			node = Q.peek();
			Q.remove();
			depth = D.peek();
			D.remove();

			for (Node neighbour : node.getNeighbours()) {
				if (depth == maxDepth) {
					if (visitedList[neighbour.getIndex()])
						numOfEdges++;
				} else {
					if (!visitedList[neighbour.getIndex()]) {
						visitedList[neighbour.getIndex()] = true;
						Q.add(neighbour);
						D.add(depth + 1);
					}
					numOfEdges++;
				}
			}
		}
		if (numOfEdges % 2 != 0) {
			System.err.println("BFS for finding Strategic nodes is wrong");
		}
		return numOfEdges / 2;
	}

	private static Node[] getStrategicNodes(World world) {
		if (strategicNodes == null) {
			strategicNodes = Arrays.copyOf(world.getMap().getNodes(),
					numberOfNodes);
			Arrays.sort(strategicNodes, new Comparator<Node>() {
				@Override
				public int compare(Node n1, Node n2) {
					if (n1.getNeighbours().length > n2.getNeighbours().length) {
						return -1;
					} else if (n1.getNeighbours().length < n2.getNeighbours().length) {
						return 1;
					}
					return 0;
				}
			});
		}
		return strategicNodes;
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

class AITest {
	public static void printNodesNumberOfNeighbors(Node[] nodes, String name) {
		System.out.println("=== Number of neighbors in " + name + " ===");
		for (int i = 0; i < nodes.length; i++) {
			System.out.print("[" + i + ":" + nodes[i].getNeighbours().length
					+ "] ,");
		}
		System.out.println("\n");
	}

	public static void printWeights(int[] weights, String name) {
		System.out.println("=== Weights propagation from " + name + " ===");
		for (int i = 0; i < weights.length; i++) {
			System.out.print("[" + i + ":" + weights[i] + "] ,");
		}
		System.out.println("\n");
	}

	public static void printNodesIndex(Node[] nodes, String name) {
		System.out.println("=== Index of " + name + " ===");
		for (int i = 0; i < nodes.length; i++) {
			System.out.print("[" + i + ":" + nodes[i].getIndex() + "] ,");
		}
		System.out.println("\n");
	}

	public static void printAllDistance(int[][] allDistances) {
		System.out.println("==== All Distances ====");
		for (int i = 0; i < allDistances.length; i++) {
			for (int j = 0; j < allDistances.length; j++) {
				System.out.print(i + "," + j + ":" + allDistances[i][j] + "  ");
			}
			System.out.println();
		}
		System.out.println("\n");
	}
}