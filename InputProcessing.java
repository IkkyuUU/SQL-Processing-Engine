import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Function: Read inputs from a .txt file, and return 7 variables we need to generating the 
 * 			 MFStructure and algorithms
 * Note: Need to consider abnormal situation
 * @author Matrix: Zhiqian Yu, Peiying Deng, Chao Xi
 */
public class InputProcessing {
	
	static ArrayList<String> list = new ArrayList<String>();
	
	//Selection Attributes
	static String[] Att = new String[]{};
	//Where Conditions
	static String[] WH = new String[]{};
	//Number of Grouping Variables
	static int n;
	//Grouping Attributes
	static String[] V = new String[]{};
	//Aggregate Functions
	static String[] F_V = new String[]{};
	//Such that Conditions
	static String[] ST = new String[]{};
	//Having Conditions
	static String[] G = new String[]{};	
	
	/**
     * Read file, and return 7 data groups we need
     * @param filePath
     * @return SelectionAttr, NumberOfGV, WhereCond, GroupAttr, 
     * 		   AggregateFunction, SuchThatCond, HavingCond
     */
    public void readFile(String filePath){
        try {
        	String encoding="utf8";
            File file=new File(filePath);
            if(file.isFile() && file.exists()) //if file exists
            {
            	//considering encoding
                InputStreamReader read = new InputStreamReader(new FileInputStream(file),encoding);
                BufferedReader bf = new BufferedReader(read);
                String sp_read = null;
                while((sp_read=bf.readLine())!=null) //keep reading lines
            	{
                	if(!"".equals(sp_read))
                		list.add(sp_read);
            	}
                
                for ( int i = 0; i < list.size(); i++)
                {
                	//store the content for generating problems
                	switch(i)
                	{
                	case 0: Att = list.get(i).split("\\s*,\\s*");
                			//System.out.println(Arrays.toString(Att));
                			break;
                	case 1: if(!list.get(i).toLowerCase().startsWith("null"))
                				WH = list.get(i).split("\\s*,\\s*");
                			//System.out.println(Arrays.toString(WH));
    						break;
                	case 2:	n = Integer.parseInt(list.get(i));
    						//System.out.println(n);
    						break;
                	case 3: V = list.get(i).split("\\s*,\\s*");
            				//System.out.println(Arrays.toString(V));
        					break;
                	case 4: F_V = list.get(i).split("\\s*,\\s*");
            				//System.out.println(Arrays.toString(F_V));
        					break;
                	case 5: ST = list.get(i).split("\\s*,\\s*");
            				//System.out.println(Arrays.toString(ST));
    						break;
                	case 6: if(!list.get(i).toLowerCase().startsWith("null"))
        						G = list.get(i).split("\\s*,\\s*");
            				//System.out.println(Arrays.toString(G));
    						break;
                	}	
                }
                read.close();
            }
            else{ System.out.println("Can't find the INPUTS"); }
        }
        catch (Exception e) {
            System.out.println("ERROR when reading file");
            e.printStackTrace();
        }
    }

    public String[] getSelectionAttr() {
		return Att;
	}

	public String[] getWhereCond() {
		return WH;
	}
	
	public int getNumberOfGV() {
		return n;
	}
	
	public String[] getGroupAttr() {
		return V;
	}
	
	public String[] getAggregateFunction() {
		return F_V;
	}
	
	public String[] getSuchThatCond() {
		return ST;
	}
	
	public String[] getHavingCond() {
		return G;
	}

}