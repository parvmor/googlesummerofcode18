all: compile run

compile:
	javac -d bin/ -cp src src/tarjanUF/*.java

clean:
	rm -rf bin
	mkdir bin

run:
	java -ea -cp bin tarjanUF.Main ${GRAPH} ${THREADS} ${INIT}
