import sys

filename = sys.argv[1]
file = open(filename, 'r')
pattern_map = {}
summary = [0, 0]
for line in file:
	#p_group is the pattern group we made a pattern from
	#t_group is the target group we matched the pattern to
	(graph, p_group, p_group_size, number_matches, t_group_size, percent_matched, t_group) = line.split('\t')
	if p_group in pattern_map:
		values = pattern_map.get(p_group)
		values[1] += int(number_matches)
		values[2] += int(t_group_size)
		summary[0] += int(number_matches)
		summary[1] += int(t_group_size)
	else:
		#values = [graph, p_group_size, int(number_matches), int(t_group_size)]
		values = [p_group_size, int(number_matches), int(t_group_size)]
		pattern_map[p_group] = values

for pattern in pattern_map:
	print pattern, "\t".join([str(x) for x in pattern_map[pattern]])
print "total FP :", summary[0], "out of :", summary[1]
