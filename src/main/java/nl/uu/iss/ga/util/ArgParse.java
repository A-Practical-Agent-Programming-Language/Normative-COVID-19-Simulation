package main.java.nl.uu.iss.ga.util;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.annotation.Arg;
import net.sourceforge.argparse4j.impl.action.StoreTrueArgumentAction;
import net.sourceforge.argparse4j.inf.*;

import java.io.*;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class ArgParse {
    private static final Logger LOGGER = Logger.getLogger(ArgParse.class.getName());

    @Arg(dest = "personsfiles")
    private List<File> personsFiles;

    @Arg(dest = "householdsfiles")
    private List<File> householdsFiles;

    @Arg(dest = "activityfiles")
    private List<File> activityFiles;

    @Arg(dest = "locationsfiles")
    private List<File> locationsfiles;

    @Arg(dest = "normsfile")
    private File normsfile;

    @Arg(dest = "statefiles")
    private List<File> statefiles;

    @Arg(dest = "seed")
    private long seed;

    @Arg(dest = "threads")
    private int threads;

    @Arg(dest = "iterations")
    private int iterations;

    @Arg(dest = "startdate")
    private LocalDate startdate;

    @Arg(dest = "factionliberal")
    private double fractionliberal;

    @Arg(dest = "modeliberal")
    private double modeliberal;

    @Arg(dest = "modeconservative")
    private double modeconservative;

    @Arg(dest = "nodename")
    private int node;

    @Arg(dest = "descriptor")
    private String descriptor;

    @Arg(dest = "connectpansim")
    private boolean connectpansim;

    @Arg(dest = "logproperties")
    private File logproperties;

    private Random random = null;

    public ArgParse(String[] args) {
        ArgumentParser p = getParser();
        try {
            p.parseArgs(args, this);
            verifyLogProperties();
            verifyFiles();
        } catch (ArgumentParserException e) {
            p.handleError(e);
            System.exit(1);
        }
    }

    public List<File> getPersonsFiles() {
        return personsFiles;
    }

    public List<File> getHouseholdsFiles() {
        return householdsFiles;
    }

    public List<File> getActivityFiles() {
        return activityFiles;
    }

    public List<File> getLocationsfiles() {
        return locationsfiles;
    }

    public List<File> getStatefiles() {
        return statefiles;
    }

    public File getNormsfile() {
        return normsfile;
    }

    public double getFractionliberal() {
        return fractionliberal;
    }

    public double getModeliberal() {
        return modeliberal;
    }

    public double getModeconservative() {
        return modeconservative;
    }

    public int getNode() {
        return node;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public boolean isConnectpansim() {
        return connectpansim;
    }

    public long getSeed() {
        return seed;
    }

    public Random getRandom() {
        if(this.random == null) {
            this.random = new Random();
            if(this.seed >= 0)
                this.random.setSeed(this.seed);
        }
        return this.random;
    }

    public int getThreads() {
        return threads;
    }

    public int getIterations() {
        return iterations;
    }

    public LocalDate getStartdate() {
        return startdate;
    }

    public File getLogProperties() {
        return logproperties;
    }

    private void verifyFiles() {
        try {
            this.activityFiles = findFilesInList(this.activityFiles);
            this.householdsFiles = findFilesInList(this.householdsFiles);
            this.personsFiles = findFilesInList(this.personsFiles);
            this.locationsfiles = findFilesInList(this.locationsfiles);
            if(this.statefiles != null) {
                this.statefiles = findFilesInList(this.statefiles);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to verify files", e);
            getParser().printHelp();
            System.exit(1);
        }
    }

    private List<File> findFilesInList(List<File> files) throws FileNotFoundException {
        if(files == null) return Collections.emptyList();

        List<File> verifiedFiles = new ArrayList<>();
        for(File f : files) {
            verifiedFiles.add(findFile(f));
        }
        return verifiedFiles;
    }

    private File findFile(File f) throws FileNotFoundException {
        File existingFile = null;
        if(f.getAbsoluteFile().exists()) {
            existingFile = f;
        } else if (!f.isAbsolute()) {
            URL r = getClass().getClassLoader().getResource(f.toString());
            if (r != null) {
                f = new File(r.getFile()).getAbsoluteFile();
                if (f.exists())
                    existingFile = f;
            }
        }
        if(existingFile != null) {
            return existingFile;
        } else {
            throw new FileNotFoundException("File not found: " + f.getName());
        }
    }

    private void verifyLogProperties() {
        if(this.logproperties != null) {
            try {
                this.logproperties = findFile(this.logproperties);
                InputStream stream = new FileInputStream(this.logproperties);
                LogManager.getLogManager().readConfiguration(stream);
                LOGGER.log(Level.INFO, "Properites file for logger loaded");
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Properties file for logger not found. Using defaults");
            }
        }
    }

    private ArgumentParser getParser() {
        ArgumentParser parser = ArgumentParsers.newFor("2APL/SimpleEpiDemic Disease Simulation").build()
                .defaultHelp(true)
                .description("A ecological simulation environment for simulation of human acceptance of measures" +
                        " aimed at reducing spread of novel diseases");

        ArgumentGroup schedulefiles = parser.addArgumentGroup("Activity Schedule files")
                .description("The input files specifying the default behavior");
        schedulefiles.addArgument("--personsfile", "-pf")
                .type(File.class)
                .required(true)
                .dest("personsfiles")
                .nargs("+")
                .help("Specify the location of the file containing the details of individual agents in the artificial population");

        schedulefiles.addArgument("--householdsfile", "-hf")
                .type(File.class)
                .required(true)
                .dest("householdsfiles")
                .nargs("+")
                .help("Specify the location of the file containing the details of the households in the artificial population");

        schedulefiles.addArgument("--activityfile", "-af")
                .type(File.class)
                .required(true)
                .dest("activityfiles")
                .nargs("+")
                .help("Specify the location of the file(s) containing all the activities of all the agents in the artificial population");

        schedulefiles.addArgument("--locationsfile", "-lf")
                .type(File.class)
                .required(false)
                .dest("locationsfiles")
                .nargs("+")
                .help("Specify the location of the file containing all the activity locations of all the agents in the " +
                        "artificial population. This file may only be absent if the activity files provide the location " +
                        "data");

        schedulefiles.addArgument("--normsfile", "-nf")
                .type(File.class)
                .required(true)
                .dest("normsfile")
                .help("Specify the location of the file containing the schema for when norms are activated, updated " +
                        "or deactivated");

        schedulefiles.addArgument("--statefile", "-sf")
                .type(File.class)
                .required(false)
                .dest("statefiles")
                .nargs("+")
                .help("Specify the location of the file containing all the activity locations of all the agents in the artificial population");

        ArgumentGroup parameters = parser.addArgumentGroup("Tunable parameters")
                .description("The parameters that define the distributions from which agent properties will " +
                        "be randomly sampled. These parameters dictate probabilities for various reasoning " +
                        "factors that determine how agents will deviate from default behavior");
        parameters.addArgument("--fraction-liberal", "-l")
                .dest("fractionliberal")
                .type(Double.class)
                .required(false)
                .setDefault(.5)
                .help("Probability that a household will be assigned liberal. The remaining houeholds will be " +
                        "assigned as conversative");

        parameters.addArgument("--liberal-mode", "-lm")
                .dest("modeliberal")
                .type(Double.class)
                .required(false)
                .setDefault(.6)
                .help("The value towards which the normal distribution from which government attitude for liberal " +
                        "agents will be sampled. A sampled value of 1 indicates agent is highly likely to follow " +
                        "government directives while a value of 0 indicates the agent is highly unlikely to follow " +
                        "agent directives");

        parameters.addArgument("--conservative-mode", "-cm")
                .dest("modeconservative")
                .type(Double.class)
                .required(false)
                .setDefault(.6)
                .help("The value towards which the normal distribution from which government attitude for conservative " +
                        "agents will be sampled. A sampled value of 1 indicates agent is highly likely to follow " +
                        "government directives while a value of 0 indicates the agent is highly unlikely to follow " +
                        "agent directives");

        ArgumentGroup optimization = parser.addArgumentGroup("Runtime optimization");
        optimization.addArgument("--iterations", "-i")
                .type(Integer.class)
                .required(false)
                .dest("iterations")
                .setDefault(Integer.MAX_VALUE)
                .help("Specify the number of iterations to run this simulation. This parameter is ignored when" +
                        "pansim is connected by using the \"-c\" flag.");

        optimization.addArgument("--start-date")
                .required(false)
                .action(new ParseLocalDateArgumentAction())
                .dest("startdate")
                .help("Specify the date corresponding to the first time step. This option is used to potentially " +
                                "align the model with real-world data, both in the produced output, as in for the " +
                                "activation of norms.\n" +
                                "If this option is omitted, the first time step will be assigned the date when the first " +
                                "norm is activated. Format is YYYY-MM-DD");

        optimization.addArgument("--seed", "-s")
                .type(Long.TYPE)
                .required(false)
                .dest("seed")
                .setDefault(-1)
                .help("Specify a seed to use for random operations. Default is -1, indicating no seed is used");

        optimization.addArgument("--threads", "-t")
                .type(Integer.class)
                .required(false)
                .setDefault(8)
                .dest("threads")
                .help("Specify the number of threads to use for execution");

        optimization.addArgument("--node")
                .type(Integer.class)
                .required(false)
                .setDefault(-1)
                .dest("nodename")
                .help("Specify the name of this node, to distinguish output files if multiple nodes are run simultaniously");

        optimization.addArgument("--descriptor")
                .type(String.class)
                .required(false)
                .dest("descriptor")
                .help("Allows to specify a string that will be used in the naming scheme of output files");

        optimization.addArgument("--log-properties")
                .type(File.class)
                .required(false)
                .dest("logproperties")
                .setDefault(new File("logging.properties"));

        optimization.addArgument("--connect-pansim", "-c")
                .type(Boolean.class)
                .required(false)
                .setDefault(false)
                .action(new StoreTrueArgumentAction())
                .dest("connectpansim")
                .help("If this argument is present, the simulation will run in PANSIM mode, meaning it will send" +
                        "the generated behavior to the PANSIM environment. If absent, no PANSIM connection is required," +
                        "but behavior is not interpreted");

        return parser;
    }

    static class ParseLocalDateArgumentAction implements ArgumentAction {

        @Override
        public void run(ArgumentParser parser, Argument arg, Map<String, Object> attrs, String flag, Object value) throws ArgumentParserException {
            if(value instanceof  String) {
                String dateString = (String)value;
                try {
                    LocalDate date = LocalDate.parse(dateString, DateTimeFormatter.ISO_DATE);
                    attrs.put(arg.getDest(), date);
                } catch (DateTimeParseException e) {
                    throw new ArgumentParserException(e.getMessage(), parser);
                }
            } else {
                throw new ArgumentParserException("--start-date must be of format YYYY-MM-DD", parser);
            }
        }

        @Override
        public void onAttach(Argument arg) {
        }

        @Override
        public boolean consumeArgument() {
            return true;
        }
    }

}
