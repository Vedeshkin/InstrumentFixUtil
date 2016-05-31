/**
 * Created by Valentin on 31.05.2016.
 */
public enum fields {
    SYMBOL(0),
    DATE(1),
    TIME(2),
    OPENPRICE(4),
    HIGHPRICE(5),
    LOWPRICE(6),
    CLOSEPRICE(7),
    VOLUME(8);
    private final int field;

    fields(int field) {
        this.field = field;
    }

    public int getField() {
        return field;
    }

}
