import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Generate a MFStructure when given a list of grouping variables and a list of aggregate
 * functions.
 * A object of <code>MFStructure</code> is able to get the type of grouping variables and
 * aggregate functions, then write them to a Java file. Finally, format the generated Java
 * file.
 * @author fisheryzhq
 *
 */

public class GenerateMFStructure {
	
	private static ArrayList<String> AggregateFunction = new ArrayList<String>();
	private static ArrayList<String> GroupAttr = new ArrayList<String>();
    
	/**
	 * Get the type of a given grouping variable. Format the string to match the style
	 * of code in MFStructure.
	 * @param groupAttr A grouping attribute from a list of grouping attributes
	 * @param stmt The statement which is used to execute a certain SQL
	 * @return A string which contains the type and name of the grouping variable
	 * 		   and which is formated to match the style of code in MFStructure, if
	 * 		   SQL is executed successfully; <code>null</code>, if executing SQL
	 * 		   fails.
	 */
	private static String getTypeGroupAttr(String groupAttr, Statement stmt) {
		//Generate SQL to get the type of groupAttr
		String sql = "select data_type from information_schema.columns "
				+ "where column_name = '" + groupAttr + "' and table_name = 'sales'";
		try {
			//Execute the sql and get the type of grouping attribute
			ResultSet rs = stmt.executeQuery(sql);
			rs.next();
			String type = "";
			//Convert the database type to Java type
			if(rs.getString(1).contains("char")){
				type = "String";
			}
			if(rs.getString(1).contains("int")){
				type = "int";
			}
			GroupAttr.add(type + " " + groupAttr);
			String gaTofile = "    public " + type + " " + groupAttr + ";\n";
			return gaTofile;
		} catch (SQLException e) {
			System.err.println("Execute SQL Fail!");
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Get the type of a given aggregate function. Format the string to match the style
	 * of code in MFStructure.
	 * @param aggrFunc A aggregate function from a list of aggregate function
	 * @param stmt The statement which is used to execute a certain SQL
	 * @return A string which contains the type and name of the aggregate function
	 * 		   and which is formated to match the style of code in MFStructure, if
	 * 		   SQL is executed successfully; <code>null</code>, if executing SQL
	 * 		   fails.
	 */	
	private static String getTypeAggregateFunction(String aggrFunc, Statement stmt) {
		//Pre-processing the aggrFunc, in order to find out type
		String[] sepAggrFunc = new String[] {};
		sepAggrFunc = aggrFunc.split("_");
		
		String sql = "select data_type from information_schema.columns "
				+ "where column_name = '" + sepAggrFunc[sepAggrFunc.length-1] + "' and table_name = 'sales'";
		
		String type = "";
		
		try {
			//Execute the sql and get the type of grouping attribute
			ResultSet rs = stmt.executeQuery(sql);
			rs.next();		
			//Convert the database type to Java type
			if(rs.getString(1).contains("char")){
				type = "String";
			}
			if(rs.getString(1).contains("int")){
				type = "int";
			}
		} catch (SQLException e) {
			System.err.println("Execute SQL Fail!");
			e.printStackTrace();
			return null;
		}
		
		String aggrFuncTofile = "";
		
		if(sepAggrFunc[0].equals("avg"))
		{
			aggrFuncTofile = "    public int sum_" + sepAggrFunc[1] + "_" + sepAggrFunc[2] + ";\n" 
					+ "    public int count_" + sepAggrFunc[1] + "_" + sepAggrFunc[2] + ";\n";
		}
		else if(sepAggrFunc[0].equals("count")){
			aggrFuncTofile = "    public int " + aggrFunc + ";\n";
		}
		else {
			aggrFuncTofile = "    public " + type + " " + aggrFunc + ";\n";
		}
		//System.out.println(aggrFuncTofile);
		return aggrFuncTofile;
	}
	
	/**
	 * Given the grouping attributes and aggregate function from the input, generate
	 * MFStructure.java in the file path location. Also, this function will format the
	 * Java file.
	 * @param GA A list of grouping variables read from the input
	 * @param F A list of aggregate functions read from the input
	 * @param filepath The location that the MFStructure.java is stored
	 */	
	public void PrintMFStruct(String[] GA, String[] F, String filepath) {
		
		String fileout = filepath + "MFStructure.java";
		File fout = new File(fileout);
		
		try{
			FileOutputStream output = new FileOutputStream(fout);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(output));
			//First line of the class MFStructure
			bw.write("public class MFStructure {\n");
			
			//Connect to database
			DBConnection db = new DBConnection();
			if(!db.getSqlDriver()){
				System.err.println("Loading SQL Driver Fail!");
			}
			Connection conn = db.connectDB();
			Statement stmt = conn.createStatement();
			
			//Deal with grouping attributes, then write them to MFStructure.java
			for(int i = 0; i < GA.length; i++){
				//Get the type of the grouping variable and format the string to write to file
				String gaTofile = getTypeGroupAttr(GA[i], stmt);
				bw.write(gaTofile);
			}
			//System.out.println(GroupAttr);
			
			//Deal with Aggregate Functions, then write them to MFStructure.java
			Set<String> fTofile = new HashSet<String>();
			for(int i = 0; i < F.length; i++){
				//Get the type of Aggregate Function and format it
				String temp = getTypeAggregateFunction(F[i], stmt);
				//Remove the duplicates
				String[] addStr = temp.split("\n");
				for(String str : addStr){
					fTofile.add(str);
				}
			}
			
			for(String aggrFunc : fTofile){
				//System.out.println(aggrFunc);
				bw.write(aggrFunc + "\n");
				String[] temp = aggrFunc.split(" ");
				//System.out.println(temp[6]);
				AggregateFunction.add(temp[6]);
			}	
			
			//System.out.println(AggregateFunction);
			
			bw.write("}\n");
			bw.close();
			db.closeConnection(conn);
		}catch (FileNotFoundException e){
			System.err.println("Error Opening the file!");
			e.printStackTrace();
		}catch (IOException e){
			System.err.println("Error Writting the file!");
			e.printStackTrace();
		}catch (SQLException e) {
			System.err.println("Fail Creating Statement!");
			e.printStackTrace();
		}
		
		//System.out.println("public class MFStructure {");	
	}
	
	public ArrayList<String> getAggregateFunction(){
		return AggregateFunction;
	}
	
	public ArrayList<String> getGroupingAttr() {
		return GroupAttr;
	}
	
	/*public static void main(String[] args){
		String filepath = "/Users/fisheryzhq/Desktop/CS562-DBMS2/Java/test/";
		GenerateMFStructure mf = new GenerateMFStructure();
		InputProcessing in = new InputProcessing();
		in.readFile("/Users/fisheryzhq/Desktop/CS562-DBMS2/Java/input.txt");
		mf.PrintMFStruct(in.getGroupAttr(), in.getAggregateFunction(), filepath);
		PreProcessing pr = new PreProcessing(AggregateFunction);
		pr.preProSuchThat(in);
	}*/
}
