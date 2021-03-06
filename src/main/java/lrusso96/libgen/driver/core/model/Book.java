package lrusso96.libgen.driver.core.model;

import java.net.URI;

public class Book {
    private int id;
    private String author;
    private String title;
    private String MD5;
    private int year;
    private int pages;
    private String language;
    private int filesize;   //bytes
    private String extension;
    private URI cover;
    private URI download;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMD5() {
        return MD5;
    }

    public void setMD5(String MD5) {
        this.MD5 = MD5;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getReadableFilesize() {
        int unit = 1000;
        if (filesize < unit) return filesize + " B";
        int exp = (int) (Math.log(filesize) / Math.log(unit));
        String pre = Character.toString(("kMGTPE").charAt(exp - 1));
        return String.format("%.1f %sB", filesize / Math.pow(unit, exp), pre);
    }

    public void setFilesize(int filesize) {
        this.filesize = filesize;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public URI getCover() {
        return cover;
    }

    public void setCover(URI cover_uri) {
        this.cover = cover_uri;
    }

    public URI getDownload() {
        return download;
    }

    public void setDownload(URI download) {
        this.download = download;
    }

    @Override
    public String toString() {
        return "Book{" + "id=" + id + ", author='" + author + '\'' + ", title='" + title + '\'' + ", MD5='" + MD5 +
                '\'' + ", year=" + year + ", pages=" + pages + ", language='" + language + '\'' + ", filesize=" +
                filesize + ", extension='" + extension + '\'' + ", cover='" + cover + '\'' + '}';
    }
}
