package simulator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import network.Edge;
import network.Node;

/**
 * Used for logging data to the output file. File will be generated in ./output/log.txt
 */
public class Log {

    public static boolean shouldWrite = true;

	private static BufferedWriter writer;
	private static File outputFile = new File("output/log.txt");
	private static List<Edge> writtenEdges = new ArrayList<Edge>();

	private static void write(String s){
        if (shouldWrite){
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
	}
	
	public static void writeBs(List<Node> nodes){
		String s = ""+nodes.get(0).nodeId;
		for (int i = 1; i < nodes.size(); i++){
			s += ", " + nodes.get(i).nodeId;
		}
		Log.write("bs " + s);
	}
	
	public static void writeElected(List<Node> nodes){
		List<Integer> ids = new ArrayList<Integer>();
		for (Node n : nodes){
			if(!ids.contains(n.leaderId)) ids.add(n.leaderId);
		}
		for (int n : ids){
			Log.write("elected " + n);
		}
	}
	
	public static void writeEdge(Edge e){
		if (!writtenEdges.contains(e)){
			Log.write("added " + e.left.nodeId + "-" + e.right.nodeId);
		}
		writtenEdges.add(e);
	}
	
	public static void writeData(Node from, Node to){
		Log.write("data from " + from.nodeId + " to " + to.nodeId + ", energy:" + from.energyLevel);
	}
	
	public static void writeNodeDown(Node n){
		Log.write("node down " + n.nodeId);
	}
	
}
