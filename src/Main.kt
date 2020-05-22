import codegeneration.CodeGenerator
import sablecc.lexer.LexerException
import sablecc.node.TWhitespace
import sablecc.node.Token
import sablecc.parser.Parser
import sablecc.parser.ParserException
import semantics.contextualConstraints.ContextualConstraintAnalyzer
import semantics.symbolTable.SymbolTableBuilder
import semantics.typeChecking.TypeChecker
import java.io.File
import java.io.FileNotFoundException
import java.lang.Exception


fun main(args: Array<String>) {
    if (args.size != 2) {
        println("Invalid arguments, run with: dumpling <input_path> <output_path>")
        return
    }

    val inputPath: String = args[0]
    val outputPath: String = args[1]


    val inputReader = try {
        File(inputPath).reader()
    } catch (e: FileNotFoundException) {
        println("Input file does not exist:\n$inputPath")
        return
    } catch (e: Exception) {
        println("Unexpected error loading file.")
        return
    }

    val output = DumplingCompiler().compile(inputReader)

    if (output != null) {
        val outputFile = File(outputPath)

        outputFile.writeText(output)
    }
}

fun formatToSabbleCC(lines: List<String>) {
    val match = "\"([^)]*)\"".toRegex()
    val namematch = "[.]([^)]*)[)]".toRegex()

    for (l in lines) {
        val m = match.find(l)
        val n = namematch.find(l)
        if (m != null && n != null) {
            val mk = m.groupValues[1]
            val nk = n.groupValues[1].toLowerCase()
            println("$nk = '$mk';")
        }
    }
}