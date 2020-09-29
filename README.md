

# Invoking program
See manual.txt for command line arguments, or call --help when running the application
Note the flag `-c` (or `--connect-pansim`) should be used to share data with the [PanSim](https://github.com/parantapa/pansim) simulation environment.

# Prerequisites
This manual assumes Maven is installed for easy package management

Prerequisites:
* Java 14+ (not tested with lower versions)
* Sim2APL
* (Maven)

## Sim2APL
Download Sim2APL from Bitbucket, and checkout the `feature/generic-plan-return` branch.

```bash
$ git clone https://AJdeMooij@bitbucket.org/goldenagents/sim2apl.git 
$ cd sim2apl
$ git checkout origin/feature/generic-plan-return -b feature/generic-plan-return
```

Install the package using Maven:

```bash
$ mvn -U clean install
```

This will automatically add the library to your local Maven repository, so no further action is required here.

## This library
Clone the master branch of this library and install with Maven, or open in an IDE with Maven support (e.g. VSCode, Idea Intellij, Eclipse or NetBeans) and let the IDE set up the project.

```bash
$ git clone https://AJdeMooij@bitbucket.org/goldenagents/sim2apl-episimpledemics.git
$ cd sim2apl-episimpledemics
$ mvn -U clean install
```

The application requires various arguments, either when invoked from the command line or when used in an IDE.
See [manual.txt](manual.txt) for more information, or invoke the program with the argument `--help`
