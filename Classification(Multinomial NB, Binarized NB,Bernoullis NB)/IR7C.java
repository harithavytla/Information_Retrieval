// IR7C.java CS5154/6054 2019 Cheng
// BernoulliNB
// Nct is the model
// The BIM trick (11.15 becomes 11.16) is used for score initialization
// Usage: java IR7C 

import java.io.*;
import java.util.*;

public class IR7C{

  static final int numberOfClasses = 2;
  int[] numberOfDocs = new int[numberOfClasses];
  int[] numberOfTerms = new int[numberOfClasses];
  int[] Nc = new int[numberOfClasses];
  double[] logNc = new double[numberOfClasses];
  int N = 0;
  ArrayList<HashMap<String, HashSet<Integer>>> indexes = 
     new ArrayList<HashMap<String, HashSet<Integer>>>(numberOfClasses);
  boolean[] training0 = null;  boolean[] training1 = null;
  ArrayList<HashMap<String, Integer>> Ncts = 
	new ArrayList<HashMap<String, Integer>>(numberOfClasses);
  int nct = 0;  double z = 0;  // for forEach
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
	logNc[0] = Math.log(Nc[0] + 1.0); logNc[1] = Math.log(Nc[1] + 1.0);
	b = Math.log((double)Nc[0]/(double)Nc[1]);
	System.out.println(Nc[0] + " " + Nc[1] + " " + N);
	System.out.println(numberOfDocs[0] + " " + numberOfDocs[1]);
  }

  void trainBNB(int c, boolean[] training){
	// This is still the old function from IR7A and IR7B
	// You need to change all Tct's to Nct's 
	HashMap<String, Integer> hmap = new HashMap<String, Integer>();
	indexes.get(c).forEach((x, y) -> {
		nct = 0;
		y.forEach(u -> { if (training[u]) nct++; });
		if (nct > 0) hmap.put(x, nct); });
	Ncts.add(hmap);
  }

  void computeR(){
	Ncts.get(0).forEach((x, y) -> {if (y > 0) r.put(x, 0.0); });
	Ncts.get(0).forEach((x, y) -> {if (y > 0) r.put(x, 0.0); });
	// vocab contains only terms in the training set
	r.forEach((x, y) -> {
		double rx = 0;
		if (Ncts.get(0).containsKey(x)){  // Nct[0] > 0
			nct = Ncts.get(0).get(x);
			z = Math.log(Nc[0] - nct + 1.0);
			rx += Math.log(nct + 1.0) - z;
			// log of pt/(1-pt) = (Nct[0] + 1)/(Nc[0] - Nct[0] + 1)
			b += z; // log (1-pt) for b
		}else{
			rx -= logNc[0];  // pt/(1-pt) = 1/(Nc[0] + 1) when Nct[0] = 0
			b += logNc[0];
		}
		if (Ncts.get(1).containsKey(x)){
			nct = Ncts.get(1).get(x);
			z = Math.log(Nc[1] - nct + 1.0);
			rx -= Math.log(nct + 1.0) - z;
			b -= z; 
		}else{
			rx += logNc[1];  // when Nct[1] = 0
			b -= logNc[1];
		}
		r.put(x, rx);
		});
	b += r.size() * (Math.log(Nc[1] + 2.0) - Math.log(Nc[0] + 2.0));
  }

  void applyBNB(){
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
   IR7C ir7 = new IR7C();
   ir7.readInvertedIndexWithoutTfs("adInvertedTf.txt", 0);
   ir7.readInvertedIndexWithoutTfs("isrInvertedTf.txt", 1);
   ir7.selectTrainingSets(0.6);
   ir7.trainBNB(0, ir7.training0);     
   ir7.trainBNB(1, ir7.training1);
   ir7.computeR();
   ir7.applyBNB();
   ir7.accuracy();
 }
}

      