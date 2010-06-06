import re, sys

pattern = sys.argv[1]
target = sys.argv[2]

pattern_list = re.split("\[(.*?)\]", pattern)

pattern_ptr = 0
target_ptr  = 0
max_ptr = len(target)

while pattern_ptr < len(pattern_list):
	if target_ptr >= len(target):
		print pattern, "doesn't match", target
		print "(target_ptr overran) target_ptr(", target_ptr, ") >= len(target)(", len(target), ")"
		sys.exit(0)

	pattern_char = pattern_list[pattern_ptr]
	print "pattern_char=", pattern_char, "pattern_list[%i]" % pattern_ptr
	target_char = target[target_ptr]
	print "target_char=", target_char, "target_char[%i]" % target_ptr

	if max_ptr is not None and target_ptr > max_ptr:
		print pattern, "doesn't match", target
		print "(max_ptr overran) max_ptr(", max_ptr, ")< target_ptr(", target_ptr, ")"
		sys.exit(0)

	if pattern_char == target_char:
		print "chars equal"
		pattern_ptr += 1
		max_ptr = target_ptr
		#now process an insert to set the max_ptr and the target_ptr

		if pattern_ptr >= len(pattern_list):	#don't try and get a range for the last in the list
			continue
		range = pattern_list[pattern_ptr]
		pattern_ptr += 1
		if range == '':
			max_ptr = None
			target_ptr += 1
			continue
		elif range.find("-") != -1:
			(min, max) = range.split("-")
		else:
			(min, max) = (range, range)
		target_ptr += int(min)		 	#move in the target by the minimum
		print "target_ptr=", target_ptr
		max_ptr += int(max) + 1			#set the maximum position
		print "max_ptr=", max_ptr

	target_ptr += 1

print pattern, "matches", target
