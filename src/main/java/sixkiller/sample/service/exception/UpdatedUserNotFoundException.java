package sixkiller.sample.service.exception;

/**
 * Created by ala on 17.5.16.
 */
public class UpdatedUserNotFoundException extends Exception {

    String userName;

    public UpdatedUserNotFoundException(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

}
