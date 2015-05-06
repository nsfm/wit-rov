COMP = javac
RUN = java
DEPS = .:lib/hidapi-1.1.jar:lib/sqlite-jdbc-3.8.7.jar
COMPARGS = -cp "$(DEPS)" -d bin
COMPFILES = src/com/witrov/*/*.java 
MAINCLASS = com.witrov.main.MainFrame
LIB = lib/
CLASSPATH = bin/
RUNARGS = -Djava.library.path=$(LIB) -classpath "$(DEPS):bin"


build:
	$(COMP) $(COMPARGS) $(COMPFILES)
	mkdir -p /root/WIT_ROV
	mkdir -p /root/WIT_ROV/lib
	cp $(LIB)/*.so /root/WIT_ROV/lib
run:
	$(RUN) $(RUNARGS) $(MAINCLASS)

all: build run
