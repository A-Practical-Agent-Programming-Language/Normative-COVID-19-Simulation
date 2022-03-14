package nl.uu.iss.ga.util;

import nl.uu.cs.iss.ga.sim2apl.core.logging.Loggable;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Java2APLLogger extends Loggable {

    @Override
    public void log(Class<?> c, Level level, Object message) {
        Logger.getLogger(c.getName()).log(level, message.toString());
    }

    @Override
    public void log(Class<?> c, Object message) {
        Logger.getLogger(c.getName()).log(Level.INFO, message.toString());
    }

    @Override
    public void log(Class<?> c, Exception ex) {
        Logger.getLogger(c.getName()).log(Level.SEVERE, ex.getMessage(), ex);
    }

    @Override
    public void log(Class<?> c, Level level, Exception ex) {
        Logger.getLogger(c.getName()).log(level, ex.getMessage(), ex);
    }
}
