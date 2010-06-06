import sys
class Recurser(object):

	def __init__(self, pattern, inserts, target):
		self.pattern = pattern
		self.inserts = inserts
		self.target = target

	def recurse(self, pattern_position, target_position):
		print "pattern position=", pattern_position, "target_position=", target_position
		if pattern_position >= len(self.pattern) or target_position >= len(self.target):
			return False
		pattern_char = self.pattern[pattern_position]
		target_char = self.target[target_position]
		if (pattern_char == target_char):
			print pattern_char, "==", target_char
			if pattern_position >= len(self.inserts):
				return True
			(min, max) = [int(x) for x in self.inserts[pattern_position]]
			print "ranging from", min + 1, "to", max + 2
			for i in range(min + 1, max + 2):
				next_position = target_position + i
				print "trying", i, "in range ", min, max, "position=", next_position
				match = self.recurse(pattern_position + 1, next_position)
				if match:
					return True
		return False

if __name__=="__main__":
	pattern = sys.argv[1]
	inserts = sys.argv[2]
	insert_list = [x.split("-") for x in inserts[1:-1].split("][")]
	target = sys.argv[3]

	r = Recurser(pattern, insert_list, target)
	match = r.recurse(0, 0)
	if match:
		print pattern, "with", inserts, "matches", target
	else:
		print pattern, "with", inserts, "does not match", target
