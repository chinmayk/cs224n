#!/usr/bin/sh
rm ../results/PMI*.log
java -cp ~/cs224n/pa1/java/classes cs224n.assignments.WordAlignmentTester -dataPath /afs/ir/class/cs224n/pa1/data/ -model cs224n.wordaligner.PMIModel -evalSet test -trainSentences 10000 -verbose -language chinese > ../results/PMIresultChineseLarge.log

java -cp ~/cs224n/pa1/java/classes cs224n.assignments.WordAlignmentTester -dataPath /afs/ir/class/cs224n/pa1/data/ -model cs224n.wordaligner.PMIModel -evalSet test -trainSentences 10000 -verbose -language hindi > ../results/PMIresultHindiLarge.log

java -cp ~/cs224n/pa1/java/classes cs224n.assignments.WordAlignmentTester -dataPath /afs/ir/class/cs224n/pa1/data/ -model cs224n.wordaligner.PMIModel -evalSet test -trainSentences 10000 -verbose -language french > ../results/PMIresultFrenchLarge.log


