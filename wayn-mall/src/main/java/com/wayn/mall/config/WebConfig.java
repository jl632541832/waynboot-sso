package com.wayn.mall.config;

import com.wayn.mall.intercepter.AdminLoginInterceptor;
import com.wayn.mall.intercepter.MallLoginValidateIntercepter;
import com.wayn.mall.intercepter.MallShopCartNumberInterceptor;
import com.wayn.mall.intercepter.RepeatSubmitInterceptor;
import com.wayn.ssocore.filter.SsoFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.DispatcherType;
import java.util.Arrays;
import java.util.LinkedHashMap;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${wayn.uploadDir}")
    private String uploadDir;

    @Value("${wayn.ssoServerUrl}")
    private String ssoServerUrl;

    @Value("${wayn.xssFilter.excludeUrls}")
    private String excludeUrls;

    @Autowired
    private RepeatSubmitInterceptor repeatSubmitInterceptor;

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/", "/index");
    }


    @Bean
    public FilterRegistrationBean filterRegistrationBean() {
        FilterRegistrationBean bean = new FilterRegistrationBean();
        SsoFilter ssoFilter = new SsoFilter();
        ssoFilter.setSsoServerUrl(ssoServerUrl);
        bean.setFilter(ssoFilter);
        bean.setDispatcherTypes(DispatcherType.REQUEST);
        bean.setName("ssoFilter");
        bean.setUrlPatterns(Arrays.asList("/admin/*"));
        LinkedHashMap<String, Object> linkedHashMap = new LinkedHashMap<>();
        linkedHashMap.put("excludeUrls", excludeUrls);
        bean.setInitParameters(linkedHashMap);
        return bean;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        /** 本地文件上传路径 */
        registry.addResourceHandler("/upload/**").addResourceLocations("file:" + uploadDir + "/");
        registry.addResourceHandler("/goods-img/**").addResourceLocations("file:" + uploadDir + "/");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new MallLoginValidateIntercepter())
                .excludePathPatterns("/login")
                .excludePathPatterns("/logout")
                .excludePathPatterns("/")
                .excludePathPatterns("/index")
                .excludePathPatterns("/search")
                .excludePathPatterns("/coupon")
                .excludePathPatterns("/goods/**")
                .excludePathPatterns("/seckill/list")
                .excludePathPatterns("/seckill/detail/*")
                .excludePathPatterns("/seckill/time/now")
                .excludePathPatterns("/seckill/*/exposer")
                .excludePathPatterns("/register")
                .excludePathPatterns("/upload/**")
                .excludePathPatterns("/goods-img/**")
                .excludePathPatterns("/common/**")
                .excludePathPatterns("/com/wayn/mall/**")
                .excludePathPatterns("/admin/**");

        // 购物车中的数量统一处理
        registry.addInterceptor(mallShopCartNumberInterceptor())
                .excludePathPatterns("/admin/**")
                .excludePathPatterns("/register")
                .excludePathPatterns("/login")
                .excludePathPatterns("/logout")
                .excludePathPatterns("/common/**")
                .excludePathPatterns("/**/*.jpg")
                .excludePathPatterns("/**/*.png")
                .excludePathPatterns("/**/*.gif")
                .excludePathPatterns("/**/*.map")
                .excludePathPatterns("/**/*.css")
                .excludePathPatterns("/**/*.js");

        // 添加一个拦截器，拦截以/admin为前缀的url路径（后台登陆拦截）
        registry.addInterceptor(new AdminLoginInterceptor())
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/login")
                .excludePathPatterns("/admin/dist/**")
                .excludePathPatterns("/admin/plugins/**");

        registry.addInterceptor(repeatSubmitInterceptor).addPathPatterns("/**");
    }

    @Bean
    public MallShopCartNumberInterceptor mallShopCartNumberInterceptor() {
        return new MallShopCartNumberInterceptor();
    }
}
