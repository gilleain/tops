#! /usr/bin/env python
"""
Script to calculate sensitivity and specificity of insert and non-insert patterns
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

def fileCount(filename):
	return len(readFile(filename))

def generatePatternFromGroup(mode, filename):
	explorer_jarfile = "/local/brc/users/maclean/tops/jars/drg.jar"
	if mode == "-n":
		command = "java -jar %s -f %s -g" % (explorer_jarfile, filename)
	elif mode == "-i":
		command = "java -jar %s -f %s -gi" % (explorer_jarfile, filename)
	else:
		sys.exit(0)
	result = run(command)
	line = result.read().rstrip('\n')
	result.close()
	numPatternGroup = fileCount(filename)
	try:
		(compression, pattern) = line.split("\t")
		if mode == "-n":
			return pattern
		elif mode == "-i":
			return "pattern %s" % pattern
	except Exception:
		print "ERROR for", filename
		return "pattern NC"

def matchPatternToGroup(mode, pattern, filename):
	if mode == "-n":
		drg_jarfile = "/local/brc/users/maclean/tops/jars/drg.jar"
		qualified_classname = "tops.engine.drg.Matcher"
		command = "java -cp %s %s '%s' %s" % (drg_jarfile, qualified_classname, pattern, filename)
	elif mode == "-i":
		insert_jarfile = "/local/brc/users/maclean/tops/jars/inserts.jar"
		command = "java -jar %s '%s' %s OFF" % (insert_jarfile, pattern, filename)
	else:
		sys.exit(0)
	result = run(command)
	matches = []
	for line in result:
		matches.append(line)
	result.close()
	return matches

def senspe(mode, pattern, numPatternGroup, pattern_file, target_file):
	numTarget = fileCount(target_file)
	matches = matchPatternToGroup(mode, pattern, target_file)
	numMatches = len(matches)
	percent_matched = str(int((numMatches * 100) / numTarget )) + "%"
	pattern_basename = os.path.basename(pattern_file)
	target_basename = os.path.basename(target_file)
	data = (pattern[8:], pattern_basename, numPatternGroup, numMatches, numTarget, percent_matched, target_basename)
	#print "pattern %s for group %s (%i) matched %i from %i (%s) in group %s" % data
	print "\t".join([str(x) for x in data])

pattern_path = sys.argv[1]
target_path = sys.argv[2]
mode = sys.argv[3]
if os.path.isfile(pattern_path) and os.path.isfile(target_path):
	pattern = generatePatternFromGroup(mode, pattern_path)
	numPatternGroup = fileCount(pattern_path)
	senspe(mode, pattern, numPatternGroup, pattern_path, target_path)
else:
	for pattern_filename in os.listdir(pattern_path):
		full_pattern_path = "%s/%s" % (pattern_path, pattern_filename)
		pattern = generatePatternFromGroup(mode, full_pattern_path)
		numPatternGroup = fileCount(full_pattern_path)
		for target_filename in os.listdir(target_path):
			full_target_path = "%s/%s" % (target_path, target_filename)
			if full_pattern_path != full_target_path:
				senspe(mode, pattern, numPatternGroup, full_pattern_path, full_target_path)
