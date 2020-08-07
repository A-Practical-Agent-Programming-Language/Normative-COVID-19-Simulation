package main.java.nl.uu.iss.ga.util;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.annotation.Arg;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;

public class ArgParse {

    @Arg(dest = "personsfile")
    private File personsFile;

    @Arg(dest = "householdsfile")
    private File householdsFile;

    @Arg(dest = "activityfile")
    private File activityFile;

    @Arg(dest = "seed")
    private long seed;

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

    public File getActivityFile() {
        return activityFile;
    }

    public long getSeed() {
        return seed;
    }

    private void verifyFiles() {
        try {
            this.activityFile = findFile(this.activityFile);
            this.householdsFile = findFile(this.householdsFile);
            this.personsFile = findFile(this.personsFile);
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
            URL r = getClass().getClassLoader().getResource(f.getName());
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
                .help("Specify the location of the file containing all the activities of all the agents in the artificial population");

        parser.addArgument("--seed", "-s")
                .type(Long.TYPE)
                .required(false)
                .dest("seed")
                .setDefault(42)
                .help("Specify a seed to use for random operations");

        return parser;
    }

}
