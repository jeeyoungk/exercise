# http://www.aktau.be/2013/08/07/a-makefile-for-modern-c-programming-on-unix-like-operating-systems/
COMPILER ?= clang++
COMPILER_FLAGS ?= -std=c++11
HEADERS = lib/graph.h
OBJECTS = lib/graph.o

# compile object files
%.o: %.cpp $(HEADERS)
	$(COMPILER) $(COMPILER_FLAGS) -c $< -o $@

# compile main files.
%: %.cpp $(HEADERS) $(OBJECTS)
	$(COMPILER) $(COMPILER_FLAGS) $(OBJECTS) $< -o $@

all: dijkstra tree database quadtree fordfulkerson

format: *.cpp lib/*.cpp $(HEADERS)
	clang-format -i $^
