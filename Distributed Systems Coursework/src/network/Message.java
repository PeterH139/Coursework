package network;

public class Message {
	
	public static final int DISCOVER_ID = 0;
	public static final int DISCOVER_REPLY_ID = 1;
	public static final int FIND_MWOE_ID = 2;
	public static final int REPORT_MWOE_ID = 3;
	public static final int SELECTED_MWOE_ID = 4;
	public static final int CONNECT_ID = 5;
	public static final int CONNECT_ACCEPT_ID = 6;
	public static final int LEADER_CHANGE_ID = 7;
	public static final int TEST_EDGE_ID = 8;
	public static final int ACCEPT_EDGE_ID = 9;
	public static final int REJECT_EDGE_ID = 10;
	
	
	public int id;
	public Node sender;
	public Node receiver;
	public Edge edge; // Used in reporting MWOEs
	public int leaderId; // Used in leader change events
	
	public Message(Node sender, Node receiver, int id, Edge edge, int leaderId){
		this.sender = sender;
		this.receiver = receiver;
		this.id = id;
		this.edge = edge;
		this.leaderId = leaderId;
	}
	
	public Message(Node sender, Node receiver, int id){
		this(sender, receiver, id, null, -1);
	}
	
	public Message(Node sender, Node receiver, int id, Edge edge){
		this(sender, receiver, id, edge, -1);
	}
	
	public Message(Node sender, Node receiver, int id, int leaderId){
		this(sender, receiver, id, null, leaderId);
	}
}
