package network;

public class Message {
	
	public static final float MESSAGE_COST_MULTIPLIER = 1.2f;

    public enum Type {
        DISCOVER, DISCOVER_REPLY,
        FIND_MWOE, REPORT_MWOE, SELECTED_MWOE,
        CONNECT, CONNECT_ACCEPT, LEADER_CHANGE,
        TEST_EDGE, ACCEPT_EDGE, REJECT_EDGE,
        DATA_MESSAGE,
        NODE_DOWN, EMERGENCY_LEADER
    }

    public Type type;
	public Node sender;
	public Node receiver;
	public Edge edge; // Used in reporting MWOEs
	public int leaderId; // Used in leader change events
	
	public Message(Node sender, Node receiver, Type type, Edge edge, int leaderId){
		this.sender = sender;
		this.receiver = receiver;
		this.type = type;
		this.edge = edge;
		this.leaderId = leaderId;
	}
	
	public Message(Node sender, Node receiver, Type type){
		this(sender, receiver, type, null, -1);
	}
	
	public Message(Node sender, Node receiver, Type type, Edge edge){
		this(sender, receiver, type, edge, -1);
	}
	
	public Message(Node sender, Node receiver, Type type, int leaderId){
		this(sender, receiver, type, null, leaderId);
	}
	
	@Override
	public String toString() {
		return this.sender.toString() + " >> " + this.receiver.toString() + ": " + this.type + " " + this.edge + " " + this.leaderId;
	}
}
