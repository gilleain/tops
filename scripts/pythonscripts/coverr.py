import Image, math, re, sys
edge_pattern = re.compile("\d+:\d+\w")

#alternative ways to get the homologs list
def convertClassToHomologs(classification_filename):
	homologs = {}
	classifications = {}
	total_number_of_examples = 0
	file = open(classification_filename, 'r')
	for line in file:
		total_number_of_examples += 1
		name, classification = line.strip('\n').split('\t')
		classificationbits = classification.split(".")
		classification_stub = ".".join(classificationbits[0:2])
		name_list = classifications.setdefault(classification_stub, [])
		name_list.append(name)
	file.close()
	for classification in classifications:
		name_list = classifications[classification] 
		for name in name_list:
                        homolog_list = list(name_list)
                        homolog_list.remove(name)
                        homologs[name] = homolog_list
	number_of_homologous_pairs = calculateNumberOfHomologousPairs(homologs)
	total_number_of_pairs = (total_number_of_examples * (total_number_of_examples - 1)) / 2
	return total_number_of_pairs, number_of_homologous_pairs, homologs

def logCompression(compression, pattern):
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
	return compression

def calculateNumberOfHomologousPairs(homologs):
	count = 0
        for name in homologs:
                group = homologs[name]
                group_size = len(group)
                count += group_size
        return int(count / 2)

def readHomologs(homologs_filename):
	homologs = {}
	total_number_of_pairs = 0
	number_of_homologous_pairs = 0
	file = open(homologs_filename, 'r')
	for line in file:
		total_number_of_pairs += 1
		partner1, partner2, homology = line.strip('\n').split()
		homolog_list1 = homologs.setdefault(partner1, [])
		homolog_list2 = homologs.setdefault(partner2, [])
		if homology == 'h':
			number_of_homologous_pairs += 1
			homolog_list1.append(partner2)
			homolog_list2.append(partner1)
	file.close()
	return total_number_of_pairs, number_of_homologous_pairs, homologs

mode = sys.argv[1]
homologs_filename = sys.argv[2]
if mode == '-c':
	total_number_of_pairs, number_of_homologous_pairs, homologs = convertClassToHomologs(homologs_filename)
else:
	total_number_of_pairs, number_of_homologous_pairs, homologs = readHomologs(homologs_filename)

number_of_non_homologous_pairs = total_number_of_pairs - number_of_homologous_pairs
print "#", number_of_non_homologous_pairs, number_of_homologous_pairs, total_number_of_pairs 

allvalldata_filename = sys.argv[3]
file = open(allvalldata_filename, 'r')
homolog_symbol = ""
data = []
REAL_homologous_pairs = 0
REAL_non_homologous_pairs = 0
for line in file:
	bits = line.strip('\n').split('\t')
	compression, partner1, partner2, pattern = (bits[0], bits[1], bits[2], bits[3])
	#log_compression = logCompression(compression, pattern)
	if partner2 in homologs[partner1]:
		homolog_symbol = "H"
		REAL_homologous_pairs += 1
	else:
		REAL_non_homologous_pairs += 1
		homolog_symbol = "N"
	#data.append((log_compression, homolog_symbol, partner1, partner2))
	data.append((compression, homolog_symbol, partner1, partner2))
data.sort()
cumulative_coverage = 0
cumulative_error = 0
for row in data:
	compression, homolog_symbol, p1, p2 = row
	if homolog_symbol == "H":
		cumulative_coverage += 1
	if homolog_symbol == "N":
		cumulative_error += 1
	#percentage_coverage = float(cumulative_coverage) / float(number_of_homologous_pairs)
	percentage_coverage = float(cumulative_coverage) / float(REAL_homologous_pairs)
	#percentage_error = float(cumulative_error) / float(number_of_non_homologous_pairs)
	percentage_error = float(cumulative_error) / float(REAL_non_homologous_pairs)
	print "%f\t%f\t%s\t%s\t%s" % (percentage_error, percentage_coverage, homolog_symbol, p1, p2)
