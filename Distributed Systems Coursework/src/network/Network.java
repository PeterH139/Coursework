package network;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import simulator.Log;

public class Network {
	
	public static List<Message> messagesToSend;
	public static float minimumEnergy;
	
	public List<Node> nodes;
	public List<Node> leaders;
	public Queue<Integer> broadcastIds;
	
	public Network(){
		nodes = new ArrayList<Node>();
		leaders = new ArrayList<Node>();
		broadcastIds  = new LinkedList<Integer>();
		messagesToSend = new ArrayList<Message>();
	}
	
	public void addNode(int nodeId, float posX, float posY, float energy, float range){
		Node n = new Node(nodeId, posX, posY, energy, range);
		nodes.add(n);
		leaders.add(n);
	}
	
	public void addBroadcast(int nodeId){
		this.broadcastIds.add(nodeId);
	}
	
	public void discover(){
		for (Node n : nodes){
			for (Node m : nodes){
				if (n.nodeId != m.nodeId && distanceBetweenNodes(n,m) <= n.range){
					n.initiateDiscover(m);
				}
			}
		}
		waitForExecution();
	}
	
	public void buildMst(){
		System.out.println("Begin Build MST");
		 while (true){
			System.out.println("Round Start");
			// We have started a new level. Write to the log file which leaders we will be contacting.
			Log.writeBs(leaders);
			
			// Each fragment finds all possible MWOEs
			for (Node n : leaders) n.initiateEdgeFind();
			waitForExecution();
			
			// Poll each of the leaders to find the status of MWOE selection
			// If we have no edges to add to the tree, we are done.
			int numEdges = 0;
			for (Node n : leaders) {
				numEdges += n.candidateEdges.size();
				for (Edge e : n.candidateEdges){
					System.out.println(e.toString());
				}
			}
			System.out.println("Edges: " + numEdges);
			
			if (numEdges == 0) break; 
			
			// Tell the leaders to start merging
			for (Node n : leaders) n.initiateMerge();
			waitForExecution();
				
			// Tell the leaders to broadcast their ID in the new tree(s).
			for (Node n : leaders) n.initiateLeaderChange();
			waitForExecution();

            // Update the list of leaders.
            List<Node> toRemove = new ArrayList<Node>();
            for (Node n : leaders) if (!n.isLeader) toRemove.add(n);
            leaders.removeAll(toRemove);
			
			// We have elected new leaders. Write to the log!
			// The elected leaders can be found from the leaderId of previous leaders.
		    Log.writeElected(toRemove);
		 }
		 System.out.println("MST Built");
	}
	
	public void executeTransmissions(){
		int numAlive = nodes.size();
		for (int id : this.broadcastIds){
            for (Node n : nodes){
                if (n.nodeId == id && n.isAlive){
                    n.dataBroadcast();
                    break;
                }
            }
			waitForExecution();
			 
			// If any nodes went down as a result of the last broadcast, then we need to rebuild the tree.
			int i = 0;
			for (Node n : nodes) if (n.isAlive) i++;
			if (i < numAlive) {
                // Update the leaders since they will have changed as a result of the node death(s).
                this.leaders.clear();
                for (Node n : nodes) if (n.isLeader) leaders.add(n);

                Log.shouldWrite = false;
                buildMst();
                Log.shouldWrite = true;
            }
			
			numAlive = i;
		}
	}
	
	/**
	 * We are still executing if there are any messages in the network.
	 */
	private void waitForExecution() {
		boolean executing = true;
		while(executing){
			for (Node n : nodes) if (n.isAlive) n.timestep();
			int numMessages = Network.messagesToSend.size();
			sendAllMessages();
			System.out.println("----");
			executing = (numMessages != 0);
		}
	}
	
	private void sendAllMessages(){
		for (Message m : Network.messagesToSend){
            if (m.sender.isAlive || m.type == Message.Type.NODE_DOWN){
                m.receiver.messageQueue.add(m);
                System.out.println(m.toString());
            }
		}
		Network.messagesToSend.clear();
	}
	
	public static float distanceBetweenNodes(Node a, Node b){
		double sqdist = Math.pow((a.positionX - b.positionX),2.0) + Math.pow((a.positionY - b.positionY),2.0);
		return (float) Math.sqrt(sqdist);
	}
}
