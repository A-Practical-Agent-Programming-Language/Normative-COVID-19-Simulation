package nl.uu.iss.ga.model.data.dictionary;

import nl.uu.iss.ga.model.data.dictionary.util.CodeTypeInterface;

public enum Gender implements CodeTypeInterface {
    MALE(1),
    FEMALE(2);

    private final int code;

    Gender(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
