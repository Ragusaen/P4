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