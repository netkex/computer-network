CXXFLAGS=--std=c++23 -g -Wall -Werror -O3
LDXXFLAGS=-lstdc++fs -lstdc++ -lpthread

default: run

server: main.cpp server.cpp server.h storage.h
	$(CXX) $(CXXFLAGS) -o $@ main.cpp server.cpp $(LDXXFLAGS)

run: server
	./server $(ARGS)

clean:
	rm -f server