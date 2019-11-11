// IR3B.java CS5154/CS6054 Cheng 2019
// This program randomly select a stretch of DNA of probeLength from the given viral DNA
// at probeBegins with two deletions
// It then runs Smith-Waterman (a local version of the edit distance) 
// by dynamic programming
// and displays the top ten alignments of the mutated probe on the given sequence
// Usage:  java IR3B

import java.io.*;

import java.util.*;



public class IR3B{

	static int probeLength = 20;
	static int segmentLength = 100;
	static int tops = 10;  
	String sequence = null;
	String probe = null;
	Random random = new Random();
	int probeBegins = 0;
	int sequenceLength = 0;
	int[][] m = null;

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

	sequenceLength = sequence.length();

 }



 void genProbe(){
	probeBegins = random.nextInt(sequence.length() - probeLength);
	probe = sequence.substring(probeBegins, probeBegins + 10) + sequence.substring(probeBegins + 12, probeBegins + 22);
	System.out.println(probe + " at " + probeBegins);
 }

 void SmithWaterman(){
	m = new int[probeLength + 1][sequenceLength + 1];
	int[] s2 = new int[sequenceLength + 1];
	int[] s1 = new int[probeLength + 1];
	for (int i = 0; i < probeLength; i++) s1[i + 1] = probe.charAt(i);
	for (int i = 0; i < sequenceLength; i++) s2[i + 1] = sequence.charAt(i);
	for (int i = 0; i <= sequenceLength; i++) m[0][i] = 0;
	for (int i = 1; i <= probeLength; i++) m[i][0] = i;
//	System.out.print(" ");
//	for (int i = 0; i < probeLength; i++) System.out.print(" " + probe.charAt(i));
//	System.out.println();
//	for (int i = 0; i <= probeLength; i++) System.out.print(m[i][0] + " ");
//	System.out.println();
	for (int i = 1; i <= sequenceLength; i++){
//		System.out.print(m[0][i]);
		for (int j = 1; j <= probeLength; j++){
			int a = m[j - 1][i - 1] + (s2[i] == s1[j] ? 0 : 1);
			int b = m[j - 1][i] + 1;
			int c = m[j][i - 1] + 1;
			m[j][i] = Math.min(Math.min(a,b),c); // your code to take the minimum of a, b, and c, as in Figure 3.5 of IIR
//			System.out.print(" " + m[j][i]);
		}
//		System.out.println(" " + sequence.charAt(i - 1));
	}
  }

  void topAlignments(){
	int[] topScores = new int[tops + 1];
	int[] topEndPositions = new int[tops + 1];
	int numberOfTops = 0;
	for (int i = probeLength; i <= sequenceLength; i++){
		int score = m[probeLength][i];
		int k = numberOfTops - 1; for (; k >= 0; k--)
			if (score < topScores[k]){ 
				topEndPositions[k + 1] = topEndPositions[k];
				topScores[k + 1] = topScores[k];
			}else break;
		if (k + 1 < tops){ topEndPositions[k + 1] = i; topScores[k + 1] = score; }
		if (numberOfTops < tops) numberOfTops++;
	}
	for (int k = 0; k < tops; k++){
		int begins = topEndPositions[k] - probeLength - 2;
		System.out.println(begins + " " + sequence.substring(begins, begins + probeLength + 2) +
				" " + topScores[k]);
	}
 }	


 public static void main(String[] args){

 	IR3B ir3 = new IR3B();

	ir3.readSeq("viral1.txt");

	ir3.genProbe();
	ir3.SmithWaterman();
	ir3.topAlignments();
 }

}
