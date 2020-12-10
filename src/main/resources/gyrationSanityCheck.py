import os, sys

"""
This script exctracts the relavant columns for the script that calculates the
radius of gyration, from the locations file from the Charlottesville sample.

The locations file contains too many columns, so the columns would not align
with what the gyration calculator would expect without first converting it.

The columns in the output are 
col 0: HouseholdID*
col 1: PersonID
col 2: LocationID*
col 3: Latitude
col 4: Longitude

*this column is not relevant for gyration calculator, and may not be what is
in the actual file
"""

def starttimeToDay(starttime):
	return int(int(starttime) / 60 / 60 / 24)

# Clean up directories to avoid using old files
if os.path.exists(sys.argv[1] + "_locations"):
	for f in os.listdir(sys.argv[1] + "_locations"):
		os.remove(sys.argv[1] + '_locations/' + f)

if os.path.exists(sys.argv[1] + '_output'):
	for f in os.listdir(sys.argv[1] + '_output'):
		os.remove(sys.argv[1] + '_output/' + f)
	os.rmdir(sys.argv[1] + '_output')

# Keeps track of activities per day
locations = dict()
for i in range(7):
	locations[i] = list()

# Read passed locations file, and add each location file to the locations dictionary for the corresponding day
for a in sys.argv[1:]:
	with open(a, 'r') as f:
		for l in f.readlines()[1:]:
			cols = l.split(",")
			# We only store a few columns, so cols 3 and 4 align with latitude and longitude expected by gyration calculator script
			# Note that we switch around col 7 (latitude) and 8 (longitude)
			locations[starttimeToDay(cols[4])].append("\t".join([cols[i] for i in [0,1,6,8,7]]) + "\n")

if not os.path.exists(sys.argv[1] + "_locations"):
	os.mkdir(sys.argv[1] + "_locations")

# Write each set of locations to a file corresponding to that day
for c in locations:
	with open(sys.argv[1] + '_locations/day-{0}-locations.csv'.format(c), 'w') as f:
		for line in locations[c]:
			f.write(line)

# Call gyration calculation script
os.system('python3 gyration_radius_calculator.py -i {0}_locations -o {0}_output'.format(sys.argv[1]))

# Calculate averages for each day
averagePerDay = dict()
for f in os.listdir(sys.argv[1] + '_output'):
	day = f[4]
	kms = []
	with open(sys.argv[1] + '_output/' + f) as fl:
		for l in fl.readlines()[1:]:
			kms.append(float(l.split(",")[-1].replace("\n","")))
	averagePerDay[day] = sum(kms) / len(kms)

# Print averages for each day
for c in sorted(averagePerDay.keys()):
	print(c,averagePerDay[c])


"""
$ python3 gyrationSanityCheck.py usa_va_charlottesville_location_assignment_week_1_6_0.csv

0 3.1596771023005794
1 3.0201431323225787
2 3.0301251364309327
3 3.0412118452413273
4 3.1210377053570375
5 2.904215816789324
6 2.4572346969386634
"""	