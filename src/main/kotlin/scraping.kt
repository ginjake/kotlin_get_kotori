import org.jsoup.Jsoup
import khttp.get

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.HttpStatus
import org.apache.http.util.EntityUtils
import java.nio.file.Paths
import java.nio.file.Files

var search_word = "南ことり"
// 複数ワードのときは空白入れる
//var search_word = "南ことり メイド"
val limit = 40 //40の倍数単位でしか取得できない

var UA = "Mozilla/5.0"

/*
    APIにわたす値(.crumb,.ncrumb)を取得する。
    javascriptの中に定義されているんので正規表現で頑張る
    User-Agentが適当だとその箇所のjavascript自体が生成されない。
 */
fun getCrumb(q_search_word:String) :Map<String,String> {

    var ncrumb = ""
    var crumb = ""
    try {
        var html =Jsoup.connect("https://search.yahoo.co.jp/image/search")
                .timeout(1000000)
                .userAgent(UA)
                .data("p",q_search_word)
                .get()
                .body().toString()

        ncrumb = Regex("pg_ncrumb: \"(.+)\"").find(html)?.groups?.get(1)?.value ?: throw IllegalArgumentException()
        crumb = Regex("(.*)(?=\\=).").find(ncrumb.toString())?.groups?.get(0)?.value ?: throw IllegalArgumentException()
    } catch (e: Exception) {
        println("【crumb値の取得に失敗しました】")
        System.exit(-1)
    }

    return mapOf(
            "crumb" to crumb,
            "ncrumb" to ncrumb
    )
}

/*
APIを叩き、htmlを返す。
jsonで帰ってくるが、resultの中は実質htmlのため
 */
fun createHTML(q_search_word:String,page:Int,crumb:Map<String,String>) : String{
    var html:String = ""
    try {
        var getData  = mapOf(
              "n" to "40",
              "b" to (21+page).toString(),
              "p" to q_search_word,
              "ei" to "UTF-8",
              ".crumb" to crumb.get("crumb").toString(),
              ".ncrumb" to crumb.get("ncrumb").toString(),
              "p_ex" to q_search_word
        )
        var response = get("https://search.yahoo.co.jp/image/paginationjs",params = getData).jsonObject
        html = response["result"].toString().replace("\\","")
    } catch (e: Exception) {
        println("【画像リスト取得に失敗しました")
        System.exit(-1)
    }
  return html.toString()
}

fun main(args: Array<String>) {
    val Crumb:Map<String,String> = getCrumb(search_word)

    var counter: Int = 0
    for (i in 0..((limit/40))) {
        val HTML: String = createHTML(search_word, i, Crumb)
        val doc = Jsoup.parse(HTML)

        for (element in doc.select("p.tb a")) {
            val url = element.attr("href")
            try {
                val client = DefaultHttpClient()
                val request = HttpGet(url)
                // add request header
                request.addHeader("User-Agent", UA)
                val response = client.execute(request)
                val status = response.getStatusLine().getStatusCode()
                if (HttpStatus.SC_OK == status) {
                        //ファイルの保存処理
                        val entity = response.entity
                        val file_type:String = Regex(".+(jpeg|png|gif|bmp|webp)").find(entity.contentType.toString())?.groups?.get(1)?.value ?: throw IllegalArgumentException()
                        val FILENAME:String = "save/"+counter.toString()+"."+file_type
                        Files.write(Paths.get(FILENAME), if (entity == null) ByteArray(0) else EntityUtils.toByteArray(entity))
                        println(counter.toString() + ":" + url)
                        counter++

                } else {
                    println("【ファイル取得に失敗しました】"+url)
                }
            } catch (e: Exception) {
                println("【ファイルの取得通信に失敗しました】"+url)
            }
        }
    }

}

