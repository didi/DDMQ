## Dependency 

- log4cpp(1.1.3): logging utility library.
- thrift(0.9.2): rpc utility library.
- boost(1.64.0): refer to thrift install guide.

## Compile 

- Step 0: `make dep`
- Step 1: `make`
- Step 2: `sudo make install`

## Use case 

- Step0: Include corresponding header files in source file like `src/test_consumer_pool.cpp` or `src/test_consumer.c`
- Step1: Compile with flags `-I/path/to/the/header/file -lconsumer`, refer to Makefile for details.

Note: default.conf is the sample configuration for log4cpp.
