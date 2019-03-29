package hr.fer.zemris.java.trazilica.shell.components;

import java.util.List;

/**
 * A vector utility class. Used for defining and providing most common vector
 * utility methods used throughout this project.
 *
 * @author Mario Bobic
 */
public class VectorUtilities {

    /**
     * Disables instantiation.
     */
    private VectorUtilities() {
    }

    /**
     * Calculates a scalar product of the two specified vectors.
     * <p>
     * Vectors must be of same sizes, else an
     * {@linkplain IllegalArgumentException} is thrown.
     *
     * @param vector1 first vector
     * @param vector2 second vector
     * @return scalar product of the two specified vectors
     * @throws IllegalArgumentException if vectors are not of same length
     * @throws NullPointerException if either vector is <tt>null</tt>
     */
    public static double scalarProduct(List<Double> vector1, List<Double> vector2) {
        if (vector1.size() != vector2.size()) {
            throw new IllegalArgumentException("Vectors must be of same length!");
        }

        double scalarProduct = 0.0;
        for (int i = 0, n = vector1.size(); i < n; i++) {
            scalarProduct += vector1.get(i) * vector2.get(i);
        }
        return scalarProduct;
    }

    /**
     * Calculates the norm of the specified <tt>vector</tt>.
     *
     * @param vector a vector
     * @return the norm of the specified <tt>vector</tt>
     * @throws NullPointerException if vector is <tt>null</tt>
     */
    public static double norm(List<Double> vector) {
        double sum = 0.0;

        for (double value : vector) {
            sum += value * value;
        }

        return Math.sqrt(sum);
    }

}
