import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Generate all algorithms to calculate the results of the query given the MFStructure.
 * A object of <code>EMFQueryProcessing</code> is able to process the EMF Query inputs based on
 * the MFStructure, to generate a corresponding complete program, then write them to a Java file. 
 * Finally, format the generated Java file.
 * @author Matrix: Zhiqian Yu, Peiying Deng, Chao Xi
 */

public class GenerateAlgorithms {

	/**
	 * Get the all packages we need to use in the generated program.
	 * @return A string which contains all the packages we need to import.
	 */
	private static String GenerateImport() {
		String importTofile = "";
		importTofile = importTofile + "import java.sql.*;\n";
		importTofile = importTofile + "import java.util.*;\n";
		return importTofile;
	}
	
	/**
	 * Generate the function to do the Database connection in the Calculate Program.
	 * @return A function for connecting database.
	 */
	private static String GenerateDBConnection(){
		String dbToFile = "\n    private static ResultSet DBConnection() {\n";
		dbToFile = dbToFile + "        String usr = \"postgres\";\n";
		dbToFile = dbToFile + "        String pwd = \"yobdc1314\";\n";
		dbToFile = dbToFile + "        String url = \"jdbc:postgresql://localhost:5432/postgres\";\n";
		dbToFile = dbToFile + "        try {\n";
		dbToFile = dbToFile + "            Class.forName(\"org.postgresql.Driver\");\n";
		dbToFile = dbToFile + "            System.out.println(\"Success loading Driver!\");\n";
		dbToFile = dbToFile + "            Connection conn = DriverManager.getConnection(url, usr, pwd);\n";
		dbToFile = dbToFile + "            System.out.println(\"Success connecting server!\");\n";
		dbToFile = dbToFile + "            Statement st = conn.createStatement();\n";
		dbToFile = dbToFile + "            String ret = \"select * from sales\";\n";
		dbToFile = dbToFile + "            return st.executeQuery(ret);\n";
		dbToFile = dbToFile + "        } catch (SQLException e) {\n";
		dbToFile = dbToFile + "            System.out.println(\"Connection URL or username or password errors!\");\n";
		dbToFile = dbToFile + "            e.printStackTrace();\n";
		dbToFile = dbToFile + "        } catch(Exception e) {\n";
		dbToFile = dbToFile + "            System.out.println(\"Fail loading Driver!\");\n";
		dbToFile = dbToFile + "            e.printStackTrace();\n";
		dbToFile = dbToFile + "        }\n";
		dbToFile = dbToFile + "        return null;\n";
		dbToFile = dbToFile + "    }\n";
		return dbToFile;
	}
	
	/**
	 * To deal with the WHERE condition
	 * @param wh: a string array contains all the where conditions
	 * @param rsnum: the number of the current time of scan.
	 * @return Judgment statements corresponding to where clause.
	 */
	private static String DecomposeforWH (String[] wh, String rsnum) {
		String judge ="";
		String wh_new ="";
		// deal with the repeated declarations
		Set<String> repDclr = new HashSet<String>();
		
		for(int i=0; i<wh.length; i++){
			if(wh[i].equals("or")||wh[i].equals("OR")){
				judge = judge+" || ";
                continue;
			}
            if(wh[i].equals("and")||wh[i].equals("AND")){
            	judge = judge+" && ";
                continue;
			}
		    String[] sepwh = new String[] {};
		    wh[i] = wh[i].replaceAll("°∞|°±", "\"");
		    sepwh = wh[i].split("\\s+");
		    if(sepwh.length==3){
		    	//type: String
		        if(sepwh[2].contains("\"")){
		        	repDclr.add("            String "+sepwh[0]+" = rs"+rsnum+".getString(\""+sepwh[0]+"\");\n");
		        	//wh_new =wh_new+"            String "+sepwh[0]+" = rs"+rsnum+".getString(\""+sepwh[0]+"\");\n";
		        	if(sepwh[1].equals("!=")||sepwh[1].equals("<>"))
		        		judge = judge+"!"+sepwh[0]+".equals("+sepwh[2] +")";
		        	if(sepwh[1].equals("="))
		        		judge = judge+sepwh[0]+".equals("+sepwh[2] +")";
		        }
		        //type: Int
		       	else{
		       		repDclr.add("            int "+sepwh[0]+" = rs"+rsnum+".getInt(\""+sepwh[0]+"\");\n");
		       		//wh_new =wh_new+"            int "+sepwh[0]+" = rs"+rsnum+".getInt(\""+sepwh[0]+"\");\n";
		       		if(sepwh[1].equals("<>"))
		       			judge = judge+sepwh[0]+"!="+sepwh[2];
		       		if(sepwh[1].equals("="))
		       			judge = judge+sepwh[0]+"=="+sepwh[2];
		       		else
		       			judge = judge+sepwh[0]+sepwh[1]+sepwh[2];
		       	}
		    }
		}
		for(String temp : repDclr){
			wh_new = wh_new + temp;
		}
		wh_new = wh_new + "            if( !("+judge+") )\n";
		wh_new = wh_new + "                continue;\n";
        return wh_new;
    }
	
	/**
	 * Generate the scan0 algorithm for the MFStructure.
	 * @param in: the pre-processing of input file
	 * @param mf: the MFStructure
	 * @param suchThat: the pre-processing of such that clause
	 * @return A function for doing scan0 of the relation to complete the initialization 
	 * 		   of grouping attributes.
	 */
	private static String GenerateScanZero(InputProcessing in, GenerateMFStructure mf, PreProcessing suchThat){
		String SuchThattoFile = "";
		SuchThattoFile = SuchThattoFile + "        ResultSet rs0 = DBConnection();\n";
		SuchThattoFile = SuchThattoFile + "        while(rs0.next()) {\n";
		SuchThattoFile = SuchThattoFile + "            String hashKey = \"\";\n";
		//if the where conditions exists, deal with the them first
		if(in.getWhereCond().length!=0){
			String[] wh = in.getWhereCond();
			String where_c = DecomposeforWH(wh, "0");
			SuchThattoFile = SuchThattoFile + where_c;
		}
		//declare grouping attributes and make them into one hashkey
		for(String ga : mf.getGroupingAttr()){
			SuchThattoFile = SuchThattoFile + "            "+ ga.split(" ")[0] + " " + ga.split(" ")[1] + " = rs0.get" + ga.split(" ")[0].replaceFirst(ga.split(" ")[0].substring(0, 1), ga.split(" ")[0].substring(0, 1).toUpperCase()) +"(\"" + ga.split(" ")[1] + "\");\n";
			SuchThattoFile = SuchThattoFile + "            hashKey += " + ga.split(" ")[1] + ";\n";
		}
		//if aggregate attributes exist in such that conditions, declare them
		ArrayList<String> aggrFuncAttr;
		if((aggrFuncAttr = suchThat.getFunctionAttr(0)) != null){
			for(String temp : aggrFuncAttr){
				SuchThattoFile = SuchThattoFile + "            int " + temp + " = rs0.getInt(\"" + temp + "\");\n";
			}
		}
		//if the hashkey already existed, update the entry in MFStructure
		SuchThattoFile = SuchThattoFile + "            if(mapOfMF.containsKey(hashKey)) {\n";
		SuchThattoFile = SuchThattoFile + "                MFStructure mf = mapOfMF.get(hashKey);\n";
		ArrayList<String> aggrFunc;
		if((aggrFunc = suchThat.hasAggrFuncGV(0)) != null){
			for(String temp : aggrFunc){
				if(suchThat.getFuctionOperation(temp).equals("sum")){
					SuchThattoFile = SuchThattoFile + "                mf." + temp.split(";")[0] + " += " 
										+ temp.split("_")[2] + "\n";
				}
				if(suchThat.getFuctionOperation(temp).equals("count")){
					SuchThattoFile = SuchThattoFile + "                mf."+ temp.split(";")[0] + " += 1;\n";							
				}
				if(suchThat.getFuctionOperation(temp).equals("max")){
					SuchThattoFile = SuchThattoFile + "                if(mf."+ temp.split(";")[0] + " < " 
										+ temp.split("_")[2].split(";")[0] + ") {\n";
					SuchThattoFile = SuchThattoFile + "                    mf."+ temp.split(";")[0] + " = " + temp.split("_")[2] + "\n";
					SuchThattoFile = SuchThattoFile + "                }\n";
				}
				if(suchThat.getFuctionOperation(temp).equals("min")){
					SuchThattoFile = SuchThattoFile + "                if(mf."+ temp.split(";")[0] + " > " 
										+ temp.split("_")[2].split(";")[0] + ") {\n";
					SuchThattoFile = SuchThattoFile + "                    mf."+ temp.split(";")[0] + " = " + temp.split("_")[2] + "\n";
					SuchThattoFile = SuchThattoFile + "                }\n";
				}
			}		
		}
		SuchThattoFile = SuchThattoFile + "            }\n";
		//if the hashkey does not exist, generate a new entry into MFStructure and do the initialization
		SuchThattoFile = SuchThattoFile + "            else {\n";
		SuchThattoFile = SuchThattoFile + "                MFStructure mf = new MFStructure();\n";
		for(String ga : mf.getGroupingAttr()){
			SuchThattoFile = SuchThattoFile + "                mf." + ga.split(" ")[1] + " = " + ga.split(" ")[1] + ";\n";
		}
		for(String temp : aggrFunc){
			if(suchThat.getFuctionOperation(temp).equals("sum")){
				SuchThattoFile = SuchThattoFile + "                mf." + temp.split(";")[0] + " = " 
									+ temp.split("_")[2] + "\n";
			}
			if(suchThat.getFuctionOperation(temp).equals("count")){
				SuchThattoFile = SuchThattoFile + "                mf."+ temp.split(";")[0] + " = 1;\n";							
			}
			if(suchThat.getFuctionOperation(temp).equals("max")){
				SuchThattoFile = SuchThattoFile + "                mf." + temp.split(";")[0] + " = " 
						+ temp.split("_")[2] + "\n";
			}
			if(suchThat.getFuctionOperation(temp).equals("min")){
				SuchThattoFile = SuchThattoFile + "                mf." + temp.split(";")[0] + " = " 
						+ temp.split("_")[2] + "\n";
			}
		}
		SuchThattoFile = SuchThattoFile + "                mapOfMF.put(hashKey, mf);\n";
		SuchThattoFile = SuchThattoFile + "            }\n";
		SuchThattoFile = SuchThattoFile + "        }\n";
		return SuchThattoFile;
	}
	
	/**
	 * Generate the algorithm for recursive scans of the relation.
	 * @param in: the pre-processing of input file
	 * @param mf: the MFStructure
	 * @param suchThat: the pre-processing of such that clause
	 * @return A function for during the ith scan of the relation, computing the aggregate function's 
	 * 		   value of certain grouping variable.
	 */
	private static String GenerateSuchThat(InputProcessing in, PreProcessing suchThat, GenerateMFStructure mf) {
		String SuchThattoFile = "\n    private static void CalculateMF() throws SQLException {\n";
		// Scan 0 to generate entries in MFStructure
		SuchThattoFile += GenerateScanZero(in, mf, suchThat);
		// Scan n times to update all entries in MFStructure
		for(int i = 1; i <= in.getNumberOfGV(); i++){
			SuchThattoFile = SuchThattoFile + "        ResultSet rs" + Integer.toString(i) + " = DBConnection();\n";
			SuchThattoFile = SuchThattoFile + "        while(rs" + Integer.toString(i) + ".next()) {\n";
			//if the where conditions exists, deal with the them first
			if(in.getWhereCond().length!=0){
				String[] wh = in.getWhereCond();
				String where_c = DecomposeforWH(wh, Integer.toString(i));
				SuchThattoFile = SuchThattoFile + where_c;
			}
			//declare all attributes that needed in the update of MFStructure
			ArrayList<String> AttrType = suchThat.getSHAttrType(i);
			ArrayList<String> GVAttr = suchThat.getSHGVAttr(i);
			Set<String> osForGetDataFromDB = new HashSet<String>(); //Avoid repeated declarations
			for(int j = 0; j < AttrType.size(); j++){
				osForGetDataFromDB.add("            " + AttrType.get(j) + " " + GVAttr.get(j) + " = rs"+  Integer.toString(i) + ".get" 
						+ AttrType.get(j).replace(AttrType.get(j).substring(0, 1), AttrType.get(j).substring(0, 1).toUpperCase()) + "(\"" + GVAttr.get(j) + "\");\n");
			}
			ArrayList<String> aggrFuncAttr;
			if((aggrFuncAttr = suchThat.getFunctionAttr(i)) != null){
				for(String temp : aggrFuncAttr){
					osForGetDataFromDB.add("            int " + temp + " = rs" + Integer.toString(i) + ".getInt(\"" + temp + "\");\n");
				}
			}
			for(String temp : osForGetDataFromDB){
				SuchThattoFile = SuchThattoFile + temp;
			}
			//materialize the judgments in such that conditions
			ArrayList<String> AttrValue = suchThat.getSHAttrValue(i);
			ArrayList<String> Operation = suchThat.getSHOperation(i);
			ArrayList<Integer> AttrValueType = suchThat.getSHAttrValueType(i);
			ArrayList<String> tempCond = new ArrayList<String>();
			ArrayList<String> refCond = new ArrayList<String>();
			for(int j = 0; j < AttrValueType.size(); j++){
				//Check the predicates that is a value, not a reference to former grouping variables
				if(AttrValueType.get(j) == 1){
					if(AttrType.get(j).equals("int")){
						if(Operation.get(j).equals("=")){
							tempCond.add(GVAttr.get(j) + " == " + AttrValue.get(j));
						}
						else{
							tempCond.add(GVAttr.get(j) + " " + Operation.get(j) + " " + AttrValue.get(j));
						}
						
					}
					else {
						tempCond.add(GVAttr.get(j) + ".equals(\"" + AttrValue.get(j).split("'")[1] + "\")");
					}
				}
				//the predicates that is a former grouping variable
				else if(AttrValueType.get(j) == 0){
					if(AttrType.get(j).equals("int")){
						if(Operation.get(j).equals("=")){
							refCond.add("mf." + GVAttr.get(j) + " == " + AttrValue.get(j));
						}
						else{
							refCond.add(AttrValue.get(j) + " " + Operation.get(j) + " " + "mf." + GVAttr.get(j));
						}
						
					}
					else {
						if(Operation.get(j).equals("!=")){
							refCond.add("!(mf." + GVAttr.get(j) + ".equals(" + GVAttr.get(j) + "))");
						}
						else{
							refCond.add("mf." + GVAttr.get(j) + ".equals(" + GVAttr.get(j) + ")");
						}		
					}
				}
				//the predicates that is a aggregate function
				else {
					//the predicates that is avg(), we need to deal with it into sum/count
					if(AttrValue.get(j).split("_")[0].equals("avg")){
						String opSum = AttrValue.get(j).replaceFirst(AttrValue.get(j).substring(0, 3), "sum");
						String opCount = AttrValue.get(j).replaceFirst(AttrValue.get(j).substring(0, 3), "count");
						refCond.add(" doAvg((float)(mf." + opSum + "), mf." + opCount + ") " + Operation.get(j) + GVAttr.get(j));
					} 
					else{
						if(Operation.get(j).equals("=")){
							refCond.add(GVAttr.get(j) + " == mf." + AttrValue.get(j));
						}
						else{
							refCond.add(" mf." + AttrValue.get(j) + " " + Operation.get(j) + GVAttr.get(j));
						}
					}
				}
			}
			//Connect all value conditions in order to output
			String ValueCond = "";
			for(int j = 0; j < tempCond.size(); j++){
				if(j > 0){
					ValueCond += " && ";
				}
				ValueCond += tempCond.get(j);
			}
			//Connect all reference condition in order to output
			String referenceCond = "";
			for(int j = 0; j < refCond.size(); j++){
				if(j > 0){
					referenceCond += " && ";
				}
				referenceCond += refCond.get(j);
			}
			//if there is a value condition, then output the condition
			if(ValueCond.length() != 0){
				SuchThattoFile = SuchThattoFile + "            if(!(" + ValueCond + ") ) {\n";
				SuchThattoFile = SuchThattoFile + "                continue;\n";
				SuchThattoFile = SuchThattoFile + "            }\n";
			}
			//For each predicate that references to the former grouping variable, update all relative entries in MF
			SuchThattoFile = SuchThattoFile + "            for(String key : mapOfMF.keySet()) {\n";
			SuchThattoFile = SuchThattoFile + "                MFStructure mf = mapOfMF.get(key);\n";
			SuchThattoFile = SuchThattoFile + "                if(" + referenceCond + ") {\n";
			ArrayList<String> aggrFunc;
			if((aggrFunc = suchThat.hasAggrFuncGV(i)) != null){
				for(String temp : aggrFunc){
					if(suchThat.getFuctionOperation(temp).equals("sum")){
						SuchThattoFile = SuchThattoFile + "                    mf." + temp.split(";")[0] + " += " 
											+ temp.split("_")[2] + "\n";
					}
					if(suchThat.getFuctionOperation(temp).equals("count")){
						SuchThattoFile = SuchThattoFile + "                    mf."+ temp.split(";")[0] + " += 1;\n";							
					}
					if(suchThat.getFuctionOperation(temp).equals("max")){
						SuchThattoFile = SuchThattoFile + "                    if(mf."+ temp.split(";")[0] + " < " 
											+ temp.split("_")[2].split(";")[0] + ") {\n";
						SuchThattoFile = SuchThattoFile + "                        mf."+ temp.split(";")[0] + " = " + temp.split("_")[2] + "\n";
						SuchThattoFile = SuchThattoFile + "                    }\n";
					}
					if(suchThat.getFuctionOperation(temp).equals("min")){
						SuchThattoFile = SuchThattoFile + "                    if(mf."+ temp.split(";")[0] + " == 0) {\n";
						SuchThattoFile = SuchThattoFile + "                        mf."+ temp.split(";")[0] + " = " + temp.split("_")[2] + "\n";
						SuchThattoFile = SuchThattoFile + "                    }\n";
						SuchThattoFile = SuchThattoFile + "                    if(mf."+ temp.split(";")[0] + " > " 
											+ temp.split("_")[2].split(";")[0] + ") {\n";
						SuchThattoFile = SuchThattoFile + "                        mf."+ temp.split(";")[0] + " = " + temp.split("_")[2] + "\n";
						SuchThattoFile = SuchThattoFile + "                    }\n";
					}
				}		
			}
			SuchThattoFile = SuchThattoFile + "                }\n";			
			SuchThattoFile = SuchThattoFile + "            }\n";
			SuchThattoFile = SuchThattoFile + "        }\n";
		}
		SuchThattoFile = SuchThattoFile + "    }\n";
		return SuchThattoFile;
	}
    
	/**
	 * To deal with the HAVING condition
	 * @param hc: a string of one of the having conditions
	 * @return Judgment statements corresponding to having clause.
	 */   
    private static String Decompose (String hc) {
        String[] sephc1 = new String[] {};
        String[] sephc2 = new String[] {};
        sephc1 = hc.split("\\s+");
        String hc_new = "";
        for(int i=0; i<sephc1.length; i++){
        	//Preprocess hc, in order to change avg to sum/count
            if(sephc1[i].contains("_")){
                sephc2 = sephc1[i].split("_");
                //if we find 'avg'
                if(("avg").equals(sephc2[0])){
                    sephc1[i] = "doAvg((float)(sum";
                    for(int j=1; j<sephc2.length; j++){
                        sephc1[i] = sephc1[i]+"_"+sephc2[j];
                    }
                    sephc1[i] = sephc1[i]+"),count";
                    for(int j=1; j<sephc2.length; j++){
                        sephc1[i] = sephc1[i]+"_"+sephc2[j];
                    }
                    sephc1[i] = sephc1[i]+")";
                }
            }
            if(sephc1[i].equals("="))
            	sephc1[i] = "==";
            if(sephc1[i].equals("or")||sephc1[i].equals("OR"))
                sephc1[i]="||";
            if(sephc1[i].equals("and")||sephc1[i].equals("AND"))
                sephc1[i]="&&";
            
            hc_new = hc_new+sephc1[i];
        }
        return hc_new;
    }
    
    /**
	 * Generate the algorithm to calculate avg(), meanwhile do the judge on the denominator "count", 
	 * if it is 0, return 0.
	 * @return A function for calculating the average function.
	 */
    private static String GenerateDoAvg(){
        String avToFile = "\n    private static float doAvg(float sum, int cnt){\n";
        avToFile = avToFile + "        float avg=0;\n";
        avToFile = avToFile + "        if(cnt==0)\n";
        avToFile = avToFile + "            return 0;\n";
        avToFile = avToFile + "        else\n";
        avToFile = avToFile + "            avg=sum/cnt;\n";
        avToFile = avToFile + "        return avg;\n";
        avToFile = avToFile + "    }\n";
        
        return avToFile;
    }
    
    /**
	 * Generate the algorithm to deal with Having Conditions.
	 * @param in: the pre-processing of input file
	 * @param mf: the MFStructure
	 * @return A function for calculating having conditions.
	 */
    private static String GenerateHCond (InputProcessing in, GenerateMFStructure mf) {
        
        String[] hc = in.getHavingCond();
        ArrayList<String> aggrFunc = mf.getAggregateFunction();
        //Iterate through the entries of MFStructure
        String gToFile = "\n    private static void HCond (){\n";
        gToFile = gToFile + "        Iterator<Map.Entry<String, MFStructure>> it = mapOfMF.entrySet().iterator();\n";
        gToFile = gToFile + "        while(it.hasNext()){\n";
        gToFile = gToFile + "        	Map.Entry<String, MFStructure> entry = it.next();\n";
        gToFile = gToFile + "        	MFStructure mf = entry.getValue();\n";
        //declare all aggregate attributes
        for(String temp : aggrFunc){
        	gToFile = gToFile + "        	int "+ temp.split(";")[0] +" = mf." + temp.split(";")[0] + ";\n";
        }
        //For the Having condition, if the entry satisfies, continue
        gToFile = gToFile + "        	if(";
        String temp = "";
        for(int i=0; i<hc.length; i++){
        	temp = temp+Decompose(hc[i])+" ";
        }
        gToFile = gToFile + temp;
        gToFile = gToFile + ")\n";
        gToFile = gToFile + "        		continue;\n";
        //if not satisfy, remove the entry
        gToFile = gToFile + "        	else\n";
        gToFile = gToFile + "        		it.remove();\n";
        gToFile = gToFile + "        }\n";
        gToFile = gToFile + "    }\n";
        
        return gToFile;
    }
    
    /**
	 * Preprocess a string in order to change avg to sum/count to do the calculation
	 * @param st: a string need to be processed
	 * @return A new string that turn avg into sum/count
	 */
    private static String DecomposeforPrt (String st) {
        String[] sepst1 = new String[] {};
        String[] sepst2 = new String[] {};
        sepst1 = st.split("\\s+");
        String st_new = "";
        for(int i=0; i<sepst1.length; i++){
        	sepst2 = sepst1[i].split("_");
            //if we find 'avg'
        	if(("avg").equals(sepst2[0])){
        		//doAvg((float)(sum), count);
                sepst1[i] = "doAvg((float)(mf.sum";
                for(int j=1; j<sepst2.length; j++){
                	sepst1[i] = sepst1[i]+"_"+sepst2[j];
                }
                sepst1[i] = sepst1[i]+"),mf.count";
                for(int j=1; j<sepst2.length; j++){
                	sepst1[i] = sepst1[i]+"_"+sepst2[j];
                }
                sepst1[i] = sepst1[i]+")";
            }
            else	//if not 'avg'
                sepst1[i] ="mf."+sepst1[i];

            st_new = st_new+sepst1[i];
        }
        return st_new;
    }
    
    /**
	 * Generate the algorithm to print out the results, including format.
	 * @param in: the pre-processing of input file
	 * @return A function for printing results.
	 */
    private static String GeneratePrt(InputProcessing in){
               
        String[] att = in.getSelectionAttr();
        
        String prToFile = "\n    private static void PrintResult (){\n";
        prToFile = prToFile + "        Set<Map.Entry<String,MFStructure>> Set = mapOfMF.entrySet();\n";
        //=====================
        //Print out the title
        //=====================
        prToFile = prToFile + "        System.out.println(\"";
        for(int i=0; i<att.length; i++){
            if(att[i].equals("cust")){
                prToFile = prToFile +"CUST\"+\"     \"+\"";
                continue;
            }
            if(att[i].equals("prod")){
                prToFile = prToFile +"PROD\"+\"     \"+\"";
                continue;
            }
            if(i==att.length-1){
                prToFile = prToFile +att[i].toUpperCase();
                continue;
            }
            prToFile = prToFile +att[i].toUpperCase()+"\"+\" \"+\"";
        }
        prToFile = prToFile + "\");\n";
        prToFile = prToFile + "        System.out.println(\"";
        for(int i=0; i<att.length; i++){
            String temp ="";
            if(att[i].equals("cust")||att[i].equals("prod")){
                prToFile = prToFile +"========\"+\" \"+\"";
                continue;
            }
            temp = att[i];
            if(i==att.length-1){
                for(int k=0; k<temp.length(); k++)
                    prToFile = prToFile+"=";
                continue;
            }
            for(int k=0; k<temp.length(); k++)
                prToFile = prToFile+"=";
            prToFile = prToFile+"\"+\" \"+\"";
        }
        prToFile = prToFile + "\");\n";
        //=======================
        //Print out the results
        //=======================
        prToFile = prToFile + "        for(Map.Entry<String,MFStructure> entry: Set){\n";
        prToFile = prToFile + "        	MFStructure mf = entry.getValue();\n";
        for(int i=0; i<att.length; i++){
            if(att[i].equals("cust")||att[i].equals("prod")){
                prToFile = prToFile + "        	System.out.print(String.format(\"%-9s\", mf.";
                prToFile = prToFile + att[i]+"));\n";
                continue;
            }
            if(att[i].equals("state")){
                prToFile = prToFile + "        	System.out.print(String.format(\"%-6s\", mf.";
                prToFile = prToFile + att[i]+"));\n";
                continue;
            }
            prToFile = prToFile + "        	System.out.print(String.format(\"%"+att[i].length()+"s \", ";
            Pattern pattern = Pattern.compile("[0-9]*");
            if(att[i].contains("/")){
            	String[] att_sp = att[i].split("/"); 
            	prToFile = prToFile + "(float)(";
            	Matcher isNum = pattern.matcher(att_sp[0]);
            	if (!isNum.matches())	prToFile += "mf.";
            	prToFile += att_sp[0]+")/";
            	isNum = pattern.matcher(att_sp[1]);
            	if (!isNum.matches())	prToFile += "mf.";
            	prToFile += att_sp[1]+"));\n";
            	continue;
            }
            if(att[i].contains("\\*")){
            	String[] att_sp = att[i].split("\\*"); 
            	prToFile = prToFile + "(float)(";
            	Matcher isNum = pattern.matcher(att_sp[0]);
            	if (!isNum.matches())	prToFile += "mf.";
            	prToFile += att_sp[0]+")*";
            	isNum = pattern.matcher(att_sp[1]);
            	if (!isNum.matches())	prToFile += "mf.";
            	prToFile += att_sp[1]+"));\n";
            	continue;
            }
            if(att[i].contains("\\+")){
            	String[] att_sp = att[i].split("\\+"); 
            	prToFile = prToFile + "(float)(";
            	Matcher isNum = pattern.matcher(att_sp[0]);
            	if (!isNum.matches())	prToFile += "mf.";
            	prToFile += att_sp[0]+")+";
            	isNum = pattern.matcher(att_sp[1]);
            	if (!isNum.matches())	prToFile += "mf.";
            	prToFile += att_sp[1]+"));\n";
            	continue;
            }
            if(att[i].contains("-")){
            	String[] att_sp = att[i].split("-"); 
            	prToFile = prToFile + "(float)(";
            	Matcher isNum = pattern.matcher(att_sp[0]);
            	if (!isNum.matches())	prToFile += "mf.";
            	prToFile += att_sp[0]+")-";
            	isNum = pattern.matcher(att_sp[1]);
            	if (!isNum.matches())	prToFile += "mf.";
            	prToFile += att_sp[1]+"));\n";
            	continue;
            }
            prToFile = prToFile + DecomposeforPrt(att[i])+"));\n";
        }
        prToFile = prToFile + "        	System.out.println(\"\");\n";
        prToFile = prToFile + "        }\n";
        prToFile = prToFile + "    }\n";
        
        return prToFile;
    }    
    
    /**
	 * Generate the main function to run the code.
	 * @param in: the pre-processing of input file
	 * @return The main function
	 */
    private static String GeneratePrtMain(InputProcessing in){
        String prToFile = "\n	public static void main(String[] args){\n";
        prToFile = prToFile + "		try {\n";
        prToFile = prToFile + "			CalculateMF();\n";
        prToFile = prToFile + "		} catch (SQLException e) {\n";
        prToFile = prToFile + "			e.printStackTrace();\n";
        prToFile = prToFile + "		}\n";
        //if Having Conditions exist, output the HCond() function
        if(in.getHavingCond().length != 0)
        	prToFile = prToFile + "		HCond();\n";
        prToFile = prToFile + "		PrintResult();\n";
        prToFile = prToFile + "	}\n";
        
        return prToFile;
    }
    
    /**
	 * To print all the algorithms into an EMFQueryProccessing.java file.
	 * @param in: the pre-processing of input file
	 * @param mf: the MFStructure
	 * @param suchThat: the pre-processing of such that clause
	 * @param filepath: where .java file stored in
	 * 
	 */
    public static void PrintAlgorithms(InputProcessing in, PreProcessing suchThat, GenerateMFStructure mf, String filepath) {
		
		String fileout = filepath + "EMFQueryProccessing.java";
		File fout = new File(fileout);
		
		try{
			FileOutputStream output = new FileOutputStream(fout);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(output));
			bw.write(GenerateImport());
			bw.write("\npublic class EMFQueryProccessing {\n");
			bw.write("    private static HashMap<String, MFStructure> mapOfMF = new HashMap<String, MFStructure>();\n");
			bw.write(GenerateDBConnection());
			bw.write(GenerateSuchThat(in, suchThat, mf));
			bw.write(GenerateDoAvg());
			if(in.getHavingCond().length != 0)
				bw.write(GenerateHCond(in, mf));
			bw.write(GeneratePrt(in));
			bw.write(GeneratePrtMain(in));
			bw.write("}\n");
			bw.close();
		}catch (FileNotFoundException e){
			System.err.println("Error Opening the file!");
			e.printStackTrace();
		}catch (IOException e){
			System.err.println("Error Writting the file!");
			e.printStackTrace();
		}
	}
	
    public static void getFromInterface(String path) {
		String filepath = "/Users/fisheryzhq/Documents/workspace/EMFGeneratedCode/src/";
		InputProcessing in = new InputProcessing();
		in.readFile(path);
		GenerateMFStructure mf = new GenerateMFStructure();
		mf.PrintMFStruct(in.getGroupAttr(), in.getAggregateFunction(), filepath);
		PreProcessing suchThat = new PreProcessing(mf.getAggregateFunction());
		suchThat.preProSuchThat(in);
		PrintAlgorithms(in, suchThat, mf, filepath);
	}
    /**
	 * The main function.
	 * Eventually generate two files: MFStructure.java & EMFQueryProccessing.java .
	 */
	/*public static void main(String[] args){
		String filepath = "/Users/fisheryzhq/Documents/workspace/EMFGeneratedCode/src/";
		InputProcessing in = new InputProcessing();
		in.readFile("/Users/fisheryzhq/Documents/workspace/EMFGeneratedCode/src/input.txt");
		GenerateMFStructure mf = new GenerateMFStructure();
		mf.PrintMFStruct(in.getGroupAttr(), in.getAggregateFunction(), filepath);
		PreProcessing suchThat = new PreProcessing(mf.getAggregateFunction());
		suchThat.preProSuchThat(in);
		PrintAlgorithms(in, suchThat, mf, filepath);
	}*/
	
}
