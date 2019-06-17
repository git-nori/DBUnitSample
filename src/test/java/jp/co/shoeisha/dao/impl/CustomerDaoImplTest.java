package jp.co.shoeisha.dao.impl;

import static org.dbunit.Assertion.assertEqualsByQuery;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.List;

import javax.sql.DataSource;

import jp.co.shoeisha.dao.CustomerDao;
import jp.co.shoeisha.exception.DuplicateException;
import jp.co.shoeisha.exception.NotFoundException;
import jp.co.shoeisha.model.Customer;

import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.excel.XlsDataSet;
import org.dbunit.dataset.xml.XmlDataSet;
import org.dbunit.ext.mysql.MySqlConnection;
import org.dbunit.operation.DatabaseOperation;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class CustomerDaoImplTest {

    /** ClassPathXmlApplicationContext. */
    private static ClassPathXmlApplicationContext context = null;

    /** テスト対象DAOクラス. */
    private CustomerDao dao;

    @BeforeClass
    public static void init() {
        //Spring類の設定ファイルを読み込む
        context = new ClassPathXmlApplicationContext(new String[] {
                "iBatisConfig.xml",
                "customerDao.xml",
                "transactionDataSource_DaoUT.xml",
                "aop.xml"});
    }


    @Before
    public void before() throws Exception {
        dao = (CustomerDao) context.getBean("customerDao");

        Connection con = createConnection();

        /** バックアップ処理 **/
        //
        // DbUnitでのコネクション確立
        //
        // MySQL5.1の場合は、スキーマ名にnullを設定
        IDatabaseConnection dbUnitConnection = createIDatabaseConnection(con);

        //
        // テスト前のDBの状態をバックアップ
        //

        // 接続先に登録されている全てのテーブルが取得対象
        IDataSet dataSet = dbUnitConnection.createDataSet();
        File backUpFile = new File("backUp.xml");
        // FileにXML形式で出力
        OutputStream os = null;
        try {
            os = new FileOutputStream(backUpFile);
            XmlDataSet.write(dataSet, os, "UTF-8");
        } finally {
            if (os != null) {
                os.close();
            }
        }

        /** テストを実施する為にデータを設定 */
        // Excelファイルを読み込み、IDataSetインスタンスを生成
        File importFile = new File("Excelフォーマット.xls");
        InputStream is = null;
        try {
            is = new FileInputStream(importFile);
            IDataSet importDataSet = new XlsDataSet(is);

            // IDataSetインスタンスをDBに反映
            DatabaseOperation.CLEAN_INSERT.execute(dbUnitConnection, importDataSet);

        } finally {
            if (is != null) {
                is.close();
            }
            con.commit();
            dbUnitConnection.close();
        }
    }

    @After
    public void after() throws Exception {

        Connection con = createConnection();
        IDatabaseConnection dbUnitConnection = createIDatabaseConnection(con);

        /** バックアップしたファイルを元に、テスト前の状態に戻す */

        // Excelファイルを読み込み、IDataSetインスタンスを生成
        File backUpFile = new File("backUp.xml");
        InputStream is = null;
        try {
            is = new FileInputStream(backUpFile);
            IDataSet importDataSet = new XmlDataSet(is);

            // IDataSetインスタンスをDBに反映
            DatabaseOperation.CLEAN_INSERT.execute(dbUnitConnection, importDataSet);
        } finally {
            if (is != null) {
                is.close();
            }
            con.commit();
            dbUnitConnection.close();
        }
    }

    /**
     * 主キーでのSELECTテスト.
     */
    @Test
    public void testFindByKey() {

        Customer ret = null;
        /** データが取得できることの確認 */
        try {
            ret = dao.findByKey(1L, "01");
        } catch (NotFoundException e) {
            // 取れなかったらテスト失敗
            fail();
        }
        // 取得できた項目のテスト
        // 期待値との比較をカラムに設定された値毎に行う
        assertThat(ret.getCustomerCd(), is(1L));
        assertThat(ret.getCustomerType(), is("01"));
        assertThat(ret.getCustomerName(), is("顧客名=1"));
        assertThat(ret.getCreateDateTime(), is(Timestamp.valueOf("2009-01-01 01:23:45.0")));
        assertThat(ret.getLastUpdateDateTime(), is(Timestamp.valueOf("2009-02-01 17:06:39.0")));

        /** もし、nullを許可するカラムがあれば・・・ */
        // データが取得できることの確認
        try {
            ret = dao.findByKey(2L, "03");
        } catch (NotFoundException e) {
            // 取れなかったらテスト失敗
            fail();
        }
        assertThat(ret.getCustomerCd(), is(2L));
        assertThat(ret.getCustomerType(), is("03"));
        assertThat(ret.getCustomerName(), is(nullValue()));
        assertThat(ret.getCreateDateTime(), is(Timestamp.valueOf("2009-01-12 01:23:45.0")));
        assertThat(ret.getLastUpdateDateTime(), is(Timestamp.valueOf("2009-02-11 17:06:39.0")));

        /** 該当レコードが存在しない場合のテスト */
        try {
            // 第1引数に存在する値はあるが、第2引数に存在するデータは無い
            ret = dao.findByKey(1L, "AA");
            // 取れたらテスト失敗
            fail();
        } catch (NotFoundException e) {
            // ExceptionがthrowされればOK
        }

        /** 該当レコードが存在しない場合のテスト */
        try {
            // 第1引数に存在する値は無いが、第2引数に存在するデータはある
            ret = dao.findByKey(9999L, "01");
            // 取れたらテスト失敗
            fail();
        } catch (NotFoundException e) {
            // ExceptionがthrowされればOK
        }

        /** 引数にnullを設定した場合のテスト */
        try {
            // 第1引数に存在する値は無いが、第2引数に存在するデータはある
            ret = dao.findByKey(1L, null);
            // 取れたらテスト失敗
            fail();
        } catch (NotFoundException e) {
            // NotFoundExceptionはテスト失敗
            fail();
        } catch (IllegalArgumentException e) {
            // 仕様上IllegalArgumentExceptionがthrowされればOK
        }
    }

    /**
     * 複数件取得できる場合のSELECTテスト.
     */
    @Test
    public void tesstFindByCustomerCd() {

        /** 通常ケース */
        List < Customer > retList;
        retList = dao.findByCustomerCd(10L);

        // 取得した件数の確認
        assertThat(retList.size(), is(3));
        // 常にこの順番で取得できることから、order byのテストも兼ねられる
        // リストの0番目の取得情報確認
        Customer ret = retList.get(0);
        assertThat(ret.getCustomerCd(), is(10L));
        assertThat(ret.getCustomerType(), is("01"));
        assertThat(ret.getCustomerName(), is("顧客名はあいう"));
        assertThat(ret.getCreateDateTime(), is(Timestamp.valueOf("2009-01-05 01:23:45.0")));
        assertThat(ret.getLastUpdateDateTime(), is(Timestamp.valueOf("2009-02-06 17:06:39.0")));

        // リストの1番目の取得情報確認
        ret = retList.get(1);
        assertThat(ret.getCustomerCd(), is(10L));
        assertThat(ret.getCustomerType(), is("02"));
        assertThat(ret.getCustomerName(), is("顧客名はアイウ"));
        assertThat(ret.getCreateDateTime(), is(Timestamp.valueOf("2009-01-06 01:23:45.0")));
        assertThat(ret.getLastUpdateDateTime(), is(Timestamp.valueOf("2009-02-07 17:06:39.0")));

        // リストの2番目の取得情報確認
        ret = retList.get(2);
        assertThat(ret.getCustomerCd(), is(10L));
        assertThat(ret.getCustomerType(), is("03"));
        assertThat(ret.getCustomerName(), is("顧客名はかきく"));
        assertThat(ret.getCreateDateTime(), is(Timestamp.valueOf("2009-01-04 01:23:45.0")));
        assertThat(ret.getLastUpdateDateTime(), is(Timestamp.valueOf("2009-02-05 17:06:39.0")));

        /** 該当レコードが存在しない場合の確認 */
        retList = dao.findByCustomerCd(9999L);
        assertThat(retList.size(), is(0));


        /** nullを設定した場合のテスト */
        try {
            dao.findByCustomerCd(null);
            fail();
        } catch (IllegalArgumentException e) {
            // 仕様上IllegalArgumentExceptionがthrowされればOK
        }

    }

    /**
     * 動的に検索条件が変わる場合のSELECTテスト.
     */
    @Test
    public void testFindByAny() {

        /** customerCdを検索条件として設定 */
        Customer keys = new Customer();
        keys.setCustomerCd(4L);
        List < Customer > retList;
        retList = dao.findByAny(keys);

        // 件数の確認
        assertThat(retList.size(), is(3));

        // 常にこの順番で取得できることから、order byのテストも兼ねられる
        // リストの0番目の取得情報確認
        Customer ret = retList.get(0);
        assertThat(ret.getCustomerCd(), is(4L));
        assertThat(ret.getCustomerType(), is("06"));
        assertThat(ret.getCustomerName(), is("shoeisha1"));
        assertThat(ret.getCreateDateTime(), is(Timestamp.valueOf("2009-01-01 02:23:45.0")));
        assertThat(ret.getLastUpdateDateTime(), is(Timestamp.valueOf("2009-02-01 18:06:39.0")));

        // リストの1番目の取得情報確認
        ret = retList.get(1);
        assertThat(ret.getCustomerCd(), is(4L));
        assertThat(ret.getCustomerType(), is("07"));
        assertThat(ret.getCustomerName(), is("shoeisha2"));
        assertThat(ret.getCreateDateTime(), is(Timestamp.valueOf("2009-02-01 03:23:45.0")));
        assertThat(ret.getLastUpdateDateTime(), is(Timestamp.valueOf("2009-03-01 19:06:39.0")));

        // リストの2番目の取得情報確認
        ret = retList.get(2);
        assertThat(ret.getCustomerCd(), is(4L));
        assertThat(ret.getCustomerType(), is("08"));
        assertThat(ret.getCustomerName(), is("shoeisha3"));
        assertThat(ret.getCreateDateTime(), is(Timestamp.valueOf("2009-03-01 04:23:45.0")));
        assertThat(ret.getLastUpdateDateTime(), is(Timestamp.valueOf("2009-04-01 20:06:39.0")));

        /** customerNameを検索条件として設定 */
        keys = new Customer();
        keys.setCustomerName("顧客名=");
        retList = dao.findByAny(keys);
        // 件数の確認
        assertThat(retList.size(), is(3));

        // ※DBのカラムとプロパティのマッピングのテストは
        // 前で確認済みなので、以降は、Key情報だけ確認すれば良い
        ret = retList.get(0);
        assertThat(ret.getCustomerCd(), is(1L));
        assertThat(ret.getCustomerType(), is("01"));

        ret = retList.get(1);
        assertThat(ret.getCustomerCd(), is(2L));
        assertThat(ret.getCustomerType(), is("02"));

        ret = retList.get(2);
        assertThat(ret.getCustomerCd(), is(3L));
        assertThat(ret.getCustomerType(), is("03"));

        /** customerCdとcustomerNameを検索条件として設定 */
        keys = new Customer();
        keys.setCustomerCd(3L);
        keys.setCustomerName("shoeisha");
        retList = dao.findByAny(keys);
        // 件数の確認
        assertThat(retList.size(), is(2));

        ret = retList.get(0);
        assertThat(ret.getCustomerCd(), is(3L));
        assertThat(ret.getCustomerType(), is("AA"));

        ret = retList.get(1);
        assertThat(ret.getCustomerCd(), is(3L));
        assertThat(ret.getCustomerType(), is("BC"));

        /** 該当レコードが存在しない場合 */
        keys = new Customer();
        keys.setCustomerCd(9999L);
        retList = dao.findByAny(keys);
        // 件数の確認
        assertThat(retList.size(), is(0));

        /** nullを設定した場合のテスト */
        try {
            dao.findByAny(null);
            fail();
        } catch (IllegalArgumentException e) {
            // 仕様上IllegalArgumentExceptionがthrowされればOK
        }
    }

    /**
     * INSERTのテスト.
     *
     * @throws Exception 例外
     */
    @Test
    public void testInsert() throws Exception {
        /** 正常パターン */
        Customer target = new Customer();
        target.setCustomerCd(4L);
        target.setCustomerType("12");
        target.setCustomerName("片桐");
        target.setCreateDateTime(Timestamp.valueOf("2009-07-01 04:23:45.0"));
        target.setLastUpdateDateTime(Timestamp.valueOf("2009-09-23 07:01:07.0"));

        try {
            dao.insert(target);
        } catch (DuplicateException e) {
            // 一意制約に違反しないデータを入れたのにエラーになるのはバグ
            fail();
        }

        /** Excelで作成した期待値と実際のDBの情報を比較 */
        Connection con = createConnection();
        IDatabaseConnection dbUnitConnection = createIDatabaseConnection(con);

        //
        // Assertion.assertEqualsByQueryを使用した比較
        //

        // 期待値のDataSetを生成
        File expectedFile = new File("Insertテスト期待値.xls");
        InputStream expectedIs = null;
        try {
            expectedIs = new FileInputStream(expectedFile);
            IDataSet hikakuDataSet = new XlsDataSet(expectedIs);

            // 期待値と実際のDBのテーブルが合っているか確認
            assertEqualsByQuery(hikakuDataSet, dbUnitConnection,
                    "SELECT * FROM T_CUSTOMER ORDER BY CUSTOMER_CD, CUSTOMER_TYPE", "t_customer",
                    new String[0]);
        } finally {
            if (expectedIs != null) {
                expectedIs.close();
            }
            con.commit();
            dbUnitConnection.close();
        }

        /** 一意制約違反のデータ */
        target = new Customer();
        target.setCustomerCd(4L);
        target.setCustomerType("12");
        target.setCustomerName("片桐");
        target.setCreateDateTime(Timestamp.valueOf("2009-07-01 04:23:45.0"));
        target.setLastUpdateDateTime(Timestamp.valueOf("2009-09-23 07:01:07.0"));
        try {
            dao.insert(target);
            // 正常終了はバグ
            fail();
        } catch (DuplicateException e) {
            // 一意制約に違反しないデータを入れたからExceptionがthrowされる
        }

        /** 引数にnullを設定した場合のテスト */
        try {
            dao.insert(null);
            fail();
        } catch (IllegalArgumentException e) {
            // 仕様上IllegalArgumentExceptionがthrowされればOK
        }
    }

    /**
     * UPDATEのテスト.
     *
     * @throws Exception 例外
     */
    @Test
    public void testUpdate() throws Exception {
        /** 通常パターン */
        Customer target = new Customer();
        target.setCustomerCd(1L);
        target.setCustomerType("01");
        target.setCustomerName("一宗");
        target.setCreateDateTime(Timestamp.valueOf("2012-01-01 01:23:45.0"));
        target.setLastUpdateDateTime(Timestamp.valueOf("2015-09-23 07:01:07.0"));
        target.setBeforeLastUpdateDateTime(Timestamp.valueOf("2009-02-01 17:06:39.0"));

        try {
            dao.update(target);
        } catch (NotFoundException e) {
            // UPDATE対象のデータが存在するのに更新できないのはバグ
            fail();
        }

        /** Excelで作成した期待値と実際のDBの情報を比較 */
        Connection con = createConnection();
        IDatabaseConnection dbUnitConnection = createIDatabaseConnection(con);

        //
        // Assertion.assertEqualsByQueryを使用した比較
        //

        // 期待値のDataSetを生成
        File expectedFile = new File("Updateテスト期待値.xls");
        InputStream expectedIs = null;
        try {
            expectedIs = new FileInputStream(expectedFile);
            IDataSet hikakuDataSet = new XlsDataSet(expectedIs);

            // 期待値と実際のDBのテーブルが合っているか確認
            // (LAST_UPDATE_DATETIMEの値を比較しない場合の書き方)
            assertEqualsByQuery(hikakuDataSet, dbUnitConnection,
                    "SELECT * FROM T_CUSTOMER ORDER BY CUSTOMER_CD, CUSTOMER_TYPE", "t_customer",
                    new String[] {"LAST_UPDATE_DATETIME"});
        } finally {
            if (expectedIs != null) {
                expectedIs.close();
            }
            con.commit();
            dbUnitConnection.close();
        }

        /** 更新対象のレコードが存在しないデータ */
        target = new Customer();
        target.setCustomerCd(1L);
        target.setCustomerType("01");
        target.setCustomerName("一宗");
        target.setCreateDateTime(Timestamp.valueOf("2009-01-01 01:23:45.0"));
        target.setLastUpdateDateTime(Timestamp.valueOf("2009-09-23 07:01:07.0"));
        target.setBeforeLastUpdateDateTime(Timestamp.valueOf("2009-02-01 17:06:39.0"));
        try {
            dao.update(target);
            // 正常終了はおかしい
            fail();
        } catch (NotFoundException e) {
            // 更新できないのは正しい
        }

        /** 引数にnullを設定した場合のテスト */
        try {
            dao.update(null);
            fail();
        } catch (NotFoundException e) {
            // NotFoundExceptはテスト失敗
            fail();
        } catch (IllegalArgumentException e) {
            // 仕様上IllegalArgumentExceptionがthrowされればOK
        }

    }

    /**
     * DELETEのテスト
     *
     * @throws Exception 例外
     */
    @Test
    public void testDelete() throws Exception {
        /** 正常ケース */
        try {
            dao.deleteByKey(10L, "02");
        } catch (NotFoundException e) {
            // 削除できるデータのはずなのに、例外はおかしい
            fail();
        }

        /** Excelで作成した期待値と実際のDBの情報を比較 */
        Connection con = createConnection();
        IDatabaseConnection dbUnitConnection = createIDatabaseConnection(con);

        //
        // Assertion.assertEqualsByQueryを使用した比較
        //

        // 期待値のDataSetを生成
        File expectedFile = new File("Deleteテスト期待値.xls");
        InputStream expectedIs = null;
        try {
            expectedIs = new FileInputStream(expectedFile);
            IDataSet hikakuDataSet = new XlsDataSet(expectedIs);

            // 期待値と実際のDBのテーブルが合っているか確認
            // (LAST_UPDATE_DATETIMEの値を比較しない場合の書き方)
            assertEqualsByQuery(hikakuDataSet, dbUnitConnection,
                    "SELECT * FROM T_CUSTOMER ORDER BY CUSTOMER_CD, CUSTOMER_TYPE", "t_customer",
                    new String[] {"LAST_UPDATE_DATETIME"});
        } finally {
            if (expectedIs != null) {
                expectedIs.close();
            }
            con.commit();
            dbUnitConnection.close();
        }

        /** 削除レコードが存在しない場合 */
        // customerCdが存在せず、customerTypeは存在する
        try {
            dao.deleteByKey(9999L, "01");
            // 正常終了はおかしい
            fail();
        } catch (NotFoundException e) {
            // 削除できないのは正しい
        }

        /** 削除レコードが存在しない場合 */
        // customerCdが存在し、customerTypeは存在しない
        try {
            dao.deleteByKey(1L, "ZZ");
            // 正常終了はおかしい
            fail();
        } catch (NotFoundException e) {
            // 削除できないのは正しい
        }

        /** 引数にnullが設定された時 */
        try {
            dao.deleteByKey(1L, null);
            // 正常終了はおかしい
            fail();
        } catch (NotFoundException e) {
            // 削除できないのはおかしい
            fail();
        } catch (IllegalArgumentException e) {
            // 仕様上IllegalArgumentExceptionがthrowされればOK
        }

    }

    /**
     * Excelファイルを用意することが面倒な場合
     * ※確実に処理対象のレコードが1件の場合であることとSELECTのテストは行われている前提
     */
    @Test
    public void testNotUseAssertEqualsByQuery() {
        /** 正常パターン */
        Customer target = new Customer();
        target.setCustomerCd(4L);
        target.setCustomerType("12");
        target.setCustomerName("片桐カズムネ");
        target.setCreateDateTime(Timestamp.valueOf("2009-07-01 04:23:45.0"));
        target.setLastUpdateDateTime(Timestamp.valueOf("2009-09-23 07:01:07.0"));

        try {
            dao.insert(target);
        } catch (DuplicateException e) {
            // 一意制約に違反しないデータを入れたのにエラーになるのはバグ
            fail();
        }

        // SELECTメソッドを使用してデータを取得
        Customer ret = null;
        try {
            ret = dao.findByKey(4L, "12");
        } catch (NotFoundException e) {
            // 取得できないのはおかしい
            fail();
        }

        assertThat(ret.getCustomerCd(), is(4L));
        assertThat(ret.getCustomerType(), is("12"));
        assertThat(ret.getCustomerName(), is("片桐カズムネ"));
        assertThat(ret.getCreateDateTime(), is(Timestamp.valueOf("2009-07-01 04:23:45.0")));
        assertThat(ret.getLastUpdateDateTime(), is(Timestamp.valueOf("2009-09-23 07:01:07.0")));

    }

    /**
     * Connection接続.
     *
     * @return Connection
     * @throws Exception 例外
     */
    private Connection createConnection() throws Exception {
    	DataSource dataSource = (DataSource)context.getBean("mySqlDataSource");
    	return dataSource.getConnection();
    }

    /**
     * IDatabaseConnection接続
     *
     * @param conn Connection
     * @return IDatabaseConnection
     * @throws Exception 例外
     */
    private IDatabaseConnection createIDatabaseConnection(Connection conn) throws Exception {
        // MySQL5.1の場合は、スキーマ名にnullを設定
        return new MySqlConnection(conn, null);

    }
}
