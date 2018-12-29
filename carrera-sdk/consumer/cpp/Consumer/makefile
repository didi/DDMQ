CC = g++
RM = rm
LD = ld
AR = ar

USR_DIR    =/usr/local
#THRIFT_DIR =$(USR_DIR)/include/thrift
THRIFT_DIR =./thirdparty/thrift
SPDLOG_DIR = ./thirdparty/spdlog
#BOOST_DIR  =$(USR_DIR)/include/boost
INCS_DIRS  =-I$(USR_DIR)/include -I$(THRIFT_DIR) -I$(SPDLOG_DIR) -I./thirdparty #-I$(BOOST_DIR)
#LIBS_DIRS  =-L$(THRIFT_DIR)/lib
LIBS_DIRS  =-L./thirdparty/thrift/lib64_release -L$(USR_DIR)/lib -L./thirdparty/carrera/lib64_release
CPP_DEFS   =-D=HAVE_CONFIG_H
CPP_OPTS   =-Wall -O2 -std=c++11 -g -fexceptions
LIBS       =-lcarrera_con -lthrift -lpthread
LD_FLAGS  =-shared -fPIC 

LIB_SOURCE  := ServiceDiscoveryService.cpp CarreraConsumer.cpp consumerProxy_constants.cpp mq_logger.cpp ConsumerService.cpp  consumerProxy_types.cpp  SimpleCarreraClient.cpp carreraServiceDiscovery_constants.cpp 	carreraServiceDiscovery_types.cpp ServiceDiscoveryService_server.skeleton.cpp
LIB_OBJS    := $(patsubst %.cpp,%.o,$(LIB_SOURCE))
TARGET_LIB := libcarrera.so
TARGET_STATIC_LIB := ./thirdparty/carrera/lib64_release/libcarrera_con.a

EXAMPLE_SOURCE := CarreraClientExample.cpp
EXAMPLE_OBJ := $(patsubst %.cpp,%.o,$(EXAMPLE_SOURCE))

all:example

example:lib
	$(CC) $(CPP_OPTS) $(CPP_DEFS) $(INCS_DIRS) $(LIBS_DIRS) $(EXAMPLE_SOURCE) $(LIBS) -o consumer_example

lib:$(LIB_OBJS)
	$(RM) -rf ./thirdparty/carrera
	mkdir ./thirdparty/carrera
	mkdir ./thirdparty/carrera/lib64_release
	cp *.h ./thirdparty/carrera
	echo $(LIB_OBJS)
	$(AR) rcs $(TARGET_STATIC_LIB) $(LIB_OBJS)
#$(CC) $(LD_FLAGS) $(LIBS_DIRS) $(LIBS) -o $(TARGET_LIB) $(LIB_OBJS)

%.o:%.cpp
	@echo Compiling $< ...
	$(CC) -c $(CPP_OPTS) $(LD_FLAGS) $(CPP_DEFS) $(INCS_DIRS)  $< -o $*.o

.PHONY: clean

clean:
	$(RM) -rf ./thirdparty/carrera
	$(RM) -r *_example *.o 
