// IR8A.java CS5154/6054 2018 Cheng
// binarized MNB with feature selection using MI
// Usage: java IR8A numberOfSelectedFeatures

import java.io.*;
import java.util.*;

public class IR8A{

  static final int numberOfClasses = 2;
  int numberOfSelectedFeatures = 0;
  int[] numberOfDocs = new int[numberOfClasses];
  int[] numberOfTerms = new int[numberOfClasses];
  int[] Nc = new int[numberOfClasses];
  int N = 0;
  ArrayList<HashMap<String, HashSet<Integer>>> indexes = 
     new ArrayList<HashMap<String, HashSet<Integer>>>(numberOfClasses);
  boolean[] training0 = null;  boolean[] training1 = null;
  ArrayList<HashMap<String, Integer>> Ncts = 
	new ArrayList<HashMap<String, Integer>>(numberOfClasses);
  int nct = 0;  int n = 0;
  double b = 0;
  TreeMap<Double, String> sorted = new TreeMap<Double, String>();
  int[] nctSum = new int[numberOfClasses];  double logNorms = 0;
  HashMap<String, Double> r = new HashMap<String, Double>();
  double[] scores0 = null;  double[] scores1 = null;

 public IR8A(int k){ numberOfSelectedFeatures = k; }

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
		nct = 0;
		y.forEach(u -> { if (training[u]) nct++; });
		if (nct > 0) hmap.put(x, nct); });
	Ncts.add(hmap);
  }

  void computeMI(String t, int n){
	double N11 = Ncts.get(0).containsKey(t) ? Ncts.get(0).get(t) : 0;
	double N10 = Ncts.get(1).containsKey(t) ? Ncts.get(1).get(t) : 0;
	double Nx1 = Nc[0], Nx0 = Nc[1];
      	double N01 = Nx1 - N11, N00 = Nx0 - N10;
      	double N1x = N11 + N10, N0x = N01 + N00;
      	double MI = 0;
     	if (N11 > 0) MI += N11 * Math.log(N * N11 / N1x / Nx1);  
	if (N01 > 0) MI += N01 * Math.log(N * N01 / N0x / Nx1);  
	if (N10 > 0) MI += N10 * Math.log(N * N10 / N1x / Nx0);  
	if (N00 > 0) MI += N00 * Math.log(N * N00 / N0x / Nx0); 
        sorted.put(MI + n * 0.00001, t);
  } 

  void selectFeatures(){
	HashSet<String> features = new HashSet<String>();
	for (int i = 0; i < numberOfClasses; i++) Ncts.get(i).forEach((x, y) -> features.add(x));
	n = 0;
	features.forEach(x -> computeMI(x, n++));
	for (int i = 0; i < numberOfSelectedFeatures; i++){ 
		String t = sorted.pollLastEntry().getValue();
		System.out.println(t);
		r.put(t, 0.0);
	}
  }

  void computeR(){
	nctSum[0] = nctSum[1] = 0;
	r.forEach((x, y) -> {
		double rx = 0;
		if (Ncts.get(0).containsKey(x)){
			int df = Ncts.get(0).get(x);
			nctSum[0] += df;
			rx += Math.log(df + 1.0);
		}
		if (Ncts.get(1).containsKey(x)){
			int df = Ncts.get(1).get(x);
			nctSum[1] += df;
			rx -= Math.log(df + 1.0);
		}
		r.put(x, rx);
		});
	logNorms = Math.log((double)(nctSum[0] + numberOfSelectedFeatures))
		- Math.log((double)(nctSum[1] + numberOfSelectedFeatures));
	r.forEach((x, y) -> r.put(x, y - logNorms));
  }

  void applyMNB(){
	scores0 = new double[numberOfDocs[0]];
	for (int i = 0; i < numberOfDocs[0]; i++) scores0[i] = 0;
	scores1 = new double[numberOfDocs[1]];
	for (int i = 0; i < numberOfDocs[1]; i++) scores1[i] = 0;
	r.forEach((x, y) -> {
		if (indexes.get(0).containsKey(x))
			indexes.get(0).get(x).forEach(u -> scores0[u] += y);
		if (indexes.get(1).containsKey(x))
			indexes.get(1).get(x).forEach(u -> scores1[u] += y);
		});
  }

  void accuracy(){
	int trainingTP = 0;  int testTP = 0;
	int trainingFP = 0;  int testFP = 0;
	int trainingFN = 0;  int testFN = 0;
	int trainingTN = 0;  int testTN = 0;
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
		System.out.println("For MI training accuracy ="+ (double)(trainingTP+trainingTN)/(trainingTP +  trainingFP + trainingFN + trainingTN));
	System.out.println("For MI testing accuracy ="+ (double)(testTP + testTN)/(testTP +  testFP + testFN + testTN));
  }

 public static void main(String[] args){
   if (args.length == 0){
	System.err.println("Usage: java IR8A numberOfSelectedFeatures");
	return;
   }
   IR8A ir8 = new IR8A(Integer.parseInt(args[0]));
   ir8.readInvertedIndexWithoutTfs("adInvertedTf.txt", 0);
   ir8.readInvertedIndexWithoutTfs("isrInvertedTf.txt", 1);
   ir8.selectTrainingSets(0.6);
   ir8.trainMNB(0, ir8.training0);     
   ir8.trainMNB(1, ir8.training1);
   ir8.selectFeatures();
   ir8.computeR();
   ir8.applyMNB();
   ir8.accuracy();

 }
}

      