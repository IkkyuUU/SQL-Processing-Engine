import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Get the input from InputProcessing, pre-processing the input in order to use them in
 * output conveniently. The class mainly deal with aggregate functions and such that
 * clause. 
 * @author Zhiqian Yu, Peiying Deng, Chao Xi
 *
 */

public class PreProcessing {
	
	private static ArrayList<String> AggrFunc;
	private HashMap<String, SuchThatClause> suchThat = new HashMap<String, SuchThatClause>();
	
	/**
	 * Constructor of the class. Initialize the private member of it.
	 * @param F_V The aggregate functions in the input
	 */
	public PreProcessing(ArrayList<String> F_V) {
		AggrFunc = F_V;
	}
	
	/**
	 * Given the number of a grouping variable, find out all its aggregate functions in
	 * the MFStructure.
	 * @param GV the number of a grouping variable
	 * @return A list of aggregation functions of the given grouping variable
	 */
	private static ArrayList<String> getAggrFuncByGV(int GV) {
		String gv = Integer.toString(GV);
		ArrayList<String> listOfAggrFunc = new ArrayList<String>();
		for(String aggr : AggrFunc){
			if(aggr.contains(gv)){
				listOfAggrFunc.add(aggr);
			}
		}
		return listOfAggrFunc;
	}
	
	/**
	 * Given an aggregate function, get the operation of the aggregate function
	 * @param aggr an aggregate function
	 * @return The operation of the aggregate function
	 */
	public String getFuctionOperation(String aggr) {
		String temp[] = aggr.split("_");
		return temp[0];
	}
	
	/**
	 * Given the number of a grouping variable, find out all operands of its aggregate
	 * functions.
	 * @param GV_Num the number of a grouping variable
	 * @return A list of operands of its aggregate functions
	 */
	public ArrayList<String> getFunctionAttr(int GV_Num) {
		ArrayList<String> listOfAggrFunc = getAggrFuncByGV(GV_Num);
		Set<String> setOfFuncAttr = new HashSet<String>();
		for(String temp : listOfAggrFunc){
			setOfFuncAttr.add(temp.split("_")[2].split(";")[0]);
		}
		ArrayList<String> listOfFuncAttr = new ArrayList<String>();
		for(String temp : setOfFuncAttr){
			listOfFuncAttr.add(temp);
		}
		return listOfFuncAttr;
	}
	
	/**
	 * Check whether a grouping variable has its own aggregate function.
	 * @param GV_Num Grouping variable's number, including grouping variable 0
	 * @return <code>Aggregate Function</code> if the grouping variable contains aggregate function;
	 * 		 	<code>null</code> if the grouping variable doesn't contain aggregate function.
	 */
	public ArrayList<String> hasAggrFuncGV(int GV_Num){
		return getAggrFuncByGV(GV_Num);
	}
	
	/**
	 * Pre-process Such That Clause. Seperate all of predicates to fit in the <class>SuchThatClause</code>
	 * For example: for a predicate "quant_2 > sum_1_quant", after the pre-processing, we should
	 * 				get the following:
	 * 				GVAttr:quant, GVNum:2, Operation:>, GVAttrValue:sum_1_quant,
	 * 				AttrType:int, AttrValueType:2(which means "sum_1_quant" is a aggregates of other GV) 
	 * @param in the instance of InputProcessing
	 */
	public void preProSuchThat(InputProcessing in) {
		String[] stCond = in.getSuchThatCond();
		for(String eachCond : stCond){
			String[] sepCond = eachCond.split(" and ");
			//For each possible operation in the predicates, seperate again
			for(String temp : sepCond){
				if(temp.contains("!=")){
					String[] parts = temp.split("!=");
					if(suchThat.containsKey(parts[0].split("_")[1])){
						SuchThatClause sh = suchThat.get(parts[0].split("_")[1]);
						sh.GVAttr.add(parts[0].split("_")[0]);
						sh.Operation.add("!=");
						sh.AttrValue.add(parts[1]);
						sh.AttrType.add(SuchThatClause.getAttrType(parts[0].split("_")[0]));
						sh.AttrValueType.add(SuchThatClause.getAttrValueType(parts[1]));
					}
					else {
						SuchThatClause sh = new SuchThatClause(parts[0].split("_")[0], parts[0].split("_")[1], parts[1], "!=");
						suchThat.put(parts[0].split("_")[1], sh);
					}
				}else if (temp.contains(">=")) {
					String[] parts = temp.split(">=");
					System.out.println(Arrays.toString(parts));
					if(suchThat.containsKey(parts[0].split("_")[1])){
						SuchThatClause sh = suchThat.get(parts[0].split("_")[1]);
						sh.GVAttr.add(parts[0].split("_")[0]);
						sh.Operation.add(">=");
						sh.AttrValue.add(parts[1]);
						sh.AttrType.add(SuchThatClause.getAttrType(parts[0].split("_")[0]));
						sh.AttrValueType.add(SuchThatClause.getAttrValueType(parts[1]));
					}
					else {
						SuchThatClause sh = new SuchThatClause(parts[0].split("_")[0], parts[0].split("_")[1], parts[1], ">=");
						suchThat.put(parts[0].split("_")[1], sh);
					}
				}
				else if (temp.contains("<=")) {
					String[] parts = temp.split("<=");
					System.out.println(Arrays.toString(parts));
					if(suchThat.containsKey(parts[0].split("_")[1])){
						SuchThatClause sh = suchThat.get(parts[0].split("_")[1]);
						sh.GVAttr.add(parts[0].split("_")[0]);
						sh.Operation.add("<=");
						sh.AttrValue.add(parts[1]);
						sh.AttrType.add(SuchThatClause.getAttrType(parts[0].split("_")[0]));
						sh.AttrValueType.add(SuchThatClause.getAttrValueType(parts[1]));
					}
					else {
						SuchThatClause sh = new SuchThatClause(parts[0].split("_")[0], parts[0].split("_")[1], parts[1], "<=");
						suchThat.put(parts[0].split("_")[1], sh);
					}
				}else if (temp.contains(">")) {
					String[] parts = temp.split(">");
					System.out.println(Arrays.toString(parts));
					if(suchThat.containsKey(parts[0].split("_")[1])){
						SuchThatClause sh = suchThat.get(parts[0].split("_")[1]);
						sh.GVAttr.add(parts[0].split("_")[0]);
						sh.Operation.add(">");
						sh.AttrValue.add(parts[1]);
						sh.AttrType.add(SuchThatClause.getAttrType(parts[0].split("_")[0]));
						sh.AttrValueType.add(SuchThatClause.getAttrValueType(parts[1]));
					}
					else {
						SuchThatClause sh = new SuchThatClause(parts[0].split("_")[0], parts[0].split("_")[1], parts[1], ">");
						suchThat.put(parts[0].split("_")[1], sh);
					}
				}else if (temp.contains("<")) {
					String[] parts = temp.split("<");
					System.out.println(Arrays.toString(parts));
					if(suchThat.containsKey(parts[0].split("_")[1])){
						SuchThatClause sh = suchThat.get(parts[0].split("_")[1]);
						sh.GVAttr.add(parts[0].split("_")[0]);
						sh.Operation.add("<");
						sh.AttrValue.add(parts[1]);
						sh.AttrType.add(SuchThatClause.getAttrType(parts[0].split("_")[0]));
						sh.AttrValueType.add(SuchThatClause.getAttrValueType(parts[1]));
					}
					else {
						SuchThatClause sh = new SuchThatClause(parts[0].split("_")[0], parts[0].split("_")[1], parts[1], "<");
						suchThat.put(parts[0].split("_")[1], sh);
					}
				}else if (temp.contains("=")) {
					String[] parts = temp.split("=");
					System.out.println(Arrays.toString(parts));
					if(suchThat.containsKey(parts[0].split("_")[1])){
						SuchThatClause sh = suchThat.get(parts[0].split("_")[1]);
						sh.GVAttr.add(parts[0].split("_")[0]);
						sh.Operation.add("=");
						sh.AttrValue.add(parts[1]);
						sh.AttrType.add(SuchThatClause.getAttrType(parts[0].split("_")[0]));
						sh.AttrValueType.add(SuchThatClause.getAttrValueType(parts[1]));
					}
					else {
						SuchThatClause sh = new SuchThatClause(parts[0].split("_")[0], parts[0].split("_")[1], parts[1], "=");
						suchThat.put(parts[0].split("_")[1], sh);
					}
				}
			}
		}
	}
	
	/**
	 * The rest of function is to get the information in such that clause, given the number
	 * of grouping variable.
	 * @param GV_Num
	 * @return
	 */
	public ArrayList<String> getSHAttrType(int GV_Num) {
		return suchThat.get(Integer.toString(GV_Num)).AttrType;
	}
	
	public ArrayList<String> getSHGVAttr(int GV_Num) {
		return suchThat.get(Integer.toString(GV_Num)).GVAttr;
	}
	
	public ArrayList<String> getSHAttrValue(int GV_Num) {
		return suchThat.get(Integer.toString(GV_Num)).AttrValue;
	}
	
	public ArrayList<String> getSHOperation(int GV_Num) {
		return suchThat.get(Integer.toString(GV_Num)).Operation;
	}
	
	public ArrayList<Integer> getSHAttrValueType(int GV_Num) {
		return suchThat.get(Integer.toString(GV_Num)).AttrValueType;
	}
	
}
