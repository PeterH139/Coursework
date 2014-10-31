package simulator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import network.Edge;
import network.Node;

public class Log {

	private static BufferedWriter writer;
	private static File outputFile = new File("output/log.txt");
	private static List<Edge> writtenEdges = new ArrayList<Edge>();
	
	public static void write(String s){
		if (writer == null){
			try {
				if (!outputFile.exists()){
					outputFile.getParentFile().mkdirs();
					outputFile.createNewFile();
				}
				writer = new BufferedWriter(new FileWriter("output/log.txt"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		try {
			writer.write(s + "\n");
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void writeBs(List<Node> nodes){
		List<Node> ns = nodes;
		String s = ""+ns.remove(0).nodeId;
		for (Node n : ns){
			s += ", " + n.nodeId;
		}
		Log.write("bs " + s);
	}
	
	public static void writeElected(List<Node> nodes){
		for (Node n : nodes){
			Log.write("elected " + n.nodeId);
		}
	}
	
	public static void writeEdge(Edge e){
		if (!writtenEdges.contains(e)){
			Log.write("connect " + e.left.nodeId + "-" + e.right.nodeId);
		}
		writtenEdges.add(e);
	}
	
}
