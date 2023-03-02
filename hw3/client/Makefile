CXXFLAGS=--std=c++23 -g -Wall -Werror -O3
LDXXFLAGS=-lstdc++fs

default: run

client: main.cpp
	$(CXX) $(CXXFLAGS) -o $@ main.cpp $(LDXXFLAGS)

run: client
	./client $(ARGS)

clean:
	rm -f client