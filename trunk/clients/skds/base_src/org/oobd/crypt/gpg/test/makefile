include local.make
OPT =-cp ../../../../../:.

all : Tester.class

Tester.class : PGPUtils.java KeyReader.java  PGPFileProcessor.java  Tester.java
	$(JAVAC) $(OPT) Tester.java


run:
	java $(OPT) Tester
