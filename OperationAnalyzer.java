import java.nio.file.Path;
import java.util.List;

public class OperationAnalyzer implements Runnable {
    private final Path catalogFile;
    private final String operationArgument;
    private final List<Book> booksFromFile;

    private int searchResultsFound;
    private int booksAdded;
    private int errorsEncountered;

    public OperationAnalyzer(Path catalogFile, String operationArgument, List<Book> booksFromFile) {
        this.catalogFile = catalogFile;
        this.operationArgument = operationArgument;
        this.booksFromFile = booksFromFile;
    }
    @Override
    public void run() {
        try {
            if (FileHandler.isIsbnSearch(operationArgument)) {
                Book foundBook =
                        FileHandler.findByIsbn(booksFromFile, operationArgument);
                if (foundBook == null) {
                    System.out.println("No matching books found.");
                    searchResultsFound = 0;
                } else {
                    LibraryBookTracker.printBooksStatic(List.of(foundBook));
                    searchResultsFound = 1;
                }
                booksAdded = 0;
            } else if (FileHandler.isAddRecordFormat(operationArgument)) {
                try {
                    Book newBook =
                            FileHandler.parseAddRecord(operationArgument);

                    FileHandler.appendBookThenSortAndRewrite(catalogFile, booksFromFile, newBook);

                    LibraryBookTracker.printBooksStatic(List.of(newBook));
                    booksAdded = 1;
                    searchResultsFound = 0;

                } catch (BookCatalogException ex) {
                    errorsEncountered++;
                    System.out.println("Error: " + ex.getMessage());
                    ErrorHandler.logUserInputError(
                            catalogFile, operationArgument, ex);
                }
            } else {
                if (operationArgument.contains(":")) {
                    MalformedBookEntryException ex =
                            new MalformedBookEntryException("New book record must be: title:author:isbn:copies");
                    System.out.println("Error: " + ex.getMessage());
                    errorsEncountered++;
                    ErrorHandler.logUserInputError(
                            catalogFile, operationArgument, ex);
                } else {
                    List<Book> results =
                            FileHandler.findByTitleKeyword(
                                    booksFromFile, operationArgument);
                    if (results.isEmpty()) {
                        System.out.println("No matching books found.");
                        searchResultsFound = 0;
                    } else {
                        LibraryBookTracker.printBooksStatic(results);
                        searchResultsFound = results.size();
                    }
                }
                booksAdded = 0;
            }
        } catch (Exception ex) {
            errorsEncountered++;
            System.out.println("Unexpected error: " + ex.getMessage());
            ErrorHandler.logUserInputError(
                    catalogFile,
                    (operationArgument == null ? "N/A" : operationArgument),
                    ex);
        }
    }
    public int getSearchResultsFound() {
        return searchResultsFound;
    }
    public int getBooksAdded() {
        return booksAdded;
    }
    public int getErrorsEncountered() {
        return errorsEncountered;
    }
}