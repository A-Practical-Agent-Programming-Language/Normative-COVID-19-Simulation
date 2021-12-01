# Specification of the Synthetic Population Files
For our simulations we have used a synthetic population from which we instantiate the agents.
A synthetic population in the right format is required to run this simulation (at least without modifications), 
but we have not been granted the right to redistribute these files. 
In this manual we detail the specifications of the synthetic population
required to instantiate our simulation, so that you may be able to produce or convert your own.

We have also taken a sample from the synthetic population representing the details and activities of 3 agents
(2 adults and one child) from a single household. These files can be found in 
[src/main/resources/charlottesville_example](src/main/resources/charlottesville_examples)
and can serve as extra illustration of this documentation.

## Why we use a synthetic population
Agents in our simulation are drawn from a synthetic
population of the state of Virginia, USA. This synthetic population has been constructed from multiple
data sources including the American Community Survey (ACS), the National Household Travel Survey (NHTS), 
and various location and building data sets, as described in 
[this technical report](https://nssac.bii.virginia.edu/~swarup/papers/US-pop-generation.pdf). 
This gives us a very detailed representation of the
region we are studying (multiple counties within Virginia). Agents are assigned demographic variables drawn from the
ACS, such as age, sex, race, household income, or, optionally,
a designation as an essential worker, e.g., medical or retail.

The behavior of agents is characterized by weekly activity schedules,
a set of typical daily activities the agents perform over the course of one week 
obtained by integrating data from the NHTS.
The activity schedule defines the location, start time and duration of the agent's activities as one 
of 7 distinct high level activity types.
Appropriate locations are assigned to different activities using
data from multiple sources, including [HERE](https://www.here.com/), 
the [Microsoft Building Database](https://github.com/microsoft/USBuildingFootprints), 
and the [National Center for Education Statistics](https://nces.ed.gov/) (for school locations). 

## Structure of the synthetic population
The synthetic population consists of three classes of files:
* Person files
* Household files
* Activity files

These are all .CSV files. The contents of each class of file may be distributed over multiple actual .CSV files.
The files contain references to records from the other files through IDs. 
Specifically, the `pid` (person ID) is used to denote a unique record from the Person files, 
and the `hid` (household ID) to denote a unique record from the Household files.

The person files contain one record for each synthetic person or agent in the population, and specifies the relevant
socio-demographical characteristics of that person or agent. 
Each person belongs to a household, and each household has at least one member. 
These persons share a residential location, to which agents withdraw when performing the 
`HOME` type of activity, or when not performing any activity at all (due to cancellation after normative reasoning).

The activity files specify with high granularity for each person in the Person files what they are 
typically doing over the course of one week. 
Each activity is encoded as a single record, and characterized by one of 7 high level *activity types*:
* **HOME** stay at or work from home
* **WORK** go to work or take a work-related trip
* **SHOP** buy goods (e.g., groceries, clothes, appliances)
* **SCHOOL** attend school as a student
* **COLLEGE** attend college as a student
* **RELIGIOUS** religious or other community activities
* **OTHER** any other class of activities, including recreational activities, exercise, dining at a restaurant, etc.

The original synthetic population also contains *detailed activity types*, 
but as these have not been guaranteed to have been sampled accurately,
they are not used for the simulation. 
However, they can be useful in understanding the semantics of the higher level activity types.
The [`DetailedActivity.java`](src/main/java/nl/uu/iss/ga/model/data/dictionary/DetailedActivity.java) 
file specifies an ENUM in which the detailed activity types that the synthetic population 
uses are grouped by their higher level activity types.

## File format specification
All the fields used in each of the three files representing the synthetic population will be detailed here.

The synthetic population is split over each county in the state of Virginia. 
This allows us to run simulations for which we select the counties ourselves, 
instead of always using the entire state of Virginia.
In the [sample configuration](src/main/resources/config.toml) with which the simulation can be instantiated, 
we refer to the synthetic population for the county of Charlottesville City.

Not all values in the synthetic population used for this research are actually employed in the simulation. 
However, some are still parsed by the model, so their presence is required. 
The unused values are below marked with an asterisk, and can be given arbitrary values (within their type constraints) 
without having an effect on the simulation, while the values from the synthetic population (present in the sample
files) are not documented here at all.
Do note that this repository contains ongoing research, and these values may be used in later versions.

In the following, categorical types are distinguished in that they are linked to a 
Java ENUM where the possible value types are also documented.

### Person files
In the sample config, one Person file for Charlottesville City is specified: 
[`charlottesville_examples/usa_va_charlottesville_person_1_6_0.csv`](src/main/resources/charlottesville_examples/usa_va_charlottesville_person_1_6_0.csv)

Each of the person files is parsed using the [`PersonReader`](src/main/java/nl/uu/iss/ga/model/reader/PersonReader.java) 
and each person record is instantiated in the [`Person`](src/main/java/nl/uu/iss/ga/model/data/Person.java) class.

Each record in a person file encodes one person from the synthetic population.
Each Person file should have at least the following fields:

* `hid`: A long-typed real value representing a unique household ID
* `pid`: A long-typed real value representing the unique ID of this record
* `serialno*`: An integer-typed real value. Originally a unique value that refers to the survey number from
which this agent was sampled.
* `age`: An integer-typed real number representing the age of this person
* [`relationship*`](src/main/java/nl/uu/iss/ga/model/data/dictionary/Relationship.java): 
A categorical integer in the range [0,17] representing the relationship of this person to the reference person
of the household. The semantics of the values can be found in the 
[Relationship](src/main/java/nl/uu/iss/ga/model/data/dictionary/Relationship.java) enum.
* [`sex*`](src/main/java/nl/uu/iss/ga/model/data/dictionary/Gender.java): 
A categorical integer in the range [1,2] representing the gender of the agent, 
where `1` means `male` and `2` means `female`
* [`school_enrollment*`](src/main/java/nl/uu/iss/ga/model/data/dictionary/SchoolEnrollment.java): 
A categorical integer in the range [1,3] or the letter `b` representing how the person is enrolled in a
school program. `1` means not enrolled, `2` means enrolled in public education, 
`3` means enrolled in private education or homeschooled, 
and `b` means not applicable (for persons of 3 years old or less).
* [`grade_level_attending`](src/main/java/nl/uu/iss/ga/model/data/dictionary/GradeLevel.java): 
A categorical integer in the range [1,16] representing the grade level of the person enrolled in a
school, or the string `bb` if not enrolled. `1` means nursery school or preschool, `2` means kindergarten, `3`-`14`
represent the grade levels 1 to 12 respectively, `15` represents an undergraduate student, and `16` represents a 
graduate student or professional level education beyond a bachelor level.
* [`employment_status*`](src/main/java/nl/uu/iss/ga/model/data/dictionary/EmploymentStatus.java): 
A categorical integer in the range [1-6] representing the employment status of the person, or `bb` if no
employment status is available (for persons under the age of 16), where `1` represents a civilian employed at work,
`2` means a civilian employed with a job, but not at work, `3` means unemployed, `4` means armed forces at work, 
`5` means armed forces with a job but not at work, and `6` means the agent is not in the labor force.
* `occupation_socp*`: A string that originally represents one of a very large number of jobs. For this reason not
encoded for the purpose of this simulation, and can be any string value for this work.
* [`designation`](src/main/java/nl/uu/iss/ga/model/data/dictionary/Designation.java): 
Optional categorical integer representing the (optional) essential designation of this persons job.
Possible designations are {`military`, `government`, `retail`, `none`, `education`, `medical`, 
`care_facilitation`, `dmv`}, where `none` and a null value are equivalent

### Household files
In the sample config, one Household file for Charlottesville City is specified: 
[`charlottesville_examples/usa_va_charlottesville_household_1_6_0.csv`](src/main/resources/charlottesville_examples/usa_va_charlottesville_household_1_6_0.csv)

Each of the household files is parsed using the [`HouseholdReader`](src/main/java/nl/uu/iss/ga/model/reader/HouseholdReader.java)
and each household record is instantiated in the [`Household`](src/main/java/nl/uu/iss/ga/model/data/Household.java) class.

Each record in a household file encodes one household.
Each Household file should have at least the following fields:

* `hid`: A long-typed real value representing the unique ID of this record
* `serialno*`: An integer-typed real value. Originally a unique value that refers to the survey number from
  which this agent was sampled.
* `puma*`: Public Use Microdata Areas code. 
See [census.gov](https://www.census.gov/programs-surveys/geography/guidance/geo-areas/pumas.html)
* [`hh_size*`](src/main/java/nl/uu/iss/ga/model/data/dictionary/UnitSize.java): 
A categorical integer in the range [1,3] representing the size of the household, 
where `1` means a house on less than one acre of ground, `2` means house on between 1 and 10 acres of ground,
`3` means a house on more than 10 acres of ground, or `b` representing a non-single-family house or a mobile home.
* `vehicles*`: An integer-typed real number representing the number of vehicles the household jointly owns
* `hh_income*`: An integer-typed real valued number representing the yearly joint household income. 
For reference, for the synthetic population of Charlottesville City, the range is [0,846000] 
with an average of `79259`, a median of `54100` and a standard deviation of `87745`
* [`units_in_structure*`](src/main/java/nl/uu/iss/ga/model/data/dictionary/UnitsInStructure.java): 
A categorical 2-digit 0-padded integer in the range [1,10] representing if the house or apartment type 
is part of a bigger structure. The semantics of the values can be found in the 
[UnitsInStructure](src/main/java/nl/uu/iss/ga/model/data/dictionary/UnitsInStructure.java) enum.
* [`business*`](src/main/java/nl/uu/iss/ga/model/data/dictionary/BusinessOnProperty.java): 
Categorical value from {'b', 1, 2, 9}, with `b` representing a non-single-family house or a mobile home, 
`1` meaning yes, there is a business on this property, `2` means no, and 
`9` means the case could not be sampled, as it was from 2016 or later.
* [`heating_fuel*`](src/main/java/nl/uu/iss/ga/model/data/dictionary/Fuel.java): 
Categorical value representing the type of heating fuel used by the household. 
The semantics of the values can be found in the 
[Fuel](src/main/java/nl/uu/iss/ga/model/data/dictionary/Fuel.java) enum.
* [`household_language*`](src/main/java/nl/uu/iss/ga/model/data/dictionary/Language.java): 
Categorical value representing the primary language spoken by the household. 
The semantics of the values can be found in the 
[Language](src/main/java/nl/uu/iss/ga/model/data/dictionary/Language.java) enum.
* [`family_type_and_employment_status*`](src/main/java/nl/uu/iss/ga/model/data/dictionary/FamilyEmployment.java): 
Categorical value representing the family structure. 
The semantics of the values can be found in the 
[FamilyEmployment](src/main/java/nl/uu/iss/ga/model/data/dictionary/FamilyEmployment.java) enum.
* `workers_in_family*`: An integer-typed real value representing the number of workers in the family
* `rlid`: A unique long-valued ID representing the residence location
* `residence_longitude`: The longitude of the residence location. 
**IMPORTANT**: This value is used to calculate the radius of gyration, so should be sampled accurately
* `residence_latitude`: The latitude of the residence location. 
**IMPORTANT**: This value is used to calculate the radius of gyration, so should be sampled accurately

### Activity files
In the sample config, two Activity files for Charlottesville City are specified, one for adult agents, and one for children:
* [`charlottesville_examples/usa_va_charlottesville_activity_assignment_adult_week_1_6_0.csv`](src/main/resources/charlottesville_examples/usa_va_charlottesville_activity_assignment_adult_week_1_6_0.csv)
* [`charlottesville_examples/usa_va_charlottesville_activity_assignment_child_week_1_6_0.csv`](src/main/resources/charlottesville_examples/usa_va_charlottesville_activity_assignment_child_week_1_6_0.csv)

The activity files encode the activities over the course of one week for each agent in the population.

The `TRIP` activity type is not currently used in the simulation, but it is a good idea to include them anyway, or 
otherwise leave gaps between activities to account for travel time. Apart from travel time, ideally there should be no
gaps in the weekly activity schedule of any agent.

Each of the activity files is parsed using the [`ActivityFileReader`](src/main/java/nl/uu/iss/ga/model/reader/ActivityFileReader.java)
and each household record is instantiated in the [`Activity`](src/main/java/nl/uu/iss/ga/model/data/Activity.java) class.

Each record in the activity file encodes one activity for one agent. 
Each activity file should have at least the following fields:

* `pid`: The long-typed value representing the unique ID of the person for this activity
* `hid`: The long-typed value representing the unique ID of the household the person for this activity belongs to
* `activity_numer*`: A long-valued unique ID for this record
* [`activity_type`](src/main/java/nl/uu/iss/ga/model/data/dictionary/ActivityType.java): 
A categorical integer in the range [0,7] representing either 
`TRIP`, `HOME`, `WORK`, `SHOP`, `OTHER`, `SCHOOL`, `COLLEGE`, or `RELIGIOUS` respectively.
* [`detailed_activity*`](src/main/java/nl/uu/iss/ga/model/data/dictionary/DetailedActivity.java): 
A more detailed specification of what type this activity is. 
As explained previously, these detailed activity types have not been guaranteed to have been sampled 
accurately which is why we have opted not to use them in our model. 
However, they can be useful to understand the semantics of the higher level `activity_type`s. 
See [`DetailedActivity.java`](src/main/java/nl/uu/iss/ga/model/data/dictionary/DetailedActivity.java) 
for more information.
* [`start_time`](src/main/java/nl/uu/iss/ga/model/data/ActivityTime.java): 
A long value representing a time stamp for when the activity starts as the number of seconds since monday morning
(so `0` represents the first second of a Monday, and `24 * 60 * 60 = 86400` represents the first second of Tuesday).
* `duration`: The number of seconds an activity continues
* `lid`<sup>♰</sup>: A long-typed value representing the unique ID of the location to be visited. Multiple visits of this
or other agents to the same location should have the same ID
* `longitude`<sup>♰</sup>: The longitude of the activity location.
  **IMPORTANT**: This value is used to calculate the radius of gyration, so should be sampled accurately
* `latitude`<sup>♰</sup>: The latitude of the activity location.
  **IMPORTANT**: This value is used to calculate the radius of gyration, so should be sampled accurately
* [`travel_mode*`](src/main/java/nl/uu/iss/ga/model/data/dictionary/TransportMode.java)<sup>♰</sup>:
  A categorical integer in the range [-9,-7] ∪ [1,20] ∪ {97} representing the mode of transport employed during a
  `TRIP` type activity (no value required for other activity types)
  The semantics of the values can be found in the
  [`TransportMode*`](src/main/java/nl/uu/iss/ga/model/data/dictionary/TransportMode.java) enum.


### An optional class of files: Location assignment
The location designation of activities can be split to a separate class of files, as long as for each activity number
generated in the activity files, 
there is a location assigned in one of the location designation files.
This is the case in the provided samples, but it is not necessary, as all the relevant information used by the 
simulation can be specified as above.

In the sample config, one location assignment file is specified:
[`charlottesville_examples/usa_va_charlottesville_location_assignment_week_1_6_0.csv`](src/main/resources/charlottesville_examples/usa_va_charlottesville_location_assignment_week_1_6_0.csv)

Each record encodes the location for exactly one activity that is specified in the activity files.
If this approach is taken, the fields marked with a cross (<sup>♰</sup>) can be *moved* to this class of files
(i.e. deleted from the activity files), while the fields `hid`, `pid`, `activity_number`, `activity_type`, 
`start_time`, and `duration` should be replicated, with the exact same values for matching records.