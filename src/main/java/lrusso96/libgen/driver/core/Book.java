package lrusso96.libgen.driver.core;

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

    public int getId() {
        return id;
    }

    void setId(int id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    void setTitle(String title) {
        this.title = title;
    }

    public String getMD5() {
        return MD5;
    }

    void setMD5(String MD5) {
        this.MD5 = MD5;
    }

    public int getYear() {
        return year;
    }

    void setYear(int year) {
        this.year = year;
    }

    public int getPages() {
        return pages;
    }

    void setPages(int pages) {
        this.pages = pages;
    }

    public String getLanguage() {
        return language;
    }

    void setLanguage(String language) {
        this.language = language;
    }

    public String getReadableFilesize() {
        int unit = 1000;
        if (filesize < unit) return filesize + " B";
        int exp = (int) (Math.log(filesize) / Math.log(unit));
        String pre = ("kMGTPE").charAt(exp-1) + "";
        return String.format("%.1f %sB", filesize / Math.pow(unit, exp), pre);
    }


    void setFilesize(int filesize) {
        this.filesize = filesize;
    }

    public String getExtension() {
        return extension;
    }

    void setExtension(String extension) {
        this.extension = extension;
    }

    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", author='" + author + '\'' +
                ", title='" + title + '\'' +
                ", MD5='" + MD5 + '\'' +
                ", year=" + year +
                ", pages=" + pages +
                ", language='" + language + '\'' +
                ", filesize=" + getReadableFilesize() +
                ", extension='" + extension + '\'' +
                '}';
    }
}
