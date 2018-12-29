FINAL_CFLAGS=-Wall -O0 -g -fexceptions 
CONSUMER_FINAL_LIBS=-lthrift -lpthread  -llog4cpp  
CTEST_FINAL_LIBS=-lconsumer 
SHARED_COMPILE_FLAGS=-fPIC 
SHARED_LINK_FLAGS=-shared 
FINAL_LIB_DIR= -L. -L/usr/lib 
FINAL_INCLUDE_DIR=-I. -I/usr/include
FINAL_CFLAGS+=$(FINAL_INCLUDE_DIR)
PREFIX:=/usr
INSTALL=install

CONSUMER_LIB_NAME=libconsumer.so 
TEST_CONSUMER_CPP=test_consumer_cpp
TEST_CONSUMER_C=test_consumer_c
CONSUMER_OBJ= ConsumerPool.o ConsumerConfig.o Consumer.o consumerProxy_constants.o consumerProxy_types.o ConsumerService.o ConsumerWrapper.o ConsumerLogger.o
CONSUMER_OBJ_TEST_C= test_consumer.o
CONSUMER_OBJ_TEST_CPP= test_consumer_pool.o

CONSUMER_CC=$(QUIET_CC)$(CC) $(FINAL_CFLAGS)
CONSUMER_CXX=$(QUIET_CC)$(CXX) $(FINAL_CFLAGS)
CONSUMER_CC_LD=$(QUIET_LINK)$(CC)
CONSUMER_CXX_LD=$(QUIET_LINK)$(CXX)
CONSUMER_INSTALL=$(QUIET_INSTALL)$(INSTALL)

CCCOLOR="\033[34m"  
LINKCOLOR="\033[34;1m"                                                                                                           
SRCCOLOR="\033[33m"                                                                                                        
BINCOLOR="\033[37;1m"                                                                                                                  
MAKECOLOR="\033[32;1m"                                                                                                 
ENDCOLOR="\033[0m"                                                                                                                   

QUIET_CC = @printf "\t%b %b" $(CCCOLOR)CC$(ENDCOLOR) $(SRCCOLOR)$@$(ENDCOLOR)"\n" 1>&2;                                                
QUIET_LINK = @printf "\t%b %b" $(LINKCOLOR)LINK$(ENDCOLOR) $(BINCOLOR)$@$(ENDCOLOR)"\n" 1>&2;                                
QUIET_INSTALL = @printf "\t%b %b" $(LINKCOLOR)INSTALL$(ENDCOLOR) $(BINCOLOR)$@$(ENDCOLOR)"\n" 1>&2;         

all: $(CONSUMER_LIB_NAME) $(TEST_CONSUMER_CPP) $(TEST_CONSUMER_C)
	@echo ""
	@echo "Hint: sudo make install to copy libs to /usr/lib"
	@echo ""

.PHONY: all 

install:
	$(CONSUMER_INSTALL) $(CONSUMER_LIB_NAME) $(PREFIX)/lib 
	@ldconfig
	@echo ""
	@echo "Hint: already copy "$(CONSUMER_LIB_NAME) "to "$(PREFIX)/lib
	@echo ""

.PHONY: install 

# Deps (use make dep to generate this)
include Makefile.dep
dep: 
	$(CXX) -MM *.cpp *.c > Makefile.dep

.PHONY: dep


# libconsumer.so
$(CONSUMER_LIB_NAME): $(CONSUMER_OBJ)
	$(CONSUMER_CXX_LD) $(SHARED_LINK_FLAGS) -o $@ $^ $(FINAL_LIB_DIR) $(CONSUMER_FINAL_LIBS)

# consumer_test_cpp
$(TEST_CONSUMER_CPP): $(CONSUMER_OBJ) $(CONSUMER_OBJ_TEST_CPP)
	$(CONSUMER_CXX_LD) -o $@ $^ $(FINAL_LIB_DIR) $(CONSUMER_FINAL_LIBS)

# consumer_test_c
$(TEST_CONSUMER_C): $(CONSUMER_OBJ_TEST_C) $(CONSUMER_LIB_NAME)
	$(CONSUMER_CC_LD) -o $@ $^ $(FINAL_LIB_DIR) $(CTEST_FINAL_LIBS)

%.o: %.cpp
	$(CONSUMER_CXX) $(SHARED_COMPILE_FLAGS) -c $<
%.o: %.c
	$(CONSUMER_CC) -c $<

.PHONY: clean 
clean:
	rm -rf $(TEST_CONSUMER_C) $(TEST_CONSUMER_CPP) $(CONSUMER_LIB_NAME) $(CONSUMER_OBJ) $(CONSUMER_OBJ_TEST_C) $(CONSUMER_OBJ_TEST_CPP)
