#!/usr/bin/sh

#Setup
cd
cd cs224n/pa1-mt
ln -s /afs/ir/class/cs224n/pa1/data/mt/lm.bin
ln -s /afs/ir/class/cs224n/pa1/data/mt/mt-dev.fr
ln -s /afs/ir/class/cs224n/pa1/data/mt/mt-dev.en
ln -s /afs/ir/class/cs224n/pa1/data/mt/mt-dev-test.fr
ln -s /afs/ir/class/cs224n/pa1/data/mt/mt-dev-test.en

cd
cd cs224n/pa1-mt
ln -s /afs/ir/class/cs224n/pa1/data/mt/lm.bin
ln -s /afs/ir/class/cs224n/pa1/data/mt/mt-dev.fr
ln -s /afs/ir/class/cs224n/pa1/data/mt/mt-dev.en
ln -s /afs/ir/class/cs224n/pa1/data/mt/mt-dev-test.fr
ln -s /afs/ir/class/cs224n/pa1/data/mt/mt-dev-test.en

export HOME=~/cs224n/pa1-mt
export MOSES=/afs/ir/class/cs224n/bin/mosesdecoder
export GIZA=/afs/ir/class/cs224n/bin/giza-pp-read-only/external-bin-dir

#Get alignments
java -cp ~/cs224n/pa1/java/classes cs224n.assignments.WordAlignmentTester \
-dataPath ~/cs224n \
-model cs224n.wordaligner.PMIModel -language pa1-mt \
-outputAlignments ~/cs224n/pa1-mt/training/corpus.align \
-trainSentences 200

#Get default phrase table
mkdir -p $HOME/train/model
$MOSES/scripts/training/train-model.perl --max-phrase-length 6 \
--external-bin-dir $GIZA --first-step 4 --last-step 9 \
-root-dir $HOME/train -corpus $HOME/training/corpus -f f -e e \
-alignment-file $HOME/training/corpus -alignment align \
-lm 0:3:"$HOME"/lm.bin:8