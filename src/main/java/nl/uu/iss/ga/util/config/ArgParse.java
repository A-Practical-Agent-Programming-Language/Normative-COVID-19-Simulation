package main.java.nl.uu.iss.ga.util.config;

import main.java.nl.uu.iss.ga.util.Methods;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.annotation.Arg;
import net.sourceforge.argparse4j.impl.action.StoreTrueArgumentAction;
import net.sourceforge.argparse4j.inf.ArgumentGroup;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import org.apache.commons.math3.distribution.BetaDistribution;
import org.tomlj.Toml;
import org.tomlj.TomlArray;
import org.tomlj.TomlParseResult;
import org.tomlj.TomlTable;

import javax.annotation.Nullable;
import java.io.*;
import java.net.URL;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class ArgParse {
    private static final Logger LOGGER = Logger.getLogger(ArgParse.class.getName());

    @Arg(dest = "configuration")
    private File configuration;

    private List<File> householdVotingAssignments = null;

    private List<ConfigModel> counties = new ArrayList<>();

    private File normFile;

    @Arg(dest = "threads")
    private int threads;

    private long iterations;

    private LocalDate startdate;

    @Arg(dest = "modeliberal")
    private double modeliberal;

    @Arg(dest = "modeconservative")
    private double modeconservative;

    @Arg(dest = "outputdir")
    private String outputdir;

    private int node;

    private String descriptor;

    @Arg(dest = "connectpansim")
    private boolean connectpansim;

    @Arg(dest = "writegraph")
    private boolean writegraph;

    @Arg(dest = "suppresscalculations")
    private boolean suppresscalculations;

    @Arg(dest = "logproperties")
    private File logproperties;

    @Arg(dest = "diseaseseeddays")
    private Integer diseaseseeddays;

    @Arg(dest = "diseaseseednumber")
    private Integer diseaseseednumber;

    @Arg(dest = "additionaldiseasedays")
    private Integer additionaldiseasedays;

    @Arg(dest = "additionaldiseasedaysnum")
    private Integer additionaldiseasedaysnum;

    @Arg(dest = "fatigue")
    private Double fatigue;

    @Arg(dest = "fatiguestart")
    private long fatigueStart;

    private BetaDistribution liberalTrustDistribution;
    private BetaDistribution conservativeTrustDistribution;

    private boolean saveStateDataFrames = false;

    private Random random;

    public ArgParse(String[] args) {
        ArgumentParser p = getParser();
        try {
            p.parseArgs(args, this);
            verifyLogProperties();
            try {
                this.configuration = findFile(this.configuration);
                processConfigFile(p);
            } catch (IOException e) {
                throw new ArgumentParserException(e.getMessage(), p);
            }
        } catch (ArgumentParserException e) {
            p.handleError(e);
            System.exit(1);
        }
    }

    private void processConfigFile(ArgumentParser p) throws ArgumentParserException {
        TomlParseResult result = null;
        try {
            result = Toml.parse(Paths.get(this.configuration.getAbsolutePath()));
        } catch (IOException e) {
            throw new ArgumentParserException(e.getMessage(), p);
        }
        if(result.errors().size() > 0) {
            StringBuilder errorMessage = new StringBuilder();
            errorMessage.append("Error(s) encountered parsing configuration file:\n");
            result.errors().forEach(x -> {
                errorMessage.append(x.getMessage()).append(" ").append(x.position()).append("\n");
            });
            throw new ArgumentParserException(errorMessage.toString(), p);
        } else {
            try {
                if(result.contains("simulation.norms")) {
                    this.normFile = findFile(new File(result.getString("simulation.norms")));
                } else {
                    throw new Exception("No norms file specified (use key \"norms\"");
                }

                this.startdate = getDateFromTable(result, "simulation.startdate");
                if(result.contains("simulation.iterations")) {
                    LocalDate d = getDateFromTable(result, "simulation.iterations");
                    if(d == null) {
                        this.iterations = result.getLong("simulation.iterations");
                    } else {
                        this.iterations = ChronoUnit.DAYS.between(this.startdate, d) + 1;
                    }
                } else {
                    this.iterations = Long.MAX_VALUE;
                }

                if (result.contains("simulation.seed")) {
                    this.random = new Random(result.getLong("simulation.seed"));
                } else {
                    this.random = new Random();
                }

                if (result.contains("householdVotingAssignment"))  {
                    this.householdVotingAssignments = new ArrayList<File>();
                    TomlArray arr = result.getArray("householdVotingAssignment");
                    for(int i = 0; i < arr.size(); i++) {
                        this.householdVotingAssignments.add(findFile(new File(arr.getString(i))));
                    }
                }

                if(result.contains("infectionseeding.perday") && this.diseaseseednumber == null) {
                    this.diseaseseednumber = result.getLong("infectionseeding.perday").intValue();
                }
                if(result.contains("infectionseeding.days") && this.diseaseseeddays == null) {
                    this.diseaseseeddays = result.getLong("infectionseeding.days").intValue();
                }
                if(result.contains("infectionseeding.additional_every_other_days") && this.additionaldiseasedays == null) {
                    this.additionaldiseasedays = result.getLong("infectionseeding.additional_every_other_days").intValue();
                }
                if(result.contains("infectionseeding.additional_every_other_days_num") && this.additionaldiseasedaysnum == null) {
                    this.additionaldiseasedaysnum = result.getLong("infectionseeding.additional_every_other_days_num").intValue();
                }
                if(result.contains("savestate")) {
                    this.saveStateDataFrames = result.getBoolean("savestate");
                }

                this.descriptor = result.getString("output.descriptor");
                this.node = (int) result.getLong("output.node", () -> -1);

                TomlTable table = result.getTable("counties");

                for (String s : table.keySet()) {
                    this.counties.add(new ConfigModel(this, s, table.getTable(s)));
                }
            } catch (Exception e) {
                throw new ArgumentParserException(e.getMessage(), p);
            }
        }
    }

    private LocalDate getDateFromTable(TomlParseResult parseResult, String dottedKey) {
        if(parseResult.contains(dottedKey)) {
            if(parseResult.isLocalDate(dottedKey)) {
                return parseResult.getLocalDate(dottedKey);
            } else if (parseResult.isString(dottedKey)) {
                return LocalDate.parse(parseResult.getString(dottedKey), DateTimeFormatter.ISO_DATE);
            }
        }
        return null;
    }

    @Nullable public List<File> getHouseholdVotingAssignments() {
        return householdVotingAssignments;
    }

    public List<ConfigModel> getCounties() {
        return counties;
    }

    public File getNormFile() {
        return normFile;
    }

    public Random getSystemWideRandom() {
        return random;
    }

    public BetaDistribution getLiberalTrustDistribution() {
        if (this.liberalTrustDistribution == null)
                this.liberalTrustDistribution = Methods.getBetaDistribution(this.random, this.modeliberal);
        return this.liberalTrustDistribution;
    }

    public BetaDistribution getConservativeTrustDistribution() {
        if (this.conservativeTrustDistribution == null)
            this.conservativeTrustDistribution = Methods.getBetaDistribution(this.random, this.modeconservative);
        return this.conservativeTrustDistribution;
    }

    public Double getFatigue() {
        return fatigue;
    }

    public long getFatigueStart() {
        return fatigueStart;
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

    public boolean writeGraph() {
        return writegraph;
    }

    public boolean suppressCalculations() {
        return suppresscalculations;
    }

    public boolean saveStateDataFrames() {
        return saveStateDataFrames;
    }

    public int getThreads() {
        return threads;
    }

    public long getIterations() {
        return iterations;
    }

    public LocalDate getStartdate() {
        return startdate;
    }

    public String getOutputDir() {
        return outputdir;
    }

    public boolean isDiseaseSeeding() {
        return diseaseseeddays + diseaseseednumber > 0;
    }

    public int getDiseaseSeedDays() {
        return diseaseseeddays == null ? 0 : diseaseseeddays;
    }

    public int getDiseaseSeedNumAgentsPerDay() {
        return diseaseseednumber == null ? 0 : diseaseseednumber;
    }

    public Integer getAdditionalEveryOtherDays() {
        return diseaseseednumber == null && additionaldiseasedaysnum == null ? null : this.additionaldiseasedays;
    }

    public Integer getAdditionalDiseaseSeedNumber() {
        if (this.additionaldiseasedays == null) {
            return null;
        } else {
            return this.additionaldiseasedaysnum == null ? this.diseaseseednumber : this.additionaldiseasedaysnum;
        }
    }

    public static File findFile(File f) throws FileNotFoundException {
        File existingFile = null;
        if(f.getAbsoluteFile().exists()) {
            existingFile = f;
        } else if (!f.isAbsolute()) {
            URL r = ArgParse.class.getClassLoader().getResource(f.toString());
            if (r != null) {
                f = new File(r.getFile()).getAbsoluteFile();
                if (f.exists())
                    existingFile = f;
            }
        }
        if(existingFile != null) {
            return existingFile;
        } else {
            throw new FileNotFoundException("File not found: " + f.toString());
        }
    }

    private void verifyLogProperties() {
        if(this.logproperties != null) {
            try {
                this.logproperties = findFile(this.logproperties);
                InputStream stream = new FileInputStream(this.logproperties);
                LogManager.getLogManager().readConfiguration(stream);
                LOGGER.log(Level.INFO, "Properties file for logger loaded");
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

        parser.addArgument("--config")
                .type(File.class)
                .required(true)
                .dest("configuration")
                .help("Specify the TOML configuration file");

        ArgumentGroup behaviorCalibration = parser.addArgumentGroup("Behavior Calibration")
                .description("Arguments used for calibrating the behavior model");

        behaviorCalibration.addArgument("--mode-liberal", "-ml")
                .required(true)
                .type(double.class)
                .dest("modeliberal")
                .help("The mode of the government attitude distribution of liberal voting agents");

        behaviorCalibration.addArgument("--mode-conservative", "-mc")
                .required(true)
                .type(double.class)
                .dest("modeconservative")
                .help("The mode of the government attitude distribution of liberal voting agents");

        behaviorCalibration.addArgument("--fatigue")
                .required(false)
                .type(double.class)
                .dest("fatigue")
                .setDefault(0)
                .help("The fatigue factor related to trust of the agent. After `fatigue-start`, this factor will be subtracted from the agent's prior trust every day");

        behaviorCalibration.addArgument("--fatigue-start")
                .required(false)
                .type(long.class)
                .setDefault(0)
                .dest("fatiguestart")
                .help("When (time step) to start decreasing the agent's trust using the fatigue factor");

        ArgumentGroup diseaseCalibration = parser.addArgumentGroup("Disease Calibration")
                .description("Arguments used for calibrating the disease model");

        diseaseCalibration.addArgument("--disease-seed-days")
                .required(false)
                .type(int.class)
                .dest("diseaseseeddays")
                .help("The number of days at the beginning of simulation to seed the --disease-seed-number of agents with an infected state." +
                        " If this argument is also specified in the TOML configuration, the CLI value takes precedent. If no value is specified," +
                        " no agents are seeded with the disease");

        diseaseCalibration.addArgument("--disease-seed-number")
                .required(false)
                .type(int.class)
                .dest("diseaseseednumber")
                .help("The number of agents at the beginning of simulation to seed during the initial --disease-seed-days with an infected state." +
                        " If this argument is also specified in the TOML configuration, the CLI value takes precedent. If no value is specified," +
                        " no agents are seeded with the disease");

        diseaseCalibration.addArgument("--disease-seed-additional-frequency")
                .required(false)
                .type(Integer.class)
                .dest("additionaldiseasedays")
                .help("A number of days between additional seeding of infectious agents. After initial seeding has finished, " +
                        "an additional --disease-seed-number of agents will be seeded with an infected state every " +
                        "--disease-seed-additional-frequency days. E.g., if the initial number of days is 5, and this value is " +
                        "10, agents will be seeded on the 1th, 2nd, 3th, 4th, 5th, 10th, 20th, 30th etc days of the simulation." +
                        " If this argument is also specified in the TOML configuration, the CLI value takes precedent. If no value is specified," +
                        " no agents are seeded with the disease after the initial seeding");

        diseaseCalibration.addArgument("--disease-seed-additional-number")
                .required(false)
                .type(Integer.class)
                .dest("additionaldiseasedaysnum")
                .help("The number of agents to seed during additional seeding, " +
                        "an additional --disease-seed-additional-number (or --disease-seed-number if this argument is not set) " +
                        "of agents will be seeded with an infected state every " +
                        "--disease-seed-additional-frequency days. E.g., if the initial number of days is 5, and this value is " +
                        "10, agents will be seeded on the 1th, 2nd, 3th, 4th, 5th, 10th, 20th, 30th etc days of the simulation." +
                        " If this argument is also specified in the TOML configuration, the CLI value takes precedent. If no value is specified," +
                        " no agents are seeded with the disease after the initial seeding");

        ArgumentGroup optimization = parser.addArgumentGroup("Runtime optimization");

        optimization.addArgument("--threads", "-t")
                .type(Integer.class)
                .required(false)
                .setDefault(8)
                .dest("threads")
                .help("Specify the number of threads to use for execution");

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

        optimization.addArgument("--write-graph", "-g")
                .type(Boolean.class)
                .required(false)
                .setDefault(false)
                .action(new StoreTrueArgumentAction())
                .dest("writegraph")
                .help("If this argument is passed, the program will, every time step, write the state of each agent," +
                        "as well as each visit-pair of agents who have been in the same location at the same time, to " +
                        "a file. This can be used to construct a network graph of interactions over time, but slows " +
                        "the simulation down significantly, and is more demanding on memory resources.");

        optimization.addArgument("--suppress-calculations")
                .type(Boolean.class)
                .required(false)
                .setDefault(false)
                .action(new StoreTrueArgumentAction())
                .dest("suppresscalculations")
                .help("Suppress all secondary calculations not required to run the simulation, such as calculation of" +
                        "the radius of gyration, visit averages, and everything that is only calculated to perform" +
                        "logging");

        optimization.addArgument("--output-dir", "-o")
                .type(String.class)
                .required(false)
                .dest("outputdir")
                .setDefault(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                .help("Specify a sub directory of \"output\" where output files of the behavior model will be stored." +
                        " By default, the current time code will be used");

        return parser;
    }
}
