package dao.consoleDao;

import models.Record;
import dao.DataService;
import io.vavr.collection.List;
import models.User;

public class ConsoleDao implements DataService {
    private List<User> users;

    public ConsoleDao() {
        this.users = fillTestData();
    }

    @Override
    public List<User> getUsers() {
        return users;
    }

    @Override
    public void update(User user) {
        users = users.removeFirst(x -> x.id() == user.id()).append(user);
    }

    @Override
    public User getUserById(int id) {
        return users.filter(x -> x.id() == id).peek();
    }

    @Override
    public void delete(int id) {
        users = users.removeFirst(x -> x.id() == id);
    }

    @Override
    public void addUser(User user) {
        users = users.append(user);
    }

    private List<User> fillTestData() {
        int id = 0;
        return List.of(
            User.builder()
                .name("name1")
                .appendRecord(
                    Record.builder()
                        .name("Ivan Memov")
                        .addNumber("+79204685467")
                        .addNumber("+7923343334")
                        .build()
                )
                .appendRecord(
                    Record.builder()
                        .name("Alexander Medvedev")
                        .addNumber("+79213345454")
                        .addNumber("+79102103344")
                        .build()
                )
                .id(id++)
                .build(),

            User.builder()
                .name("name2")
                .appendRecord(
                    Record.builder()
                        .name("Anton Chayka")
                        .addNumber("8800553535")
                        .addNumber("891234546565")
                        .build()
                )
                .appendRecord(
                    Record.builder()
                        .name("Mikhail Evlakov")
                        .addNumber("+79213345454")
                        .addNumber("+79102103344")
                        .build()
                )
                .appendRecord(
                    Record.builder()
                        .name("Mikhail Zhvaneckiy")
                        .addNumber("+78900909876")
                        .addNumber("+77008789898")
                        .build()
                )
                .id(id++)
                .build(),

            User.builder()
                .name("name3")
                .appendRecord(
                    Record.builder()
                        .name("Glenn Quagmire")
                        .addNumber("+79204685467")
                        .build()
                )
                .appendRecord(
                    Record.builder()
                        .name("Peter Griffin")
                        .addNumber("+79213345454")
                        .build()
                )
                .id(id++)
                .build()
        );
    }
}
