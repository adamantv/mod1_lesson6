package org.example.web.controllers;

import org.apache.log4j.Logger;
import org.example.app.services.BookService;
import org.example.exceptions.RegexpSyntaxException;
import org.example.exceptions.UploadFileException;
import org.example.web.dto.Book;
import org.example.web.dto.BookIdToRemove;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Controller
@RequestMapping(value = "/books")
public class BookShelfController {

    private Logger logger = Logger.getLogger(BookShelfController.class);
    private BookService bookService;

    @Autowired
    public BookShelfController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/shelf")
    public String books(Model model) {
        logger.info("got book shelf");
        model.addAttribute("book", new Book());
        model.addAttribute("bookIdToRemove", new BookIdToRemove());
        model.addAttribute("bookList", bookService.getAllBooks());
        return "book_shelf";
    }

    @PostMapping("/save")
    public String saveBook(@Valid Book book, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("book", book);
            model.addAttribute("bookIdToRemove", new BookIdToRemove());
            model.addAttribute("bookList", bookService.getAllBooks());
            return "book_shelf";
        } else {
            bookService.saveBook(book);
            logger.info("current repository size: " + bookService.getAllBooks().size());
            return "redirect:/books/shelf";
        }
    }

    @PostMapping("/removeById")
    public String removeBook(@Valid BookIdToRemove bookIdToRemove, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("book", new Book());
            model.addAttribute("bookList", bookService.getAllBooks());
            return "book_shelf";
        } else {
            bookService.removeBookById(bookIdToRemove.getId());
            return "redirect:/books/shelf";
        }
    }

    @PostMapping("/removeByRegex")
    public String removeBook(@RequestParam(value = "queryRegex") String queryRegex) throws RegexpSyntaxException {
        if (queryRegex == null || queryRegex.isEmpty()) {
            throw new RegexpSyntaxException("Regexp is empty, try again");
        }
        boolean isRemoved = bookService.removeBooksByRegex(queryRegex);
        if (!isRemoved) {
            throw new RegexpSyntaxException("Incorrect regexp format, try again");
        }
        return "redirect:/books/shelf";
    }

    @PostMapping("/uploadFile")
    public String uploadFile(@RequestParam("file") MultipartFile file) throws UploadFileException, IOException {
        if (file == null || file.isEmpty()) {
            throw new UploadFileException("File is empty, need to try upload file again");
        }
        String name = file.getOriginalFilename();
        byte[] bytes = file.getBytes();

        //create dir
        String rootPath = System.getProperty("catalina.home");
        File dir = new File(rootPath + File.separator + "external_uploads");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        //create file
        File serverFile = new File(dir.getAbsolutePath() + File.separator + name);
        try (BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(serverFile))) {
            stream.write(bytes);
            logger.info("new file saved at: " + serverFile.getAbsolutePath());
        } catch (IOException e) {
            logger.error("Error during upload file");
        }
        return "redirect:/books/shelf";
    }

    @ExceptionHandler(UploadFileException.class)
    public String handlerUploadFileException(Model model, UploadFileException exception) {
        model.addAttribute("errorMessage", exception.getMessage());
        return "errors/500";
    }

    @ExceptionHandler(RegexpSyntaxException.class)
    public String handlerRegexpSyntaxException(Model model, RegexpSyntaxException exception) {
        model.addAttribute("errorMessage", exception.getMessage());
        return "errors/500";
    }
}
