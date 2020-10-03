#!/bin/python3

lines = list()
with open('usa_va_charlottesville_location_assignment_week_1_6_0.csv') as f:
	lines = list(map(lambda x: dict(zip('hid,pid,activity_number,activity_type,start_time,duration,lid,longitude,latitude,travel_mode'.split(','), x.split(','))), f.readlines()[1:]))


locationMap = dict()

for l in lines:
	if not l['lid'] in locationMap:
		locationMap[l['lid']] = []
	locationMap[l['lid']].append((l['longitude'], l['latitude']))



falses = 0

for lid in locationMap:
	if(len(set(locationMap[lid]))) > 1:
		print(lid, set(locationMap[lid]), "occurs {0} times".format(len(locationMap[lid])))
		falses+=1

print("{0} locations ({1} unique) found".format(len(lines), len(locationMap)))
print("{0} locations have more than one coordinate associated with it".format(falses))



