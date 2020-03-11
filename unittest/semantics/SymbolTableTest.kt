package semantics

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.Error
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class SymbolTableTest {

    @Test
    fun addVariableIsActuallyAdded() {
        val st = SymbolTable()
        val expected = "Int"

        st.add("a", Identifier("Int"))
        val actual = st.find("a").type

        assertEquals(expected, actual)
    }

    @Test
    fun findSymbolFromLowerScope() {
        val st = SymbolTable()
        val expected = Identifier("a")
        val identifier = Identifier("a")

        st.add("a", identifier)
        st.openScope()
        val actual = st.find("a")

        assertEquals(expected,actual)
    }

    @Test
    fun addThrowsExceptionWhenVariableIsAlreadyDeclared() {
        val st = SymbolTable()

        st.add("a", Identifier("Float"))

        assertThrows<IdentifierAlreadyDeclaredException> { st.add("a", Identifier("Int")) }
    }

    @Test
    fun findThrowsExceptionWhenVariableIsUsedBeforeDeclaration() {
        val st = SymbolTable()

        assertThrows<IdentifierUsedBeforeDeclarationException> { st.find("a") }
    }

    @Test
    fun closingScopeZeroThrowsException() {
        val st = SymbolTable()

        assertThrows<CloseScopeZeroException> { st.closeScope() }
    }

    @Test
    fun locallyDeclaredVariableIsSetAsLocallyDeclared() {
        val st = SymbolTable()

        st.openScope()
        st.add("a",Identifier("Int"))

        assertTrue { st.declaredLocally("a") }
    }

    @Test
    fun locallyDeclaredVariableExpectFalse(){
        val st = SymbolTable()
        val expected = false

        st.add("a", Identifier("Int"))
        st.openScope()
        val actual = st.declaredLocally("a")

        assertEquals(expected, actual)
    }
}