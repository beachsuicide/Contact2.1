package dao.database;

import dao.DataService;
import io.vavr.collection.List;
import models.Record;
import models.User;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

public class PostgresDataService implements DataService {
    private final ConnectionBuilder connectionBuilder = new PostgreConnection();
    private final String QUERIES_PATH = "src/main/resources/queries.properties";
    private final Properties queries = new Properties();

    public PostgresDataService() throws IOException, ClassNotFoundException {
        queries.load(new FileInputStream(QUERIES_PATH));
    }

    @Override
    public List<User> getUsers() {
        List<User> users = List.empty();
        try (
            var connection = connectionBuilder.getConnection();
            var usersSet = connection.createStatement().executeQuery(queries.getProperty("SELECT_USERS"))
        ) {

            while (usersSet.next()) {
                users = users.append(getUserById(usersSet.getInt("user_id")));
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return users;
    }

    @Override
    public void update(User user) {
        try (
            var connection = connectionBuilder.getConnection();
            var userStm = connection.prepareStatement(queries.getProperty("UPDATE_USER"));
            var recordStm = connection.prepareStatement(queries.getProperty("INSERT_RECORDS"));
            var numberStm = connection.prepareStatement(queries.getProperty("INSERT_NUMBERS"))
        ) {
            userStm.setString(1, user.name());
            userStm.setInt(2, user.id());

            //execute
            userStm.executeUpdate();

            //delete records and numbers
            deleteRecords(user.id());
            deleteNumbers(getRecordsId(user.id()));

            //insert records and numbers
            for (Record record : user.records()) {
                recordStm.setInt(1, user.id());
                recordStm.setString(2, record.name());
                recordStm.addBatch();

                for (String number : record.numbers()) {
                    numberStm.setString(1, number);
                    numberStm.setInt(2, record.id());
                    numberStm.addBatch();
                }
            }
            recordStm.executeUpdate();
            numberStm.executeUpdate();
        } catch (SQLException ex) {
            //
        }
    }

    private void fillRecordsStm(User owner, PreparedStatement recStm, PreparedStatement numbersStm) throws SQLException {
        for (Record record : owner.records()) {
            recStm.setInt(1, owner.id());
            recStm.setString(2, record.name());
            recStm.addBatch();

            fillNumbersStm(record, numbersStm);
        }
    }

    private void fillNumbersStm(Record person, PreparedStatement numbersStm) throws SQLException {
        for (String number : person.numbers()) {
            numbersStm.setString(1, number);
            numbersStm.setInt(2, person.id());

            numbersStm.addBatch();
        }
    }

    @Override
    public User getUserById(int userId) {
        User user = null;
        try (
            var connection = connectionBuilder.getConnection();
            var userStm = connection.prepareStatement(queries.getProperty("SELECT_USER_BY_ID"))
        ) {
            //get user
            userStm.setInt(1, userId);
            var rsUser = userStm.executeQuery();

            //get records by user id
            var recordsStm = connection.prepareStatement(queries.getProperty("SELECT_RECORDS"));
            recordsStm.setInt(1, userId);
            var rsRecords = recordsStm.executeQuery();

            user = parseUser(rsUser, rsRecords, connection);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return user;
    }

    private User parseUser(ResultSet rsUser, ResultSet rsRecords, Connection connection) throws SQLException {
        User user = null;
        while (rsUser.next()) {
            user = User.builder()
                .id(rsUser.getInt("user_id"))
                .name(rsUser.getString("user_name"))
                .records(parseRecords(rsRecords, connection))
                .build();
        }
        return user;
    }

    private List<Record> parseRecords(ResultSet rsRecords, Connection connection) throws SQLException {
        List<Record> records = List.empty();
        while (rsRecords.next()) {
            var numbersStm = connection.prepareStatement(queries.getProperty("SELECT_NUMBERS"));
            numbersStm.setInt(1, rsRecords.getInt("record_id"));
            var numbersSet = numbersStm.executeQuery();

            records = records.append(Record.builder()
                .id(rsRecords.getInt("record_id"))
                .name(rsRecords.getString("record_name"))
                .numbers(parseNumbers(numbersSet))
                .build());
        }
        return records;
    }

    private List<String> parseNumbers(ResultSet rsNumbers) throws SQLException {
        List<String> numbers = List.empty();
        while (rsNumbers.next()) {
            numbers = numbers.append(rsNumbers.getString("phone_number"));
        }
        return numbers;
    }

    @Override
    public void delete(int userId) {
        try (var connection = connectionBuilder.getConnection();
             var stm = connection.prepareStatement(queries.getProperty("DELETE_USER"))
        ) {
            stm.setInt(1, userId);
            stm.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Override
    public void addUser(User user) {
        try (
            var connection = connectionBuilder.getConnection();
            var userStm = connection.prepareStatement(queries.getProperty("INSERT_USER"));
            var numberStm = connection.prepareStatement(queries.getProperty("INSERT_NUMBERS"))
        ) {
            userStm.setString(1, user.name());

            //execute
            userStm.executeUpdate();

            //insert records and numbers
            for (Record record : user.records()) {
                var recordStm = connection.prepareStatement(queries.getProperty("INSERT_RECORDS"), new String[]{"record_id"});
                recordStm.setInt(1, user.id());
                recordStm.setString(2, record.name());
                recordStm.executeUpdate();


                var id = recordStm.getGeneratedKeys();
                while (id.next()) {
                    for (String number : record.numbers()) {
                        numberStm.setString(1, number);
                        numberStm.setInt(2, Integer.parseInt(id.getString(1)));
                        numberStm.addBatch();
                    }
                }

                numberStm.executeBatch();
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private void deleteRecords(int userId) {
        try (var connection = connectionBuilder.getConnection();
             var stm = connection.prepareStatement(queries.getProperty("DELETE_RECORDS"))
        ) {
            stm.setInt(1, userId);
            stm.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private ArrayList<Integer> getRecordsId(int userId) {
        var recordsId = new ArrayList<Integer>();
        try (var connection = connectionBuilder.getConnection();
             var stm = connection.prepareStatement(queries.getProperty("SELECT_RECORDS_ID"))
        ) {
            stm.setInt(1, userId);
            var recordsSet = stm.executeQuery();
            while (recordsSet.next())
                recordsId.add(recordsSet.getInt(1));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return recordsId;
    }

    private void deleteNumbers(ArrayList<Integer> recordsId) {
        try (var connection = connectionBuilder.getConnection()) {
            for (Integer recordId : recordsId) {
                var stm = connection.prepareStatement(queries.getProperty("DELETE_NUMBERS"));
                stm.setInt(1, recordId);
                stm.executeUpdate();
                stm.close();
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}