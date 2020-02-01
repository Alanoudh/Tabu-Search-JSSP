import java.util.LinkedList;
import java.util.Scanner;

/*
 * instance 1: 3 5 2 3 4 7 1 3 2 10 3 2 4 2 1 4 1 7 2 4 3 6 4 2 3 8 4 1 2 11 1 7
 * instance 2: 3 5 2 3 4 7 2 10 3 2 4 2
 * instance 5: 
 *  4 88 8 68 6 94 5 99 1 67 2 89 9 77 7 99 0 86 3 92
 5 72 3 50 6 69 4 75 2 94 8 66 0 92 1 82 7 94 9 63
 9 83 8 61 0 83 1 65 6 64 5 85 7 78 4 85 2 55 3 77
 7 94 2 68 1 61 4 99 3 54 6 75 5 66 0 76 9 63 8 67
 3 69 4 88 9 82 8 95 0 99 2 67 6 95 5 68 7 67 1 86
 1 99 4 81 5 64 6 66 8 80 2 80 7 69 9 62 3 79 0 88
 7 50 1 86 4 97 3 96 0 95 8 97 2 66 5 99 6 52 9 71
 4 98 6 73 3 82 2 51 1 71 5 94 7 85 0 62 8 95 9 79
 0 94 6 71 3 81 7 85 1 66 2 90 4 76 5 58 8 93 9 97
 3 50 0 59 1 82 8 67 7 56 9 96 6 58 4 81 5 59 2 96
 
 instance 6:
 7 62 8 24 5 25 3 84 4 47 6 38 2 82 0 93 9 24 1 66
 5 47 2 97 8 92 9 22 1 93 4 29 7 56 3 80 0 78 6 67
 1 45 7 46 6 22 2 26 9 38 0 69 4 40 3 33 8 75 5 96
 4 85 8 76 5 68 9 88 3 36 6 75 2 56 1 35 0 77 7 85
 8 60 9 20 7 25 3 63 4 81 0 52 1 30 5 98 6 54 2 86
 3 87 9 73 5 51 2 95 4 65 1 86 6 22 8 58 0 80 7 65
 5 81 2 53 7 57 6 71 9 81 0 43 4 26 8 54 3 58 1 69
 4 20 6 86 5 21 8 79 9 62 2 34 0 27 1 81 7 30 3 46
 9 68 6 66 5 98 8 86 7 66 0 56 3 82 1 95 4 47 2 78
 0 30 3 50 7 34 2 58 1 77 5 34 8 84 4 40 9 46 6 44
 
 */
public class TabuSearch {

	static int numberOfJobs =10;
	static int numberOfMachines =10; //==number of operations
	static int[][] problemInstance = new int[numberOfJobs][numberOfMachines*2];
	static int firstPossibleTime = 0;
	static LinkedList<int[]>[] machines = new LinkedList[numberOfMachines];
	static int[] neighbor;
	static int[] s;
	static int[] move;
	static int currentFitness;
	static int[] bestFoundSolution = new int[numberOfJobs];
	static int bestFoundFitness = 100000000;
	static int tabuListSize = (10+numberOfJobs)/numberOfMachines;
	static LinkedList<int[]> tabuList = new LinkedList();
	static int[][] frequencyMatrix = new int[numberOfJobs][numberOfJobs];
	//static int maxNumOfIterations = 100 ;
	static int row =0;
	static int col =0; 
	static int iterations = 10; // Read it from the user
	static int numOfDiversification = 2;//This att indicates the possible number of consequietive Diversification without improvement
    static int countConseqDiversifNoImprovement=0;//used for termination criteria
    static int possibleIterWithoutImprovement = 4;//execute the diversification procedure when *possibleIterWithoutImprovement* iterations is reached without any improvement.
    //read it from user
	public static void main(String[] args) {
		
		Scanner sc = new Scanner(System.in);

		for(int r=0 ; r<numberOfJobs ; r++) 
			for(int c=0 ; c<(numberOfMachines*2) ; c++) 
				problemInstance[r][c] = sc.nextInt();
		
		for(int i=0 ;i<5 ; i++) {
			JSSPTabuSearch();
			System.out.println("Best found fitness in run ["+(i+1)+"] : "+bestFoundFitness);
			reinitilizeValues();

		}			
		//System.out.println("makeSpan = "+decoding(jobList));
	}
	
	
	public static void reinitilizeValues() {
		bestFoundFitness = 100000000;	
		
		for(int i=0 ; i<numberOfMachines ; i++)
			machines[i].clear();	
		
		tabuList.clear();
		bestFoundSolution = new int[numberOfJobs];
		
		for(int i=0 ; i<numberOfMachines ; i++)
			for(int j=0 ; j<numberOfMachines ; j++)
				frequencyMatrix[i][j]=0;
		
		countConseqDiversifNoImprovement=0;
	}
	//*********************************************************
	
	public static void JSSPTabuSearch() {
			
		s = generateInitialSolution();
		int i=0;//count for iteration
		int j=0;// counter for apply diversification criteria
		while((i<iterations)|| (countConseqDiversifNoImprovement!=numOfDiversification)) {
		
			//System.out.println("SOLUTION NUMBER "+ i);
			
			 bestNeighbor(s); /* we checked the conditions non tabu or aspiration criteria holds inside the bestNeighbor(s) fun,
			  Also update the tabu list*/
			 
			//current is better
			 if(currentFitness>bestFoundFitness) {
				 j++;
			}
			 
			//neighbor is better,Update global beat if possible
		    else {
		    	if(currentFitness<bestFoundFitness) {
		    		bestFoundFitness=currentFitness;
		    		bestFoundSolution=neighbor;
		    		countConseqDiversifNoImprovement=0;
		    	}
		    		
		    	
		    }//end else
		      
			 s = neighbor;
			 updateLongTerm();//Update long term memory
			if(j==possibleIterWithoutImprovement) { // Diversification criteria holds
				diversification();
				countConseqDiversifNoImprovement++;
				j=0;
			}
				
			 
			i++;
		}//end while
		
		
	}
	
	//*********************************************************
	
	public static int[] generateInitialSolution() {
		
		int[] initialSolution = new int[numberOfJobs];
		int random;
		int counter= 1;

		
		while(counter<=numberOfJobs) {	
			double randomDouble = Math.random();
			randomDouble = randomDouble * numberOfJobs; // randomDouble = randomDouble * maximum(exlusive) + minimum (inclusive);
			random = (int) randomDouble;
			 if(initialSolution[random]==0) {
				 initialSolution[random]=counter;
				 counter++;
			 }
		}
		return initialSolution;
	}
	//*********************************************************
	
	public static void updateLongTerm() {
		int jobID=0;
		for (int i=0;i<numberOfJobs;i++) {
			jobID=s[i]-1;//cheeckkk
			frequencyMatrix[jobID][i]=frequencyMatrix[jobID][i]+1;
		}
		
	}// end updateLongTerm
	
	//*********************************************************
	/*Function that recives the permutation of jobs, convert it to a complete schedule
	It returns the makeSpan of the resulting schedule */
	public static int decoding(int[] jobList) {
		
		int counter=0;
		int makeSpan = 0;
		int[] currentJob = new int[numberOfMachines*2];

		// Make sure the list is initialized before adding to it
		for(int i=0 ; i<numberOfMachines ; i++)
				machines[i] = new LinkedList<int[]>();
			
		for(int i=0 ; i<numberOfJobs ; i++) {
			currentJob = copyArray(jobList[i]-1);// jobList[i]-1 return the index of the highest priority job
			firstPossibleTime = 0;
			counter=0;
			while(counter<(numberOfMachines*2)) {
				makeSpan = add(currentJob[counter],currentJob[counter+1], makeSpan);//add(machine number, duration , makespan)
				counter+=2;//to go to the next operation 
			}
		}
			
		return makeSpan;
	}
	//*********************************************************
	
	//To add the operation to its machine at the first possible time 
	private static int add(int mNumber , int duration , int makeSpan) {
		
		boolean added = false;
		int iter;
		int[] operation1;
		int[] operation2;
		int startTime = firstPossibleTime; 
		int finishTime = startTime + duration; 
		
		if(!machines[mNumber].isEmpty()) {
			iter = machines[mNumber].size()-1;//number of possible locations to add the new operations
			for(int i=0 ; i<iter ; i++) {
				operation1 = machines[mNumber].get(i);
				operation2 = machines[mNumber].get(i+1);
				
				//operation[0] = start time , operation[1] = finish time
				if((operation2[0]-operation1[1])>=duration) {//there is place to add the operation between op1 and op2 that belongs to the same machine
					if(firstPossibleTime<=operation1[1]) {
						startTime = operation1[1];
						int[] pair = {startTime , finishTime}; 
						machines[mNumber].add(i , pair);
						added = true;
					}else if(firstPossibleTime<operation2[0]) {
						if(finishTime<=operation2[0]) {
							int[] pair = {startTime , finishTime}; 
							machines[mNumber].add(i , pair);
							added = true;
						}
					}else {
						
					}
				}
			}//end loop
			if(!added) {
				if(firstPossibleTime<machines[mNumber].getLast()[1])
					startTime = machines[mNumber].getLast()[1];
				finishTime = startTime + duration;
				int[] pair = { startTime , finishTime}; 
				machines[mNumber].add(pair);
			}
				
		}else {//the liked list is empty 
			int[] pair = { startTime , finishTime}; 
			machines[mNumber].add(pair);
		}
		
		
		firstPossibleTime = finishTime ;
		if( firstPossibleTime > makeSpan)
			makeSpan = firstPossibleTime;
		
		return makeSpan;
	}
	//*********************************************************
	
	private static int[] copyArray(int k) {//copies the kth row of the instance matrix to array 
		
		int[] array = new int[numberOfMachines*2];
		for(int i=0; i<numberOfMachines*2; i++) { 
			array[i] = problemInstance[k][i];
		}	
		return array;
	}
	//*********************************************************
	
	public static void bestNeighbor(int[] current) {
		
		int numberOfNeighbors = factorial(numberOfJobs)/(factorial(numberOfJobs-2)*2);
		int[][] neigbors = new int[numberOfNeighbors][numberOfJobs];
		int[] fitness = new int[numberOfNeighbors];
		int[][] moves = new int[numberOfNeighbors][2];
		int[] currentCopy = new int[numberOfJobs];

		int pos=0;
		int a , b;
		
		for(int i=0 ; i< numberOfJobs ;i++) {
			for(int j=i+1 ; j<numberOfJobs ; j++) {
				
				 for(int k=0; k<numberOfJobs ; k++) {
					 currentCopy[k] = current[k];
			    }
				 
				a=current[i];
			    b=current[j];
			    currentCopy[i]=b;
			    currentCopy[j]=a;
			    
			    for(int k=0; k<numberOfJobs ; k++) 
			    		neigbors[pos][k] = currentCopy[k];

			    fitness[pos]= decoding(currentCopy);
			    moves[pos][0]= a;
			    moves[pos][1]= b;
			    pos++;
			}
		}
		
		neighbor = neigbors[0];
		currentFitness = fitness[0];
		
		for(int i=1 ; i<pos ; i++) //check if pos == numberOfNeighbors
			if(fitness[i]<currentFitness) 
				if(!isTabu(moves[i])||aspirationCriteria(fitness[i])) {
				currentFitness = fitness[i];
				neighbor = neigbors[i];
		        move = moves[i];
		        addToTabu(move);
		        break;
				}
	}
	
	
	//*********************************************************
	
	public static boolean aspirationCriteria(int fitness) {
		return fitness > bestFoundFitness;
	}
	//*********************************************************
	
	public static boolean isTabu(int[] move) {
		
		for(int i=0 ; i< tabuList.size() ; i++)
			if(((move[0]==tabuList.get(i)[0])&&(move[1]==tabuList.get(i)[1]))||((move[1]==tabuList.get(i)[0])&&(move[0]==tabuList.get(i)[1])))
				return true;
		return false;
		
	}
	//*********************************************************
	
	public static void addToTabu(int[] move) {
		
		if(tabuList.size()==tabuListSize)
			tabuList.removeLast();
		tabuList.addFirst(move);
		
	}
	//*********************************************************
	
	public static Boolean diversification() {
		
		int[] newSolution = new int [numberOfJobs];
		int[] values;
		Boolean improvement = false;
		for(int i=0 ; i< numberOfJobs ; i++) {
			 values = getMinValue();
			//System.out.println("row :"+values[1]+" col:"+values[0]);
			newSolution[values[0]]=(values[1]+1);
			
		}
		
		 for(int i=0 ;i< numberOfJobs ;i++)
			  for(int j=0 ; j< numberOfJobs ; j++)
					  frequencyMatrix[i][j] = 0;

		 
		for(int i=0 ; i< numberOfJobs ; i++) {
			//System.out.println("index: "+newSolution[i]);
			
		}
		
		int newSolFitness=decoding(newSolution);
		if(newSolFitness<bestFoundFitness)
			improvement = true;
		s=newSolution;
		
		return improvement;
	}
	
	//*********************************************************
	private static int factorial(int number) {
		int result =1;
		for (int factor = 2; factor <= number; factor++) {
            result *= factor;
        }
		return result;
	}
	//*********************************************************
	
	private static int[] getMinValue(){
		
		  int[]  values = {0,0} ;
		  
		  int minValue = frequencyMatrix[0][0];
		  
		  for(int i=0 ;i< numberOfJobs ;i++){
			  for(int j=0 ; j< numberOfJobs ; j++)
				  if(frequencyMatrix[i][j] < minValue){
					  minValue = frequencyMatrix[i][j];
					  values[0] = j;//col
					  values[1] = i;//row
				  }
		  }
		  
		  for(int i=0 ; i< numberOfJobs; i++)
			  frequencyMatrix[values[1]][i] = 100000000;
		  
		  for(int i=0 ; i<numberOfJobs ; i++)
			  frequencyMatrix[i][values[0]] = 100000000;
		  
		  return values;
		  
		}
	//*********************************************************
	
}//end class 