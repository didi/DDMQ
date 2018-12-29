CC = g++
RM = rm
LD = ld
AR = ar

USR_DIR    =/usr/local
#THRIFT_DIR =$(USR_DIR)/include/thrift
THRIFT_DIR = ./thirdparty/thrift
SPDLOG_DIR = ./thirdparty/spdlog
#BOOST_DIR  =$(USR_DIR)/include/boost
INCS_DIRS  =-I$(USR_DIR)/include -I$(THRIFT_DIR) -I$(SPDLOG_DIR) -I./thirdparty #-I$(BOOST_DIR)
#INCS_DIRS  =-I$(USR_DIR)/include #-I$(BOOST_DIR)
#LIBS_DIRS  =-L$(THRIFT_DIR)/lib
LIBS_DIRS  =-L./thirdparty/thrift/lib64_release -L$(USR_DIR)/lib -L./thirdparty/curl/lib64_release -L./thirdparty/jsoncpp/lib64_release -L./thirdparty/carrera/lib64_release

CPP_DEFS   =-D=HAVE_CONFIG_H
CPP_OPTS   =-std=c++11  -Wall -O2 -g -fexceptions
LIBS       =-lcarrera -lthrift -lpthread -lcurl -ljsoncpp -lstdc++ -luuid 
LD_FLAGS  =-shared -fPIC 

LIB_SOURCE  :=CarreraProducerCWrapper.cpp producerProxy_constants.cpp producerProxy_types.cpp ProducerService.cpp CarreraConnection.cpp CarreraProducer.cpp BridgeQHolder.cpp DelayProducer.cpp HttpClient.cpp ServiceDiscoveryService.cpp ServiceDiscoveryService_server.skeleton.cpp carreraServiceDiscovery_types.cpp carreraServiceDiscovery_constants.cpp
LIB_OBJS    := $(patsubst %.cpp,%.o,$(LIB_SOURCE))
TARGET_LIB := libcarrera.so
TARGET_STATIC_LIB := ./thirdparty/carrera/lib64_release/libcarrera.a

REALTIME_C_EXAMPLE_SOURCE := CarreraClientExample.c
REALTIME_EXAMPLE_SOURCE := CarreraClientExampleOld.cpp
DELAY_EXAMPLE_SOURCE := DelayClientExample.cpp
REALTIME_SD_EXAMPLE_SOURCE := CarreraClientExample.cpp
REALTIME_SD_TEST_EXAMPLE_SOURCE := CarreraClientTest.cpp
EXAMPLE_OBJ := $(patsubst %.cpp,%.o,$(EXAMPLE_SOURCE))


all:delay_example realtime_example_old realtime_c_example realtime_example realtime_sd_test_example

realtime_example:lib
	$(CC) $(CPP_OPTS) $(CPP_DEFS) $(INCS_DIRS) $(LIBS_DIRS) $(REALTIME_SD_EXAMPLE_SOURCE) $(LIBS) -o realtime_example

realtime_sd_test_example:lib
	$(CC) $(CPP_OPTS) $(CPP_DEFS) $(INCS_DIRS) $(LIBS_DIRS) $(REALTIME_SD_TEST_EXAMPLE_SOURCE) $(LIBS) -o realtime_sd_test_example

delay_example:lib
	$(CC) $(CPP_OPTS) $(CPP_DEFS) $(INCS_DIRS) $(LIBS_DIRS) $(DELAY_EXAMPLE_SOURCE) $(LIBS) -o delay_example

realtime_example_old:lib
	$(CC) $(CPP_OPTS) $(CPP_DEFS) $(INCS_DIRS) $(LIBS_DIRS) $(REALTIME_EXAMPLE_SOURCE) $(LIBS) -o realtime_example_old

realtime_c_example:lib
	$(CC) $(CPP_OPTS) $(CPP_DEFS) $(INCS_DIRS) $(LIBS_DIRS) $(REALTIME_C_EXAMPLE_SOURCE) $(LIBS) -o realtime_c_example

lib:$(LIB_OBJS)
	$(RM) -rf ./thirdparty/carrera
	mkdir ./thirdparty/carrera
	mkdir ./thirdparty/carrera/lib64_release
	cp *.h ./thirdparty/carrera
	echo $(LIB_OBJS)
#$(CC) $(LD_FLAGS) $(LIBS_DIRS) $(LIBS) -o $(TARGET_LIB) $(LIB_OBJS)
	$(AR) rcs $(TARGET_STATIC_LIB) $(LIB_OBJS)
#rm $(TARGET_LIB)

%.o:%.cpp
	@echo Compiling $< ...
	$(CC) -c $(CPP_OPTS) $(LD_FLAGS) $(CPP_DEFS) $(INCS_DIRS)  $< -o $*.o

.PHONY: clean

clean:
	$(RM) -rf ./thirdparty/carrera
	$(RM) -r *_example *.o 
