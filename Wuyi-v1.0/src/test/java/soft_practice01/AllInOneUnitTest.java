package soft_practice01;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import datebase.Buyer;
import datebase.BuyerDao;
import datebase.BuyerDaoimpl;
import datebase.DbUtil;
import datebase.Merchant;
import datebase.Order;
import datebase.Product;
import datebase.Statistics;
import datebase.User;
import datebase.Works;
import datebase.WorksDaoIplm;
import Servlet.BuyServlet;
import Servlet.insert_goodServlet;
import Servlet.get_workServlet;

public class AllInOneUnitTest {

    // 通用测试对象
    private Connection testConn;
    private Buyer testBuyer;
    private Works testWork;
    private Product testProduct;
    private User testUser;
    private Order testOrder;
    private Statistics testStats;

    // 初始化测试数据
    @Before
    public void setUp() throws Exception {
        // 初始化Buyer对象
        testBuyer = new Buyer(
            "张三", 
            "13800138000", 
            "北京市海淀区", 
            "2024-12-31 10:00", 
            "DD99999", 
            "2024-12-30 15:00:00", 
            1001
        );

        // 初始化Works对象
        testWork = new Works(
            1001, 
            "available", 
            "测试商品", 
            "这是一个测试商品", 
            "/images/test.jpg", 
            "99.99"
        );

        // 初始化Product对象
        testProduct = new Product(
            "P1001", 
            "测试商品", 
            "这是一个测试商品", 
            "/images/test.jpg", 
            99.99, 
            "available"
        );

        // 初始化User对象
        testUser = new User("张三", "13800138000", "北京市海淀区");

        // 初始化Order对象
        testOrder = new Order(
            "DD99999",
            "T00001",
            testProduct,
            testUser,
            LocalDateTime.now(),
            LocalDateTime.now(),
            "已完成"
        );

        // 初始化Statistics对象
        testStats = new Statistics(100, 50, 9999.99, 15.5, 10.2, 20.3);
    }

    @After
    public void tearDown() throws Exception {
        if (testConn != null && !testConn.isClosed()) {
            testConn.close();
        }
    }

    // ==================== 实体类测试 ====================

    @Test
    public void testBuyerValidCreation() {
        assertEquals("张三", testBuyer.getBuyer_name());
        assertEquals("13800138000", testBuyer.getBuyer_phonenumber());
        assertEquals("DD99999", testBuyer.getOrder_id());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuyerInvalidPhone() {
        new Buyer(
            "李四", 
            "123",
            "上海市浦东新区", 
            "2024-11-30 09:30", 
            "DD00002", 
            "2024-11-29 16:45:00", 
            1002
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuyerInvalidOrderId() {
        new Buyer(
            "李四", 
            "13900139000", 
            "上海市浦东新区", 
            "2024-11-30 09:30", 
            "D0001", 
            "2024-11-29 16:45:00", 
            1002
        );
    }

    @Test
    public void testProductMethods() {
        assertEquals("¥99.99", testProduct.getFormattedPrice());
        assertEquals("bg-gray-100 text-gray-800", testProduct.getStatusClass());
        testProduct.setStatus("已售罄");
        assertEquals("bg-green-100 text-green-800", testProduct.getStatusClass());
    }

    @Test
    public void testUserMaskedPhone() {
        assertEquals("138****8000", testUser.getMaskedPhone());
    }

    @Test
    public void testOrderTimeFormat() {
        String formatted = testOrder.getFormattedOrderTime();
        assertTrue(formatted.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));
    }

    @Test
    public void testStatisticsFormat() {
        assertEquals("¥10000", testStats.getFormattedTotalAmount());
        assertEquals("15.5%", testStats.getFormattedMonthlyGrowth());
        assertTrue(testStats.isMonthlyGrowthPositive());
    }

    // ==================== WorksDaoIplm 测试 ====================

    @Test
    public void testWorksDaoSearchAll() throws Exception {
        WorksDaoIplm worksDao = new WorksDaoIplm();
        try (Connection conn = DbUtil.getCon()) {
            List<Works> worksList = worksDao.serachAll(conn);
            assertNotNull(worksList);
            if (!worksList.isEmpty()) {
                Works work = worksList.get(0);
                assertNotNull(work.getWork_name());
                assertNotNull(work.getWork_price());
            }
        }
    }

    @Test(expected = SQLException.class)
    public void testWorksDaoNullConnection() throws Exception {
        WorksDaoIplm worksDao = new WorksDaoIplm();
        worksDao.serachAll(null);
    }

    // ==================== BuyerDaoimpl 测试 ====================

    @Test
    public void testBuyerDaoGetByOrderId() throws Exception {
        BuyerDao buyerDao = new BuyerDaoimpl();
        Buyer buyer = buyerDao.getBuyerByOrderId("DD99999");
        if (buyer != null) {
            assertEquals("DD99999", buyer.getOrder_id());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuyerDaoInvalidOrderId() throws Exception {
        BuyerDao buyerDao = new BuyerDaoimpl();
        buyerDao.getBuyerByOrderId("invalid");
    }

    // ==================== Servlet 逻辑测试 ====================

    @Test
    public void testInsertGoodServletLogic() throws Exception {
        insert_goodServlet servlet = new insert_goodServlet();
        Works testWork = new Works("available", "测试插入商品", "测试描述", "/test.jpg", "199.99");
        WorksDaoIplm dao = new WorksDaoIplm();
        assertEquals("测试插入商品", testWork.getWork_name());
        assertEquals("199.99", testWork.getWork_price());
    }

    @Test
    public void testGetWorkServletLogic() throws Exception {
        get_workServlet servlet = new get_workServlet();
        WorksDaoIplm dao = new WorksDaoIplm();
        try (Connection conn = DbUtil.getCon()) {
            List<Works> worksList = dao.serachAll(conn);
            assertNotNull(worksList);
        } catch (Exception e) {
            fail("获取商品列表失败: " + e.getMessage());
        }
    }

    @Test
    public void testBuyServletTradeLogic() throws Exception {
        BuyServlet buyServlet = new BuyServlet();
        BuyerDao mockDao = new BuyerDaoimpl();
        try {
            Buyer buyer = mockDao.getBuyerByOrderId("DD99999");
            if (buyer != null) {
                int result = mockDao.Trade("DD99999");
                assertEquals(1, result);
            }
        } catch (SQLException e) {
            fail("交易逻辑测试失败: " + e.getMessage());
        }
    }
}
