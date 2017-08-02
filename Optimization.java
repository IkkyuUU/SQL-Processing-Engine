import java.util.ArrayList;
import java.util.HashMap;

/**
 * Generate the topological graph according to the dependency of each grouping variable.
 * Sort the topological graph, store the order in a HashMap with keys that specify the
 * number of table scan.
 * @author Zhiqian Yu, Peiying Deng, Chao Xi
 *
 */
public class Optimization {
	
	//To store the topological graph
	private static HashMap<String, TopologicalGraph> tlGrap = new HashMap<String, TopologicalGraph>();
	//To store the result of topological sort
	private static HashMap<Integer, ArrayList<String>> sortList = new HashMap<Integer, ArrayList<String>>(); 
	
	/**
	 * Initialize the topological graph. Every grouping variable should represent a node,
	 * including the grouping variable zero.
	 * @param in the instance of InputProcessing
	 */
	private static void initialTopological(InputProcessing in) {
		for(int i = 0; i <= in.getNumberOfGV(); i++){
			TopologicalGraph tlg = new TopologicalGraph(Integer.toString(i));
			tlGrap.put(Integer.toString(i), tlg);
		}
	}
	
	/**
	 * Generate the dependency. Find the predecessors and descendants of each node according
	 * to the definition of the dependency in the paper. Add one more rule that if a grouping
	 * variable has the same relative entry as the grouping variable zero, it can be processed
	 * in the scan zero.
	 * @param mf the instance of GenerateMFStructure
	 * @param sh the instance of PreProcessing
	 * @param in the instance of InputProcessing
	 */
	private static void generateDependency(GenerateMFStructure mf, PreProcessing sh, InputProcessing in) {
		ArrayList<String> ga = mf.getGroupingAttr();
		System.out.println(ga);
		for(int i = 1; i <= in.getNumberOfGV(); i++){
			ArrayList<String> testForga = new ArrayList<String>();
			ArrayList<String> testForop = new ArrayList<String>();
			ArrayList<Integer> shValueType = sh.getSHAttrValueType(i);
			ArrayList<String> shValue = sh.getSHAttrValue(i);
			ArrayList<String> shOp = sh.getSHOperation(i);
			System.out.println(shOp);
			for(int j = 0; j < shValueType.size(); j++){
				if(shValueType.get(j) == 1){
					continue;
				}
				else if(shValueType.get(j) == 2){
					//System.out.println(shValue.get(j).split("_")[1]);
					TopologicalGraph tg = tlGrap.get(shValue.get(j).split("_")[1]);
					tg.descendent.add(Integer.toString(i));
					tg = tlGrap.get(Integer.toString(i));
					tg.predecessor.add(shValue.get(j).split("_")[1]);
				}
				else {
					//System.out.println(shOp.get(j));
					testForga.add(shValue.get(j));
					testForop.add(shOp.get(j));
					System.out.println(testForop);
					//System.out.println(shOp.get(j));
				}
			}
			if(ga.size() != testForga.size()){
				TopologicalGraph tg = tlGrap.get("0");
				tg.descendent.add(Integer.toString(i));
				tg = tlGrap.get(Integer.toString(i));
				tg.predecessor.add("0");
			}
			else {
				for(int j = 0; j < ga.size(); j++){
					if(!(ga.get(j).split(" ")[1].equals(testForga.get(j)))){
						TopologicalGraph tg = tlGrap.get("0");
						tg.descendent.add(Integer.toString(i));
						tg = tlGrap.get(Integer.toString(i));
						tg.predecessor.add("0");
						break;
					}
					System.out.println(testForop.get(j));
					if(!(testForop.get(j).equals("="))){
						TopologicalGraph tg = tlGrap.get("0");
						tg.descendent.add(Integer.toString(i));
						tg = tlGrap.get(Integer.toString(i));
						tg.predecessor.add("0");
						break;
					}
				}
			}
			testForga.clear();
		}
	}
	
	/**
	 * Perform the topological sort.
	 * 1. Put all nodes without any predecessor in the sort list
	 * 2. Delete the number of the node from the predecessor of their descendant node
	 * 3. Delete those nodes from the topological graph.
	 * 4. Repeat above steps untill the topological graph is empty.
	 */
	private static void generateSortList() {
		int round = 0;
		while(tlGrap.size() > 0)
		{
			ArrayList<String> temp = new ArrayList<String>();
			for(String key : tlGrap.keySet()){
				TopologicalGraph tg = tlGrap.get(key);
				if(tg.predecessor.size() == 0){
					temp.add(tg.nodeNum);
				}
			}

			for(String re : temp){
				TopologicalGraph tg = tlGrap.get(re);
				for(int i = 0; i < tg.descendent.size(); i++){
					TopologicalGraph rePre = tlGrap.get(tg.descendent.get(i));
					rePre.predecessor.remove(tg.nodeNum);
				}
				tlGrap.remove(re);
			}
			sortList.put(round, temp);
			round++;
			//System.out.println(tlGrap);
			//System.out.println(sortList);
		}
	}
	
	/**
	 * The function that can be referenced from other classes. 
	 * @param mf
	 * @param sh
	 * @param in
	 * @return The sort list of the topological graph.
	 */
	public HashMap<Integer, ArrayList<String>> getTgSortList(GenerateMFStructure mf, PreProcessing sh, InputProcessing in) {
		initialTopological(in);
		generateDependency(mf, sh, in);
		generateSortList();
		return sortList;
	}
	
}
