#http://www.aktau.be/2013/08/07/a-makefile-for-modern-c-programming-on-unix-like-operating-systems/
COMPILER ?= clang++
COMPILER_FLAGS ?= -std=c++11
DEPS = lib/*.h

%.o: %.cpp $(DEPS)
	$(COMPILER) $(COMPILER_FLAGS) -c $< -o $@

%: %.cpp $(DEPS)
	$(COMPILER) $(COMPILER_FLAGS) $< -o $@

all: dijkstra tree database quadtree fordfulkerson

format: *.cpp lib/*.h
	clang-format -i $^
