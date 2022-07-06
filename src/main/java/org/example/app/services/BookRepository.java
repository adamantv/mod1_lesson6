package org.example.app.services;

import org.apache.log4j.Logger;
import org.example.web.dto.Book;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Repository
public class BookRepository implements ProjectRepository<Book>, ApplicationContextAware {

    private final Logger logger = Logger.getLogger(BookRepository.class);
    //private final List<Book> repo = new ArrayList<>();
    private ApplicationContext context;
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final static Pattern PATTERN_FIELD = Pattern.compile("^(size|author|title)/");
    private final static Pattern PATTERN_SIZE_VALUE = Pattern.compile("^[1-9]\\d*$"); //Ненулевое положительное целое число

    @Autowired
    public BookRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Book> retreiveAll() {
        List<Book> books = jdbcTemplate.query("SELECT * FROM books", (ResultSet rs, int rowNum) ->
        {
            Book book = new Book();
            book.setId(rs.getInt("id"));
            book.setAuthor(rs.getString("author"));
            book.setTitle(rs.getString("title"));
            book.setSize(rs.getInt("size"));

            return book;
        });
        return new ArrayList<>(books);
    }

    @Override
    public void store(Book book) {
        MapSqlParameterSource parametrSource = new MapSqlParameterSource();
        parametrSource.addValue("author", book.getAuthor());
        parametrSource.addValue("title", book.getTitle());
        parametrSource.addValue("size", book.getSize());
        jdbcTemplate.update("INSERT INTO books(author, title, size) VALUES (:author, :title, :size)", parametrSource);
        logger.info("store new book: " + book);
    }

    @Override
    public boolean removeItemById(Integer bookIdToRemove) {
        MapSqlParameterSource parameterSource = new MapSqlParameterSource();
        parameterSource.addValue("id", bookIdToRemove);
        jdbcTemplate.update("DELETE FROM books WHERE id =: id", parameterSource);
        logger.info("remove book completed!");
        return true;
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
                removeItemById(book.getId());
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

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    private void defaultInit() {
        logger.info("default INIT in book repo bean");
    }

    private void defaultDestroy() {
        logger.info("default DESTROY in book repo bean");
    }
}
