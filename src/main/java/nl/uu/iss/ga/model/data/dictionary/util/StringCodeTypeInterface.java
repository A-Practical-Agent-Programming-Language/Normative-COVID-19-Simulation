package nl.uu.iss.ga.model.data.dictionary.util;

public interface StringCodeTypeInterface extends CodeTypeInterface {

    String getStringCode();

    static <E extends Enum<E> & StringCodeTypeInterface> E valueOfCodeString(Class<E> eClass, String code) {
        for ( final E enumConstant : eClass.getEnumConstants() ) {
            if (code.equals(enumConstant.getStringCode())) {
                return enumConstant;
            }
        }
        return null;
    }

    static int parseStringcode(String stringcode) {
        return parseStringcode(stringcode, -1);
    }

    static int parseStringcode(String stringcode, int defaultInteger) {
        try {
            return Integer.parseInt(stringcode);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    static <T extends Enum<T> & StringCodeTypeInterface> T parseAsEnum(Class<T> type, String codeValue) {
        return StringCodeTypeInterface.valueOfCodeString(type, codeValue);
    }
}
