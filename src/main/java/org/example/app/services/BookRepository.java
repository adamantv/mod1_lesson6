package org.example.app.services;

import org.apache.log4j.Logger;
import org.example.web.dto.Book;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Repository
public class BookRepository implements ProjectRepository<Book> {

    private final Logger logger = Logger.getLogger(BookRepository.class);
    private final List<Book> repo = new ArrayList<>();
    private final static Pattern PATTERN_FIELD = Pattern.compile("^(size|author|title)/");
    private final static Pattern PATTERN_SIZE_VALUE = Pattern.compile("^[1-9]\\d*$"); //Ненулевое положительное целое число

    @Override
    public List<Book> retreiveAll() {
        return new ArrayList<>(repo);
    }

    @Override
    public void store(Book book) {
        if (!book.getAuthor().isEmpty() || !book.getTitle().isEmpty() || book.getSize() != null) {
            book.setId(book.hashCode());
            logger.info("store new book: " + book);
            repo.add(book);
        } else {
            logger.info("forbidden to store book with empty fields");
        }
    }

    @Override
    public boolean removeItemById(Integer bookIdToRemove) {
        for (Book book : retreiveAll()) {
            if (book.getId().equals(bookIdToRemove)) {
                logger.info("remove book completed: " + book);
                return repo.remove(book);
            }
        }
        return false;
    }

    @Override
    public boolean removeItemByAuthor(String author) {
        List<Book> booksForRemoving = new ArrayList<>();
        for (Book book : retreiveAll()) {
            if (book.getAuthor().contains(author)) {
                logger.info("need to remove book by author: " + book);
                booksForRemoving.add(book);
            }
        }
        return removeItemsByList(booksForRemoving);
    }

    @Override
    public boolean removeItemByTitle(String title) {
        List<Book> booksForRemoving = new ArrayList<>();
        for (Book book : retreiveAll()) {
            if (book.getTitle().contains(title)) {
                logger.info("need to remove book by title: " + book);
                booksForRemoving.add(book);
            }
        }
        return removeItemsByList(booksForRemoving);
    }

    @Override
    public boolean removeItemBySize(Integer size) {
        List<Book> booksForRemoving = new ArrayList<>();
        for (Book book : retreiveAll()) {
            if (book.getSize().equals(size)) {
                logger.info("need to remove book by size: " + book);
                booksForRemoving.add(book);
            }
        }
        return removeItemsByList(booksForRemoving);
    }

    private boolean removeItemsByList(List<Book> booksForRemoving) {
        if (!booksForRemoving.isEmpty()) {
            for (Book book : booksForRemoving) {
                repo.remove(book);
                logger.info("removed book: " + book);
            }
            return true;
        }
        return false;
    }

    /**
     * possibility to delete all books by regular expression for the author, title or size field.
     * correct format for queryRegex query - fieldname/fieldValue
     * for example: author/Lermontov, size/500, title/Borodino
     *
     * @param queryRegex - query with regex from user
     * @return successful or unsuccessful result
     */
    public boolean removeItemByRegex(String queryRegex) {
        Matcher matcherField = PATTERN_FIELD.matcher(queryRegex);
        if (matcherField.lookingAt()) {
            String fieldName = queryRegex.substring(0, queryRegex.lastIndexOf("/"));
            String fieldValue = matcherField.replaceAll("");
            if (fieldValue == null || fieldValue.isEmpty()) {
                logger.warn("No value present for queryRegex " + queryRegex);
                return false;
            }
            switch (fieldName) {
                case "author":
                    return removeItemByAuthor(fieldValue);
                case "title":
                    return removeItemByTitle(fieldValue);
                case "size":
                    if (PATTERN_SIZE_VALUE.matcher(fieldValue).matches()) {
                        return removeItemBySize(Integer.parseInt(fieldValue));
                    } else {
                        logger.error("Size must be positive " + fieldValue);
                        return false;
                    }
                default:
                    logger.error("Something went wrong for query " + queryRegex);
                    return false;
            }
        } else {
            logger.error("Unknown fieldName in query " + queryRegex);
            return false;
        }
    }
}
