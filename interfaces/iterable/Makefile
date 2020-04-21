
SUBDIRS = $(filter-out /.,$(wildcard */.))

ROOT_DIR ?= .
BUILD_DIR ?= ./.build

PACKAGE_NAME ?= root

C_FILES =  $(wildcard *.c)
CPP_FILES = $(wildcard *.cpp)
TEST_FILES = $(wildcard *.unit)
O_FILES = $(subst .c,.o,$(C_FILES)) $(subst .cpp,.o,$(CPP_FILES))
OUNIT_FILES = $(subst .unit,.ounit,$(TEST_FILES))
MAIN_FILES = $(wildcard *.main)
OMAIN_FILES = $(subst .main,.omain,$(MAIN_FILES))

ALL_O_FILES = $(O_FILES) $(OMAIN_FILES) $(OUNIT_FILES)

CFLAGS += -g
CFLAGS += -I $(ROOT_DIR)

RECURSIVE_MAKE = $(MAKE) -C $@ $(COMMAND) BUILD_DIR=../$(BUILD_DIR) BUILD_MODE=$(BUILD_MODE) PACKAGE_NAME=$(PACKAGE_NAME).$(subst /.,,$@) ROOT_DIR=../$(ROOT_DIR)
COPY = cp $@ $(BUILD_DIR)/$(PACKAGE_NAME).$@

build : build.cmd $(ALL_O_FILES)

test : clean build test.build

run : LangSpec.run

%.run : build %.run.build
	@echo

%.exe : build %.exe.build
	@echo

%.o : %.cpp
	@$(CXX) -c $(CFLAGS) $(CXXFLAGS) $(CPPFLAGS) -o $@ $<
	@$(COPY)

%.o : %.c
	@$(CC) -c $(CFLAGS) $(CPPFLAGS) -o $@ $<
	@$(COPY)

%.ounit : %.unit
	@$(CC) -c $(CFLAGS) $(CXXFLAGS) $(CPPFLAGS) -o $@ -x c++ $<
	@$(COPY)

%.omain : %.main
	@$(CC) -c $(CFLAGS) $(CXXFLAGS) $(CPPFLAGS) -o $@ -x c++ $<
	@$(COPY)

test : clean $(subst .unit,.test,$(TEST_FILES))

clean : _clean
	@$(MAKE) -C $(BUILD_DIR) clean
	
_clean : _clean.cmd
	@rm -fr $(ALL_O_FILES)

ifeq ($(SUBDIRS),)
%.cmd :
	@echo
else
%.cmd :
	@make $(SUBDIRS) COMMAND=$*
endif

%.build :
	@$(MAKE) -C $(BUILD_DIR) $*

$(SUBDIRS) :
	@cp Makefile $@/Makefile
	@$(RECURSIVE_MAKE)

phony :

.PHONY : %.set %.cmd %.exe %.run %.run.build build %.build all clean test run $(SUBDIRS)
