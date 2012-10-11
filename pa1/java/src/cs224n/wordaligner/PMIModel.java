package cs224n.wordaligner;

import cs224n.util.CounterMap;
import cs224n.util.Counter;
import cs224n.util.Counters;

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
public class PMIModel implements WordAligner {
	
    private static final long serialVersionUID = 1315751943476440535L;
    
    // TODO: Use arrays or Counters for collecting sufficient statistics
    // from the training data.
    private CounterMap<String,String> sourceTargetCounts;
    private Counter<String> sourceCounts;
    private Counter<String> targetCounts;
    
    public Alignment align(SentencePair sentencePair) {
    // Placeholder code below. 
    // TODO Implement an inference algorithm for Eq.1 in the assignment
    // handout to predict alignments based on the counts you collected with train().
    Alignment alignment = new Alignment();
    int numSourceWords = sentencePair.getSourceWords().size();
    int numTargetWords = sentencePair.getTargetWords().size();
    List<String> sourceWords = sentencePair.getSourceWords();
    List<String> targetWords = sentencePair.getTargetWords();
    //Only set alignments to NULL words in the target
//    targetWords.add(NULL_WORD);
    int maxIndex = 0; double maxProbability = 0.0; double probability=0.0;
    for (int srcIndex = 0; srcIndex < numSourceWords; srcIndex++) {
        maxIndex = 0;
        maxProbability= 0.0;
        for(int tgtIndex = 0; tgtIndex < numTargetWords+1; tgtIndex++) {
           probability = (this.sourceTargetCounts.getCount(sourceWords.get(srcIndex), targetWords.get(tgtIndex))/
                   (this.sourceCounts.getCount(sourceWords.get(srcIndex))* this.targetCounts.getCount(targetWords.get(tgtIndex))));

           if(probability > maxProbability) {
               maxIndex = tgtIndex;
               maxProbability = probability;
           }

        }
//        if(maxIndex == numTargetWords) {
//            //Set alignment to -1
//            alignment.addPredictedAlignment(srcIndex, -1);
//        }
        alignment.addPredictedAlignment(srcIndex, maxIndex);
// int tgtIndex = srcIndex;
//      if (tgtIndex < numTargetWords) {
//        // Discard null alignments
//        alignment.addPredictedAlignment(tgtIndex, srcIndex);
//      }


    }
    return alignment;
  }

  public void train(List<SentencePair> trainingPairs) {
    sourceTargetCounts = new CounterMap<String,String>();
    this.targetCounts = new Counter<String>();
    this.sourceCounts = new Counter<String>();
    for(SentencePair pair : trainingPairs){
      List<String> targetWords = pair.getTargetWords();
      List<String> sourceWords = pair.getSourceWords();
      for(String source : sourceWords){
          for(String target : targetWords){
          sourceTargetCounts.incrementCount(source, target, 1.0);
          this.sourceCounts.incrementCount(source, 1.0);
          this.targetCounts.incrementCount(target, 1.0);
        }
      }
    }
      //Add a NULL alignment to each possible source word and target word
//      for(String source: sourceCounts.keySet()) {
//          sourceTargetCounts.setCount(source, NULL_WORD, 1.0);
//      }

//      for(String target: targetCounts.keySet()) {
//          sourceTargetCounts.setCount(NULL_WORD, target, 1.0);
//      }
      
      //Now, add NULL to both source and target.
      //Make sure this is done after null alignments are added,
      // because otherwise, we'll have null word aligning with null word
//      sourceCounts.setCount(NULL_WORD, 1.0);
//      targetCounts.setCount(NULL_WORD, 1.0);
      
      //normalize
      this.sourceCounts = Counters.normalize(sourceCounts);
      this.targetCounts = Counters.normalize(targetCounts);
      this.sourceTargetCounts = Counters.conditionalNormalize(sourceTargetCounts);
  }
}
