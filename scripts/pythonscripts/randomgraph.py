import os, time, random

class RandomPattern(object):
	
	def __init__(self):
		graph = RandomGraph()
		self.min = 0
		self.max = 10
		self.pattern = self.makePattern(graph.sses, graph.edges)
	
	def makePattern(self, sses, edges):
		new_sses = ""
		for sse_index in range(0, len(sses) - 1):
			num1 = self.randNum()
			num2 = self.randNum()
			new_sses += sses[sse_index]
			new_sses += "["
			if (num1 < num2): 
				new_sses += str(num1)
			else:
				new_sses += str(num2)
			new_sses += "-"
			if (num2 > num1): 
				new_sses += str(num2)
			else:
				new_sses += str(num1)
			new_sses += "]"
		new_sses += sses[-1]
		return (new_sses, edges)

	def randNum(self):
		return random.randint(self.min, self.max)

	def __repr__(self):
		return self.pattern[0] + " " + "".join(self.pattern[1])

class RandomGraph(object):
	min_sse_length = 1
	max_sse_length = 10
	alphabet = ['E', 'e', 'H', 'h']

	def __init__(self):
		self.sses = self.makeSSEList()
		self.edges = self.makeEdges(self.sses)

	def makeSSEList(self):
		numberOfSSEs = random.choice(range(self.min_sse_length, self.max_sse_length))
		sses = ['N']
		for i in range(0, numberOfSSEs):
			sses.append(random.choice(self.alphabet))
		sses.append('C')
		return sses

	def makeEdges(self, sses):
		strand_indices = []
		for i in range(len(sses)):
			if sses[i] in ['E', 'e']:
				strand_indices.append(i)

		random.shuffle(strand_indices)	#make a random ordering of the sheet

		last = None
		edges = []
		for j in strand_indices:
			if last is not None:
				jType = sses[j]
				lastType = sses[last]
				if jType == lastType:
					edgeType = "P"
				else:
					edgeType = "A"
				pair = [j, last]
				pair.sort()
				edge = "%i:%i%s" % (pair[0], pair[1], edgeType)
				edges.append(edge)
			last = j
		return edges

	def __repr__(self):
		return "".join(self.sses) + " " + "".join(self.edges)

class MatchingTimer(object):
	java_command = "java -jar jars/inserts.jar"

	def run(self):
		start_time = time.time()
		#(start_hour, start_min, start_sec) = time.gmtime()[3:6]
		#result_stream = os.popen(java_command, "r")
		end_time = time.time()
		#(end_hour, end_min, end_sec) = time.gmtime()[3:6]
		#print end_min - start_min, "mins", end_sec - start_sec, "secs"

if __name__=="__main__":
	import sys
	number = 10
	if len(sys.argv) != 1:
		number = int(sys.argv[1])
	for i in range(number):
		r = RandomGraph()
		print "graph%i" % i, r
	p = RandomPattern()
	print "pattern", p

