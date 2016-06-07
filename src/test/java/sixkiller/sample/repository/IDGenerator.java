package sixkiller.sample.repository;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ala on 9.5.16.
 */
public class IDGenerator {

    private static AtomicInteger atomicInteger = new AtomicInteger(0);

    public static String getNextId() {
        return String.valueOf(atomicInteger.incrementAndGet());
    }

}
