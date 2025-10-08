package soft_practice01;

import static org.junit.Assert.*;
import org.junit.*;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

import datebase.*;
import Servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServlet;

import java.sql.Connection;
import java.util.List;
import java.lang.reflect.Method;
import java.util.UUID;

public class Function_Test {

    private static Connection con;
    private WorksDaoIplm worksDao;
    private BuyerDaoimpl buyerDao;

    // Servlet实例
    private insert_goodServlet insertGoodServlet = new insert_goodServlet();
    private Buyer_message_servlet buyerMessageServlet = new Buyer_message_servlet();
    private BuyServlet buyServlet = new BuyServlet();
    private modify_goodStatusServlet modifyGoodServlet = new modify_goodStatusServlet();
    private HistoryProductsServlet historyServlet = new HistoryProductsServlet();

    // 测试数据 - 使用UUID确保唯一性
    private String testOrderId;
    private int testWorkId;
    private final String uniqueProductName = "流程测试商品_" + UUID.randomUUID().toString().substring(0, 8);

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        // 初始化数据库连接并关闭自动提交（便于回滚）
        con = DbUtil.getCon();
        if (con == null) {
            fail("数据库连接初始化失败");
        }
        con.setAutoCommit(false);
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        // 测试结束后回滚并关闭连接
        if (con != null) {
            try {
                con.rollback();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                con.close();
            }
        }
    }

    @Before
    public void setUp() throws Exception {
        worksDao = new WorksDaoIplm();
        buyerDao = new BuyerDaoimpl();
    }

    @After
    public void tearDown() throws Exception {
        // 每个测试方法结束后回滚，避免数据污染
        if (con != null) {
            con.rollback();
        }
    }

    /**
     * 完整业务流程测试：上架→预订→查看购买人→确认交易→查看历史
     */
    @Test
    public void testCompleteTradeFlow() throws Exception {
        // 1. 测试商品上架
        testWorkId = testAddNewProductSuccess();
        assertTrue("商品上架失败", testWorkId > 0);

        // 2. 测试用户预订
        testOrderId = testUserReservationSuccess();
        assertNotNull("用户预订失败", testOrderId);
        assertTrue("订单ID格式不正确", testOrderId.matches("^[A-Za-z]{2}\\d{5}$"));

        // 3. 测试查看意向购买人
        Buyer selectedBuyer = testViewInterestedBuyers();
        assertNotNull("未查询到意向购买人", selectedBuyer);
        assertEquals("购买人信息不匹配", "测试", selectedBuyer.getBuyer_name());

        // 4. 测试确认交易
        boolean tradeSuccess = testConfirmTrade();
        assertTrue("交易确认失败", tradeSuccess);

        // 5. 测试查看历史记录
        boolean historyExists = testCheckHistory();
        assertTrue("历史记录未找到", historyExists);
    }

    /**
     * 测试商品上架功能（正常场景）
     */
    private int testAddNewProductSuccess() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockHttpSession session = new MockHttpSession();
        request.setSession(session);

        // 设置商品参数（使用唯一名称避免冲突）
        request.setParameter("work_status", "available");
        request.setParameter("work_name", uniqueProductName);
        request.setParameter("work_description", "用于完整流程测试的商品");
        request.setParameter("work_price", "299.99");
        request.setParameter("work_image", "flow_test.jpg");

        // 调用插入Servlet
        callDoGet(insertGoodServlet, request, response);

        // 验证响应状态
        assertEquals("Servlet响应状态异常", 302, response.getStatus());

        // 验证结果
        List<Works> workList = (List<Works>) session.getAttribute("wklist");
        assertNotNull("商品列表为空", workList);
        
        for (Works work : workList) {
            if (uniqueProductName.equals(work.getWork_name())) {
                return work.getId();
            }
        }
        fail("未找到新上架的商品");
        return -1;
    }

    /**
     * 测试用户预订功能（正常场景）
     */
    private String testUserReservationSuccess() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setMethod("POST");

        // 设置预订参数
        request.setParameter("buyer_name", "测试");
        request.setParameter("buyer_phonenumber", "13800138000");
        request.setParameter("trading_address", "测试地址123号");
        request.setParameter("trading_time", "2024-12-31T16:00");

        // 调用预订Servlet的doPost方法
        Method doPostMethod = Buyer_message_servlet.class.getDeclaredMethod("doPost", 
            HttpServletRequest.class, HttpServletResponse.class);
        doPostMethod.setAccessible(true);
        doPostMethod.invoke(buyerMessageServlet, request, response);

        // 验证无错误信息
        assertNull("预订过程出现错误", request.getAttribute("errorMessage"));
        
        // 获取订单ID
        String orderId = (String) request.getAttribute("order_id");
        assertNotNull("订单ID为空", orderId);
        return orderId;
    }

    /**
     * 测试查看意向购买人功能
     */
    private Buyer testViewInterestedBuyers() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockHttpSession session = new MockHttpSession();
        request.setSession(session);

        // 调用购买Servlet查询购买人
        request.setParameter("method", "trade");
        request.setParameter("orderId", testOrderId);
        callDoGet(buyServlet, request, response);

        // 验证无错误信息
        assertNull("查询购买人过程出现错误", request.getAttribute("errorMsg"));
        
        // 验证结果
        Buyer selectedBuyer = (Buyer) session.getAttribute("selected_buyer");
        assertNotNull("未找到对应的购买人", selectedBuyer);
        return selectedBuyer;
    }

    /**
     * 测试确认交易功能
     */
    private boolean testConfirmTrade() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockHttpSession session = new MockHttpSession();
        
        // 准备商品列表数据
        List<Works> workList = worksDao.serachAll(con);
        assertNotNull("商品列表查询失败", workList);
        session.setAttribute("wklist", workList);
        request.setSession(session);

        // 设置修改参数（状态改为已售）
        request.setParameter("work_id", String.valueOf(testWorkId));
        request.setParameter("work_status1", "sold");
        request.setParameter("work_name1", uniqueProductName);
        request.setParameter("work_description1", "用于完整流程测试的商品");
        request.setParameter("work_price1", "299.99");
        request.setParameter("work_image1", "flow_test.jpg");

        // 调用修改状态Servlet
        callDoGet(modifyGoodServlet, request, response);

        // 验证响应状态
        assertEquals("修改状态Servlet响应异常", 302, response.getStatus());

        // 验证状态更新
        List<Works> updatedList = (List<Works>) session.getAttribute("wklist");
        assertNotNull("更新后的商品列表为空", updatedList);
        
        for (Works work : updatedList) {
            if (work.getId() == testWorkId) {
                return "sold".equals(work.getWork_status());
            }
        }
        fail("未找到目标商品或状态未更新");
        return false;
    }

    /**
     * 测试查看历史记录功能
     */
    private boolean testCheckHistory() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setParameter("search", testOrderId);

        // 调用历史记录Servlet
        callDoGet(historyServlet, request, response);

        // 验证历史记录
        List<Order> orders = (List<Order>) request.getAttribute("orders");
        assertNotNull("历史记录列表为空", orders);
        assertFalse("历史记录列表为空", orders.isEmpty());
        
        for (Order order : orders) {
            if (testOrderId.equals(order.getOrderNumber())) {
                return true;
            }
        }
        fail("历史记录中未找到目标订单");
        return false;
    }

    /**
     * 反射工具方法：调用Servlet的protected doGet方法
     */
    private void callDoGet(HttpServlet servlet, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Method doGetMethod = HttpServlet.class.getDeclaredMethod("doGet", 
            HttpServletRequest.class, HttpServletResponse.class);
        doGetMethod.setAccessible(true);
        doGetMethod.invoke(servlet, request, response);
    }
}