package datastructures;

import javafx.util.Pair;

import java.util.*;

/**
 * A data structure which maintains it's values in a tabular form.
 * Each element in the Table is associated with a Row and a Column.
 * Table supports {@code null}s for values.
 * <p>
 * All Column headers are unique
 * <p>
 * All Row identifiers are unique
 * <p>
 * Backed by a {@link HashMap}, whose Key is a {@link Pair}.
 * Each entry for the table is logically stored in the HashMap, with the Key {@code Pair<Column, Row>}
 *
 * @param <V> The type of values stored in this Table.
 * @param <C> The type of Column headers for this Table.
 * @param <R> The type of identifiers for Rows of this Table.
 * @author Hari Krishnan
 */
public class PairedHashMapTable<V, C, R> implements Table<V, C, R> {

    private final Map<Pair<C, R>, V> map;
    private final Set<C> headers;
    private final Set<R> identifiers;

    public PairedHashMapTable() {
        map = new HashMap<>();
        headers = new HashSet<>();
        identifiers = new HashSet<>();
    }


    /**
     * Adds a new Column to this Table, and sets the value for all existing Rows to {@code null}
     * If this Table already contains a Column with specified header, the call leaves the Table
     * unchanged and returns {@code false}
     *
     * @param header The Column header to add to this Table.
     * @return {@code true} if this Table did not already contain a Column with the specified header.
     */
    @Override
    public boolean addColumn(C header) {
        return headers.add(header);
    }

    /**
     * Adds a new Column to this Table, and sets the value for existing Rows to the value in the Map,
     * keyed by it's identifier. Rows missing entries in the map is associated with {@code null}.
     * Additional keys(other than known Row identifiers) are ignored.
     * <p>
     * This implementation iterates over the {@link Map.Entry}.
     * Performance is improved if additional keys are not passed.
     * </p>
     *
     * @param header The Column header to add to this Table
     * @param values A mapping between existing Row identifiers and their values under this Column.
     *               Additional keys are ignored, missing keys are assumed to have value {@code null}.
     * @throws DuplicateIdentifierException If this Table already contained a Column with the specified header.
     */
    @Override
    public void addColumn(C header, Map<R, V> values) throws DuplicateIdentifierException {
        if (headers.add(header)) {
            values.forEach((identifier, value) -> {
                if (identifiers.contains(identifier))
                    set(header, identifier, value);
            });
        } else {
            throw new DuplicateIdentifierException("Column '" + header + "' already exists in this Table!");
        }

    }

    /**
     * Adds a new Row to this Table, and sets the value for all existing Columns to {@code null}
     * If this Table already contains a Row with specified identifier, the call leaves the Table
     * unchanged and returns {@code false}
     *
     * @param identifier The Row identifier to add to this Table.
     * @return {@code true} if this Table did not already contain a Row with the specified identifier.
     */
    @Override
    public boolean addRow(R identifier) {
        return identifiers.add(identifier);
    }

    /**
     * Adds a new Row to this Table, and sets the value for existing Columns to the value in the Map,
     * keyed by it's header. Columns missing entries in the map is associated with {@code null}.
     * Additional keys(other than known Column headers) are ignored.
     * <p>
     * This implementation iterates over the {@link Map.Entry}.
     * Performance is improved if additional keys are not passed.
     * </p>
     *
     * @param identifier The Row identifier to add to this Table
     * @param values     A mapping between existing Column headers and their values for this Row.
     *                   Additional keys are ignored, missing keys are assumed to have value {@code null}.
     * @throws DuplicateIdentifierException If this Table already contained a Row with the specified identifier.
     */
    @Override
    public void addRow(R identifier, Map<C, V> values) throws DuplicateIdentifierException {
        if (identifiers.add(identifier)) {
            values.forEach((header, value) -> {
                if (headers.contains(header))
                    set(header, identifier, value);
            });
        } else {
            throw new DuplicateIdentifierException("Row '" + identifier + "' already exists in this Table!");
        }
    }

    /**
     * Returns a mapping between all known Columns and it's value for given Row identifier.
     * Missing values are mapped to {@code null}.
     *
     * @param identifier The identifier for the Row to get.
     * @return a mapping between all known Columns and it's value for the identifier.
     * @throws NoSuchElementException if this Table does not contain a Row with the specified identifier.
     */
    @Override
    public Map<C, V> getRow(R identifier) throws NoSuchElementException {
        if (!identifiers.contains(identifier))
            throw new NoSuchElementException("Row '" + identifier + "' does not exist in this Table!");

        // NOTE: Using Collectors.toMap(..) results in a NullPointerException, value of map is null.
        // See https://bugs.openjdk.java.net/browse/JDK-8148463
        return headers
                .stream()
                .collect(
                        HashMap::new,
                        (map, header) -> map.put(header, get(header, identifier)),
                        HashMap::putAll
                );
    }

    /**
     * Returns a mapping between all known Rows and it's value for given Column header.
     * Missing values are mapped to {@code null}.
     *
     * @param header The header for the Column to get.
     * @return a mapping between all known Rows and it's value under the header.
     * @throws NoSuchElementException if this Table does not contain a Column with the specified header.
     */
    @Override
    public Map<R, V> getColumn(C header) throws NoSuchElementException {
        if (!headers.contains(header))
            throw new NoSuchElementException("Column '" + header + "' does not exist in this Table!");

        // NOTE: Using Collectors.toMap(..) results in a NullPointerException, value of map is null.
        // See https://bugs.openjdk.java.net/browse/JDK-8148463
        return identifiers
                .stream()
                .collect(
                        HashMap::new,
                        (map, identifier) -> map.put(identifier, get(header, identifier)),
                        HashMap::putAll
                );
    }

    /**
     * Get the value associated with the specified Column and Row.
     *
     * @param header     The Row identifier
     * @param identifier The Column header
     * @return the value associated with given Column and Row. {@code null} is no such value exists.
     */
    @Override
    public V get(C header, R identifier) {
        return map.get(getKey(header, identifier));
    }

    /**
     * Get the value associated with the specified Column and Row, but if no such value exists, return a default value.
     *
     * @param header       The Row identifier
     * @param identifier   The Column header
     * @param defaultValue The default value to return if Table does not contain value
     * @return the value associated with given Column and Row. {@code null} is no such value exists.
     */
    @Override
    public V getOrElse(C header, R identifier, V defaultValue) {
        return contains(header, identifier)
                ? get(header, identifier)
                : defaultValue;
    }

    private Pair<C, R> getKey(C header, R identifier) {
        return new Pair<>(header, identifier);
    }

    /**
     * Associate the specified value with the specified Column and Row.
     *
     * @param header     The Column header
     * @param identifier The Row identifier
     * @param value      The value to associate with specified Column and Row
     * @throws NoSuchElementException if either of specified Column or Row does not exist in this Table.
     */
    @Override
    public void set(C header, R identifier, V value) throws NoSuchElementException {
        if (!headers.contains(header))
            throw new NoSuchElementException("Column '" + header + "' does not exist in this Table!");

        if (!identifiers.contains(identifier))
            throw new NoSuchElementException("Row '" + identifier + "' does not exist in this Table!");

        map.put(getKey(header, identifier), value);
    }

    /**
     * Sets the value associated with the specified Column and Row to be null.
     * If this Table did not contain a value for the specified Column or Row, the call leaves the Table
     * unchanged and returns {@code false}
     *
     * @param header     The Column header
     * @param identifier The Row identifier
     * @return {@code true} if this Table contained the specified Column and Row.
     */
    @Override
    public boolean clear(C header, R identifier) {
        if (!contains(header, identifier))
            return false;
        set(header, identifier, null);
        return true;
    }

    /**
     * Sets the value associated with all the existing Columns in this Table for specified Row to be {@code null}.
     * If this Table did not contain the specified Row, the call leaves the Table unchanged and returns {@code false}
     *
     * @param identifier The identifier for the Row to clear.
     * @return {@code true} if this Table contained the specified Row.
     */
    @Override
    public boolean clearRow(R identifier) {
        if (!identifiers.contains(identifier))
            return false;

        return headers.stream().map(header -> clear(header, identifier)).reduce(false, (a, b) -> a || b);
    }


    /**
     * Sets the value associated with all the existing Rows in this Table for specified Column to be {@code null}.
     * If this Table did not contain the specified Column, the call leaves the Table unchanged and returns {@code false}
     *
     * @param header The header for the Column to clear.
     * @return {@code true} if this Table contained the specified Column.
     */
    @Override
    public boolean clearColumn(C header) {
        if (!headers.contains(header))
            return false;

        return identifiers.stream().map(identifier -> clear(header, identifier)).reduce(false, (a, b) -> a || b);
    }

    /**
     * Removes the specified Row from this Table.
     * If this Table did not contain the specified Row, the call leaves the Table unchanged and returns {@code false}
     *
     * @param identifier The identifier for the Row to remove.
     * @return {@code true} if this Table contained the specified Column and Row.
     */
    @Override
    public boolean removeRow(R identifier) {
        boolean changed = identifiers.remove(identifier);
        if (changed) {
            headers.forEach(header -> map.remove(getKey(header, identifier)));
        }
        return changed;
    }

    /**
     * Removes the specified Column from this Table.
     * If this Table did not contain the specified Column, the call leaves the Table unchanged and returns {@code false}
     *
     * @param header The header for the Column to remove.
     * @return {@code true} if this Table contained the specified Column.
     */
    @Override
    public boolean removeColumn(C header) {
        boolean changed = headers.remove(header);
        if (changed) {
            identifiers.forEach(identifier -> map.remove(getKey(header, identifier)));
        }
        return changed;
    }

    /**
     * Returns a {@link Set} of all Column headers of this Table.
     *
     * @return A {@link Set} of Column headers.
     */
    @Override
    public Set<C> getColumnHeaders() {
        return new HashSet<>(headers);
    }

    /**
     * Returns a {@link Set} of all Row identifiers of this Table.
     *
     * @return A {@link Set} of Row identifiers.
     */
    @Override
    public Set<R> getRowIdentifiers() {
        return new HashSet<>(identifiers);
    }

    /**
     * Returns true if this Table contains a mapping for the specified Column and Row.
     *
     * @param header   The header of the Column to check existence.
     * @param identity The identity of the Row to check existence.
     * @return {@code true} if a non-null value was mapped to the specified Column and Row.
     */
    @Override
    public boolean contains(C header, R identity) {
        return map.containsKey(getKey(header, identity));
    }

    /**
     * Returns true if this Table contains a Column with the specified header.
     *
     * @param header The header of the Column to check existence.
     * @return {@code true} if the Table contains the specified Column.
     */
    @Override
    public boolean containsColumn(C header) {
        return headers.contains(header);
    }

    /**
     * Returns true if this Table contains a Row with the specified identifier.
     *
     * @param identifier The identity of the Row to check existence.
     * @return {@code true} if the Table contains the specified Row.
     */
    @Override
    public boolean containsRow(R identifier) {
        return identifiers.contains(identifier);
    }

    /**
     * Return the contents of this Table, represented as a String, with each Row delimited by a new line character.
     * The table header is show at the nexus of Column header(shown horizontally) and Row header(shown vertically)
     * Each Column tableHeader, Row identifier and all the value
     * are represented using the {@link String} representation returned by it's {@code toString()},
     * padded with enough spaces so that the width of the 'cell' matches the supplied width.
     * If representation is more that the supplied characters long, the first {@code width - 3} characters are used followed by {@code ...}
     *
     * @param width The desired width od each cell.
     */
    @Override
    public String representation(int width, String tableHeader) {

        if (headers.isEmpty() || identifiers.isEmpty())
            return "Table has " + headers.size() + " Columns and " + identifiers.size() + " Rows.Add more data to visualize the Table";

        StringBuilder unit = new StringBuilder();

        unit.append("+");
        for (int i = 0; i < width; i++) {
            unit.append("-");
        }

        StringBuilder separator = new StringBuilder();
        separator.append(unit);
        for (int i = 0; i < headers.size(); i++) {
            separator.append(unit);
        }
        separator.append("+");


        StringBuilder top = new StringBuilder();
        String rows = identifiers.stream().map(identifier -> {
            boolean buildTop = top.length() == 0;
            if (buildTop) {
                top.append(separator);
                top.append("\n");
                top.append("|");
                top.append(representation(tableHeader, width));
            }
            StringBuilder row = new StringBuilder();
            row.append("|");
            row.append(representation(identifier.toString(), width));
            headers.forEach(header -> {
                if (buildTop) {
                    top.append("|");
                    top.append(representation(header.toString(), width));
                }
                row.append("|");
                row.append(getCell(header, identifier, width));
            });
            if (buildTop) {
                top.append("|");
                top.append("\n");
                top.append(separator);
            }
            row.append("|");
            return row.toString();
        }).reduce((s1, s2) -> s1 + "\n" + s2).orElse("");

        return top + "\n" + rows + "\n" + separator;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PairedHashMapTable<?, ?, ?> that = (PairedHashMapTable<?, ?, ?>) o;
        return map.equals(that.map) && headers.equals(that.headers) && identifiers.equals(that.identifiers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(map, headers, identifiers);
    }

    @Override
    public String toString() {
        return representation(20);
    }

    private String representation(String str, int target) {
        if (str.length() > target)
            return str.substring(0, target - 3) + "...";

        StringBuilder strBuilder = new StringBuilder(str);
        while (strBuilder.length() < target) {
            strBuilder.insert(0, " ");
        }
        return strBuilder.substring(0, target);
    }

    private String getCell(C header, R identifier, int width) {

        String value = "";
        if (contains(header, identifier)) {
            V entry = get(header, identifier);
            value = entry == null ? "null" : entry.toString();
        }

        return representation(value, width);
    }
}
