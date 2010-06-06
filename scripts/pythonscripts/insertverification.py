#! /usr/bin/env python
"""Script to verify the correctness of insert patterns by generating a pattern for some groups
and then matching that back to the group to see if we get total coverage.
"""
import os, sys

def run(string):
	#print "running", string
	return os.popen(string)

def readFile(filename):
	file = open(filename, 'r')
	lines = file.readlines()
	file.close()
	return lines

def generateInsertPatternFromGroup(filename):
	explorer_jarfile = "/local/brc/users/maclean/tops/jars/drg.jar"
	command = "java -jar %s -f %s -g" % (explorer_jarfile, filename)
	result = run(command)
	line = result.read().rstrip('\n')
	result.close()
	try:
		(compression, vertex_edges) = line.split("\t")
		return "pattern %s" % vertex_edges
	except Exception:
		print "ERROR for", filename
		return "pattern NC"

def matchPatternToGroup(pattern, filename):
	insert_jarfile = "/local/brc/users/maclean/tops/jars/inserts.jar"
	command = "java -jar %s '%s' %s OFF" % (insert_jarfile, pattern, filename)
	result = run(command)
	matches = []
	for line in result:
		matches.append(line)
	result.close()
	return matches

def verifyFile(string_file):
	strings = readFile(string_file)
	pattern = generateInsertPatternFromGroup(string_file)
	matches = matchPatternToGroup(pattern, string_file)
	percent_matched = int((len(matches) / len(strings)) * 100)
	if percent_matched < 100:
		print pattern
		print "matched %i from %i (%i) in group %s" % (len(matches), len(strings), percent_matched, string_file)
	else:
		print "group", string_file, "OK"

path = sys.argv[1]
if os.path.isfile(path):
	verifyFile(path)
else:
	for filename in os.listdir(path):
		verifyFile("%s/%s" % (path, filename))
