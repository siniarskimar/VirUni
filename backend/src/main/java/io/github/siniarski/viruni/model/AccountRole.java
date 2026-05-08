package io.github.siniarski.viruni.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

@Entity
public class AccountRole {
    public static final AccountRole USER = new AccountRole(1, "ROLE_USER");
    public static final AccountRole TEACHER = new AccountRole(2, "ROLE_TEACHER");
    public static final AccountRole ADMIN = new AccountRole(3, "ROLE_ADMIN");

    @Id
    private long id;

    @Column(nullable = false, unique = true)
    private String name;

    protected AccountRole() {}

    private AccountRole(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public static AccountRole[] values() {
        return new AccountRole[] {USER, TEACHER, ADMIN};
    }

    public static AccountRole valueOf(String name) {
        return valueOfOptional(name)
                .orElseThrow(IllegalArgumentException::new);
    }

    public static Optional<AccountRole> valueOfOptional(String name) {
        return Arrays.stream(values())
                .filter(r -> r.getName().equals(name))
                .findFirst();
    }

    public static AccountRole valueOf(long id) {
        return Arrays.stream(values())
                .filter(r -> r.getId() == id)
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AccountRole that)) return false;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Component
    public static class AccountRoleToStringConverter implements AttributeConverter<AccountRole, String> {
        @Override
        public String convertToDatabaseColumn(AccountRole attribute) {
            return attribute.getName();
        }

        @Override
        public AccountRole convertToEntityAttribute(String dbData) {
            return AccountRole.valueOf(dbData.toUpperCase());
        }
    }

    @Component
    public static class StringToAccountRoleConverter implements Converter<String, AccountRole> {
        @Override
        public AccountRole convert(String source) {
            return AccountRole.valueOf(source.toUpperCase());
        }
    }
}
