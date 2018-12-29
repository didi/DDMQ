cmake_minimum_required(VERSION 3.12)
project(LowlevelConsumer)

set(CMAKE_CXX_STANDARD 14)

include_directories(src)

add_executable(LowlevelConsumer
        src/Consumer.cpp
        src/Consumer.h
        src/ConsumerConfig.cpp
        src/ConsumerConfig.h
        src/ConsumerLogger.cpp
        src/ConsumerLogger.h
        src/ConsumerPool.cpp
        src/ConsumerPool.h
        src/consumerProxy_constants.cpp
        src/consumerProxy_constants.h
        src/consumerProxy_types.cpp
        src/consumerProxy_types.h
        src/ConsumerService.cpp
        src/ConsumerService.h
        src/ConsumerWrapper.cpp
        src/consumerwrapper.h
        src/test_consumer.c
        src/test_consumer_pool.cpp)
