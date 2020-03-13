


/* EBNF for input ({} are for structure () are actual characters):
    Node = identifier {( Node* )}?
*/
fun printTree(str: String) {
    val indents = mutableListOf<Int>()

    var i = 0
    while (i < str.length) {
        var name = ""
        while (str[i] != '(') name += str[i]

        println(name)
    }
}