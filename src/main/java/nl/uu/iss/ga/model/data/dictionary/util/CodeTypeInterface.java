package nl.uu.iss.ga.model.data.dictionary.util;

public interface CodeTypeInterface {

    int getCode();

    static <E extends Enum<E> & CodeTypeInterface> E valueOf(Class<E> eClass, int code) {
        for ( final E enumConstant : eClass.getEnumConstants() ) {
            if ( code == enumConstant.getCode()) {
                return enumConstant;
            }
        }
        return null;
    }

    static <T extends Enum<T> & CodeTypeInterface> T parseAsEnum(Class<T> type, int codeValue) {
        return CodeTypeInterface.valueOf(type, codeValue);
    }

    static <T extends Enum<T> & CodeTypeInterface> T parseAsEnum(Class<T> type, String codeValue) {
        return parseAsEnum(type, ParserUtil.parseAsInt(codeValue));
    }
}
