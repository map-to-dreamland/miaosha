package com.wx.miaosha;

import com.wx.miaosha.dao.UserDOMapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.Resource;

/**
 * Hello world!
 *
 */
@MapperScan("com.wx.miaosha.dao")
@SpringBootApplication
public class App {
    public static void main( String[] args )
    {
        SpringApplication.run(App.class,args);
    }
}
