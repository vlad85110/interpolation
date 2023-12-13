import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.StringBuilder
import kotlin.math.log10
import kotlin.math.pow

fun main() {
    val f = {x: Double ->
        log10(x) + 7 / (2 * x + 6)
    }

    val x = 24.0
    val interval = listOf(0.0, 3.0)
    val nodesCnt = 3

//    var points = makeGrid(f, interval, nodesCnt)
    var points = mapOf<Double, Double>(
        0.0 to 1.6, 1.5 to 1.0, 3.0 to 0.87
    )

    var map = polynomial(x, points)
    println("Погрешность: ${map["res"] as Double - f(x)}")
    val strF = map["strF"] as String

    points = makeGrid(f, interval, nodesCnt * 2)
    map = polynomial(x, points)
    println("Погрешность на удвоенной сетке: ${map["res"] as Double - f(x)}")
    val doubleGrid = map["strF"] as String

    createGraphics(strF, doubleGrid)
}

fun dividedDifference(points: Map<Double, Double>): Double {
    var res = 0.0
    val keys = points.keys.toList()

    for (j in keys.indices) {
        var exp = 1.0
        for (l in keys.indices) {
            if (l != j) {
                exp *= keys[j] - keys[l]
            }
        }
        res += points[keys[j]]!! / exp
    }

    return res
}

fun polynomial(x: Double, points: Map<Double, Double>): Map<String, Any> {
    val builder = StringBuilder()

    val diffPoints = HashMap<Double, Double>()

    val xs = points.keys.toList().sorted()
    var res = points[xs.first()]!!
    builder.append(res)
    diffPoints[xs.first()] = points[xs.first()]!!
    for (i in xs.indices) {
        if (i == 0) continue
        builder.append(" + ")

        diffPoints[xs[i]] = points[xs[i]]!!
        val difference = dividedDifference(diffPoints)
        builder.append("%.5f".format(difference))

        var exp = 1.0
        for (j in 0 until i) {
            exp *= x - xs[j]
            builder.append("(x - ${xs[j]})")
        }

        res += difference * exp
    }

    val strF = builder.toString().replace(",", ".").replace("+ -", "- ")

    val map = HashMap<String, Any>()
    map["res"] = res
    map["strF"] = strF
    return map
}

fun makeGrid(f: (Double) -> Double, interval: List<Double>, nodesCnt: Int): Map<Double, Double> {
    val points = HashMap<Double, Double>()

    val startPoint = interval.first()
    val length = interval.last() - interval.first()
    var cnt = 0

    do {
        val nextPoint = startPoint + length * cnt / (nodesCnt - 1)
        points[nextPoint] = f(nextPoint)
        cnt += 1
    } while (cnt != nodesCnt)

    return points
}

fun createGraphics(f: String, doubleGrid: String) {
    val templateReader = DataInputStream(FileInputStream("src/main/resources/template.html"))

    val html = File("src/main/resources/graphics.html")
    html.createNewFile()
    val htmlWriter = DataOutputStream(FileOutputStream(html))
    val htmlStr = String(templateReader.readAllBytes())
    val newStr = htmlStr.replace("%f", f).replace("%d", doubleGrid)
    htmlWriter.write(newStr.toByteArray())
}