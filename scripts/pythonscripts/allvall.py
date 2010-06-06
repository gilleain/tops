#! /usr/bin/env python
#run tops comparison on a file of a set of strings and print out a table of results 
import os, sys
def compare(name1, name2, structures):
	structure1 = "%s %s" % (name1, structures[name1])
	structure2 = "%s %s" % (name2, structures[name2])
	print "comparing", name1, name2
	os.system("java -jar ${TOPS_LIB}/drg.jar -s \"%s\" -c \"%s\"" % (structure1, structure2))

if len(sys.argv) == 1:
	print "usage : allvsall.py <filename>"
	sys.exit(0)

filename = sys.argv[1]
structures = {}
file = open(filename, 'r')
for line in file:
	line = line.rstrip('\n')
	(name, vertices, edges) = line.split(' ')
	structures[name] = "%s %s" % (vertices, edges)
file.close()

names = structures.keys()
size = len(names)
for i in range(0, size):
	for j in range(i, size):
		if i != j:
			compare(names[i], names[j], structures)
