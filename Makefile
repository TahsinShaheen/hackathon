CC=javac

all:
	$(CC) -cp "libs/*" SODAnalysis.java

run: all
	java -cp "libs/*:." SODAnalysis 34

clean:
	rm -f *.class
