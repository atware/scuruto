package lib

import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.util
import javax.imageio.ImageIO

import org.apache.commons.codec.binary.Base64
import org.parboiled.common.{ ImmutableList, StringUtils }
import org.parboiled.support.StringBuilderVar
import org.parboiled.{ BaseParser, Rule }
import org.pegdown._
import org.pegdown.ast.{ AbstractNode, Node, Visitor }
import org.pegdown.plugins.{ BlockPluginParser, InlinePluginParser, PegDownPlugins, ToHtmlSerializerPlugin }
import org.scilab.forge.jlatexmath.{ ParseException, TeXConstants, TeXFormula }

import scala.collection.JavaConverters._
import scala.util.Try

class Markdown private (val source: String) {

  def raw: String = source

  def html: String = {
    val processor = new PegDownProcessor(Extensions.ALL | Extensions.SUPPRESS_HTML_BLOCKS, PegDownPlugins.builder().withPlugin(classOf[TexParser]).build())
    val serializer = new ToHtmlSerializer(new LinkRenderer(), List[ToHtmlSerializerPlugin](new TexSerializer()).asJava)

    serializer.toHtml(processor.parseMarkdown(source.toCharArray))
  }
}

object Markdown {
  def apply(source: String) = new Markdown(source)

  def toHtml(source: String) = Markdown(source).html
}

class TexParser
    extends Parser(Extensions.ALL, 1000l, Parser.DefaultParseRunnerProvider)
    with InlinePluginParser with BlockPluginParser {

  override def inlinePluginRules(): Array[Rule] = {
    Array(rule("$[", "]$"))
  }

  override def blockPluginRules(): Array[Rule] = {
    Array(rule("$$$", block = true))
  }

  def rule(start: String, end: String = "", block: Boolean = false): Rule = {
    val startMark = String(start)
    val endMark = if (end.isEmpty) startMark else String(end)
    val startRule = if (block) Sequence(startMark, OneOrMore(AnyOf(" \r\n\t"))) else startMark
    val endRule = if (block) Sequence(OneOrMore(AnyOf(" \r\n\t")), endMark, Sp(), TestNot(NotNewline())) else endMark

    val latex = new StringBuilderVar()
    NodeSequence(
      startRule,
      OneOrMore(
        if (block) TestNot(endRule) else Sequence(TestNot(endRule), NotNewline()),
        BaseParser.ANY,
        BaseParser.ACTION(latex.append(matchedChar()))
      ),
      endRule,
      BaseParser.ACTION(push(new TexNode(latex.getString.trim, block)))
    )
  }
}

class TexNode(val latex: String, val block: Boolean) extends AbstractNode() {
  override def toString: String = {
    super.toString + " '" + StringUtils.escape(latex) + '\''
  }

  def getChildren: util.List[Node] = ImmutableList.of[Node]

  def accept(visitor: Visitor) = visitor.visit(this)

}

class TexSerializer extends ToHtmlSerializerPlugin {
  override def visit(node: Node, visitor: Visitor, printer: Printer): Boolean = {
    node match {
      case texNode: TexNode =>
        TexSerializer.convert(texNode.latex).fold(
          err => printer
            .print("<pre>")
            .printEncoded(texNode.latex)
            .print("<p style=\"color: red\">")
            .printEncoded(err)
            .print("</p></pre>"),
          img =>
            if (texNode.block)
              printer
              .print("<p class=\"jlatex\"><img src=\"data:image/png;base64,")
              .print(Base64.encodeBase64String(img))
              .print("\" alt=\"$$$ ")
              .printEncoded(texNode.latex.replaceAll("\r\n?|\n", " "))
              .print(" $$$\" /></p>")
            else
              printer
                .print("<span class=\"jlatex\"><img src=\"data:image/png;base64,")
                .print(Base64.encodeBase64String(img))
                .print("\" alt=\"$[")
                .printEncoded(texNode.latex.replaceAll("\r\n?|\n", " "))
                .print("]$\" /></span>")
        )
        true
      case _ =>
        false
    }
  }
}

object TexSerializer {

  private val cache = LRUCache[String, Either[String, Array[Byte]]](100)

  private def convert(latex: String): Either[String, Array[Byte]] = {
    cache.getOrElse(latex) {
      val formulaTry = Try {
        new TeXFormula(latex)
      }
      formulaTry.map(formula => {
        val image = formula.createBufferedImage(TeXConstants.STYLE_DISPLAY, 20, null, null).asInstanceOf[BufferedImage]
        val out = new ByteArrayOutputStream()
        ImageIO.write(image, "png", out)
        Right(out.toByteArray)
      }).recover {
        case t: ParseException => Left(t.getMessage)
        case _ => Left("Unknown parsing error.")
      }.get
    }
  }
}
