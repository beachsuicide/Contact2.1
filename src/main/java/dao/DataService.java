package dao;

import io.vavr.collection.List;
import models.User;


public interface DataService {

    List<User> getUsers();

    void update(User user);

    User getUserById(int id);

    void delete(int id);

    void addUser(User user);
}
