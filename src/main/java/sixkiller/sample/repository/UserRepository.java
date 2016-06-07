package sixkiller.sample.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import sixkiller.sample.domain.User;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByUserName(String userName);

}
