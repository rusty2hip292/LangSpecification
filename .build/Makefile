
O_FILES =  $(wildcard *.o)
TEST_FILES = $(wildcard *.ounit)
TESTS = $(subst .ounit,.test,$(wildcard *.ounit))
OMAIN_FILES = $(wildcard *.omain)

CFLAGS += -g

%.run : %.exe
	./$<
%.exe : *.%.omain test
	@$(CXX) $(CFLAGS) $(CXXFLAGS) $(CPPFLAGS) -o $*.exe $< $(O_FILES)

test : $(TESTS)

%.test : %.ounit $(O_FILES)
	@$(CXX) $(CFLAGS) $(CXXFLAGS) $(CPPFLAGS) -o $*.exe $< $(O_FILES)
	@echo running test $*
	./$*.exe

clean :
	@rm -rf *.exe *.o*
