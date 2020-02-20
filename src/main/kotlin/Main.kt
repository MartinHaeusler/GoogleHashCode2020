import java.io.File

typealias BookId = Int
typealias BookScore = Int

class Library(
    val id: Int,
    val numberOfBooks: Int,
    val signupTime: Int,
    val booksShippedPerDay: Int,
    val books: Set<BookId>
)

class Problem(
    val numberOfBooks: Int,
    val numberOfLibraries: Int,
    val numberOfDays: Int,
    val bookScores: MutableMap<BookId, BookScore> = mutableMapOf(),
    val libraries: MutableList<Library> = mutableListOf()
)

class Solution(
    val libraryScans: List<LibraryScan>
) {

    fun toOutputFormat(): String {
        val builder = StringBuilder()
        builder.append(this.libraryScans.size)
        builder.append("\n")
        for(libraryScan in this.libraryScans){
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

fun main(args: Array<String>) {
    if(args.size != 1){
        throw IllegalArgumentException("Expected 1 argument (the book file)!")
    }
    val problem = parseInputFile(File(args[0]))
    val solution = solve(problem)
    printToFile(solution, File(args[0] + ".solution.txt"))
}

private fun parseInputFile(file: File): Problem {
    if (!file.exists() || !file.isFile) {
        throw IllegalArgumentException("File does not exist or is not a file: ${file.absolutePath}")
    }
    val lines = file.readLines().toMutableList()
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
            books = bookIds
        )
    }
    return problem
}


fun printToFile(solution: Solution, file: File) {
    file.delete()
    file.createNewFile()
    file.writeText(solution.toOutputFormat())
}

fun solve(problem: Problem): Solution {
    TODO()
}
