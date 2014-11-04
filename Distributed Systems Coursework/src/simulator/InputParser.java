package simulator;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import network.Network;

public class InputParser {

	public static Network buildNetwork(String filepath, float nodeRange){
		Network outputNetwork = new Network();
		
		String line;
		try {
			FileReader r = new FileReader(filepath);
			BufferedReader reader = new BufferedReader(r);
			
			line = reader.readLine();
			while(line != null){
				// Parse line
				String[] st = line.replaceAll(",","").split(" "); // Remove commas and split on spaces
				switch(st[0]){
				case "node":
					int nodeId = Integer.parseInt(st[1]);
					float posX = Float.parseFloat(st[2]);
					float posY = Float.parseFloat(st[3]);
					float energy = Float.parseFloat(st[4]);
					outputNetwork.addNode(nodeId, posX, posY, energy, nodeRange);
					break;
				case "bcst":
					// For later tasks
					int nId = Integer.parseInt(st[2]);
					outputNetwork.addBroadcast(nId);
					break;
				default:
					Network.minimumEnergy = Float.parseFloat(st[0]);
				}
				
				// Read next line
				line = reader.readLine();
			}
			reader.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return outputNetwork;
	}
	
}
