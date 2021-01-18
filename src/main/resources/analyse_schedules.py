#!/bin/python3

import os
import sys

household = []
person = []
activities = []

important_files = [(household, "household", "hid"), (person, "person", "pid"), (activities, "locat", None)]

activity_type_dictionary = {
	0: "trip",
	1: "home",
	2: "work",
	3: "shop",
	4: "other",
	5: "school",
	6: "unknown_other",
	7: "religious"
}

"""Get the required files from the passed directory"""
for f in os.listdir(sys.argv[1]):
	if f.endswith("csv"):
		for x in important_files:
			if x[1] in f:
				x[0].append(os.path.join(sys.argv[1], f))

print(activities)


def test_duplicate(files: (list, str, str), key: str) -> (int, dict):
	"""Test if there are duplicate keys in a file"""
	duplicates = dict()
	ids = list()
	for f in files:
		with open(f, 'r') as fi:
			lines = fi.read().splitlines()
			headers = lines[0].split(",")
			for l in lines[1:]:
				line = dict(zip(headers, l.split(",")))
				id = line[key]
				if id in ids:
					try:
						duplicates[id] += 1
					except:
						duplicates[id] = 1
				else:
					ids.append(id)

	return len(ids), duplicates


def read_activities():
	activity_schedule = []
	for f in activities:
		with open(f, 'r') as fi:
			lines = fi.readlines()
			headers = lines[0].split(",")
			for l in lines[1:]:
				activity_schedule.append(dict(zip(headers, l.split(","))))

	return activity_schedule


def group_activities_per_day(activities):
	total_per_day = dict()
	activities_per_day = dict()
	for activity in activities:
		day = start_time_to_day(activity["start_time"])
		activity_type = activity_type_dictionary[int(activity["activity_type"])]
		duration = int(activity["duration"])
		if day not in activities_per_day:
			activities_per_day[day] = dict()
			total_per_day[day] = 0
		if activity_type not in activities_per_day[day]:
			activities_per_day[day][activity_type] = dict(count=0, duration=0)
		activities_per_day[day][activity_type]["count"] += 1
		activities_per_day[day][activity_type]["duration"] += duration
		total_per_day[day] += 1

	return activities_per_day, total_per_day


def start_time_to_day(start_time: int or str) -> int:
	"""Convert a start time (seconds since sunday midnight) to a day of week"""
	return int(int(start_time) / 60 / 60 / 24)


def test_duplicates():
	for f in important_files:
		if f[2] is not None:
			unique, duplicate_dict = test_duplicate(f[0], f[2])
			print(f[1], "{1} values with unique key '{0}', {2} entries occur more than once".format(f[2], unique, len(
				duplicate_dict)))


def process_activities_per_day(activities_per_day, total_per_day):
	for day in activities_per_day:
		print(day)
		for t in sorted(activities_per_day[day], key=lambda k: activities_per_day[day][k]["count"], reverse=True):
			print("\t{type}  Count: {count}   duration: {duration}".format(type=t, **activities_per_day[day][t]))
		print("\tDay total: ", total_per_day[day])


def write_activities_per_day(out, activities_per_day, total_per_day):
	ordered_types = list(map(lambda day_index: activity_type_dictionary[day_index], range(len(activity_type_dictionary))))
	headers = list(map(lambda l: f"{l}_count", ordered_types))
	headers += list(map(lambda l: f"{l}_duration", ordered_types))

	with open(out, 'w') as fo:
		fo.write("day;total;" + ";".join(headers) + "\n")
		for day in activities_per_day:
			fo.write(f"{day};")
			fo.write(f"{total_per_day[day]}")
			for k in ["count", "duration"]:
				for t in ordered_types:
					fo.write(f";{activities_per_day[day][t][k]}" if t in activities_per_day[day] else ";")
			fo.write("\n")

	print(f"Stored activity summary for {sys.argv[1]} in {out}")


if __name__ == "__main__":
	# test_duplicates()
	activities, total = group_activities_per_day(read_activities())
	process_activities_per_day(activities, total)

	if len(sys.argv) > 2:
		write_activities_per_day(sys.argv[2], activities, total)
