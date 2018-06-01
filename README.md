# これは何
kotlinでyahoo画像検索から南ことりを集めるスクリプト。

# 起動方法
`docker-compose build`  
のあとに  
`docker-compose run`

'save'ディレクトリの中に連番で画像が入る

# 設定変更
`src/main/kotlin/scraping.kt`
の中の
`search_word`
`limit`
を変更する。

# その他
スクレイピングは相手に負荷をかけるため
自己責任でお願いします
