// IR3A.java CS5154/CS6054 Cheng 2019
// This program randomly select a stretch of DNA of probeLength from the given viral DNA
// at probeBegins with two deletions
// It makes a inverted positional index for all kgrams with k = kgramLength.
// It finds the kgrams in the probe and find the positions for each kgram in the probe
// and display the results in a dot matrix (similar to BLAST).
// Only the 10 (tops) most promising segments of the dot matrix are displayed and one of them should 
// contain probeBegins.
// Usage:  java IR3A

import java.io.*;

import java.util.*;



public class IR3A{

	static int kgramLength = 5;
	static int probeLength = 20;
	static int segmentLength = 100;
	static int tops = 10;  
	HashMap<String, HashSet<Integer>> positionalIndex 
= new HashMap<String, HashSet<Integer>>();

	String sequence = null;
	String probe = null;
	Random random = new Random();
	int probeBegins = 0;
	boolean[][] dots = null;
	int globali = 0;
	int len1, len2;

  void readSeq(String filename){

	Scanner in = null;

	try {

		in = new Scanner(new File(filename));

	} catch (FileNotFoundException e){

		System.err.println(filename + " not found");

		System.exit(1);

	}

	in.nextLine();

	sequence = in.nextLine();

	int len = sequence.length() - kgramLength + 1;

	for (int pos = 0; pos < len; pos++){

		String kgram = sequence.substring(pos, pos + kgramLength);

		if (positionalIndex.containsKey(kgram))
 positionalIndex.get(kgram).add(pos);

		else{

			HashSet<Integer> hset = new HashSet<Integer>();

			hset.add(pos);

			positionalIndex.put(kgram,hset);

		}

	}

 }



 void genProbe(){
	probeBegins = random.nextInt(sequence.length() - probeLength);
	probe = sequence.substring(probeBegins, probeBegins + 10) + sequence.substring(probeBegins + 12, probeBegins + 22);
	System.out.println(probe + " at " + probeBegins);
 }

 void dotMatrix(){
	len1 = sequence.length() - kgramLength + 1;

	len2 = probeLength - kgramLength + 1;
	dots = new boolean[len1][len2];
	for (globali = 0; globali < len2; globali++){
		for (int j = 0; j < len1; j++) dots[j][globali] = false;
		positionalIndex.get(probe.substring(globali, globali + kgramLength)).forEach(x -> dots[x][globali] = true);
	}
 }

 void displayTopSegments(){
	int segments = len1 / segmentLength;
	int[] topHits = new int[tops + 1];
	int[] topSegs = new int[tops + 1];
	int numberOfTops = 0;
	for (int seg = 0; seg < segments; seg++){
		int hits = 0;  int allHits = 0;
		int base = seg * segmentLength;
		for (int i = 0; i < len2; i++){
			int colHits = 0;
			for (int j = 0; j < segmentLength; j++) 
				if (dots[base + j][i]) colHits++;
			if (colHits > 0){ hits++;  allHits += colHits; }
		}
		if (hits == len2) hits = allHits;
		int k = numberOfTops - 1; for (; k >= 0; k--)
			if (hits > topHits[k]){ 
				topSegs[k + 1] = topSegs[k];
				topHits[k + 1] = topHits[k];
			}else break;
		if (k + 1 < tops){ topSegs[k + 1] = seg; topHits[k + 1] = hits; }
		if (numberOfTops < tops) numberOfTops++;
	}
	for (int k = 0; k < tops; k++){
		System.out.println("Segment begins at " + topSegs[k] * segmentLength);
		int base = topSegs[k] * segmentLength;
		for (int i = 0; i < len2; i++){
			for (int j = 0; j < segmentLength; j++) 
				if (dots[base + j][i]) System.out.print("X");
				else System.out.print(" ");
			System.out.println();
		}
	}
 }


 public static void main(String[] args){

 	IR3A ir3 = new IR3A();

	ir3.readSeq("viral1.txt");

	ir3.genProbe();
	ir3.dotMatrix();
	ir3.displayTopSegments();
 }

}
