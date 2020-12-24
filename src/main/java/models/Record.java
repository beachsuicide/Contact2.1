package models;

import io.vavr.collection.List;

public record Record(String name, List<String> numbers, int id, Builder builderInstance) {

    public Record editName(String name) {
        return new Record(name, this.numbers(), id, builderInstance);
    }

    public Record editNumber(String newNumber, int numberPosition) {
        return new Record(
            name,
            numbers.removeAt(numberPosition).append(newNumber),
            id, builderInstance);
    }

    public static class Builder {
        private String name;
        private List<String> numbers = List.empty();
        private int id = -1;

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder addNumber(String number) {
            this.numbers = numbers.append(number);
            return this;
        }

        public Builder numbers(List<String> numbers) {
            this.numbers = numbers;
            return this;
        }

        public Record build() {
            numbers = numbers.sorted();
            return new Record(name, numbers, id, this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return builderInstance;
    }

    @Override
    public String toString() {
        return "Record{" +
            "name='" + name + '\'' +
            ", numbers=" + numbers + "}";
    }
}