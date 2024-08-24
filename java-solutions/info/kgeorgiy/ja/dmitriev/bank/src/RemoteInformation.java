package info.kgeorgiy.ja.dmitriev.bank.src;

/**
 * Utility class for package and sub-pockets.
 * Defines the port and host to connect to.
 *
 * @author Dmitriev Vladislav (bitree2004@yandex.ru)
 * @since 21
 */
public final class RemoteInformation {
    /**
     * Defines the port to connect to.
     */
    public static final Integer BANK_PORT = 8888;
    /**
     * Defines the host to connect to.
     */
    public static final String BANK_HOST = "localhost";

    /**
     * Defines the name to associate with the remote reference of BANK to connect to.
     */
    public static String DEFAULT_BANK = String.format("//%s:%s/bank", BANK_HOST, BANK_PORT);
    /**
     * Utility class.
     */
    private RemoteInformation() {
    }
}
