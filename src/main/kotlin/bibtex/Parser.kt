package bibtex

import bibtex.BibTexParser.plus
import one.wabbit.parsing.charset.CharSet
import one.wabbit.parsing.charset.Topology
import one.wabbit.parsing.grammars.*

// https://en.wikipedia.org/wiki/BibTeX
// https://www.bibtex.com/g/bibtex-format/
// https://www.overleaf.com/learn/latex/Bibliography_management_with_bibtex
// http://www2.phys.canterbury.ac.nz/~jcw83/jcwdocs/latex/bibtexbasics.html
// https://bibtexml.sourceforge.net/btxdoc.pdf
// https://www.ub.uio.no/english/subjects/naturalscience-technology/informatics/bibtexfolder.pdf
// https://d31kydh6n6r5j5.cloudfront.net/uploads/sites/106/2019/06/physicsbibtex.pdf
// https://ftp.math.utah.edu/pub/bibnet/bibtex-info.html
// https://ctan.math.illinois.edu/macros/latex/contrib/biblatex/doc/biblatex.pdf
// https://faculty.math.illinois.edu/~hildebr/tex/bibliographies.html
// https://en.wikibooks.org/wiki/LaTeX/Bibliography_Management


data class BibTexEntry(val type: String, val key: String, val fields: Map<String, BibTexValue>)
sealed class BibTexValue
data class StringValue(val value: String) : BibTexValue()
data class IdentifierValue(val value: String) : BibTexValue()
data class ConcatenatedValue(val values: List<BibTexValue>) : BibTexValue()

typealias P<A> = Parser<CharSet, Char, A>


object BibTexParser : Parser.CharModule() {
    val letterCS        = CharSet.unicodeLetter
    val letterOrDigitCS = CharSet.unicodeLetterOrDigit
    val operatorCS      = CharSet.of("+-*/=<>!&|?~^%$@")
    val whitespaceCS    = CharSet.unicodeWhitespace
    val notLineEndingCS = CharSet.of('\n').invert()

    val identifierCS = CharSet.unicodeLetterOrDigit union CharSet.of("-_:+")

    val identifierChar = char(identifierCS)
    val letterOrDigit = char(letterOrDigitCS)

    val whitespace: P<Unit> = run {
        val whitespace = whitespaceCS.ignore
        val notLE = char(notLineEndingCS)
        val comment = '%'.ignore + notLE.many.string + '\n'.ignore
        (whitespace or comment).many.ignore.namedOpaque("whitespace")
    }

    fun token(value: String): P<String> =
        (asciiCI(value) + whitespace).namedOpaque(value)
    fun token(value: Char): P<String> =
        (asciiCI(value.toString()) + whitespace).namedOpaque(value.toString())
    fun token(parser: Parser<CharSet, Char, String>): P<String> =
        (parser + whitespace)

    val COMMA  = token(",")
    val EQUALS = token("=")
    val HASH   = token("#")

    fun genericString1(startDelimiter: Char, endDelimiter: Char): P<String> {
        val notDelimiter = char(CharSet.of(startDelimiter, endDelimiter).invert())
        return token((-char(startDelimiter)
                + notDelimiter.many.string
                + -char(endDelimiter)))
    }

    fun genericString2(startDelimiter: Char, endDelimiter: Char): P<String> {
        val notDelimiter = char(CharSet.of(startDelimiter, endDelimiter).invert())
        return token((char(startDelimiter).ignore
                + (notDelimiter.ignore or delay { genericString2(startDelimiter, endDelimiter).ignore }).many.string
                + char(endDelimiter).ignore))
    }

    val identifier: P<String> =
        token((identifierChar.many.string).named("identifier"))

    val stringLiteral: P<String> =
        (genericString1('"', '"') or genericString2('{', '}')).named("stringLiteral")

    val stringValue: P<BibTexValue> =
        stringLiteral.map { StringValue(it) } or
                identifier.map { IdentifierValue(it) }

    val concatenatedValue: P<BibTexValue> = (stringValue.sepBy1(HASH)).map { ConcatenatedValue(it) }

    val entryType: P<String> = token('@').ignore + identifier

    val entryKey: P<String> = token(identifierChar.many.string)

    val entryField: P<Pair<String, BibTexValue>> = identifier + EQUALS.ignore + stringValue

    val entry: P<BibTexEntry> = (
            entryType + token("{").ignore + entryKey + COMMA.ignore +
                    (entryField.sepBy(COMMA) or entryField.many) +
                    COMMA.opt.ignore +
                    token("}").ignore
            )
        .map { BibTexEntry(it.first.first, it.first.second, it.second.associate { it }) }

    val root: P<List<BibTexEntry>> = whitespace + entry.many1

    fun parse(input: String): List<BibTexEntry> {
        val p = with (Topology.charRanges) {
            val p = Simple.compile(BibTexParser.root)
            return@with { input: String -> p.parseAll(SimpleInput.StringInput(input, 0)) }
        }

        val result = p(input)

        if (result is SimpleResult.Success) {
            return result.value
        } else {
            throw Exception("Parse error: ${result}")
        }
    }
}
