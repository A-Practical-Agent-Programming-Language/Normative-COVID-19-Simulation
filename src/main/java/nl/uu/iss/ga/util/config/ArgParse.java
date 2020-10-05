package main.java.nl.uu.iss.ga.util.config;

import main.java.nl.uu.iss.ga.simulation.environment.AgentStateMap;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.annotation.Arg;
import net.sourceforge.argparse4j.impl.action.StoreTrueArgumentAction;
import net.sourceforge.argparse4j.inf.ArgumentGroup;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import org.tomlj.Toml;
import org.tomlj.TomlParseResult;
import org.tomlj.TomlTable;

import java.io.*;
import java.net.URL;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.LongSupplier;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class ArgParse {
    private static final Logger LOGGER = Logger.getLogger(ArgParse.class.getName());

    @Arg(dest = "configuration")
    private File configuration;

    private List<ConfigModel> counties = new ArrayList<>();

    private File normFile;

    @Arg(dest = "threads")
    private int threads;

    private long iterations;

    private LocalDate startdate;

    private double modeliberal;

    private double modeconservative;

    private int node;

    private String descriptor;

    @Arg(dest = "connectpansim")
    private boolean connectpansim;

    @Arg(dest = "logproperties")
    private File logproperties;

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

                if(result.contains("calibration.modeLiberal") && result.contains("calibration.modeConservative")) {
                    this.modeconservative = result.getDouble("calibration.modeConservative");
                    this.modeliberal = result.getDouble("calibration.modeLiberal");
                } else {
                    throw new Exception("Specify both calibration.modeLiberal and calibration.modeConservative as a double value");
                }

                this.descriptor = result.getString("output.descriptor");
                this.node = (int) result.getLong("output.node", new LongSupplier() {
                    @Override
                    public long getAsLong() {
                        return -1;
                    }
                });

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

    public List<ConfigModel> getCounties() {
        return counties;
    }

    public File getNormFile() {
        return normFile;
    }

    public Random getSystemWideRandom() {
        return random;
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

    public int getThreads() {
        return threads;
    }

    public long getIterations() {
        return iterations;
    }

    public LocalDate getStartdate() {
        return startdate;
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

        return parser;
    }
}
