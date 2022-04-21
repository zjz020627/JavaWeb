package com.study.jdbc;

import java.sql.*;

/**
 * @author RenAshbell
 * @create 2022-04-21-15:32
 */
// JDBC - 查询指定fid的库存记录
public class Demo07 {
    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.jdbc.Driver");
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/fruitdb?useSSL=false&useUnicode=true&characterEncoding=utf-8", "root", "202517");
        // 3.编写sql语句
        String sql = "select count(*) from t_fruit";
        // 4.创建预处理命令对象
        PreparedStatement psmt = conn.prepareStatement(sql);
        // 5.执行查询，返回结果集
        ResultSet rs = psmt.executeQuery();
        // 6.解析结果集
        if (rs.next()){
            // 1表示读取当前行的第一列的数据
            // getInt 因为这一列是int数据，所以使用getInt
            // getInt(结果集的列名)
//            int fid = rs.getInt("fid");
            int count = rs.getInt(1);
            System.out.println("总记录条数：" + count);
        }

        rs.close();
        psmt.close();
        conn.close();

    }
}