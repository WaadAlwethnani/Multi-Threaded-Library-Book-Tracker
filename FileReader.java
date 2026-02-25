import java.nio.file.Path;
import java.util.List;

public class FileReader implements Runnable {
    private final Path catalogFile;
    private final List<Book> booksFromFile;

    private int validRecordsProcessed;
    private int errorsEncountered;

    public FileReader(Path catalogFile, List<Book> booksFromFile) {
        this.catalogFile = catalogFile;
        this.booksFromFile = booksFromFile;
    }

    @Override
    public void run() {
        try {
            FileHandler.createCatalogIfMissing(catalogFile);
            LoadResult loadResult = FileHandler.readCatalogBooks(catalogFile);
            booksFromFile.clear();
            booksFromFile.addAll(loadResult.getBooksFromFile());
            validRecordsProcessed = loadResult.getValidRecords();
            errorsEncountered += loadResult.getInvalidRecords();
        } catch (Exception ex) {
            errorsEncountered++;
            System.out.println("Error while reading catalog: " + ex.getMessage());
            ErrorHandler.logFileLineError(catalogFile, "CATALOG READ FAILURE", ex);
        }
    }
    public int getValidRecordsProcessed() {
        return validRecordsProcessed;
    }
    public int getErrorsEncountered() {
        return errorsEncountered;
    }
}