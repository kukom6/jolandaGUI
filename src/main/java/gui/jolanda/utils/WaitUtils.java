package gui.jolanda.utils;

/**
 * @author mhajas
 */
public class WaitUtils {

    public static void pause(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
