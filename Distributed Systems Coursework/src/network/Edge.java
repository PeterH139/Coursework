package network;

public class Edge {
	public Node left;
	public Node right;
	public float weight;
	
	public Edge(Node leftNode, Node rightNode, float weight){
		this.left = leftNode;
		this.right = rightNode;
		this.weight = weight;
	}
	
	public Edge(Node leftNode, Node rightNode){
		this(leftNode, rightNode, Network.distanceBetweenNodes(leftNode, rightNode));
	}
	
	public static Edge smallerOf(Edge e, Edge f){
		if (e.weight < f.weight) {
			return e;
		} else {
			return f;
		}
	}
	
	public Edge reverse(){
		return new Edge(this.right, this.left, this.weight);
	}

    // An edge is bidirectional, so a -> b is equivalent to b -> a.
	@Override
	public boolean equals(Object obj) {
        if (obj instanceof  Edge)  {
            Edge e = (Edge) obj;
            boolean eq = (this.left == e.left && this.right == e.right)
                    || (this.left == e.right && this.right == e.left);
            return eq;
        } else {
            return super.equals(obj);
        }
	}
	
	@Override
	public String toString() {
		return this.left.toString() + " -> " + this.right.toString() + " (" + this.weight + ")";
	}
}
