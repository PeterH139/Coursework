package network;

import java.util.ArrayList;
import java.util.List;

import simulator.Log;

public class Network {
	
	public float minimumEnergy;
	public List<Node> nodes;
	public List<Node> leaders;
	
	public static List<Message> messagesToSend;
	
	public Network(){
		nodes = new ArrayList<Node>();
		leaders = new ArrayList<Node>();
		messagesToSend = new ArrayList<Message>();
	}
	
	public void addNode(int nodeId, float posX, float posY, float energy, float range){
		Node n = new Node(nodeId, posX, posY, energy, range);
		nodes.add(n);
		leaders.add(n);
		System.out.println("Added " + n.nodeId + " to leaders");
	}
	
	public void discover(){
		for (Node n : nodes){
			for (Node m : nodes){
				if (n.nodeId == m.nodeId){
					continue;
				} else if (distanceBetweenNodes(n,m) <= n.range){
					n.initiateDiscover(m);
				}
			}
		}
		waitForExecution();
	}
	
	public void buildMst(){
		 while (true){
			// We have started a new level. Write to the log file which leaders we will be contacting.
			Log.writeBs(leaders);
			
			// Each fragment finds all possible MWOEs
			for (Node n : leaders){
				n.initiateEdgeFind();
			}
			waitForExecution();
			// Poll each of the leaders to find the status of MWOE selection
			int numEdges = 0;
			for (Node n : leaders){
				numEdges += n.candidateEdges.size();
				for (Edge e : n.candidateEdges){
					System.out.println(e.left.nodeId + " " + e.right.nodeId + " " + e.weight);
				}
			}
			if (numEdges == 0) break; // If we have no edges to add to the tree, we are done.
			
			// Tell the leaders to start merging
			for (Node n : leaders) {
				n.initiateMerge();
			}
			waitForExecution();
				
			// Tell the leaders to broadcast their ID in the new tree(s).
			for (Node n : leaders) {
				n.initiateLeaderChange();
			}
			waitForExecution();
			
			for (Node n : nodes){
				System.out.println("Node " + n.nodeId + " led by " + n.leaderId);
			}
			
			// Update the list of leaders.
			List<Node> toRemove = new ArrayList<Node>();
			for (Node n : leaders) if (!n.isLeader) toRemove.add(n);
			leaders.removeAll(toRemove);
			Log.writeElected(leaders); // We have elected new leaders. Write to the log!
			
			for (Node n : leaders) System.out.print(n.nodeId + " "); 
			System.out.println();
		}
	}
	
	/**
	 * We are still executing if there are any messages in the network.
	 */
	private void waitForExecution() {
		boolean executing = true;
		while(executing){
			for (Node n : nodes) n.timestep();
			int numMessages = Network.messagesToSend.size();
			sendAllMessages();
			System.out.println("----");
			executing = (numMessages != 0);
		}
	}
	
	private void sendAllMessages(){
		for (Message m : Network.messagesToSend){
			m.receiver.messageQueue.add(m);
			System.out.println(m.toString());
		}
		Network.messagesToSend.clear();
	}
	
	public static float distanceBetweenNodes(Node a, Node b){
		double sqdist = Math.pow((a.positionX - b.positionX),2.0) + Math.pow((a.positionY - b.positionY),2.0);
		return (float) Math.sqrt(sqdist);
	}
}
