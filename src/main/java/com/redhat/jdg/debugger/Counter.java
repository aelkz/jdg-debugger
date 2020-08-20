package com.redhat.jdg.debugger;

import java.io.IOException;
import java.io.Serializable;
import org.jboss.logging.Logger;

class Counter implements Serializable {
    private static final Logger LOGGER = Logger.getLogger(Counter.class);

    private int counter = 0;
    
    Counter() {
        LOGGER.debug("method called: public Counter()");
        LOGGER.info("counter serializable object is being created");
    }
    
    void increment() {
        this.counter++;
    }
    
    int getValue() {
        return this.counter;
    }
    
    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        LOGGER.debug("method called: private void writeObject(java.io.ObjectOutputStream out) throws IOException");
        LOGGER.info("counter serialization is under way:");
        LOGGER.info("counter = " + this.counter);

        out.defaultWriteObject();
    }
    
    private void readObject(java.io.ObjectInputStream in)
        throws IOException, ClassNotFoundException {

        LOGGER.debug("method called: private void readObject(java.io.ObjectInputStream in)");

        in.defaultReadObject();

        LOGGER.info("counter object have been deserialized!");
        LOGGER.info("counter = " + counter);
    }

}
