package models;

import io.vavr.collection.List;

public record User(String name, List<Record> records, int id, Builder builderInstance) {

    public static class Builder {
        private String name;
        private List<Record> records = List.empty();
        private int id;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder appendRecord(Record record) {
            records = records.append(record);
            return this;
        }

        public Builder records(List<Record> records) {
            this.records = records;
            return this;
        }

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public User build() {
            records = records.sortBy(Record::name);
            return new User(name, records, id, this);
        }
    }

    public Builder toBuilder() {
        return builderInstance;
    }

    public static Builder builder() {
        return new Builder();
    }

    public User removeRecord(int pos) {
        return builderInstance.records(records.removeAt(pos)).build();
    }

    @Override
    public String toString() {
        return "User{" +
            "name='" + name + '\'' +
            ", records=" + records +
            ", id=" + id +
            "} ";
    }
}
