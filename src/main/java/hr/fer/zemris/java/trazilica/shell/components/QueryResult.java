package hr.fer.zemris.java.trazilica.shell.components;

import java.nio.file.Path;

/**
 * This class represents a single query search result. It implements the
 * {@linkplain Comparable} interface, claiming that the natural order of these
 * elements is obtained from document <tt>similarity</tt>. The natural order is
 * set descending, so that results with higher similarity coefficient are first
 * to be seen.
 *
 * @author Mario Bobic
 */
public class QueryResult implements Comparable<QueryResult> {

    /** The document similarity to query. */
    public final double similarity;
    /** Path to document. */
    public final Path filePath;

    /**
     * Constructs an instance of {@code QueryResult} with the specified
     * parameters.
     *
     * @param similarity document similarity to query
     * @param filePath path to document
     */
    public QueryResult(double similarity, Path filePath) {
        this.similarity = similarity;
        this.filePath = filePath;
    }

    @Override
    public int compareTo(QueryResult r) {
        return -Double.compare(similarity, r.similarity);
    }

}
