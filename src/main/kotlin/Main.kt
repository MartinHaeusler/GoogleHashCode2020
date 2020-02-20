import java.io.File
import java.nio.file.Files
import kotlin.math.ceil

fun main(args: Array<String>) {
    if (args.size != 1) {
        throw IllegalArgumentException("Expected 1 argument (the book file)!")
    }
    val inputFile = File(args[0])
    val problem = parseInputFile(inputFile)
    println("Problem ${inputFile.name} has ${problem.libraries.size} libraries and ${problem.bookScores.size} books. Total time is ${problem.numberOfDays} days.")
    val solution = solve2(problem)
    val score = score(problem, solution)
    val outputDir = File(inputFile.parentFile, inputFile.nameWithoutExtension + "_output")
    if (!outputDir.exists()) {
        Files.createDirectory(outputDir.toPath())
    }

    printToFile(solution, File(outputDir, "${score}.solution.txt"))
}


typealias BookId = Int
typealias BookScore = Int


class Library(
    val id: Int,
    val numberOfBooks: Int,
    val signupTime: Int,
    val booksShippedPerDay: Int,
    val books: MutableList<BookId>
) {

    val totalScanTime: Int
        get() {
            return signupTime + (ceil(numberOfBooks.toDouble() / booksShippedPerDay)).toInt()
        }

    fun score(bookScores: Map<BookId, BookScore>, remainingTime: Int): Int {
        val booksToShip = getShipableBooks(bookScores, remainingTime)
        return booksToShip.sumBy { bookScores[it] ?: 0 }
    }

    fun getShipableBooks(bookScores: Map<BookId, BookScore>, remainingTime: Int): List<BookId> {
        val bestBooks = this.books.filter { (bookScores[it] ?: 0) > 0 }
        val subList = bestBooks.take(
            Math.min(
                Integer.MAX_VALUE.toLong(),
                ((remainingTime - signupTime).toLong() * booksShippedPerDay)
            ).toInt()
        )
        return subList
    }

}

class Problem(
    val numberOfBooks: Int,
    val numberOfLibraries: Int,
    val numberOfDays: Int,
    val bookScores: MutableMap<BookId, BookScore> = mutableMapOf(),
    val libraries: MutableList<Library> = mutableListOf()
) {

    fun totalScoreOf(library: Library): Int {
        return library.books.sumBy { this.bookScores[it]!! }
    }

}

class Solution(
    val libraryScans: MutableList<LibraryScan> = mutableListOf()
) {

    fun toOutputFormat(): String {
        val builder = StringBuilder()
        builder.append(this.libraryScans.size)
        builder.append("\n")
        for (libraryScan in this.libraryScans) {
            builder.append(libraryScan.library.id)
            builder.append(" ")
            builder.append(libraryScan.booksToScan.size)
            builder.append("\n")
            builder.append(libraryScan.booksToScan.joinToString(separator = " "))
            builder.append("\n")
        }
        return builder.toString()
    }

}

class LibraryScan(
    val library: Library,
    val booksToScan: List<BookId>
)


private fun parseInputFile(file: File): Problem {
    if (!file.exists() || !file.isFile) {
        throw IllegalArgumentException("File does not exist or is not a file: ${file.absolutePath}")
    }
    val lines = file.readLines().asSequence().filter { !it.isBlank() }.toMutableList()
    val headerLine = lines.removeAt(0)
    val headerLineSplit = headerLine.split(" ")
    val bookCount = headerLineSplit[0]
    val libraryCount = headerLineSplit[1]
    val totalDays = headerLineSplit[2]

    val problem = Problem(
        numberOfBooks = bookCount.toInt(),
        numberOfLibraries = libraryCount.toInt(),
        numberOfDays = totalDays.toInt()
    )

    val bookScoresLine = lines.removeAt(0)
    bookScoresLine.split(" ").asSequence()
        .mapIndexed { index, value -> index to value.toInt() }
        .toMap(problem.bookScores)

    for (i in lines.indices step (2)) {
        val libraryStatsLine = lines[i]
        val libraryBooksLine = lines[i + 1]

        val libraryStatLineSplit = libraryStatsLine.split(" ")
        val numberOfBooksInLibray = libraryStatLineSplit[0]
        val signupTime = libraryStatLineSplit[1]
        val booksShippedPerDay = libraryStatLineSplit[2]

        val bookIds = libraryBooksLine.split(" ").asSequence()
            .map { it.toInt() }
            .toSet()

        problem.libraries += Library(
            id = i / 2,
            numberOfBooks = numberOfBooksInLibray.toInt(),
            signupTime = signupTime.toInt(),
            booksShippedPerDay = booksShippedPerDay.toInt(),
            books = bookIds.toMutableList()
        )
    }
    return problem
}


fun printToFile(solution: Solution, file: File) {
    file.delete()
    file.createNewFile()
    file.writeText(solution.toOutputFormat())
    println("Solution file written to: ${file.absolutePath}")
}

fun solve(problem: Problem): Solution {
    problem.libraries.sortByDescending { problem.totalScoreOf(it) }

    val solution = Solution()

    var daysConsumed = 0
    var libraryIndex = 0
    while (daysConsumed < problem.numberOfDays && libraryIndex + 1 < problem.libraries.size) {
        val library = problem.libraries[libraryIndex]
        if (daysConsumed + library.totalScanTime > problem.numberOfDays) {
            // partial scan
            if (daysConsumed + library.signupTime > problem.numberOfDays) {
                // can't use this one
                libraryIndex++
                continue
            }
            val daysRemaining = problem.numberOfDays - daysConsumed
            // scan as many books as possible
            val booksToScan = library.books.take(daysRemaining * library.booksShippedPerDay)
            solution.libraryScans += LibraryScan(library, booksToScan)
            daysConsumed += daysRemaining
        } else {
            // full scan
            solution.libraryScans += LibraryScan(library, library.books.toList())
            daysConsumed += library.totalScanTime
        }
        libraryIndex++
    }

    return solution
}

fun score(problem: Problem, solution: Solution): Int {
    val allScannedBooks = solution.libraryScans.asSequence().flatMap { it.booksToScan.asSequence() }.toSet()
    return allScannedBooks.sumBy { problem.bookScores[it]!! }
}


fun solve2(problem: Problem): Solution {
    problem.libraries.sortByDescending { problem.totalScoreOf(it) }

    for (library in problem.libraries) {
        library.books.sortByDescending { problem.bookScores[it] ?: 0 }
    }

    val bookScores = problem.bookScores.toMutableMap()
    val solution = Solution()

    val remainingLibraries = problem.libraries.toMutableList()
    var remainingDays = problem.numberOfDays

    var iteration = 0
    while (remainingLibraries.isNotEmpty() && remainingDays > 0) {
        remainingLibraries.removeIf { it.signupTime >= remainingDays }
        if (remainingLibraries.isEmpty()) {
            break
        }
        remainingLibraries.sortWith(Comparator.comparing { library: Library ->
            val rankScore =
                (42 - (library.signupTime.toDouble() / library.score(bookScores, remainingDays).toDouble()))
            (rankScore * 1000).toInt()
        }.thenComparing { library -> 1 - library.signupTime }.reversed())

        val bestLibrary = remainingLibraries.removeAt(0)

        val score = bestLibrary.score(bookScores, remainingDays)
        val shippedBooks = bestLibrary.getShipableBooks(bookScores, remainingDays)
        for (book in shippedBooks) {
            bookScores.remove(book)
        }
        solution.libraryScans += LibraryScan(bestLibrary, shippedBooks)
        remainingDays -= bestLibrary.signupTime
        iteration++
        println("Remaining days: $remainingDays, Selected Library Score: ${score}")
    }
    return solution
}
