package simulator;

import network.Network;
import network.Node;

public class Run {

	private static Network network; 
	
	public static void main(String[] args){		
		if (args.length < 2){
			System.err.println("Please provide: <filepath> <range>");
			System.exit(42);
		}
		// Parse the file
		network = InputParser.buildNetwork(args[0], Float.parseFloat(args[1]));
		
		// Discover from each node
		network.discover();
		
		// Build the MST
		network.buildMst();
		
		// Execute the Broadcast messages.
        network.executeTransmissions();
		
		for (Node n : network.nodes){
			System.out.print("Tree nodes for " + n.nodeId + " ");
			for (Node m : n.treeNodes){
				System.out.print(m.nodeId + " ");
			}
			System.out.println();
		}
	}
	
}
