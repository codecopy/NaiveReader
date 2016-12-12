package sixue.naviereader;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import sixue.naviereader.data.Book;

public class BookLoader {
    private static BookLoader instance;
    private List<Book> list;
    private String saveRootPath;
    private Bitmap noCover;

    private BookLoader() {
        list = new ArrayList<>();
    }

    public static BookLoader getInstance() {
        if (instance == null) {
            instance = new BookLoader();
        }
        return instance;
    }

    public void reload(Context context) {
        try {
            InputStream is = context.getAssets().open("NoCover.jpg");
            noCover = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        File saveRoot = context.getExternalFilesDir("books");
        if (saveRoot == null) {
            return;
        }

        saveRootPath = saveRoot.getAbsolutePath();
        String json = Utils.readText(saveRootPath + "/.DIR");
        if (json == null) {
            return;
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            JavaType listType = mapper.getTypeFactory().constructParametricType(ArrayList.class, Book.class);
            list = mapper.readValue(json, listType);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void save() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(list);
            Utils.writeText(json, saveRootPath + "/.DIR");
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public int getBookNum() {
        return list.size();
    }

    public Book getBook(int i) {
        if (list.size() == 0) {
            return null;
        }
        if (i < 0) {
            i = 0;
        } else if (i >= list.size()) {
            i = list.size() - 1;
        }
        return list.get(i);
    }

    public void addBook(Book book) {
        list.add(book);

        save();
    }

    public void deleteBooks(List<Book> deleteList) {
        list.removeAll(deleteList);

        save();
    }

    public void bookBubble(int i) {
        if (list.size() == 0) {
            return;
        }
        if (i < 0) {
            i = 0;
        } else if (i >= list.size()) {
            i = list.size() - 1;
        }
        Book book = list.get(i);
        list.remove(i);
        list.add(0, book);
    }

    public void updateBook(Book book) {
        save();
    }

    public Bitmap getNoCover() {
        return noCover;
    }
}
