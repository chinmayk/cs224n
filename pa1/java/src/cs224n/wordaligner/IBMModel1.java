package cs224n.wordaligner;

import cs224n.util.CounterMap;
import cs224n.util.Counter;
import cs224n.util.Counters;
import cs224n.util.Pair;

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

public class IBMModel1 implements WordAligner {
	// IBMModel class
	
	private double[][] t_t_s;
	private double[][][][] q_j_i_l_m;
	private int maxIter = 100;
    //stores the conditional probabilities t(target|source)
    private CounterMap<String, String> sourceTargetProbabilitiesMap = new CounterMap<String, String>();

	public Alignment align(SentencePair sentencePair) {
		// alignment function
		Alignment alignment = new Alignment();
        List<String> sourceWords = sentencePair.getSourceWords();
		List<String> targetWords = sentencePair.getTargetWords();

        sourceWords.add(NULL_WORD);

        for(int i=0; i<sourceWords.size(); i++) {
            double best_p = 0.0; int best_j = 0;
            for(int j = 0; j < targetWords.size(); j++) {
                if(this.sourceTargetProbabilitiesMap.getCount(sourceWords.get(i), targetWords.get(j)) > best_p) {
                    best_j = j; best_p = this.sourceTargetProbabilitiesMap.getCount(sourceWords.get(i), targetWords.get(j));
                }
            }
            alignment.addPredictedAlignment(i, best_j);
        }
//


//
// // get words from sentence pairs
//		List<String> sourceWords = sentencePair.getSourceWords();
//		List<String> targetWords = sentencePair.getTargetWords();
//
//		int numSourceWords = sourceWords.size();
//		int numTargetWords = targetWords.size();
//		int j_opt;     // optimal alignment for a_i
//		double a_max, a, denom; // some temporary variables
//
//		// loop through source sentence to obtain its alignment in the target
//		for(int i = 0; i<numSourceWords; i++){
//			j_opt = 0;
//			a_max = 0;
//			denom = 0;
//
//			// calculate denominator
//			for(int j = 0; j<numTargetWords; j++){
//				// [fixed: zero array indexing]
//				denom += this.q_j_i_l_m[j][i][numSourceWords-1][numTargetWords-1]*this.t_t_s[j][i];
//			}
//			// alignment = argmax (q*t/denom)
//			for(int j = 0; j<numTargetWords; j++){
//				a = this.q_j_i_l_m[j][i][numSourceWords-1][numTargetWords-1]*this.t_t_s[j][i]/denom;
//				if(a > a_max){
//					a_max = a;
//					j_opt = j;
//				}
//			}
//			// add alignment to result
//			alignment.addPredictedAlignment(i, j_opt);
//		}
		return alignment;
	}

    public void train(List<SentencePair> trainingPairs) {
        Counter<String> sourceWordsCount = new Counter<String>();
        Counter<String> targetWords = new Counter<String>();
        CounterMap<String, String> sourceTargetWordCounts = new CounterMap<String, String>();
        //For iteration zero, all t(e|f) are set to be uniform.
        //I do this by first counting all english words, and then using that to set the uniform probability
        for(SentencePair pair : trainingPairs) {
            for(String englishWord : pair.getTargetWords()) {
                targetWords.setCount(englishWord, 1.0);
            }

            //In this pre-processing step, I also add NULL words to all source sentences
            (pair.getSourceWords()).add(NULL_WORD);
        }

        //We now have all English words in the targetWords Counter
        double numTargetWords = targetWords.totalCount();
        double uniformProbability = 1.0/numTargetWords;

        //Do the EM algorithm
        List<String> sourceWordsList;
        List<String> targetWordsList;
        int maxIterations = 100;

        for(int i = 0; i < maxIterations; i++) {
            //Iterate over the dataset
            for(SentencePair pair: trainingPairs) {
                //E Step
                //Accumulate counts
                sourceWordsList = pair.getSourceWords();
                targetWordsList = pair.getTargetWords();
                double q = 1.0/(sourceWordsList.size()); //Includes null word, uniform q for IBM1
                //denominator is Sum(P(a, f | e))
                double denominator = 1.0;
                double innerSum = 0.0;
                for(String sourceWord : sourceWordsList) {
                    innerSum = 0.0;
                    for(String targetWord : targetWordsList) {
                        ///////E step
                        //For first iteration
                        if(i == 0) {
                            sourceTargetWordCounts.incrementCount(sourceWord, targetWord, uniformProbability);
                            sourceWordsCount.incrementCount(sourceWord, uniformProbability);
                        }
                        else {

                            double delta = this.sourceTargetProbabilitiesMap.getCount(sourceWord, targetWord)/this.sourceTargetProbabilitiesMap.getCounter(sourceWord).totalCount();
                            sourceTargetWordCounts.incrementCount(sourceWord, targetWord, delta);
                            sourceWordsCount.incrementCount(sourceWord, delta);
                        }

                    }
                }



            }

            //M step
            for(String frenchWord : this.sourceTargetProbabilitiesMap.keySet()) {
                for(String englishWord : this.sourceTargetProbabilitiesMap.getCounter(frenchWord).keySet()) {
                    this.sourceTargetProbabilitiesMap.setCount(frenchWord, englishWord, sourceTargetWordCounts.getCount(frenchWord, englishWord)/sourceWordsCount.getCount(frenchWord));
                }
            }
        }



    }

//	public void train(List<SentencePair> trainingPairs) {
//		// [function: train IBM word alignment model 1]
//
//		// --- figure out some statistics and build hashmaps --- [checking...]
//		System.out.println("Computing statistics and building hashmaps......");
//		int Mt = 0; // # words in longest target language
//		int Ls = 0; // # words in longest source language
//		// Word-to-Index maps
//		HashMap<String, Integer> targetWordIndexMap = new HashMap<String, Integer>();
//		HashMap<String, Integer> sourceWordIndexMap = new HashMap<String, Integer>();
//		// Index-to-Word maps
//		HashMap<Integer, String> targetIndexWordMap = new HashMap<Integer, String>();
//		HashMap<Integer, String> sourceIndexWordMap = new HashMap<Integer, String>();
//
//		int targetWordIndex = 0;
//		int sourceWordIndex = 0;
//
//		for(SentencePair pair : trainingPairs){
//			// work out length of longest target/source lan sentence?
//			if(pair.getTargetWords().size()>Mt){
//				Mt = pair.getTargetWords().size();
//			}
//			if(pair.getSourceWords().size()>Ls){
//				Ls = pair.getSourceWords().size();
//			}
//
//			// construct target Word-Index or Index-Word hashmaps
//			for(String targetWord : pair.getTargetWords()){
//				if(!targetWordIndexMap.containsKey(targetWord)){
//					targetWordIndexMap.put(targetWord, targetWordIndex);
//					targetIndexWordMap.put(targetWordIndex, targetWord);
//					targetWordIndex += 1;
//				}
//			}
//
//			for(String sourceWord : pair.getSourceWords()){
//				if(!sourceWordIndexMap.containsKey(sourceWord)){
//					sourceWordIndexMap.put(sourceWord, sourceWordIndex);
//					sourceIndexWordMap.put(sourceWordIndex, sourceWord);
//					sourceWordIndex += 1;
//				}
//			}
//			// adding NULL_WORD
//			sourceWordIndexMap.put(NULL_WORD, sourceWordIndex);
//			sourceIndexWordMap.put(sourceWordIndex, NULL_WORD);
//		}
//
//		// size of target language vocabulary
//		int numTargetWords = targetWordIndexMap.size();
//		// size of source language vocabulary
//		int numSourceWords = sourceWordIndexMap.size();
//
//		// initialize parameters
//		System.out.println("Initialize parameters ......");
//		// t(t/s)
//		this.t_t_s = new double[numTargetWords][numSourceWords];
//		// q(j/i, l, m)
//		this.q_j_i_l_m = new double[Mt][Ls+1][Ls][Mt]; // 4-dimensional array with first dim including a null position
//
//		// initialize t(t/s)
//		for(int s=0; s<numSourceWords; s++){
//			for(int t=0; t<numTargetWords; t++){
//				t_t_s[t][s]= 1.0/numTargetWords; // Math.random();
//			}
//		}
//
//		int lk;
//		int mk;
//		double q;
//		double denom;
//
//		// start E-M training
//		System.out.print("Start EM training ......\n");
//		// loop over E-M iterations
//		System.out.print("EM iteration");
//		for(int iter = 1; iter <=maxIter; iter++){
//			System.out.print(iter); System.out.print(", ");
//			if(iter%20==0){
//				System.out.print("\n");
//			}
//			// ----- Accumulate counts -----
//			// initialize all counters at start of each E-M iteration (create new count variables)
//			// c(s, t) count of a pair of source and target words appearing together
//			CounterMap<String, String> c_s_t = new CounterMap<String,String>();
//			// c(s) count of source words appearing
//			Counter<String> c_s = new Counter<String>();
//			double[][][][] c_j_given_i_l_m = new double[Mt][Ls+1][Ls][Mt]; // 4-dimensional array with first dimension j null option
//			double[][][] c_i_l_m = new double[Ls+1][Ls][Mt]; // 3-dimensional array
//			double delta;
//
//			// loop over sentence pair examples
//			for(SentencePair pair : trainingPairs){
//				List<String> targetWords = pair.getTargetWords();
//				List<String> sourceWords = pair.getSourceWords();
//				lk = sourceWords.size();
//				for(int i = 0 ; i<lk+1; i++){
//					// use a for-loop to first calculate the denominator for delta value
//					denom = 0;
//					mk = targetWords.size();
//					for(int j = 0; j<mk; j++){
//						q = 1.0/(1.0+(double)(mk));
//						if(i != 0){
//							denom += q*t_t_s[(int)(targetWordIndexMap.get(targetWords.get(j)))][(int)(sourceWordIndexMap.get(sourceWords.get(i-1)))];
//						}
//						else{
//							denom += q*t_t_s[(int)(targetWordIndexMap.get(targetWords.get(j)))][(int)(sourceWordIndexMap.get(NULL_WORD))];
//						}
//					}
//
//					for(int j = 0; j<mk; j++){
//						q = 1.0/(1.0+(double)(mk));
//						// calculate delta
//						if(i != 0){
//							delta = q*t_t_s[(int)(targetWordIndexMap.get(targetWords.get(j)))][(int)(sourceWordIndexMap.get(sourceWords.get(i-1)))]/denom;
//							c_s_t.incrementCount(sourceWords.get(i-1), targetWords.get(j), delta);
//							c_s.incrementCount(sourceWords.get(i-1), delta);
//						}
//						else{
//							delta = q*t_t_s[(int)(targetWordIndexMap.get(targetWords.get(j)))][(int)(sourceWordIndexMap.get(NULL_WORD))]/denom;
//							c_s_t.incrementCount(NULL_WORD, targetWords.get(j), delta);
//							c_s.incrementCount(NULL_WORD, delta);
//						}
//
//						// note zero array indices
//						c_j_given_i_l_m[j][i][lk-1][mk-1] += delta;
//						c_i_l_m[i][lk-1][mk-1] += delta;
//
//						// re-normalize q_j_i_l_m
//						q_j_i_l_m[j][i][lk-1][mk-1] = 1.0/(1.0+(double)(mk)); //c_j_given_i_l_m[j][i][lk-1][mk-1]/c_i_l_m[i][lk-1][mk-1];
//					}
//				}
//			}
//			// ----- re-normalize t_s_t -----
//			for(int s=0; s<numSourceWords; s++){
//				for(int t=0; t<numTargetWords; t++){
//					t_t_s[t][s] = c_s_t.getCount(sourceIndexWordMap.get(s), targetIndexWordMap.get(t))/c_s.getCount(sourceIndexWordMap.get(s));
//				}
//			}
//		}
//	}
}
