include local.make
OPT =-cp ../../../../../:.:..

all : Tester.class

Tester.class :   Tester.java
	$(JAVAC) $(OPT) Tester.java


run:
	java $(OPT) org.oobd.crypt.AES.test.Tester
