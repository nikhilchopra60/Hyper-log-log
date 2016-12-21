package ProbablisticCounting;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.HashSet;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.jfree.ui.RefineryUtilities;

public class HyperLogLog {
	
	final static String inputFile = "C:\\sem 1\\ITM\\Project\\FlowTraffic.txt";
	final static int b = 5;
	final static int m= (int)Math.pow(2,b);
	
	final static double Alpha16 = 0.673, Alpha32 = 0.697, Alpha64 = 0.709, AlphaM = 0.7213/(1+1.079/m);
	
	NavigableMap<String, HashSet<String>> flowList = new TreeMap<String, HashSet<String>>();
	HashSet<String> binarySet = new HashSet<String>();
	int M[] = new int[m];
	String line = "";
	
	String sourceIP = "";
	String destIP = "";
	double E = 0.0;
	double Eprime = 0.0;
	int actualCardinality = 0;
	double estimatedCardinality = 0;
	private static HashMap<Integer,Integer> resultGraph = new HashMap<Integer,Integer>();    
	
	
	HyperLogLog(){
		
		try{
		buildFlowHashMap();
		initializeM();
		hashToBinary();
		
		}
		catch(Exception e){
			
		}
		
	}
	
public void buildFlowHashMap() throws IOException{
		
		int start = 1;
		BufferedReader br = new BufferedReader(new FileReader(inputFile));
		//flowList.put("0", "0");
		while((line = br.readLine()) != null) {
		 
			/* Split each line of the file on " "	*/
			String[] columns = line.split(" ");
		 
			/* Assign first column of the file to source IP */
			sourceIP = columns[0];
		
		 
			/* Assign Destination IP */
			for(int k=1; k<columns.length;k++){				 
				if(!(columns[k].equalsIgnoreCase(""))){
					destIP = columns[k];
					break;
				}
			}
			
						
			if(start ==1){
			HashSet<String> newFlow = new HashSet<String>();
			newFlow.add(destIP);
			flowList.put(sourceIP, newFlow);
			start = 0;
			}
			
			else if(flowList.lastKey().equals(sourceIP)){
				flowList.get(sourceIP).add(destIP);
								
			}
			
			else{
				
				HashSet<String> newFlow = new HashSet<String>();
				newFlow.add(destIP);
				flowList.put(sourceIP, newFlow);
			}
				
		}
		
		
		br.close();
	}
	
public void initializeM(){
	
	for(int i=0; i<m;i++){
		M[i] = 0;
	}
}
	/*public void findFirstBBits(){
		for(String s : binarySet){
			
			String lastBBits = s.substring((s.length()-b), s.length());
			int j = Integer.parseInt(lastBBits, 2);
			//String newString = s-lastBBits;
			String w = s.substring(0,(s.length()-b));
			//int M[] = new int[m];
			int W = calP(w);
			if(M[j]>=W){
				M[j] = M[j];
			}
			else{
				M[j] = W;
			}
		}
		
		
		
	}
	*/
	public int calP(String s){
		
		int count = 0;
		for(int i=s.length()-1;i>=0;i--){
			
			if(s.charAt(i)=='0'){
				count++;
			}
			else{
				break;
			}
		}
		
		return count+1;
	}
	public void hashToBinary(){
		
		for (String flow: flowList.keySet()){
			
			actualCardinality = flowList.get(flow).size();
			
			for (String eachFlow : flowList.get(flow)) {
				
				try{
						int hash = (getUUID(eachFlow)& 0x7fffffff);
						String padding = "%"+m+"s";
						String binary = String.format(padding, Integer.toBinaryString(hash)).replace(' ', '0');
						System.out.println("Binary = "+binary);
					//String binary = String.valueOf(Integer.toBinaryString(e)eachFlow.hashCode());
						//int j = eachFlow.hashCode() & 0x3F;
						String lastBBits = binary.substring((binary.length()-b), binary.length());
						//String firstBBits = binary.substring(0, b);
						
						int j = Integer.parseInt(lastBBits, 2);
						//String newString = s-lastBBits;
						String w = binary.substring(0,(binary.length()-b));
					//	String w1 = binary.substring(b,binary.length());
						//int M[] = new int[m];
						int W = calP(w);
						
						M[j] = Math.max(M[j], W);
										
						
						
				}
				catch(Exception e){
					System.out.println();
				}
		}
			
			double sum = 0;
			for(int j=0;j<m;j++){
				sum+= (double) Math.pow(2,(-M[j]));
			}
			
			E  = (Alpha32*(Math.pow(m, 2)))/sum;
			System.out.println(Math.pow(m, 2));
			System.out.println(((Math.pow(m, 2))*(Math.pow(sum, -1))));
			estimatedCardinality = rawEstimate(E);
			System.out.println("n= "+actualCardinality+"ncap =" +estimatedCardinality);
			
			resultGraph.put(actualCardinality, (int)estimatedCardinality);
			initializeM();
		}
	}
	
	public int calculateZeroRegisters(){
		
		int count = 0;
		for(int i=0;i<m;i++){
			if(M[i] == 0){
				count ++;
			}
		}
		
		return count;
	}
	
	
	public double rawEstimate(Double E){
		
				
		if(E<=m*(5/2)){
			double V = calculateZeroRegisters();
			if(V!=0){
				Eprime = m*Math.log(m/V);
				System.out.println(Math.log(m/V));
			}
			else{
			Eprime = E;
			}	
		}
		
		else if (E<=(Math.pow(2,32)/30)){
			Eprime = E;
		}
		
		else if (E>(Math.pow(2,32)/30)){
			Eprime =((-1*Math.pow(2,32))*(Math.log(1-E/(Math.pow(2,32)))));
		}
		
		return Eprime;
	}
	
	
	static int getUUID(String name) throws NoSuchAlgorithmException {
	    SecureRandom srA = SecureRandom.getInstance("SHA1PRNG");
	    srA.setSeed(name.getBytes());
	    return new Integer(srA.nextInt());
	}
	
	public static void main (String x[]){
		
		HyperLogLog hll = new HyperLogLog();
		
		/* Chart Plotting*/
		ScatterPlot chart = new ScatterPlot("ITM Project 4", "HyperLogLog", resultGraph);
	    chart.pack( );          
	    RefineryUtilities.centerFrameOnScreen( chart );          
	    chart.setVisible( true ); 
		
	}
}
