usage: 2APL/SimpleEpiDemic Disease Simulation
       [-h] --personsfile PERSONSFILE --householdsfile HOUSEHOLDSFILE
       --activityfile ACTIVITYFILE [--seed SEED] [--threads THREADS]

A ecological simulation environment for  simulation  of human acceptance of
measures aimed at reducing spread of novel diseases

named arguments:
  -h, --help             show this help message and exit
  --personsfile PERSONSFILE, -pf PERSONSFILE
                         Specify the location  of  the  file containing the
                         details of  individual  agents  in  the artificial
                         population
  --householdsfile HOUSEHOLDSFILE, -hf HOUSEHOLDSFILE
                         Specify the location  of  the  file containing the
                         details  of  the  households   in  the  artificial
                         population
  --activityfile ACTIVITYFILE, -af ACTIVITYFILE
                         Specify the location  of  the  file containing all
                         the  activities  of   all   the   agents   in  the
                         artificial population
  --seed SEED, -s SEED   Specify a  seed  to  use  for  random  operations.
                         Default  is  -1,  indicating   no   seed  is  used
                         (default: -1)
  --threads THREADS, -t THREADS
                         Specify  the  number   of   threads   to  use  for
                         execution (default: 8)