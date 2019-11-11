// IR7A.java CS5154/6054 2019 Cheng
// Read in two inverted indexes with Tfs
// randomly select training set for each run
// follow TrainMultinomialNB and use the training set to
// compute Nc[0], Nc[1], N, Tct for c = 0 and 1 (stored in Tcts)
// Use ApplyMultinomialNB to compute score0 and score1 for each test doc
// make classification for the test docs and report accuracy
// Usage: java IR7A 

import java.io.*;
import java.util.*;

public class IR7A{

  static final int numberOfClasses = 2;
  int[] numberOfDocs = new int[numberOfClasses];
  int[] numberOfTerms = new int[numberOfClasses];
  int[] Nc = new int[numberOfClasses];
  int N = 0;
  ArrayList<HashMap<String, HashMap<Integer, Integer>>> indexes = 
     new ArrayList<HashMap<String, HashMap<Integer, Integer>>>(numberOfClasses);
  boolean[] training0 = null;  boolean[] training1 = null;
  ArrayList<HashMap<String, Integer>> Tcts = 
	new ArrayList<HashMap<String, Integer>>(numberOfClasses);
  int tct = 0;
  double b = 0;
  HashMap<String, Double> r = new HashMap<String, Double>();
  double[] scores0 = null;  double[] scores1 = null;

 void readInvertedIndexWithTfs(String filename, int c){
 	Scanner in = null;  
    	try {
      		in = new Scanner(new File(filename));
    	} catch (FileNotFoundException e){
      		System.err.println("not found");
      		System.exit(1);
    	}
    	String[] tokens = in.nextLine().split(" ");
    	numberOfTerms[c] = Integer.parseInt(tokens[0]);
    	numberOfDocs[c] = Integer.parseInt(tokens[1]);
	HashMap<String, HashMap<Integer, Integer>> index = new
		HashMap<String, HashMap<Integer, Integer>>(numberOfTerms[c]);
    	for (int i = 0; i < numberOfTerms[c]; i++){
		String[] terms = in.nextLine().split(" ");
		int df = terms.length / 2;  // document frequency
		HashMap<Integer, Integer> postings = new HashMap<Integer, Integer>(df);
		for (int j = 0; j < df-1; j++) 
			postings.put(Integer.parseInt(terms[2 * j+1] ), 
				Integer.parseInt(terms[2 * j + 2]));
		index.put(terms[0], postings);
	}
    	in.close();
	indexes.add(index);
  }

  void selectTrainingSets(double proportion){
	training0 = new boolean[numberOfDocs[0]];
	training1 = new boolean[numberOfDocs[1]];
	Random random = new Random();
	for (int i = 0; i < numberOfDocs[0]; i++)
		training0[i] = random.nextDouble() < proportion;
	for (int i = 0; i < numberOfDocs[1]; i++)
		training1[i] = random.nextDouble() < proportion;
	Nc[0] = 0;  Nc[1] = 0;
	for (int i = 0; i < numberOfDocs[0]; i++) if (training0[i]) Nc[0]++;
	for (int i = 0; i < numberOfDocs[1]; i++) if (training1[i]) Nc[1]++;
	N = Nc[0] + Nc[1];
	b = Math.log((double)Nc[0]/(double)Nc[1]);
	System.out.println(Nc[0] + " " + Nc[1] + " " + N);
	System.out.println(numberOfDocs[0] + " " + numberOfDocs[1]);
  }

  void trainMNB(int c, boolean[] training){
	HashMap<String, Integer> hmap = new HashMap<String, Integer>();
	indexes.get(c).forEach((x, y) -> {
		tct = 0;
		y.forEach((u, v) -> { if (training[u]) tct += v; });
		if (tct > 0) hmap.put(x, tct); });
	Tcts.add(hmap);
  }

  void computeR(){
	int[] tctSums = new int[numberOfClasses];
	tctSums[0] = 0;
	Tcts.get(0).forEach((x, y) -> {
		if (y > 0){
			tctSums[0] += y;
			r.put(x, 0.0); 
		} });
	tctSums[1] = 0;
	Tcts.get(1).forEach((x, y) -> {
		if (y > 0){
			tctSums[1] += y;
			r.put(x, 0.0); 
		} });
	int B = r.size();
	double logNorms = Math.log((double)(tctSums[0] + B))
		- Math.log((double)(tctSums[1] + B));
	r.forEach((x, y) -> {
		double rx = - logNorms;
		if (Tcts.get(0).containsKey(x)) 
			rx += Math.log(Tcts.get(0).get(x) + 1.0);
		if (Tcts.get(1).containsKey(x))
			rx -= Math.log(Tcts.get(1).get(x) + 1.0);
		r.put(x, rx);
		});
  }

  void applyMNB(){
	scores0 = new double[numberOfDocs[0]];
	for (int i = 0; i < numberOfDocs[0]; i++) scores0[i] = 0;
	scores1 = new double[numberOfDocs[1]];
	for (int i = 0; i < numberOfDocs[1]; i++) scores1[i] = 0;
	r.forEach((x, y) -> {
		if (indexes.get(0).containsKey(x))
			indexes.get(0).get(x).forEach((u, v) ->
				scores0[u] += v * y);
		if (indexes.get(1).containsKey(x))
			indexes.get(1).get(x).forEach((u, v) ->
				scores1[u] += v * y);
		});
  }

  void accuracy(){
	float trainingTP = 0;  float testTP = 0;
	float trainingFP = 0;  float testFP = 0;
	float trainingFN = 0;  float testFN = 0;
	float trainingTN = 0;  float testTN = 0;
	for (int i = 0; i < numberOfDocs[0]; i++) 
		if (scores0[i] + b >= 0)
			if (training0[i]) 
			trainingTP++; 
			else 
			testTP++;
		else 
			if(training0[i])
			{			
			trainingFN++;
			System.out.println("Training mismatch documents : " + i);
			}
			else 
			{
			testFN++;
			System.out.println("Test mismatch documents: " + i);
			}
	for (int i = 0; i < numberOfDocs[1]; i++) 
		if (scores1[i] + b >= 0)
			if (training1[i]) 
			{
			trainingFP++;
			System.out.println("Training mismatch documents : " + i); 
			} 
			else
			{ 
			testFP++;
			System.out.println("Test mismatch documents: " + i);
			} 
		else 
			if (training1[i]) 
			{	
			trainingTN++;
			}
			else 
			{
			testTN++;
			}
	System.out.println("Training set values: TP " + trainingTP + " FP " + trainingFP + " FN "
		+ trainingFN + " TN " + trainingTN);
	System.out.println("Test set values: TP " + testTP + " FP " + testFP + " FN "
		+ testFN + " TN " + testTN);
	System.out.println("Accuracy for training data: " + (trainingTP+trainingTN)/(trainingTP+trainingTN+trainingFP+trainingFN));
	System.out.println("Accuracy for test data: " + (testTP+testTN)/(testTP+testTN+testFP+testFN));
  }

 public static void main(String[] args){
   IR7A ir7 = new IR7A();
   ir7.readInvertedIndexWithTfs("adInvertedTf.txt", 0);
   ir7.readInvertedIndexWithTfs("isrInvertedTf.txt", 1);
   ir7.selectTrainingSets(0.6);
   ir7.trainMNB(0, ir7.training0);     
   ir7.trainMNB(1, ir7.training1);
   ir7.computeR();
   ir7.applyMNB();
   ir7.accuracy();
 }
}

      