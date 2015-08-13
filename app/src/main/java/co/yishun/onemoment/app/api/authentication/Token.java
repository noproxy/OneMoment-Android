package co.yishun.onemoment.app.api.authentication;

/**
 * Authentication Token
 * <p>
 * Created by Carlos on 2015/8/5.
 */
interface Token {
    /**
     * @return the encoded value of token
     */
    String value();

    /**
     * @return origin value
     */
    String origin();
}
