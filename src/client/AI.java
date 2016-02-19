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
	private static final int PROPAGATION_ADD_FACTOR = 0; // 10
	private static final int MAX_STRATEGIC_DEPTH = 1;
	private static final int MAX_DISTANCE = 10000;
	private static int numberOfNodes;
	private static boolean firstTurn = true;
	private static boolean strategicNodesChanged;
	private static int expandingRadius;
	private static Node centerNode;
	private static Node[] globalStrategicNodes;
	private static Node[] localStrategicNodes;
	private static Node[] boundryNodes;
	private static int[] globalWeights;
	private static int[][] allWeights;
	private static int[][] allDistances;

	private static enum GlobalStrategy {
		getStrategicPoint, expanding, allAtack, kharTuKhar, bacteryAttack
	};

	private static enum LocalStrategies {
		strategic, ghompoz, attack, begorkh
	};

	private static GlobalStrategy globalStrategy;
	private static LocalStrategies[] localStrategies;

	private static Node globalGoal;
	private static Node[] localGoals;

	public void doTurn(World world) {
		// TODO check elapsed time

		if (firstTurn) {
			firstConfigurations(world);
			firstTurn = false;
		}

		setStrategies(world);

		System.out.println("Global Strategy = " + globalStrategy);

		if (globalStrategy == GlobalStrategy.getStrategicPoint) {
			getGlobalWeights(world);
			doTurnGetStrategicPoint(world);
		} else if (globalStrategy == GlobalStrategy.expanding) {
			getBoundryNodes(world);
			strategicNodesChanged = true;
			getGlobalWeights(world);
			doTurnExpanding(world);
		}

		/*
		 * if (globalGoal == null || globalGoal.getOwner() != world.getMyID()) {
		 * globalGoal = world.getOpponentNodes()[0];
		 * AITest.printWeights(allWeights[globalGoal.getIndex()],
		 * "Weights of node:" + globalGoal.getIndex()); }
		 * 
		 * if (globalState != null) { if (globalState == GlobalState.allAtack) {
		 * doTurnAllAtack(world, globalGoal); return; } }
		 */

	}

	private void doTurnExpanding(World world) {
		int[] nextArmies = new int[numberOfNodes]; // TODO
		int[] nextMoves = new int[numberOfNodes]; // 0 is no body

		Node[] myNodes = world.getMyNodes();
		for (Node source : myNodes) {
			Node[] neighbours = source.getNeighbours();
			int[] neighboursWeight = new int[neighbours.length];

			for (int i = 0; i < neighboursWeight.length; i++) {
				neighboursWeight[i] = globalWeights[neighbours[i].getIndex()];
			}

			// Arrays.sort(neighboursWeight);
			// TODO: add wieght according to difference of source and neighbor
			// weights

			int i = 0;
			int firstCandidateIndex = 0;
			for (; i < neighboursWeight.length; i++) {
				Node dest = candidateNeighbor(neighbours, neighboursWeight);

				if (dest.getOwner() != world.getMyID()
						&& nextMoves[dest.getIndex()] != world.getMyID() + 1) {
					// TODO : if opponent
					// TODO : if empty
					
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

	private static void doTurnGetStrategicPoint(World world) {

		int[] nextMoves = new int[numberOfNodes]; // 0 is no body

		Node[] myNodes = world.getMyNodes();
		for (Node source : myNodes) {
			Node[] neighbours = source.getNeighbours();
			int[] neighboursWeight = new int[neighbours.length];

			for (int i = 0; i < neighboursWeight.length; i++) {
				neighboursWeight[i] = globalWeights[neighbours[i].getIndex()];
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

	private static void setStrategies(World world) {

		if (globalStrategy == null) {
			globalStrategy = GlobalStrategy.getStrategicPoint;
		}

		if (globalStrategy == GlobalStrategy.getStrategicPoint) {

			if (localStrategicNodes != null && localStrategicNodes.length > 0) {
				for (int i = 0; i < localStrategicNodes.length; i++) {
					if (localStrategicNodes[i].getOwner() != world.getMyID()) {
						// still needs to seek to all localStrategicNodes
						return;
					}
				}
				// all local strategics are ours
				globalStrategy = GlobalStrategy.expanding;
			} else {
				// globalStrategicNodes
				for (int i = 0; i < globalStrategicNodes.length; i++) {
					if (globalStrategicNodes[i].getOwner() == world.getMyID()) {
						globalStrategy = GlobalStrategy.expanding;
						break;
					}
				}
			}
		}

		if (globalStrategy == GlobalStrategy.expanding) {
			if (expandingRadius == 0) {
				// TODO : Boundry
			}
			// TODO: how to get to another strategy
		}
	}

	private static void firstConfigurations(World world) {
		globalStrategy = null;
		globalGoal = null;
		localGoals = null;
		localStrategies = null;
		numberOfNodes = world.getMap().getNodes().length;
		getAllWeights(world);
		getAllDistances(world);
		// TODO if distance is time consuming merge it with weights
		figureOutStrategicNodes(world, MAX_STRATEGIC_DEPTH);
		strategicNodesChanged = true;
		expandingRadius = 0;
	}

	private static void getBoundryNodes(World world) {
		ArrayList<Node> boundryPoints = new ArrayList<Node>();
		for (Node node : world.getMyNodes()) {
			for (Node neighbor : node.getNeighbours()) {
				if (neighbor.getOwner() != world.getMyID()) {
					boundryPoints.add(node);
					break;
				}
			}
		}
		boundryNodes = new Node[boundryPoints.size()];
		boundryNodes = boundryPoints.toArray(boundryNodes);
	}

	private static void doTurnAllAtack(World world, Node globalNode) {
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

	private static void getAllDistances(World world) {

		allDistances = new int[numberOfNodes][numberOfNodes];

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
		/*
		 * Thread thread = new Thread(new Runnable() {
		 * 
		 * @Override public void run() {
		 * 
		 * } });
		 * 
		 * thread.start();
		 */
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

	private static void randomDoTurn(World world) {
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

	private static void getGlobalWeights(World world) {
		if (globalWeights == null) {
			globalWeights = new int[numberOfNodes];
		}

		if (globalStrategy == GlobalStrategy.getStrategicPoint) {
			if (strategicNodesChanged == true) {

				/*
				 * for (int i = 0; i < globalStrategicNodes.length; i++) {
				 * propagateWeight(globalStrategicNodes[i], world,
				 * globalWeights); }
				 */
				if (localStrategicNodes == null
						|| localStrategicNodes.length > 0) {

					combinePropagatedWeights(localStrategicNodes, globalWeights);
				} else {
					combinePropagatedWeights(globalStrategicNodes,
							globalWeights);
				}
				strategicNodesChanged = false;
			}
		}

		else if (globalStrategy == GlobalStrategy.expanding) {
			if (strategicNodesChanged == true) {
				combinePropagatedWeights(boundryNodes, globalWeights);
				strategicNodesChanged = false;
			}
		}
	}

	private static void combinePropagatedWeights(Node[] sources, int[] weights) {
		for (int i = 0; i < weights.length; i++) {
			int maxWeight = 0;
			for (int j = 0; j < sources.length; j++) {
				if (maxWeight < allWeights[sources[j].getIndex()][i]) {
					maxWeight = allWeights[sources[j].getIndex()][i];
				}
			}
			weights[i] = maxWeight;
		}
	}

	private static void propagateWeight(Node rootNode, World world,
			int[] weights) {
		Queue<Node> q = new LinkedList<Node>();

		boolean[] visitedList = new boolean[numberOfNodes];

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
		/*
		 * Thread thread = new Thread(new Runnable() {
		 * 
		 * @Override public void run() { allWeights = new
		 * int[numberOfNodes][numberOfNodes]; for (Node node :
		 * world.getMap().getNodes()) { propagateWeight(node, world,
		 * allWeights[node.getIndex()]); }
		 * System.out.println("\n---AllWeights computed!---"); } });
		 * 
		 * thread.start();
		 */

		allWeights = new int[numberOfNodes][numberOfNodes];
		for (Node node : world.getMap().getNodes()) {
			propagateWeight(node, world, allWeights[node.getIndex()]);
		}

		System.out.println("\n---AllWeights computed!---");
	}

	private static void figureOutStrategicNodes(World world, int maxDepth) {

		Graph map = world.getMap();
		Integer[] expansePower = new Integer[numberOfNodes];
		for (Node node : map.getNodes()) {
			// System.out.println("index: "+node.getIndex()+", "+node.getNeighbours().length);
			expansePower[node.getIndex()] = BFS(node, maxDepth);
		}
		ArrayList<Node> globalStrategicPoints = new ArrayList<>();
		ArrayList<Node> localStrategicPoints = new ArrayList<>();
		int maxPower = 0;
		for (int i = 0; i < expansePower.length; i++) {
			if (expansePower[i] > maxPower) {
				globalStrategicPoints.clear();
				maxPower = expansePower[i];
			}
			if (expansePower[i] == maxPower) {
				globalStrategicPoints.add(map.getNode(i));
			}
		}
		// search for localStrategic
		for (Node strategicNode : globalStrategicPoints) {
			int minDistFromMe = MAX_DISTANCE;
			int minDistFromOpponent = MAX_DISTANCE;
			for (Node node : world.getOpponentNodes()) {
				if (allDistances[strategicNode.getIndex()][node.getIndex()] < minDistFromOpponent) {
					minDistFromOpponent = allDistances[strategicNode.getIndex()][node
							.getIndex()];
				}
			}
			for (Node node : world.getMyNodes()) {
				if (allDistances[strategicNode.getIndex()][node.getIndex()] < minDistFromMe) {
					minDistFromMe = allDistances[strategicNode.getIndex()][node
							.getIndex()];
				}
			}
			if (minDistFromMe < minDistFromOpponent - 1) {
				localStrategicPoints.add(strategicNode);
			}
		}
		localStrategicNodes = new Node[localStrategicPoints.size()];
		globalStrategicNodes = new Node[globalStrategicPoints.size()];
		localStrategicNodes = localStrategicPoints.toArray(localStrategicNodes);
		globalStrategicNodes = globalStrategicPoints
				.toArray(globalStrategicNodes);
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

	public static void printWorldStaticConfig(World world) {
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

	public void printWorldConfig(World world) {
		System.out.println("==================== Turn " + world.getTurnNumber()
				+ " ===================");
		System.out.println("TurnRemainingTime = "
				+ world.getTurnRemainingTime());
		System.out.println("TurnTimePassed = " + world.getTurnTimePassed());
		System.out.println();
	}

}