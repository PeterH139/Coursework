package network;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import simulator.Log;

public class Node {
	
	public float energyLevel;
	public boolean isAlive;
	public int nodeId;
	public float range;
	public float positionX;
	public float positionY;
	public List<Node> neighbours;
	public Queue<Message> messageQueue;
	
	public int leaderId;
	public List<Node> treeNodes;
	public boolean isLeader;
	public List<Edge> candidateEdges;
	
	public float mwoeWeight;
	public Edge mwoe;
	public int numEdgesWaitingFor;
	
	public Node(int nodeId, float positionX, float positionY, float initialEnergy, float range){
		this.nodeId = nodeId;
		this.positionX = positionX;
		this.positionY = positionY;
		this.energyLevel = initialEnergy;
		this.isAlive = true;
		this.range = range;
		this.neighbours = new ArrayList<Node>();
		this.messageQueue = new LinkedList<Message>();
		
		this.isLeader = true;
		this.leaderId = nodeId;
		this.treeNodes = new ArrayList<Node>();
		this.candidateEdges = new ArrayList<Edge>();
		
		this.mwoeWeight = Float.MAX_VALUE;
		this.mwoe = null;
		this.numEdgesWaitingFor = 0;
	}
	
	private void send(Message msg){
		Network.messagesToSend.add(msg);
	}
	
	private void broadcast(Message m){
		for (Node n : this.treeNodes){
			if (!m.sender.equals(n)){
				send(new Message(this, n, m.type, m.edge, m.leaderId));
			}
		}	
	}
	
	public void dataBroadcast(){
		dataBroadcast(null);
	}
	
	private void dataBroadcast(Message msg){
		for (Node n : this.treeNodes){
			if (msg == null || !msg.sender.equals(n)){ // Message will only equal null on first call
				send(new Message(this, n, Message.Type.DATA_MESSAGE));
				this.energyLevel -= Network.distanceBetweenNodes(this, n) * Message.MESSAGE_COST_MULTIPLIER;
				Log.writeData(this, n);
				// Check if this node is now in danger.
				if (this.energyLevel < Network.minimumEnergy){
					// Broadcast to all tree nodes that this node is going down
					for (Node m : this.treeNodes) send(new Message(this, m, Message.Type.NODE_DOWN));
					// This node is now dead, and it cannot be a leader.
					this.isAlive = false;
					this.isLeader = false;
					System.out.println("Node Death " + this.nodeId);
					Log.writeNodeDown(this);
					// Stop sending data messages.
					break;
				}
			}
		}
	}
	
	/**
	 * @return MWOE from this node or null if no outgoing edges
	 */
	private Edge findCandidateEdge(){
		// Look at the edges around this node and pick one that is the lowest weight
		float minWeight = Float.MAX_VALUE;
		Node minNode = null;
		for (Node n : this.neighbours){
			float dist = Network.distanceBetweenNodes(this,n);
			// If this is smaller than the previous smallest edge and it belongs to a different leader
			if (dist < minWeight && n.leaderId != this.leaderId && n.isAlive){
				minWeight = dist;
				minNode = n;
			}
		}
		
		// Return an edge only if this node has an outgoing edge at all.
		if (minNode != null){
			return new Edge(this, minNode, minWeight);
		} else {
			return null;
		}
	}
	
	public void initiateDiscover(Node m){
		this.send(new Message(this, m, Message.Type.DISCOVER));
	}
	
	/**
	 * Call on leader nodes only.
	 * Adds its minEdge to a list of candidate edges and broadcast a message to all other nodes in the tree
	 * to find their MWOE and report it back.
	 */
	public void initiateEdgeFind(){
		this.candidateEdges.clear(); // Clear the candidate edges
		Edge minEdge = this.findCandidateEdge();
		if (minEdge != null){
			this.candidateEdges.add(minEdge);	
		}
		for (Node n : treeNodes){
			this.send(new Message(this, n, Message.Type.FIND_MWOE));
		}
	}
	
	public void initiateMerge(){
		// Find the actual minimum of the MWOEs
		if (candidateEdges.size() > 0){
			Edge minimumEdge = candidateEdges.get(0);
			for (Edge e : candidateEdges){
				minimumEdge = Edge.smallerOf(e, minimumEdge);
			}
			if (this.nodeId == minimumEdge.left.nodeId){
                // If the connection comes from the leader node, just send a connect message.
				this.send(new Message(this, minimumEdge.right, Message.Type.CONNECT, minimumEdge));
			} else {
				for (Node n : this.treeNodes){
					this.send(new Message(this, n, Message.Type.SELECTED_MWOE, minimumEdge));
				}
			}	
		}
	}
	
	public void initiateLeaderChange(){
		for (Node n : this.treeNodes){
			this.send(new Message(this, n, Message.Type.LEADER_CHANGE, this.nodeId));
		}
	}
	
	public void timestep(){		
		// Handle all messages that have been received and carry out required calculations
		Message m = this.messageQueue.poll(); // First Message
		while(m != null){
			switch(m.type){
			case DISCOVER:
				send(new Message(this, m.sender, Message.Type.DISCOVER_REPLY));
				break;
			case DISCOVER_REPLY:
				this.neighbours.add(m.sender);
				System.out.println("Link " + this.nodeId + " to " + m.sender.nodeId);
				break;
			case FIND_MWOE:
				// Test all neighbours to find the MWOE.
				for (Node n : this.neighbours){
                    if (n.isAlive){
                        numEdgesWaitingFor++;
                        send(new Message(this, n, Message.Type.TEST_EDGE, this.leaderId));
                    }
				}
				// Broadcast in tree that leader wants MWOE
				broadcast(m);
				break;
			case REPORT_MWOE:
				if (this.isLeader){
					 // We have received a response
					this.candidateEdges.add(m.edge);
				} else {
					// Keep broadcasting
					broadcast(m);
				}
				break;
			case SELECTED_MWOE:
				if (m.edge.left.nodeId == this.nodeId){
					// We are at the node that needs to make the connection.
					send(new Message(this, m.edge.right, Message.Type.CONNECT, m.edge));
				} else {
					// Need to keep broadcasting message in the tree.
					broadcast(m);
				}
				break;
			case CONNECT:
				System.out.println("Connect Request: " + m.edge.left.nodeId + " to " + m.edge.right.nodeId + " (" + this.nodeId + ")");
				send(new Message(this, m.sender, Message.Type.CONNECT_ACCEPT, m.edge));
				if (!this.treeNodes.contains(m.sender)) this.treeNodes.add(m.sender);
				Log.writeEdge(m.edge);
				break;
			case CONNECT_ACCEPT:
				System.out.println("Connect Accepted." + this.nodeId + " to " + m.sender.nodeId);
				if (!this.treeNodes.contains(m.sender)) this.treeNodes.add(m.sender);
				break;
			case LEADER_CHANGE:
				if (this.leaderId < m.leaderId){
					// We have a new leader, and this node is definitely not the leader.
					this.leaderId = m.leaderId;
					this.isLeader = false;
					this.candidateEdges.clear(); // No longer the leader, we shouldnt have any candidate edges.
				}
				// Continue broadcast in tree
				broadcast(m);
				break;
			case TEST_EDGE:
				// Check if our leader id is different to the leader id of the message sender and reply
				if (this.leaderId != m.leaderId){
					send(new Message(this, m.sender, Message.Type.ACCEPT_EDGE));
				} else {
					send(new Message(this, m.sender, Message.Type.REJECT_EDGE));
				}
				break;
			case ACCEPT_EDGE:
				this.numEdgesWaitingFor--; // We have received a reply from an edge, and we should action on it.
				
				// Check to see if this edge has lower weight than previous minEdge
				// If so, update the mwoe. Otherwise do nothing.				
				Edge candidateEdge = new Edge(this, m.sender);
				if (candidateEdge.weight < this.mwoeWeight){
					this.mwoeWeight = candidateEdge.weight;
					this.mwoe = candidateEdge;
				}
				
				// Check if we have received all replies yet
				if (this.numEdgesWaitingFor == 0) {
					// Initiate broadcast back in tree if we have found one.
					if (this.mwoe != null){
						send(new Message(this, m.sender, Message.Type.REPORT_MWOE, mwoe));
						this.mwoe = null; // Done for this round. Reset
					} 
				}
				break;
			case REJECT_EDGE:
				this.numEdgesWaitingFor--; // We have received a reply from an edge, but we should ignore it.
				
				// Check if we have received all replies yet
				if (this.numEdgesWaitingFor == 0) {
					if (this.mwoe != null){
						// Initiate broadcast back in tree that we have found one.
						send(new Message(this, m.sender, Message.Type.REPORT_MWOE, mwoe));
						this.mwoe = null; // Done for this round. Reset
					} 
				}
				break;
			case DATA_MESSAGE:
				dataBroadcast(m); // Continue data broadcast
				break;
			case NODE_DOWN:
				// The sender of this message has just gone down, remove it from the tree nodes.
				this.treeNodes.remove(m.sender);
				// Declare this node an emergency leader and broadcast this fact to its tree.
				this.isLeader = true;
				this.leaderId = this.nodeId;
				for (Node n : this.treeNodes){
					send(new Message(this, n, Message.Type.EMERGENCY_LEADER, this.nodeId));
				}
				break;
			case EMERGENCY_LEADER:
				// A node has gone down, we need to rebuild the tree.
				// Our new leader is the leaderId contained in this message.
				this.isLeader = false;
				this.leaderId = m.leaderId;
				// Broadcast to the rest of the tree.
				broadcast(m);
				break;
			default:
				System.err.println("Message type not recognized. " + m.type);
				System.exit(-1);
			}
			
			m = this.messageQueue.poll(); // Next message
		}
	}
	@Override
	public String toString() {
		return "Node " + this.nodeId;
	}
	
	@Override
	public boolean equals(Object obj) {
		Node n = (Node) obj;
		return this.nodeId == n.nodeId;
	}
}
