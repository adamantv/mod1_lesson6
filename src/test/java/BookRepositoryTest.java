import org.example.app.services.BookRepository;
import org.example.web.dto.Book;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BookRepositoryTest {

    @Test
    public void testRemoveItemById() {
        BookRepository bookRepository = new BookRepository();
        Book book1 = new Book();
        book1.setSize(2);
        book1.setAuthor("Pushkin");
        book1.setTitle("Evgeniy Onegin");
        bookRepository.store(book1);

        Book book2 = new Book();
        book2.setSize(2);
        book2.setAuthor("Lermontov");
        book2.setTitle("Borodino");
        bookRepository.store(book2);

        Book book3 = new Book();
        book3.setSize(2);
        book3.setAuthor("Pushkin");
        book3.setTitle("Sbornik");
        bookRepository.store(book3);

        assertThat(bookRepository.removeItemById(book1.hashCode())).isTrue();
        assertThat(bookRepository.retreiveAll()).hasSize(2);
    }

    @Test
    public void testRemoveItemByAuthor() {
        BookRepository bookRepository = new BookRepository();
        Book book1 = new Book();
        book1.setSize(2);
        book1.setAuthor("Pushkin");
        book1.setTitle("Evgeniy Onegin");
        bookRepository.store(book1);

        Book book2 = new Book();
        book2.setSize(2);
        book2.setAuthor("Lermontov");
        book2.setTitle("Borodino");
        bookRepository.store(book2);

        Book book3 = new Book();
        book3.setSize(2);
        book3.setAuthor("Pushkin");
        book3.setTitle("Sbornik");
        bookRepository.store(book3);

        assertThat(bookRepository.removeItemByAuthor("Pushkin")).isTrue();
        assertThat(bookRepository.retreiveAll()).hasSize(1);
    }

    @Test
    public void testRemoveItemByTitle() {
        BookRepository bookRepository = new BookRepository();
        Book book1 = new Book();
        book1.setSize(2);
        book1.setAuthor("Pushkin");
        book1.setTitle("Evgeniy Onegin");
        bookRepository.store(book1);

        Book book2 = new Book();
        book2.setSize(2);
        book2.setAuthor("Lermontov");
        book2.setTitle("Borodino");
        bookRepository.store(book2);

        Book book3 = new Book();
        book3.setSize(2);
        book3.setAuthor("Pushkin");
        book3.setTitle("Sbornik");
        bookRepository.store(book3);

        assertThat(bookRepository.removeItemByTitle("Sbornik")).isTrue();
        assertThat(bookRepository.retreiveAll()).hasSize(2);
    }

    @Test
    public void testSearchItemBySize() {
        BookRepository bookRepository = new BookRepository();
        Book book1 = new Book();
        book1.setSize(2);
        book1.setAuthor("Pushkin");
        book1.setTitle("Evgeniy Onegin");
        bookRepository.store(book1);

        Book book2 = new Book();
        book2.setSize(2);
        book2.setAuthor("Lermontov");
        book2.setTitle("Borodino");
        bookRepository.store(book2);

        Book book3 = new Book();
        book3.setSize(2);
        book3.setAuthor("Pushkin");
        book3.setTitle("Sbornik");
        bookRepository.store(book3);

        assertThat(bookRepository.removeItemBySize(2)).isTrue();
        assertThat(bookRepository.retreiveAll()).isEmpty();
    }
}
