package cs224n.wordaligner;

import cs224n.util.CounterMap;
import cs224n.util.Counter;
import cs224n.util.Counters;
import java.util.HashMap;
import java.util.List;

/**
 * Simple word alignment baseline model that maps source positions to target 
 * positions along the diagonal of the alignment grid.
 * 
 * IMPORTANT: Make sure that you read the comments in the
 * cs224n.wordaligner.WordAligner interface.
 * 
 * @author Dan Klein
 * @author Spence Green
 */

public class IBMModel2 implements WordAligner {
	
	// IBMModel class
	
	private double[][] t_s_t;
	private double[][][][] q_j_i_l_m;
	private int maxIter = 100;
	
	public Alignment align(SentencePair sentencePair) {
		// alignment function
		Alignment alignment = new Alignment();
		
		List<String> sourceWords = sentencePair.getSourceWords();
		List<String> targetWords = sentencePair.getTargetWords();
		
		int numSourceWords = sourceWords.size();
		int numTargetWords = targetWords.size();
		int j_opt;
		double a_max;
		double a;
		double denom;
		
		for(int i = 0; i<numSourceWords; i++){
			j_opt = 0;
			a_max = 0;
			denom = 0;
			
			for(int j = 0; j<numTargetWords; j++){
				// [fixed: zero array indexing]
				denom += this.q_j_i_l_m[j][i][numTargetWords-1][numSourceWords-1]*this.t_s_t[i][j];
			}
			for(int j = 0; j<numTargetWords; j++){
				a = this.q_j_i_l_m[j][i][numTargetWords-1][numSourceWords-1]*this.t_s_t[i][j]/denom;
				if(a > a_max){
					a_max = a;
					j_opt = j;
				}
			}
			alignment.addPredictedAlignment(j_opt, i);
		}
		return alignment;
	}
	
	public void train(List<SentencePair> trainingPairs) {
		// [function: train IBM word alignment model 1]
		
		// figure out some statistics
		System.out.println("Computing statistics ......");
		int Ms = 0; // # words in longest source language
		int Lt = 0; // # words in longest target language
		Counter<String> tgtCounts = new Counter<String>();
		Counter<String> srcCounts = new Counter<String>();
		HashMap<String, Integer> tgtWordIndexMap = new HashMap<String, Integer>();
		HashMap<String, Integer> srcWordIndexMap = new HashMap<String, Integer>();
		HashMap<Integer, String> tgtIndexWordMap = new HashMap<Integer, String>();
		HashMap<Integer, String> srcIndexWordMap = new HashMap<Integer, String>();
		
		int tgtWordIndex = 0;
		int srcWordIndex = 0;
		
		for(SentencePair pair : trainingPairs){
			// how long is the longest target/source lan sentence?
			if(pair.getTargetWords().size()>Lt){
				Lt = pair.getTargetWords().size(); 
			}
			if(pair.getSourceWords().size()>Ms){
				Ms = pair.getSourceWords().size();
			}
			for(String targetWord : pair.getTargetWords()){
				tgtCounts.incrementCount(targetWord, 1.0);
				if(!tgtWordIndexMap.containsKey(targetWord)){
					tgtWordIndexMap.put(targetWord, tgtWordIndex);
					tgtIndexWordMap.put(tgtWordIndex, targetWord);
					tgtWordIndex += 1;
				}
			}
			for(String sourceWord : pair.getSourceWords()){
				srcCounts.incrementCount(sourceWord, 1.0);
				if(!srcWordIndexMap.containsKey(sourceWord)){
					srcWordIndexMap.put(sourceWord, srcWordIndex);
					srcIndexWordMap.put(srcWordIndex, sourceWord);
					srcWordIndex += 1; 
				}
			}
		}
		
		// how many target lan words are there?
		int numTargetWords = tgtCounts.size();
		// how many source lan words are there?
		int numSourceWords = srcCounts.size();
		srcCounts = null;
		tgtCounts = null;
		
		// initialize parameters
		System.out.println("Initialize parameters ......");
		this.t_s_t = new double[numSourceWords][numTargetWords];
		this.q_j_i_l_m = new double[Lt][Ms][Lt][Ms];
		
		// initialize p(t/s)
		for(int s=0; s<numSourceWords; s++){
			float sum_T = 0;
			for(int t=0; t<numTargetWords; t++){
				t_s_t[s][t]= Math.random();
				sum_T += t_s_t[s][t];
			}
			//double sumTest = 0;
			for(int t=0; t<numTargetWords; t++){
				t_s_t[s][t]=t_s_t[s][t]/sum_T;
				//sumTest += t_s_t[s][t];
			}
			//System.out.println(sumTest);
		}
		
		// initialize q(j/i,l,m)
		for(int m=0; m<Ms; m++){
			for(int l=0; l<Lt; l++){
				for(int i=0; i<Ms; i++){
					double sumJ = 0;
					for(int j=0; j<Lt; j++){
						q_j_i_l_m[j][i][l][m] = Math.random();
						sumJ += q_j_i_l_m[j][i][l][m];
					}
					//double sumTest = 0;
					for(int j=0; j<Lt; j++){
						q_j_i_l_m[j][i][l][m] = q_j_i_l_m[j][i][l][m]/sumJ;
						//sumTest = sumTest + q_j_i_l_m[j][i][l][m];
					}
					//System.out.println(sumTest);
				}
			}
		}
		// [DOUBLE CHECKED] parameters sum to one
		
		int lk;
		int mk;
		double q;
		double denom;

		// start E-M training
		System.out.print("Start EM training ......\n");
		// for loop over iterations
		System.out.print("EM iteration");
		for(int iter = 1; iter <=maxIter; iter++){
			System.out.print(iter); System.out.print(", ");
			if(iter%20==0){
				System.out.print("\n");			  
			}
			// ----- Accumulate counts -----
			// create new count variables
			CounterMap<String, String> targetSourceCounts = new CounterMap<String,String>();
			Counter<String> targetCounts = new Counter<String>(); //count(f) as in the notes
			double[][][][] count_j_given_i_l_m = new double[Lt][Ms][Lt][Ms]; // 4-dimensional array
			double[][][] count_i_l_m = new double[Ms][Lt][Ms]; // 3-dimensional array
			// loop over sentence pair examples
			for(SentencePair pair : trainingPairs){
				List<String> targetWords = pair.getTargetWords();
				List<String> sourceWords = pair.getSourceWords();
				// add null to sourceWords? 
				mk = sourceWords.size();
				for(int i = 0 ; i<mk; i++){ // use index because need to access arrays
					// produce t and q values
					// q = 1.0/(1.0+(double)(mk));
					// use a for-loop to first calculate the denominator for delta value
					denom = 0;
					lk = targetWords.size();
					for(int j = 0; j<lk; j++){
						denom += q_j_i_l_m[j][i][lk-1][mk-1]*t_s_t[(int)(srcWordIndexMap.get(sourceWords.get(i)))][(int)(tgtWordIndexMap.get(targetWords.get(j)))];
					}
					for(int j = 0; j<lk; j++){ // include NULL
						// calculate delta
						double delta = q_j_i_l_m[j][i][lk-1][mk-1]*t_s_t[(int)(srcWordIndexMap.get(sourceWords.get(i)))][(int)(tgtWordIndexMap.get(targetWords.get(j)))]/denom;
						
						targetSourceCounts.incrementCount(targetWords.get(j), sourceWords.get(i), delta);
						targetCounts.incrementCount(targetWords.get(j), delta);
						
						// note array zero indices
						count_j_given_i_l_m[j][i][lk-1][mk-1] += delta;
						count_i_l_m[i][lk-1][mk-1] += delta;
						
						// renormalize q_j_i_l_m
						q_j_i_l_m[j][i][lk-1][mk-1] = count_j_given_i_l_m[j][i][lk-1][mk-1]/count_i_l_m[i][lk-1][mk-1];
					}
				}
			}
			// ----- renormalize t_s_t -----
			for(int s=0; s<numSourceWords; s++){
				for(int t=0; t<numTargetWords; t++){
					t_s_t[s][t] = targetSourceCounts.getCount(tgtIndexWordMap.get(t), srcIndexWordMap.get(s))/targetCounts.getCount(tgtIndexWordMap.get(t));
				}
			}
		}
	}
}
