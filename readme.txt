【DB設定】
・MySQL
　バージョンは5.1です

　ユーザ：root
　パスワード：root
　データベース：shoeisha
　に接続するように設定ファイルを記述しています。
　適宜変更してください。
　　/DbUnitSample/src/test/resources/transactionDataSource_DaoUT.xml

　ddlフォルダのsql.ddlを実行することで、テスト用のテーブルを作成します。


【フォルダ構成】
src/main/java                           ソースフォルダ
src/test/java                           テストクラスフォルダ

src/main/resources                      各種設定ファイル
    aop.xml                             トランザクション設定
    customerDao.xml                     DAOクラス設定
    iBatisConfig.xml                    iBatis用設定
    log4j.xml                           log4j用設定
    sqlMapConfig.xml                    iBatis用設定
    transactionDataSource.xml           AOPによるトランザクション、データソース設定（JUnitでは不要）

src/test/resources                      テストクラス用各種設定ファイル
    transactionDataSource_DaoUT.xml     DAOテスト用トランザクション、データソース設定

lib                                     各種Jar
libUT                                   単体テスト用Jar


【テスト前準備】
1.c:\tmp\alogディレクトリを作成して下さい。
　log4jの出力先になります。
2.c:\tmp\test.datファイルを用意して下さい。
　ExcelでのBLOBデータの指定として、URIで指定したケースの為に必要です。
　ファイルサイズが大きすぎるとDbUnitの処理でエラーになってしまうので、数KB程度にしてください。
