package sixkiller.sample.repository;

import sixkiller.sample.domain.User;

import java.util.*;

/**
 * Created by ala on 9.5.16.
 */
public class TestUserRepository extends DummyAbstractRepository<User, String> implements UserRepository {

    Collection<User> userCollection = new ArrayList<>();

    @Override
    public Optional<User> findByUserName(String userName) {
        Objects.requireNonNull(userName);

        return new ArrayList<>(userCollection).stream().filter(user -> userName.equals(user.getUserName())).findAny();
    }

    @Override
    public User save(User entity) {
        if (entity.getId() == null) {
            entity.setId(IDGenerator.getNextId());
        }

        Optional<User> userOptional = userCollection.stream().filter(user -> entity.getUserName().equals(user.getUserName()))
                .findAny();

        if (userOptional.isPresent()) {
            userCollection.remove(userOptional.get());
        }

        userCollection.add(entity);

        return entity;
    }

    @Override
    public void delete(String id) {
        Objects.requireNonNull(id);

        userCollection.removeIf(user -> id.equals(user.getId()));
    }

    @Override
    public void delete(User entity) {
        Objects.requireNonNull(entity);

        delete(entity.getId());
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(userCollection);
    }
}
