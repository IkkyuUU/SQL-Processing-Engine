import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * This class is used to parse and store the information in Such That Clause. First, We have to
 * make sure which grouping variable those predicates belongs to; second, we should get the
 * type of every attribute in the aggregates; third, we should make sure the operation in 
 * each predicates, so that we can perform clearly <code>if</code> condition. 
 * @author Zhiqian Yu, Peiying Deng, Chao Xi
 *
 */

public class SuchThatClause {
	public ArrayList<String> GVAttr;  //attributes in aggregates function
	public String GV_Number; //the grouping variable number
	public ArrayList<String> AttrValue; //the value that the condition should satisfy
	public ArrayList<String> Operation; 
	public ArrayList<String> AttrType; //the type of attributes in aggregates
	public ArrayList<Integer> AttrValueType; //0:reference, 1:value, 2:aggregates of other gv
	
	/**
	 * The constructor of the class, initialize all the value above.
	 * @param gvAttr
	 * @param gv_num
	 * @param attrValue
	 * @param op
	 */
	
	public SuchThatClause(String gvAttr, String gv_num, String attrValue, String op) {
		// TODO Auto-generated constructor stub
		GVAttr = new ArrayList<String>();
		AttrValue = new ArrayList<String>();
		Operation = new ArrayList<String>();
		AttrType = new ArrayList<String>();
		AttrValueType = new ArrayList<Integer>();
		GVAttr.add(gvAttr);
		GV_Number = gv_num;
		AttrValue.add(attrValue);
		Operation.add(op);
		AttrType.add(getAttrType(gvAttr));
		AttrValueType.add(getAttrValueType(attrValue));
	}
	
	/**
	 * Get the type of attributes and attributes in aggregates. We need to get the type
	 * from the data base.
	 * @param gvAttr
	 * @return The type of a certain attributes
	 */
	public static String getAttrType(String gvAttr) {
		DBConnection db = new DBConnection();
		if(!db.getSqlDriver()){
			System.err.println("Loading SQL diver fail!");
		}
		Connection conn = db.connectDB();
		try {
			Statement st = conn.createStatement();
			String sql = "select data_type from information_schema.columns "
					+ "where column_name = '" + gvAttr + "' and table_name = 'sales'";
			String type = "";
			ResultSet rs = st.executeQuery(sql);
			rs.next();
			//System.out.println(rs.getString(1));
			if(rs.getString(1).contains("char")){
				type = "String";
			}
			if(rs.getString(1).contains("int")){
				type = "int";
			}
			db.closeConnection(conn);
			return type;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}		
	}
	
	/**
	 * Check whether a attribute value is a "value" or a reference to the former grouping variable
	 * @param attrValue
	 * @return <code>1</code> if it is a reference; <code>0</code> if it is a "value"; <code>2</code>
	 * 			if it is a aggregates of other grouping variable.
	 */
	public static Integer getAttrValueType(String attrValue) {
		//if a string contains "'", make it a value
		if(attrValue.contains("'")){
			return 1;
		}
		//if a string contains "_", make it a aggregates of other grouping variable
		if(attrValue.contains("_")){
			return 2;
		}
		//if a string consists of all digits, make it a value
		int count = 0;
		for(int i = 0; i < attrValue.length(); i++){
			if(Character.isDigit(attrValue.charAt(i))){
				count++;
			}
		}
		//otherwise, make it a reference
		if(count == attrValue.length()){
			return 1;
		}
		else{
			return 0;
		}
	}
}
