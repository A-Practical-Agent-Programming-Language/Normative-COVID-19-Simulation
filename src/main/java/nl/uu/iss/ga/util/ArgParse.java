package main.java.nl.uu.iss.ga.util;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.annotation.Arg;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ArgParse {

    @Arg(dest = "personsfile")
    private File personsFile;

    @Arg(dest = "householdsfile")
    private File householdsFile;

    @Arg(dest = "activityfile")
    private List<File> activityFiles;

    @Arg(dest = "locationsfile")
    private File locationsfile;

    @Arg(dest = "seed")
    private long seed;

    @Arg(dest = "threads")
    private int threads;

    private Random random = null;

    public ArgParse(String[] args) {
        ArgumentParser p = getParser();
        try {
            p.parseArgs(args, this);
            verifyFiles();
        } catch (ArgumentParserException e) {
            p.handleError(e);
            System.exit(1);
        }
    }

    public File getPersonsFile() {
        return personsFile;
    }

    public File getHouseholdsFile() {
        return householdsFile;
    }

    public List<File> getActivityFiles() {
        return activityFiles;
    }

    public File getLocationsfile() {
        return locationsfile;
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

    private void verifyFiles() {
        try {
            List<File> files = new ArrayList<>();
            for (File activityFile : this.activityFiles) {
                File file = findFile(activityFile);
                files.add(file);
            }
            this.activityFiles = files;
            this.householdsFile = findFile(this.householdsFile);
            this.personsFile = findFile(this.personsFile);
            this.locationsfile = findFile(this.locationsfile);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            getParser().printHelp();
            System.exit(1);
        }
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

    private ArgumentParser getParser() {
        ArgumentParser parser = ArgumentParsers.newFor("2APL/SimpleEpiDemic Disease Simulation").build()
                .defaultHelp(true)
                .description("A ecological simulation environment for simulation of human acceptance of measures" +
                        " aimed at reducing spread of novel diseases");

        // TODO should be able to take list of arguments
        parser.addArgument("--personsfile", "-pf")
                .type(File.class)
                .required(true)
                .dest("personsfile")
                .help("Specify the location of the file containing the details of individual agents in the artificial population");

        parser.addArgument("--householdsfile", "-hf")
                .type(File.class)
                .required(true)
                .dest("householdsfile")
                .help("Specify the location of the file containing the details of the households in the artificial population");

        parser.addArgument("--activityfile", "-af")
                .type(File.class)
                .required(true)
                .dest("activityfile")
                .nargs("+")
                .help("Specify the location of the file(s) containing all the activities of all the agents in the artificial population");

        parser.addArgument("--locationsfile", "-lf")
                .type(File.class)
                .required(true)
                .dest("locationsfile")
                .help("Specify the location of the file containing all the activity locations of all the agents in the artificial population");

        parser.addArgument("--seed", "-s")
                .type(Long.TYPE)
                .required(false)
                .dest("seed")
                .setDefault(-1)
                .help("Specify a seed to use for random operations. Default is -1, indicating no seed is used");

        parser.addArgument("--threads", "-t")
                .type(Integer.class)
                .required(false)
                .setDefault(8)
                .dest("threads")
                .help("Specify the number of threads to use for execution");

        return parser;
    }

}
