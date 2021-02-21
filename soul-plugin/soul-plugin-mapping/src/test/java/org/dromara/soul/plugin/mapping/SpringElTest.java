package org.dromara.soul.plugin.mapping;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpringElTest {

    @Test
    public void helloWorld() {
        ExpressionParser parser = new SpelExpressionParser();
        Expression expression =
                parser.parseExpression("('Hello' + ' World').concat(#end)");
        EvaluationContext context = new StandardEvaluationContext();
        context.setVariable("end", "!");
        Assert.assertEquals("Hello World!", expression.getValue(context));
    }

    @Data
    @AllArgsConstructor
     public class Person {
        private String name;

        private int age;
    }

    @Test
    public void test2(){
        // 准备工作
        Person person = new Person("Tom", 18); // 一个普通的POJO
        List<String> list = Lists.newArrayList("a", "b");
        Map<String, String> map = Maps.newHashMap();
        map.put("A", "1");
        map.put("B", "2");
        EvaluationContext context = new StandardEvaluationContext();  // 表达式的上下文,
        context.setVariable("person", person);                        // 为了让表达式可以访问该对象, 先把对象放到上下文中
        context.setVariable("map", map);
        context.setVariable("list", list);
        ExpressionParser parser = new SpelExpressionParser();
// 属性
        System.out.println(parser.parseExpression("'{\"personName\":\"'+ #person.name + '\"}'").getValue(context, String.class));       // Tom , 属性访问
        System.out.println(parser.parseExpression("#person.Name").getValue(context, String.class));       // Tom , 属性访问, 但是首字母大写了
// 列表
        System.out.println(parser.parseExpression("#list[0]").getValue(context, String.class));    // a , 下标
// map
        System.out.println(parser.parseExpression("#map[A]").getValue(context, String.class));           // 1 , key
// 方法
        System.out.println(parser.parseExpression("#person.getAge()").getValue(context, Integer.class)); // 18 , 方法访问
    }

    @Test
    public void test() throws IOException, TemplateException {
        Configuration cfg = new Configuration();
        StringTemplateLoader stringLoader = new StringTemplateLoader();
        stringLoader.putTemplate("myTemplate", "{\"personName:\":\"${person.name}\",\"subA\":\"${person.sub.a}\"}" );
        cfg.setTemplateLoader(stringLoader);
        Template template = cfg.getTemplate("myTemplate");

        Map<String,Object> sub = new HashMap<>();
        sub.put("a","aaaa");

        Map<String,Object> person = new HashMap<>();
        person.put("name","Tom");
        person.put("age",18);
        person.put("sub",sub);

        Map root = new HashMap();
        root.put("person", person);

        StringWriter writer = new StringWriter();
        template.process(root, writer);
        System.out.println(writer.toString());
    }
}
