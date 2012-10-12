rm ../results/IBM1*.log
mkdir -p ../results
java -cp ~/cs224n/pa1/java/classes cs224n.assignments.WordAlignmentTester -dataPath /afs/ir/class/cs224n/pa1/data/ -model cs224n.wordaligner.IBM1Model -evalSet test -trainSentences 10000 -verbose -language chinese > ../results/IBM1resultChineseLarge.log

java -cp ~/cs224n/pa1/java/classes cs224n.assignments.WordAlignmentTester -dataPath /afs/ir/class/cs224n/pa1/data/ -model cs224n.wordaligner.IBM1Model -evalSet test -trainSentences 10000 -verbose -language hindi > ../results/IBM1resultHindiLarge.log

java -cp ~/cs224n/pa1/java/classes cs224n.assignments.WordAlignmentTester -dataPath /afs/ir/class/cs224n/pa1/data/ -model cs224n.wordaligner.IBM1Model -evalSet test -trainSentences 10000 -verbose -language french > ../results/IBM1resultFrenchLarge.log
