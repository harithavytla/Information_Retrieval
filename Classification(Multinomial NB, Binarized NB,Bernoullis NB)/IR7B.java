// IR7B.java CS5154/6054 2019 Cheng
// binarized MNB
// Tct now means Nct
// trainMNB is still the one from IR7A and you need to modify it
// Usage: java IR7B 

import java.io.*;
import java.util.*;

public class IR7B{

  static final int numberOfClasses = 2;
  int[] numberOfDocs = new int[numberOfClasses];
  int[] numberOfTerms = new int[numberOfClasses];
  int[] Nc = new int[numberOfClasses];
  int N = 0;
  ArrayList<HashMap<String, HashSet<Integer>>> indexes = 
     new ArrayList<HashMap<String, HashSet<Integer>>>(numberOfClasses);
  boolean[] training0 = null;  boolean[] training1 = null;
  ArrayList<HashMap<String, Integer>> Tcts = 
	new ArrayList<HashMap<String, Integer>>(numberOfClasses);
  int tct = 0;
  double b = 0;
  HashMap<String, Double> r = new HashMap<String, Double>();
  double[] scores0 = null;  double[] scores1 = null;

 void readInvertedIndexWithoutTfs(String filename, int c){
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
	HashMap<String, HashSet<Integer>> index = new
		HashMap<String, HashSet<Integer>>(numberOfTerms[c]);
    	for (int i = 0; i < numberOfTerms[c]; i++){
		String[] terms = in.nextLine().split(" ");
		int df = terms.length / 2;  // document frequency
		HashSet<Integer> postings = new HashSet<Integer>(df);
		for (int j = 0; j < df; j++) 
			postings.add(Integer.parseInt(terms[2 * j + 1]));
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
		y.forEach(u -> { if (training[u]) tct += u; });
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
			indexes.get(0).get(x).forEach(u ->
				scores0[u] += y);
		if (indexes.get(1).containsKey(x))
			indexes.get(1).get(x).forEach(u ->
				scores1[u] += y);
		});
  }

  void accuracy(){
	float trainingTP = 0;  float testTP = 0;
	float trainingFP = 0;  float testFP = 0;
	float trainingFN = 0;  float testFN = 0;
	float trainingTN = 0;  float testTN = 0;
	for (int i = 0; i < numberOfDocs[0]; i++) if (scores0[i] + b >= 0)
			if (training0[i]) trainingTP++; else testTP++;
		else if (training0[i]) trainingFN++; else testFN++;
	for (int i = 0; i < numberOfDocs[1]; i++) if (scores1[i] + b >= 0)
			if (training1[i]) trainingFP++; else testFP++;
		else if (training1[i]) trainingTN++; else testTN++;
	System.out.println(trainingTP + " " + trainingFP + " "
		+ trainingFN + " " + trainingTN);
	System.out.println(testTP + " " + testFP + " "
		+ testFN + " " + testTN);
	System.out.println("Accuracy for training data: " + (trainingTP+trainingTN)/(trainingTP+trainingTN+trainingFP+trainingFN));
	System.out.println("Accuracy for test data: " + (testTP+testTN)/(testTP+testTN+testFP+testFN));
  }

 public static void main(String[] args){
   IR7B ir7 = new IR7B();
   ir7.readInvertedIndexWithoutTfs("adInvertedTf.txt", 0);
   ir7.readInvertedIndexWithoutTfs("isrInvertedTf.txt", 1);
   ir7.selectTrainingSets(0.6);
   ir7.trainMNB(0, ir7.training0);     
   ir7.trainMNB(1, ir7.training1);
   ir7.computeR();
   ir7.applyMNB();
   ir7.accuracy();
 }
}

      