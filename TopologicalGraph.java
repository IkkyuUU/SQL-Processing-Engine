import java.util.ArrayList;

/**
 * This class is used for store information of a topological sort graph. Each of this class
 * represents a node in topological sort graph.
 * Each node in the graph should have a array that contains its predecessor, and an array
 * that contains its descendant. 
 * @author Zhiqian Yu, Peiying Deng, Chao Xi 
 *
 */

public class TopologicalGraph {
	public String nodeNum;
	public ArrayList<String> predecessor;
	public ArrayList<String> descendent;
	
	/**
	 * Initialize the topological graph.
	 * @param num The number of the node in the graph
	 */
	public TopologicalGraph(String num) {
		nodeNum = num;
		predecessor = new ArrayList<String>();
		descendent = new ArrayList<String>();
	}
}
