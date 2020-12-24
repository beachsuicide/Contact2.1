package service;

import dao.DataService;
import io.vavr.collection.List;
import models.Record;
import models.User;

public class ContactsManager {
    private final DataService dao;

    private ContactsManager(DataService dao) {
        this.dao = dao;
    }

    public static ContactsManager getInstance(DataService dao) {
        return new ContactsManager(dao);
    }

    public void addUser(User user) {
        dao.addUser(user);
    }


    public void addRecord(int userID, Record record) {
        var user = getUserById(userID);
        var updatedRecords = user
            .records()
            .append(record);
        user = user.toBuilder()
            .records(updatedRecords)
            .build();
        dao.update(user);
    }

    public void addRecord(int userID, String name, List<String> numbers) {
        var record = Record.builder().name(name).numbers(numbers).build();
        addRecord(userID, record);
    }

    public void editUserName(int id, String name) {
        var user = getUserById(id);
        if (user != null) {
            dao.update(user.toBuilder().name(name).build());
        }
    }

    public void editRecordName(int userID, int pos, String newName) {
        var user = getUserById(userID);
        if (user != null) {
            var oldRecords = user.records();
            var editedRecord = user.records().get(pos).editName(newName);
            var editedRecordsList = oldRecords.removeAt(pos).append(editedRecord);
            var updatedUser = user.toBuilder()
                .records(editedRecordsList)
                .build();
            dao.update(updatedUser);
        }
    }

    public void editNumber(int userID, int recordPos, String number, int numberPos) {
        var user = getUserById(userID);
        if (user != null) {
            var oldRecords = user.records();
            var editedRecord = oldRecords.get(recordPos).editNumber(number, numberPos);
            var editedRecordsList = oldRecords.removeAt(recordPos).append(editedRecord);
            dao.update(user.toBuilder().records(editedRecordsList).build());
        }
    }

    public void removeRecord(int userID, int pos) {
        var user = getUserById(userID);
        if (user != null) {
            user = user.removeRecord(pos);
            dao.update(user);
        }
    }

    public void removeUser(int id) {
        dao.delete(id);
    }

    /**
     *
     * @param id user's id
     * @return user by that id or null if user with that id is not found
     */
    public User getUserById(int id) {
        return dao.getUserById(id);
    }

    /**
     *
     * @param userID user's id
     * @return user's sorted records list or null if user with that id is not found
     */
    public List<Record> getRecordsById(int userID) {
        var user = getUserById(userID);
        if (user != null)
            return getUserById(userID).records()
                .sortBy(Record::name);
        return null;
    }

    public List<User> getUsers() {
        return dao.getUsers();
    }
}