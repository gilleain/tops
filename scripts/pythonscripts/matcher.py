import sys
class Matcher(object):

	def match(self, pattern, target):
		match_table = self.preProcess(pattern, target)
		self.concatenateMatches(pattern, target, match_table)

	def preProcess(self, pattern, target):
		#step 1 : pre-processing
		pattern_length = len(pattern)
		target_length = len(target)
		if (pattern_length > target_length):
			print "pattern larger than target : no substring possible!"
			sys.exit(0)

		match_table = {}
		for i in range(0, pattern_length):
			pattern_letter = pattern[i]
			match_list = []
			for j in range(0, target_length):
				target_letter = target[j]
				if pattern_letter == target_letter:
					match_list.append(j)
			if match_list == []: 
				print "no matches for character", i, ":", pattern_letter
				sys.exit(0)
			match_table[i] = match_list 
		return match_table
					
	def concatenateMatches(self, pattern, target, match_table):
		#step 2 : run through the match_table
		match_table_positions = {}
		for pattern_index in match_table.keys():
			match_list = match_table[pattern_index]
			if pattern_index in match_table_positions:
			for i in range(match_list:
		target_index 

if __name__=="__main__":
	#pattern and target should be both simple strings of letters eg: "abcadcda..."
	pattern = sys.argv[1]
	target = sys.argv[2]
	matcher = new Matcher()
	matcher.match(pattern, target)

