// IR1.java CS5154/6054 2017 Cheng
// Generate inverted index for a collection of documents
// Usage: java IR1 < collection > invertedIndex
// Usage: java IR1 < ad14.txt > adInverted.txt
//        java IR1 < isr4.txt > isrInverted.txt

import java.io.*;
import java.util.*;

public class IR1{

class Incidence implements Comparable{
  String term; int doc;
  public Incidence(String t, int d){
    term = t; doc = d;
  }
  public int compareTo(Object obj){
    Incidence i = (Incidence)obj;
    int diff = term.compareTo(i.term);
    if (diff == 0) diff = doc - i.doc;
    return diff;
  }
}

  int numberOfDocs = 0;
  int numberOfTerms = 0;
  int numberOfIncidences = 0;
  TreeSet<String> terms = new TreeSet<String>();
  TreeSet<Incidence> incidences = new TreeSet<Incidence>();

  void readCollection(){
    Scanner in = new Scanner(System.in);
    while (in.hasNextLine()){
      String[] parts = in.nextLine().split("\t");
      tokenize(parts[1], numberOfDocs); // title
      in.nextLine(); // ignore journal
      tokenize(in.nextLine(), numberOfDocs); // abstract
      numberOfDocs++;
    }
    numberOfTerms = terms.size();
    numberOfIncidences = incidences.size(); 
    System.out.println(numberOfTerms + " " + numberOfDocs + " " +
       numberOfIncidences);
  }

  void tokenize(String line, int doc){  
    String[] tokens = line.toLowerCase().split("[^a-z0-9]");
    // tokens contains only a-z0-9 but may be empty string or numbers
    for (String s: tokens) 
      if (s.length() > 0 && s.charAt(0) > '9'){
        incidences.add(new Incidence(s, doc));
        terms.add(s);
      }
  }

  void invertIndex(){
   String curTerm = "";
   for (Incidence i: incidences){
     if (!i.term.equals(curTerm)){
       if (!curTerm.equals("")) System.out.println();
       System.out.print(i.term);
       curTerm = i.term;
     }
     System.out.print(" " + i.doc);
   }
   System.out.println();
 }

 public static void main(String[] args){
   IR1 ir1 = new IR1();
   ir1.readCollection();
   ir1.invertIndex();
 }
}

      