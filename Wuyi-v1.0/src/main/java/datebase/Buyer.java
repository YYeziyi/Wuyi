package datebase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

/**
 * 购买人实体类，封装购买人及订单相关信息
 */
public class Buyer {
    // 1. 用户名：1-5位，可包含中文、英文、数字
    private String buyer_name;
    // 2. 手机号：必须是11位数字
    private String buyer_phonenumber;
    // 3. 交易地址：最多80个字符（一个中文/英文/数字均算1个字符）
    private String trading_address;
    // 4. 交易时间：格式 "YYYY-MM-DD HH:MM"
    private String trading_time;
    // 5. 订单编号：格式 DD00001、DD00002...（DD为自定义前缀，后5位为递增数字）
    private String order_id;
    // 6. 订单提交时间：格式 "YYYY-MM-DD HH:MM:SS"
    private String order_time;
    // 7. 商品编号：4位数字（1001、1002...）
    private int work_id;

    public Buyer() {}


    public Buyer(String buyer_name, String buyer_phonenumber, String trading_address,
                 String trading_time, String order_id, String order_time, int work_id) {
        // 对每个字段进行格式校验，不符合规则则抛出异常
        setBuyer_name(buyer_name);
        setBuyer_phonenumber(buyer_phonenumber);
        setTrading_address(trading_address);
        setTrading_time(trading_time);
        setOrder_id(order_id);
        setOrder_time(order_time);
        setWork_id(work_id);
    }


    public int getWork_id() {
        return work_id;
    }

    public void setWork_id(int work_id) {
        // 校验商品编号是否为4位数字（1-9999范围）
        if (work_id < 1 || work_id > 9999) {
            throw new IllegalArgumentException("商品编号格式错误：需为4位数字（0001-9999）");
        }
        this.work_id = work_id;
    }

    public String getBuyer_name() {
        return buyer_name;
    }


    public void setBuyer_name(String buyer_name) {
        // 正则表达式：匹配1-5位（中文、英文、数字），^$表示整串匹配
        String regex = "^[\\u4e00-\\u9fa5a-zA-Z0-9]{1,5}$";
        if (buyer_name == null || !Pattern.matches(regex, buyer_name)) {
            throw new IllegalArgumentException("用户名格式错误：需1-5位，可包含中文、英文、数字");
        }
        this.buyer_name = buyer_name;
    }

    public String getBuyer_phonenumber() {
        return buyer_phonenumber;
    }

    public void setBuyer_phonenumber(String buyer_phonenumber) {
        String regex = "^\\d{11}$"; // 正则：匹配11位数字
        if (buyer_phonenumber == null || !Pattern.matches(regex, buyer_phonenumber)) {
            throw new IllegalArgumentException("手机号格式错误：必须是11位数字");
        }
        this.buyer_phonenumber = buyer_phonenumber;
    }

    public String getTrading_address() {
        return trading_address;
    }


    public void setTrading_address(String trading_address) {
        if (trading_address == null) {
            throw new IllegalArgumentException("交易地址不能为空");
        }
        // 按字符长度校验（中文/英文/数字均算1个字符）
        if (trading_address.length() > 20) {
            throw new IllegalArgumentException("交易地址格式错误：最多20个字符");
        }
        this.trading_address = trading_address;
    }

    public String getTrading_time() {
        return trading_time;
    }


    public void setTrading_time(String trading_time) {
        // SimpleDateFormat 严格校验时间格式（setLenient(false) 关闭宽松模式）
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        sdf.setLenient(false); // 必须严格匹配格式（如2月30日、25:60分等会被判定为无效）
        try {
            sdf.parse(trading_time); // 解析成功则格式正确，失败则抛异常
        } catch (ParseException e) {
            throw new IllegalArgumentException("交易时间格式错误：需遵循 YYYY-MM-DD HH:MM 格式（如 2024-05-20 14:30）");
        }
        this.trading_time = trading_time;
    }

    public String getOrder_id() {
        return order_id;
    }


    public void setOrder_id(String order_id) {
        // 正则：前2位为大写/小写字母（DD为示例，实际可自定义），后5位为数字（00001~99999）
        String regex = "^[a-zA-Z]{2}\\d{5}$";
        if (order_id == null || !Pattern.matches(regex, order_id)) {
            throw new IllegalArgumentException("订单编号格式错误：需遵循 DD00001 格式（前2位字母，后5位数字）");
        }
        this.order_id = order_id;
    }

    public String getOrder_time() {
        return order_time;
    }


    public void setOrder_time(String order_time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setLenient(false);
        try {
            sdf.parse(order_time);
        } catch (ParseException e) {
            throw new IllegalArgumentException("订单提交时间格式错误：需遵循 YYYY-MM-DD HH:MM:SS 格式（如 2024-05-20 14:30:00）");
        }
        this.order_time = order_time;
    }

    @Override
	public String toString() {
		return "Buyer [buyer_name=" + buyer_name + ", buyer_phonenumber=" + buyer_phonenumber + ", trading_address="
				+ trading_address + ", trading_time=" + trading_time + ", order_id=" + order_id + ", order_time="
				+ order_time + ", work_id=" + String.format("%04d", work_id) + "]";
	}
}
    