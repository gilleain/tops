import math, re, sys

outfile = sys.argv[1]
file = open(outfile, 'r')
edge_pattern = re.compile("\d+:\d+\w")
for line in file:
	compression, partner, partner2, pattern = line.strip().split('\t')
	compression = float(compression)
	bits = pattern.split()
	if len(bits) == 3:
		name, vertices, edges = bits
		pattern_size = len(vertices) + len(edge_pattern.findall(edges))
	else:
		name, vertices = bits
		pattern_size = len(vertices)
	logsize = math.log(pattern_size)
	compression /= logsize
	print compression, partner, partner2, pattern
file.close()
