
C_FILES = $(wildcard *.c)

run : main.exe
	./main.exe
.PHONY : run

main.exe : $(C_FILES)
	$(CC) -g -o main.exe $(C_FILES)

clean :
	rm -rf *.o *.exe
.PHONY : clean
