package hr.fer.zemris.java.trazilica.shell.components;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * This class represents data-loading utility whose instances serve documents
 * loaded recursively from a single directory. It generates a vocabulary of all
 * encountered words, <strong>retaining</strong> only words that are not listed
 * as <strong>stopwords</strong>.
 * <p>
 * Other features that this class provides, are the following:
 * <ul>
 *   <li>fetching the vocabulary either as a <tt>Set</tt> or a <tt>List</tt>,
 *   using one of two methods:
 *   <ul>
 *     <li>{@linkplain #getVocabularySet()},
 *     <li>{@linkplain #getVocabularyList()}
 *   </ul>
 *   <li>fetching a map of files in which file paths are associated with file
 *   keys, using the {@linkplain #getFiles()} method and
 *   <li>fetching a map in which file TF-IDF vectors are associated with file
 *   keys, using the {@linkplain #getFileVectors()} method.
 * </ul>
 * <p>
 * Some very useful methods are listed below:
 * <ul>
 *   <li>obtaining a list of words from a single string, ignoring all symbols
 *   but letters, which are determined by the
 *   {@linkplain Character#isLetter(char)} method,
 *   <li>obtaining a list of words that are contained in this object's
 *   vocabulary, which is the same as calling {@linkplain #getWords(String)}
 *   and retaining only words contained in the vocabulary, and
 *   <li>generating a TF-IDF vector from a list of words.
 * </ul>
 *
 * @author Mario Bobic
 */
public class DataLoader {

    /** Set containing stop words. */
    private static Set<String> stopWords;
    static {
        try {
            stopWords = new HashSet<>(ShellUtil.loadTextResource("stopwords.txt"));
        } catch (Exception e) {
            throw new InternalError("Error loading stopwords file.", e);
        }
    }

    /** A Set version of the whole vocabulary. */
    private Set<String> vocabularySet;
    /** A List version of the whole vocabulary. */
    private List<String> vocabularyList;

    /** Map in which file paths are associated with file keys. */
    private Map<Integer, Path> files;
    /** Map in which file word lists are associated with file keys. */
    private Map<Integer, List<String>> fileWords;

    /** IDF vectors of the whole vocabulary. */
    private List<Double> idfComponents;
    /** Map in which file TF-IDF vectors are associated with file keys. */
    private Map<Integer, List<Double>> fileVectors;


    /**
     * Constructs an instance of {@code DataLoader} with the specified directory
     * path <tt>dir</tt>. Fills the vocabulary from all loaded documents and
     * creates TF-IDF vectors for all documents.
     *
     * @param dir directory in which documents are located
     * @throws NullPointerException if <tt>dir</tt> is <tt>null</tt>
     * @throws FileNotFoundException if <tt>dir</tt> is not found
     * @throws IOException if any file in <tt>dir</tt> fails to load
     */
    public DataLoader(Path dir) throws IOException {
        validateDirectory(dir);

        vocabularySet = new HashSet<>();
        files = new HashMap<>();
        fileWords = new HashMap<>();
        idfComponents = new ArrayList<>();
        fileVectors = new HashMap<>();

        // Fills files, file vocabulary and vocabulary set
        LoaderVisitor visitor = new LoaderVisitor();
        Files.walkFileTree(dir, visitor);
        vocabularyList = new ArrayList<>(vocabularySet);

        fillIdfComponents();
        fillFileVectors();
    }

    /**
     * Returns the whole vocabulary as an unmodifiable <tt>Set</tt>.
     * <p>
     * This set is of the same contents as the {@link #getVocabularyList()
     * vocabulary List} and contains the same order of elements.
     *
     * @return the whole vocabulary as an unmodifiable <tt>Set</tt>
     */
    public Set<String> getVocabularySet() {
        return Collections.unmodifiableSet(vocabularySet);
    }

    /**
     * Returns the whole vocabulary as an unmodifiable <tt>List</tt>.
     * <p>
     * This list is of the same contents as the {@link #getVocabularySet()
     * vocabulary Set} and contains the same order of elements.
     *
     * @return the whole vocabulary as an unmodifiable <tt>List</tt>
     */
    public List<String> getVocabularyList() {
        return Collections.unmodifiableList(vocabularyList);
    }

    /**
     * Returns the map in which file paths are associated with file keys.
     *
     * @return the map in which file paths are associated with file keys
     */
    public Map<Integer, Path> getFiles() {
        return files;
    }

    /**
     * Returns the map in which file TF-IDF vectors are associated with file keys.
     *
     * @return the map in which file TF-IDF vectors are associated with file keys
     */
    public Map<Integer, List<Double>> getFileVectors() {
        return fileVectors;
    }

    /**
     * Validates the path argument by testing if it leads to an existing
     * directory. Throws an exception if the path leads to a file or can
     * not be resolved.
     *
     * @param dir path to a directory
     * @throws NullPointerException if <tt>dir</tt> is <tt>null</tt>
     * @throws FileNotFoundException if directory is not found
     */
    private static void validateDirectory(Path dir) throws FileNotFoundException {
        Objects.requireNonNull(dir, "Path must not be null.");

        if (!Files.isDirectory(dir)) {
            throw new FileNotFoundException("Directory " + dir + " not found.");
        }
    }

    /**
     * Loads all words from file specified by the <tt>file</tt> path, ignoring
     * all symbols but letters, which are determined by the
     * {@linkplain Character#isLetter(char)} method.
     *
     * @param file path to file
     * @return a list of words contained in the file, may contain duplicates
     * @throws NullPointerException if <tt>file</tt> is <tt>null</tt>
     * @throws RuntimeException if an error occurs while reading the file
     */
    private static List<String> loadWords(Path file) {
        String text;
        try {
            text = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
            return getWords(text);
        } catch (Exception e) {
            throw new RuntimeException("An error occured while reading file " + file, e);
        }
    }

    /**
     * Loads all words into a list from the specified <tt>text</tt> string,
     * ignoring all symbols but letters, which are determined by the
     * {@linkplain Character#isLetter(char)} method.
     *
     * @param text text to be read
     * @return a list of words contained in the text, may contain duplicates
     * @throws NullPointerException if <tt>text</tt> is <tt>null</tt>
     */
    public static List<String> getWords(String text) {
        text = text.concat(" "); // add last space
        char[] chars = text.toCharArray();

        List<String> words = new ArrayList<>();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < chars.length; i++) {
            if (Character.isLetter(chars[i])) {
                sb.append(chars[i]);
            } else {
                String word = sb.toString().trim().toLowerCase();
                if (word.length() != 0 && !stopWords.contains(word)) {
                    words.add(word);
                }
                sb.setLength(0);
            }
        }

        return words;
    }

    /**
     * Loads all words into a list from the specified <tt>text</tt> string,
     * ignoring all symbols but letters, which are determined by the
     * {@linkplain Character#isLetter(char)} method.
     * <p>
     * This method retains only words that are contained in this object's
     * vocabulary, and is the same as calling {@linkplain #getWords(String)}
     * and retaining only words contained in the vocabulary.
     *
     * @param text text to be read
     * @return a list of words contained in the text, may contain duplicates
     */
    public List<String> getVocabularyWords(String text) {
        List<String> words = getWords(text);
        words.retainAll(vocabularySet);
        return words;
    }

    /**
     * Fills IDF components of this object. The IDF components list must be
     * initialized before calling this method.
     * <p>
     * This method shortens the execution time by a factor of 100 by
     * transforming every list from the <tt>fileWords</tt> map to a set which is
     * then asked if it contains a word from the vocabulary, for each word.
     */
    private void fillIdfComponents() {
        Integer nDocuments = files.keySet().size();

        Map<Integer, Set<String>> fileVocabulary = new HashMap<>();
        fileWords.forEach((fileKey, wordsList) -> {
            fileVocabulary.put(fileKey, new HashSet<>(wordsList));
        });

        for (String word : vocabularySet) {
            int nDocumentsContainingWord = 0;

            for (Set<String> vocabulary : fileVocabulary.values()) {
                if (vocabulary.contains(word)) {
                    nDocumentsContainingWord++;
                }
            }

            Double idf = Math.log((double) nDocuments / nDocumentsContainingWord);
            idfComponents.add(idf);
        }
    }

    /**
     * Fills TF-IDF vectors of this object. The file vectors field must be
     * initialized before calling this method.
     */
    private void fillFileVectors() {
        for (Integer key : files.keySet()) {
            List<Double> tfIdfVector = generateTfIdfVector(fileWords.get(key));
            fileVectors.put(key, tfIdfVector);
        }
    }

    /**
     * Generates a single TF-IDF vector based on the specified list of
     * <tt>words</tt> considering only the word contained in this object's
     * vocabulary.
     * <p>
     * To speed up the process, the list of words is transformed into a set,
     * so the set is first asked if it contains a word from the vocabulary,
     * and if it does then the list is asked for the frequency of the word.
     *
     * @param words list of words
     * @return a TF-IDF vector
     */
    public List<Double> generateTfIdfVector(List<String> words) {
        List<Double> tfIdfVector = new ArrayList<>();

        Set<String> wordsSet = new HashSet<>(words);  // serves to speed up the process

        for (int i = 0, n = vocabularyList.size(); i < n; i++) {
            String word = vocabularyList.get(i);
            if (!wordsSet.contains(word)) {
                tfIdfVector.add(0.0);
                continue;
            }

            long tf = Collections.frequency(words, word);
            double idf = idfComponents.get(i);

            tfIdfVector.add(tf*idf);
        }

        return tfIdfVector;
    }

    /**
     * This class is a simple file visitor that re-implements two methods of the
     * {@linkplain SimpleFileVisitor} class:
     * <ul>
     * <li>the {@link #visitFile(Path, BasicFileAttributes) visitFile} method and
     * <li>the {@link #visitFileFailed(Path, IOException) visitFileFailed} method.
     * </ul>
     * <p>
     * The <tt>visitFile</tt> method fills map of files, vocabulary for each file
     * and the whole vocabulary set.
     *
     * @author Mario Bobic
     */
    private class LoaderVisitor extends SimpleFileVisitor<Path> {

        /** Key of the file used by maps. */
        private int fileKey = 0;

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (Files.isRegularFile(file)) {
                files.put(fileKey, file.toAbsolutePath().normalize());

                List<String> words = loadWords(file);

                fileWords.put(fileKey, words);
                vocabularySet.addAll(words);

                fileKey++;
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            if (Files.isRegularFile(file)) {
                throw new IOException("An error occured while reading file " + file, exc);
            }
            return FileVisitResult.CONTINUE;
        }
    }

}
