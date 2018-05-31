import org.jsoup.Jsoup

//Jsoupが使えているかのテスト。引数とかはまだ特に関係ない。
class Hoge(val url: String) {
    fun test() {
        val doc = org.jsoup.Jsoup.parse("<div id='a'>aiueo</div><div id='b'>bbb</div>")
        println(doc.select("#a"))
    }
}


fun main(args: Array<String>) {
    val fe = Hoge("test")
    fe.test()

}

