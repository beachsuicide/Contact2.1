package ui.consoleUI;

import dao.consoleDao.ConsoleDao;
import dao.database.PostgresDataService;
import models.Record;
import models.User;
import service.ContactsManager;

import java.io.IOException;

public class Main {
    static ContactsManager mngr = ContactsManager.getInstance(new ConsoleDao());

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        mngr = ContactsManager.getInstance(new PostgresDataService());
        var lol = new PostgresDataService();
//        lol.govno();
        mngr.addUser(User.builder()
            .name("Chaika10110")
            .appendRecord(
                Record.builder()
                    .name("Ivan 1111")
                    .addNumber("+79204685467")
                    .addNumber("+7923343334")
                    .build()
            )
            .appendRecord(
                Record.builder()
                    .name("Alexander Govnov")
                    .addNumber("+79213345454")
                    .addNumber("+79102103344")
                    .build()
            )
            .appendRecord(
                Record.builder()
                    .name("Alex Shlixter")
                    .addNumber("+808099883")
                    .addNumber("+884384838438")
                    .build()
            )
            .id(3)
            .build());
//        System.out.println(mngr.getUserById(2));
//        mngr.removeUser(2);
//        editRecordName();
//        editNumber();
//        deleteUser();
    }

    private static void editRecordName() {
        System.out.println("Before editing: ");
        mngr.getRecordsById(0).forEach(System.out::println);
        System.out.println("\nAfter editing name: ");
        mngr.editRecordName(0, 0, "Юрий Степанович Рыбников");
        mngr.getRecordsById(0).forEach(System.out::println);
    }

    private static void editNumber() {
        System.out.println("Before editing: ");
        mngr.getRecordsById(0).forEach(System.out::println);
        System.out.println("\nAfter editing name: ");
        mngr.editNumber(0, 0, "900", 1);
        mngr.getRecordsById(0).forEach(System.out::println);
    }

    private static void deleteUser() {
        System.out.println("Before deleting user: ");
        mngr.getUsers().forEach(System.out::println);
        mngr.removeUser(1);
        System.out.println("\nAfter deleting user: ");
        mngr.getUsers().forEach(System.out::println);
    }
}
