package sixkiller.sample.restapi.exception;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String userName) {

        super(
                String.format("User %s not found.", userName)
        );

    }
}
