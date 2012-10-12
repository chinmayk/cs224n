 #!/usr/bin/env sh

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

HOME=~/cs224n/pa1-mt
MOSES=/afs/ir/class/cs224n/bin/mosesdecoder
GIZA=/afs/ir/class/cs224n/bin/giza-pp-read-only/external-bin-dir

# #Get alignments
# java -cp ~/cs224n/pa1/java/classes cs224n.assignments.WordAlignmentTester \
# -dataPath ~/cs224n \
# -model cs224n.wordaligner.PMIModel -language pa1-mt \
# -outputAlignments ~/cs224n/pa1-mt/training/corpus.align \
# -trainSentences 200

# #Get default phrase table
# mkdir -p $HOME/train/model
# $MOSES/scripts/training/train-model.perl --max-phrase-length 6 \
# --external-bin-dir $GIZA --first-step 4 --last-step 9 \
# -root-dir $HOME/train -corpus $HOME/training/corpus -f f -e e \
# -alignment-file $HOME/training/corpus -alignment align \
# -lm 0:3:$HOME/lm.bin:8

# #Tune parameters
# $MOSES/scripts/training/mert-moses.pl \
# --working-dir $HOME/tune \
# --decoder-flags="-distortion-limit 4" $HOME/mt-dev.fr $HOME/mt-dev.en \
# $MOSES/bin/moses $HOME/train/model/moses.ini --mertdir $MOSES/bin/

# cat $HOME/mt-dev-test.fr | $MOSES/scripts/moses-parallel.pl -decoder $MOSES/bin/moses -du \
# -f $HOME/tune/moses.ini -jobs 8 > mt-dev-test.out

$MOSES/scripts/generic/multi-bleu.perl mt-dev-test.en < mt-dev-test.out > ~/cs224n/pa1/results/distort4MERT.txt

#Allow for smaller distortion
$MOSES/scripts/training/mert-moses.pl \
--working-dir $HOME/tune \
--decoder-flags="-distortion-limit 2" $HOME/mt-dev.fr $HOME/mt-dev.en \
$MOSES/bin/moses $HOME/train/model/moses.ini --mertdir $MOSES/bin/

$MOSES/scripts/generic/multi-bleu.perl mt-dev-test.en < mt-dev-test.out > ~/cs224n/pa1/results/distort2MERTPhrase6.txt
#Remove the model once we're through
rm -rf $HOME/train/model/


#Use a smaller phrase length
mkdir -p $HOME/train/model
$MOSES/scripts/training/train-model.perl --max-phrase-length 4 \
--external-bin-dir $GIZA --first-step 4 --last-step 9 \
-root-dir $HOME/train -corpus $HOME/training/corpus -f f -e e \
-alignment-file $HOME/training/corpus -alignment align \
-lm 0:3:$HOME/lm.bin:8

$MOSES/scripts/training/mert-moses.pl \
--working-dir $HOME/tune \
--decoder-flags="-distortion-limit 4" $HOME/mt-dev.fr $HOME/mt-dev.en \
$MOSES/bin/moses $HOME/train/model/moses.ini --mertdir $MOSES/bin/

cat $HOME/mt-dev-test.fr | $MOSES/scripts/moses-parallel.pl -decoder $MOSES/bin/moses -du \
-f $HOME/tune/moses.ini -jobs 8 > mt-dev-test.out

$MOSES/scripts/generic/multi-bleu.perl mt-dev-test.en < mt-dev-test.out > ~/cs224n/pa1/results/distort4MERTPhrase6.txt

