JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
		$(JC) $(JFLAGS) $*.java

CLASSES = \
		Client.java \
		peerProcess.java \
		PeerInfo.java \
		ChokingInterval.java \
		Message.java \
		Common.java

default: classes

classes: $(CLASSES:.java=.class)

clean:
		$(RM) *.class