import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class LibraryBookTracker {
    public static void main(String[] args) {
        int validRecordsProcessed = 0;
        int searchResultsFound = 0;
        int booksAdded = 0;
        int errorsEncountered = 0;

        Path catalogFile = null;
        String operationArgument = null;
        List<Book> booksFromFile = new ArrayList<>();
        try {
            if (args.length < 2) {
                InsufficientArgumentsException ex =
                        new InsufficientArgumentsException(
                                "Usage: java LibraryBookTracker <catalogFile.txt> <operationArgument>"
                        );
                ErrorHandler.logUserInputError(Path.of("."), "NO ARGUMENTS", ex);
                throw ex;
            }
            String catalogFileName = args[0];
            operationArgument = args[1];
            if (!catalogFileName.endsWith(".txt")) {
                InvalidFileNameException ex =
                        new InvalidFileNameException("First argument must end with .txt");
                catalogFile = Path.of(catalogFileName);
                ErrorHandler.logUserInputError(catalogFile, catalogFileName, ex);
                throw ex;
            }
            catalogFile = Path.of(catalogFileName);

            FileReader fileReader = new FileReader(catalogFile, booksFromFile);
            Thread fileThread = new Thread(fileReader);
            fileThread.start();
            fileThread.join();

            validRecordsProcessed = fileReader.getValidRecordsProcessed();
            errorsEncountered += fileReader.getErrorsEncountered();

            OperationAnalyzer operationAnalyzer = new OperationAnalyzer(catalogFile, operationArgument, booksFromFile);
            Thread opThread = new Thread(operationAnalyzer);

            opThread.start();
            opThread.join();

            searchResultsFound = operationAnalyzer.getSearchResultsFound();
            booksAdded = operationAnalyzer.getBooksAdded();
            errorsEncountered += operationAnalyzer.getErrorsEncountered();

        } catch (BookCatalogException ex) {
            System.out.println("Error: " + ex.getMessage());
            errorsEncountered++;
            if (catalogFile != null) {
                ErrorHandler.logUserInputError(
                        catalogFile,
                        (operationArgument == null ? "N/A" : operationArgument),
                        ex
                );
            }
        } catch (InterruptedException ex) {
            System.out.println("Thread interrupted: " + ex.getMessage());
            errorsEncountered++;
            if (catalogFile != null) {
                ErrorHandler.logUserInputError(
                        catalogFile,
                        "THREAD INTERRUPTED",
                        ex
                );
            }
        } catch (Exception ex) {
            System.out.println("Unexpected error: " + ex.getMessage());
            errorsEncountered++;
            if (catalogFile != null) {
                ErrorHandler.logUserInputError(
                        catalogFile,
                        (operationArgument == null ? "N/A" : operationArgument),
                        ex
                );
            }
        } finally {
            System.out.println();
            System.out.println("----- Statistics -----");
            System.out.println("Valid records processed: " + validRecordsProcessed);
            System.out.println("Search results found: " + searchResultsFound);
            System.out.println("Books added: " + booksAdded);
            System.out.println("Errors encountered: " + errorsEncountered);
            System.out.println("----------------------");
            System.out.println("Thank you for using the Library Book Tracker.");
        }
    }
    public static void printBooksStatic(List<Book> books) {
        printHeader();
        for (Book book : books) {
            printBookRow(book);
        }
    }
    private static void printHeader() {
        System.out.printf("%-30s %-20s %-15s %5s%n", "Title", "Author", "ISBN", "Copies");
        System.out.println("--------------------------------------------------------------------------");
    }
    private static void printBookRow(Book book) {
        System.out.printf("%-30s %-20s %-15s %5d%n",
                book.getTitle(),
                book.getAuthor(),
                book.getIsbn(),
                book.getCopies());
    }
}