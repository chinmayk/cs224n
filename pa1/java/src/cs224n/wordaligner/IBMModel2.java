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
		
		// get words from sentence pairs
		List<String> sourceWords = sentencePair.getSourceWords();
		List<String> targetWords = sentencePair.getTargetWords();
		
		int numSourceWords = sourceWords.size();
		int numTargetWords = targetWords.size();
		int j_opt;     // optimal alignment for a_i
		double a_max, a, denom; // some temporary variables			
		
		// loop through source sentence to obtain its alignment in the target
		for(int i = 0; i<numSourceWords; i++){
			j_opt = 0;
			a_max = 0;
			denom = 0;
			
			// calculate denominator
			for(int j = 0; j<numTargetWords; j++){
				// [fixed: zero array indexing]
				denom += this.q_j_i_l_m[j][i][numTargetWords-1][numSourceWords-1]*this.t_s_t[i][j];
			}
			// alignment = argmax (q*t/denom)
			for(int j = 0; j<numTargetWords; j++){
				a = this.q_j_i_l_m[j][i][numTargetWords-1][numSourceWords-1]*this.t_s_t[i][j]/denom;
				if(a > a_max){
					a_max = a;
					j_opt = j;
				}
			}
			// add alignment to result
			alignment.addPredictedAlignment(j_opt, i);
		}
		return alignment;
	}
	
	public void train(List<SentencePair> trainingPairs) {
		// [function: train IBM word alignment model 1]
		
		// --- figure out some statistics and build hashmaps --- [checking...]
		System.out.println("Computing statistics and building hashmaps......");
		int Ms = 0; // # words in longest source language
		int Lt = 0; // # words in longest target language
		// Word-to-Index maps
		HashMap<String, Integer> targetWordIndexMap = new HashMap<String, Integer>();
		HashMap<String, Integer> sourceWordIndexMap = new HashMap<String, Integer>();
		// Index-to-Word maps
		HashMap<Integer, String> targetIndexWordMap = new HashMap<Integer, String>();
		HashMap<Integer, String> sourceIndexWordMap = new HashMap<Integer, String>();
		
		int targetWordIndex = 0; 
		int sourceWordIndex = 0; 
		
		for(SentencePair pair : trainingPairs){
			// work out length of longest target/source lan sentence?
			if(pair.getTargetWords().size()>Lt){
				Lt = pair.getTargetWords().size();
			}
			if(pair.getSourceWords().size()>Ms){
				Ms = pair.getSourceWords().size();
			}
			
			// construct target Word-Index or Index-Word hashmaps
			for(String targetWord : pair.getTargetWords()){				
				if(!targetWordIndexMap.containsKey(targetWord)){
					targetWordIndexMap.put(targetWord, targetWordIndex);
					targetIndexWordMap.put(targetWordIndex, targetWord);
					targetWordIndex += 1;
				}
			}
			// adding null
			targetWordIndexMap.put(NULL_WORD, targetWordIndex);
			targetIndexWordMap.put(targetWordIndex, NULL_WORD);
			
			for(String sourceWord : pair.getSourceWords()){				
				if(!sourceWordIndexMap.containsKey(sourceWord)){
					sourceWordIndexMap.put(sourceWord, sourceWordIndex);
					sourceIndexWordMap.put(sourceWordIndex, sourceWord);
					sourceWordIndex += 1; 
				}
			}
		}
		
		// size of target language vocabulary 
		int numTargetWords = targetWordIndexMap.size();
		// size of source language vocabulary
		int numSourceWords = sourceWordIndexMap.size();
		
		// initialize parameters
		System.out.println("Initialize parameters ......");
		// t(t/s)
		this.t_s_t = new double[numSourceWords][numTargetWords];
		// q(j/i, l, m)
		this.q_j_i_l_m = new double[Lt+1][Ms][Lt][Ms]; // 4-dimensional array with first dim including a null position
		
		// initialize t(t/s)
		for(int s=0; s<numSourceWords; s++){
			float sum_T = 0;
			for(int t=0; t<numTargetWords; t++){
				t_s_t[s][t]= 1.0/numTargetWords;// Math.random();				
			}
		}
		
		// initialize q(j/i,l,m)
		for(int m=0; m<Ms; m++){
			for(int l=0; l<Lt; l++){
				for(int i=0; i<Ms; i++){					
					for(int j=0; j<Lt+1; j++){
						q_j_i_l_m[j][i][l][m] = 1.0/(Lt+1); 
					}
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
		// loop over E-M iterations
		System.out.print("EM iteration");
		for(int iter = 1; iter <=maxIter; iter++){
			System.out.print(iter); System.out.print(", ");
			if(iter%20==0){
				System.out.print("\n");
			}
			// ----- Accumulate counts -----
			// initialize all counters at start of each E-M iteration (create new count variables)
			// c(t, s) count of a pair of target and source words appearing together
			CounterMap<String, String> c_t_s = new CounterMap<String,String>();
			// c(t) count of target words appearing
			Counter<String> c_t = new Counter<String>();
			double[][][][] c_j_given_i_l_m = new double[Lt+1][Ms][Lt][Ms]; // 4-dimensional array with first dimension j null option
			double[][][] c_i_l_m = new double[Ms][Lt][Ms]; // 3-dimensional array
			double delta;
			
			// loop over sentence pair examples
			for(SentencePair pair : trainingPairs){
				List<String> targetWords = pair.getTargetWords();
				List<String> sourceWords = pair.getSourceWords();
				// add null to sourceWords? 
				mk = sourceWords.size();
				for(int i = 0 ; i<mk; i++){ // use index because need to access arrays
					// produce t and q values
					// [TODO] Change this (q) for model 2
					q = 1.0/(1.0+(double)(mk));	   
					// use a for-loop to first calculate the denominator for delta value
					denom = 0;
					lk = targetWords.size();
					denom += q*t_s_t[(int)(sourceWordIndexMap.get(sourceWords.get(i)))][(int)(targetWordIndexMap.get(NULL_WORD))];					
					for(int j = 0; j<lk; j++){
						denom += q*t_s_t[(int)(sourceWordIndexMap.get(sourceWords.get(i)))][(int)(targetWordIndexMap.get(targetWords.get(j)))];
					}

					for(int j = 0; j<lk+1; j++){
						// calculate delta
						if(j != 0){
							// use zero position
							delta = q*t_s_t[(int)(sourceWordIndexMap.get(sourceWords.get(i)))][(int)(targetWordIndexMap.get(targetWords.get(j-1)))]/denom;
							c_t_s.incrementCount(targetWords.get(j-1), sourceWords.get(i), delta);
							c_t.incrementCount(targetWords.get(j-1), delta);
						}
						else{
							delta = q*t_s_t[(int)(sourceWordIndexMap.get(sourceWords.get(i)))][(int)(targetWordIndexMap.get(NULL_WORD))]/denom;
							c_t_s.incrementCount(NULL_WORD, sourceWords.get(i), delta);
							c_t.incrementCount(NULL_WORD, delta);
						}
						
						// note zero array indices
						c_j_given_i_l_m[j][i][lk-1][mk-1] += delta;
						c_i_l_m[i][lk-1][mk-1] += delta;
						
						// re-normalize q_j_i_l_m
						q_j_i_l_m[j][i][lk-1][mk-1] = q; //c_j_given_i_l_m[j][i][lk-1][mk-1]/c_i_l_m[i][lk-1][mk-1];
					}
				}
			}
			// ----- re-normalize t_s_t -----
			for(int s=0; s<numSourceWords; s++){
				for(int t=0; t<numTargetWords; t++){
					t_s_t[s][t] = c_t_s.getCount(targetIndexWordMap.get(t), sourceIndexWordMap.get(s))/c_t.getCount(targetIndexWordMap.get(t));
				}
			}
		}
	}
}
