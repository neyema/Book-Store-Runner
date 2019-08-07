package bgu.spl.mics;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class FutureTest {

    private Future<Integer> test;

    @Before
    public void setUp() throws Exception {
        test = new Future<>();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void get() {
        test.resolve(1);
        assertEquals((Integer)1, test.get());
    }

    @Test
    public void resolve() {
        test.resolve(1);
        assertEquals((Integer)1, test.get()); //after resolving
        assertTrue(test.isDone());
    }

    @Test
    public void isDone() {
        assertFalse(test.isDone());
        test.resolve(1);
        assertTrue(test.isDone());
    }

    @Test
    public void get_withTimeout() {
        assertNull(test.get(1, TimeUnit.SECONDS));
        test.resolve(1);
        assertEquals((Integer)1, test.get(1, TimeUnit.SECONDS));
    }
}