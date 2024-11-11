package bibtex

import one.wabbit.parsing.charset.Topology
import one.wabbit.parsing.grammars.Simple
import one.wabbit.parsing.grammars.SimpleInput
import one.wabbit.parsing.grammars.SimpleResult
import kotlin.test.Test



class ParserSpec {

    val samples = listOf(
        """
            @PREAMBLE{
              "\newcommand{\noopsort}[1]{} "
              # "\newcommand{\singleletter}[1]{#1} " 
            }

            @string { 
              me = "Bart Kiers" 
            }

            @ComMENt{some comments here}

            % or some comments here

            @article{mrx05,
              auTHor = me # "Mr. X",
              Title = {Something Great}, 
              publisher = "nob" # "ody",
              YEAR = 2005,
              x = {{Bib}\TeX},
              y = "{Bib}\TeX",
              z = "{Bib}" # "\TeX",
            },

            @misc{ patashnik-bibtexing,
                   author = "Oren Patashnik",
                   title = "BIBTEXing",
                   year = "1988"
            } % no comma here

            @techreport{presstudy2002,
                author      = "Dr. Diessen, van R. J. and Drs. Steenbergen, J. F.",
                title       = "Long {T}erm {P}reservation {S}tudy of the {DNEP} {P}roject",
                institution = "IBM, National Library of the Netherlands",
                year        = "2002",
                month       = "December",
            }
        """.trimIndent(),

        """
            @Book{abramowitz+stegun,
             author    = "Milton {Abramowitz} and Irene A. {Stegun}",
             title     = "Handbook of Mathematical Functions with
                          Formulas, Graphs, and Mathematical Tables",
             publisher = "Dover",
             year      =  1964,
             address   = "New York City",
             edition   = "ninth Dover printing, tenth GPO printing"
            }
        """.trimIndent(),

        """
            @String{j-ANN-PHYS-1900-4       = "Annalen der Physik (1900) (series 4)"}

            @Article{Einstein:1901:FCG,
              author =       "Albert Einstein",
              title =        "{Folgerungen aus den Capillarit{\"a}tserscheinungen}.
                             ({German}) [{Consequences} of the capillarity
                             phenomenon]",
              journal =      j-ANN-PHYS-1900-4,
              volume =       "309",
              number =       "3",
              pages =        "513--523",
              year =         "1901",
              CODEN =        "ANPYA2",
              DOI =          "https://doi.org/10.1002/andp.19013090306",
              ISSN =         "0003-3804",
              ISSN-L =       "0003-3804",
              bibdate =      "Wed Nov 23 14:13:37 MST 2005",
              bibsource =    "http://www.math.utah.edu/pub/tex/bib/einstein.bib",
              note =         "This is Einstein's first published paper.",
              ZMnumber =     "32.0816.03",
              acknowledgement = ack-nhfb,
              Calaprice-number = "1",
              ajournal =     "Ann. Physik (1900) (ser. 4)",
              fjournal =     "Annalen der Physik (1900) (series 4)",
              language =     "German",
              Schilpp-number = "1",
              Whittaker-number = "1",
              xxvolume =     "4",
              ZMreviewer =   "Reg.-Rat Dr. Brix (Steglitz)",
            }
        """.trimIndent(),

        """
            @article{experiments,
            Annote = {Explosions are awesome!},
            Author = {John Doe},
            Date-Added = {2010-01-24 15:27:46 -0600},
            Date-Modified = {2010-01-31 15:10:57 -0600},
            Journal = {Scientific American},
            Month = {January},
            Pages = {46-62},
            Title = {Why Do We Learn Physics?},
            Year = {2010}}
        """.trimIndent(),

        """
            @article{1970,
             jstor_articletype = {misc},
             title = {Front Matter},
             author = {},
             journal = {Journal of Ecology},
             jstor_issuetitle = {},
             volume = {58},
             number = {1},
             jstor_formatteddate = {Mar., 1970},
             pages = {pp. i-ii},
             url = {http://www.jstor.org/stable/2258166},
             ISSN = {00220477},
             abstract = {This is a replacement string for this abstract.},
             language = {English},
             year = {1970},
             publisher = {British Ecological Society},
             copyright = {Copyright © 1970 British Ecological Society},
            }
        """.trimIndent(),

        """
            @article{AbedonHymanThomas2003,
              author = "Abedon, S. T. and Hyman, P. and Thomas, C.",
              year = "2003",
              title = "Experimental examination of bacteriophage latent-period evolution as a response to bacterial availability",
              journal = "Applied and Environmental Microbiology",
              volume = "69",
              pages = "7499--7506"
            }
        """.trimIndent(),

        """
            @incollection{Abedon1994,
              author = "Abedon, S. T.",
              title = "Lysis and the interaction between free phages and infected cells",
              pages = "397--405",
              booktitle = "Molecular biology of bacteriophage T4",
              editor = "Karam, Jim D. Karam and Drake, John W. and Kreuzer, Kenneth N. and Mosig, Gisela
                        and Hall, Dwight and Eiserling, Frederick A. and Black, Lindsay W. and Kutter, Elizabeth
                        and Carlson, Karin and Miller, Eric S. and Spicer, Eleanor",
              publisher = "ASM Press, Washington DC",
              year = "1994"
            }
        """.trimIndent(),

        """
            @misc{website:fermentas-lambda,
                  author = "Fermentas Inc.",
                  title = "Phage Lambda: description \& restriction map",
                  month = "November",
                  year = "2008",
                  url = "http://www.fermentas.com/techinfo/nucleicacids/maplambda.htm"
            }
        """.trimIndent(),

        """
            @article{blackholes,
                  author = "Rabbert Klein",
                  title = "Black Holes and Their Relation to Hiding Eggs",
                  journal = "Theoretical Easter Physics",
                  publisher = "Eggs Ltd.",
                  year = "2010",
                  note = "(to appear)"
            }
        """.trimIndent(),

        """
            @book{texbook,
              author = {Donald E. Knuth},
              year = {1986},
              title = {The {\TeX} Book},
              publisher = {Addison-Wesley Professional}
            }

            @book{latex:companion,
              author = {Frank Mittelbach and Michel Gossens
                        and Johannes Braams and David Carlisle
                        and Chris Rowley},
              year = {2004},
              title = {The {\LaTeX} Companion},
              publisher = {Addison-Wesley Professional},
              edition = {2}
            }

            @book{latex2e,
              author = {Leslie Lamport},
              year = {1994},
              title = {{\LaTeX}: a Document Preparation System},
              publisher = {Addison Wesley},
              address = {Massachusetts},
              edition = {2}
            }

            @article{knuth:1984,
              title={Literate Programming},
              author={Donald E. Knuth},
              journal={The Computer Journal},
              volume={27},
              number={2},
              pages={97--111},
              year={1984},
              publisher={Oxford University Press}
            }

            @inproceedings{lesk:1977,
              title={Computer Typesetting of Technical Journals on {UNIX}},
              author={Michael Lesk and Brian Kernighan},
              booktitle={Proceedings of American Federation of
                         Information Processing Societies: 1977
                         National Computer Conference},
              pages={879--888},
              year={1977},
              address={Dallas, Texas}
            }
        """.trimIndent(),

        """
            @article{knuth:1984,
              title={Literate Programming},
              author={Donald E. Knuth},
              journal={The Computer Journal},
              volume={27},
              number={2},
              pages={97--111},
              year={1984},
              publisher={Oxford University Press}
            }
        """.trimIndent(),

        """
            @inproceedings{FosterEtAl:2003,
              author = {George Foster and Simona Gandrabur and Philippe Langlais and Pierre
                Plamondon and Graham Russell and Michel Simard},
              title = {Statistical Machine Translation: Rapid Development with Limited Resources},
              booktitle = {Proceedings of {MT Summit IX}},
              year = {2003},
              pages = {110--119},
              address = {New Orleans, USA},
            }
        """.trimIndent(),

        """
            @phdthesis{Alsolami:2012,
                title    = {An examination of keystroke dynamics
                            for continuous user authentication},
                school   = {Queensland University of Technology},
                author   = {Eesa Alsolami},
                year     = {2012}
            }
        """.trimIndent(),

        """
            @inbook{peyret2012:ch7,
              title={Computational Methods for Fluid Flow},
              edition={2},
              author={Peyret, Roger and Taylor, Thomas D},
              year={1983},
              publisher={Springer-Verlag},
              address={New York},
              chapter={7, 14}
            }
        """.trimIndent(),

        """
            @incollection{Mihalcea:2006,
              author = {Rada Mihalcea},
              title = {Knowledge-Based Methods for {WSD}},
              booktitle = {Word Sense Disambiguation: Algorithms
                           and Applications},
              publisher = {Springer},
              year = {2006},
              editor = {Eneko Agirre and Philip Edmonds},
              pages = {107--132},
              address = {Dordrecht, the Netherlands}
            }
        """.trimIndent(),

        """
            @misc{web:lang:stats,
              author = {W3Techs},
              title = {Usage Statistics of Content Languages
                       for Websites},
              year = {2017},
              note = {Last accessed 16 September 2017},
              url = {http://w3techs.com/technologies/overview/content_language/all}
            }
        """.trimIndent(),

        """
            @Book{1987:nelson,
            author = {Edward Nelson},
            title = {Radically Elementary Probability Theory},
            publisher = {Princeton University Press},
            year = {1987}
            }
        """.trimIndent(),

        """
            @article{Knuth92,
                    author = "D.E. Knuth",
                    title = "Two notes on notation",
                    journal = "Amer. Math. Monthly",
                    volume = "99",
                    year = "1992",
                    pages = "403--422",
            }

            @book{ConcreteMath,
                    author = "R.L. Graham and D.E. Knuth and O. Patashnik",
                    title = "Concrete mathematics",
                    publisher = "Addison-Wesley",
                    address = "Reading, MA",
                    year = "1989"
            }

            @unpublished{Simpson,
                    author = "H. Simpson",
                    title = "Proof of the {R}iemann {H}ypothesis",
                    note = "preprint (2003), available at 
                    \texttt{http://www.math.drofnats.edu/riemann.ps}",
            }

            @incollection{Er01,
                    author = "P. Erd{\H o}s",
                    title = "A selection of problems and results in combinatorics",
                    booktitle = "Recent trends in combinatorics (Matrahaza, 1995)",
                    publisher = "Cambridge Univ. Press",
                    address = "Cambridge",
                    pages = "1--6"
            }
        """.trimIndent()
    )

    @Test fun test() {
//        with (Topology.charRanges) {
//            val graph = BibTexParser.root.toGraph()
////        println(graph)
////        println(Ref(test1))
//            println(graph.nodeInfo()[Ref(BibTexParser.root)])
//            println(graph.show())
//            println("==========================")
//            println(graph.voidAndSimplify().show())
//        }

        val p = with (Topology.charRanges) {
            val p = Simple.compile(BibTexParser.root)
            return@with { input: String -> p.parseAll(SimpleInput.StringInput(input, 0)) }
        }

        for (input in samples) {
            val result = p(input)

            if (result !is SimpleResult.Success) {
                println(input)
                println("====================================")
                println(result)
                println("====================================")
            }
        }
    }
}
