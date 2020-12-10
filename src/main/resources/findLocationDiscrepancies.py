#!/bin/python3
import sys

split_char = ","
location_assignment_headers = 'hid,pid,activity_number,activity_type,start_time,duration,lid,longitude,latitude,travel_mode'.split(split_char)

def read_lines(infiles):
	lines = list()
	for fi in sys.argv[1:]:
		print("Considering file " + fi)
		with open(fi, 'r') as f:
			lines.extend(list(map(lambda x: dict(zip(location_assignment_headers, x.split(split_char))), f.readlines()[1:])))

	return lines

def find_locations(lines):
	locationMap = dict()
	max_lid_value = 0
	for l in lines:
		if not l['lid'] in locationMap:
			locationMap[l['lid']] = []
		locationMap[l['lid']].append((l['longitude'], l['latitude']))
		max_lid_value = max(max_lid_value, int(l['lid']))
	return locationMap, max_lid_value

def find_duplicates(locations, max_lid_value):
	duplicates = dict()
	for lid in locations:
		if(len(set(locations[lid]))) > 1:
			for l in set(locations[lid]):
				if not lid in duplicates:
					duplicates[lid] = {l : lid}
				else:
					duplicates[lid][l] = str(max_lid_value + 1)
					max_lid_value += 1

	return duplicates

def write_new_location_assignments(lines, duplicates, fout):
	with open(fout, 'w') as f:
		f.write(split_char.join(location_assignment_headers) + "\n")
		for l in lines:
			if l['lid'] in duplicates:
				l['lid'] = duplicates[l['lid']][(l['longitude'], l['latitude'])]
			lstring = split_char.join(map(lambda x: l[x], location_assignment_headers))
			f.write(lstring + "\n")


def write_stats(lines, locations, duplicates):
	for lid in duplicates:
		print(lid, set(duplicates[lid].keys()), "occurs {0} times".format(len(locations[lid])))
	print("{0} locations ({1} unique) found".format(len(lines), len(locations)))
	print("{0} locations have more than one coordinate associated with it".format(len(duplicates)))


if __name__ == "__main__":
	infiles = sys.argv[1:]
	lines = read_lines(infiles)
	locations, max_lid_value = find_locations(lines)
	# print(max_lid_value)
	duplicates = find_duplicates(locations, max_lid_value)

	# for l in duplicates:
	# 	print(l, duplicates[l])

	write_stats(lines, locations, duplicates)
	# write_new_location_assignments(lines, duplicates, "goochland_county_1_9_0/corrected_location_assignments.csv")


