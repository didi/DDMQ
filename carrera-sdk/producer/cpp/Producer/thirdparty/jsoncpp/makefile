

%.o:%.cpp
	g++ -Wall -c $?

test_json:test_json.o
	g++ -o $@ $^ -L./build64_release -ljsoncpp
