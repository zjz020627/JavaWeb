package com.study.myssm.ioc;

import com.study.myssm.util.StringUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * @author RenAshbell
 * @create 2022-04-29-11:12
 */
public class ClassPathXmlApplicationContext implements BeanFactory{

    private Map<String,Object> beanMap = new HashMap<>();
    private String path = "applicationContext.xml";

    public ClassPathXmlApplicationContext(){
        this("applicationContext.xml");
    }
    public ClassPathXmlApplicationContext(String path){
        if (StringUtil.isEmpty(path)){
            throw new RuntimeException("IOC容器的配置文件没有指定...");
        }
        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(path);
            //1.创建DocumentBuilderFactory
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            //2.创建DocumentBuilder对象
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder() ;
            //3.创建Document对象
            Document document = documentBuilder.parse(inputStream);

            //4.获取所有的bean节点
            NodeList beanNodeList = document.getElementsByTagName("bean");
            for(int i = 0 ; i<beanNodeList.getLength() ; i++){
                Node beanNode = beanNodeList.item(i);
                // 判断是否为Element节点
                if(beanNode.getNodeType() == Node.ELEMENT_NODE){
                    // 强转为Element节点
                    Element beanElement = (Element)beanNode ;
                    // 获取id
                    String beanId =  beanElement.getAttribute("id");
                    // 获取class
                    String className = beanElement.getAttribute("class");
                    // 创建Class类
                    Class beanClass = Class.forName(className);
                    // 创建bean实例
                    Object beanObj = beanClass.newInstance() ;
                    // 将bean实例对象保存到map容器中
                    beanMap.put(beanId , beanObj) ;
                    // 到目前为止, 此处需要注意的是, bean和bean之间的依赖关系还没有设置
                }
            }
            //5.组装bean之间的依赖关系
            for(int i = 0 ; i<beanNodeList.getLength() ; i++){
                Node beanNode = beanNodeList.item(i);
                // 判断是否是ELEMENT节点
                if(beanNode.getNodeType() == Node.ELEMENT_NODE) {
                    // 强转为Element节点
                    Element beanElement = (Element) beanNode;
                    // 获取对应id
                    String beanId = beanElement.getAttribute("id");
                    // 获取子节点
                    NodeList beanChildNodes = beanElement.getChildNodes();
                    for (int j = 0; j < beanChildNodes.getLength(); j++) {
                        Node beanChildNode = beanChildNodes.item(j);
                        // 判断子节点是否是property
                        if (beanChildNode.getNodeType() == Node.ELEMENT_NODE && "property".equals(beanChildNode.getNodeName())){
                            // 是的话再强转为Element节点
                            Element propertyElement = (Element) beanChildNode;
                            // 获取对应的name
                            String propertyName = propertyElement.getAttribute("name");
                            // 获取对应的ref
                            String propertyRef = propertyElement.getAttribute("ref");
                            // 1) 找到propertyRef对应的实例
                            Object refObj = beanMap.get(propertyRef);
                            // 2) 将refObj设置到当前bean对应的实例的property属性上去
                            // 先通过id找到对应的class实例
                            Object beanObj = beanMap.get(beanId);
                            // 获取Class类
                            Class beanClazz = beanObj.getClass();
                            // 获取Class类对应name的属性 - 获取属性
                            Field propertyField = beanClazz.getDeclaredField(propertyName);
                            propertyField.setAccessible(true);
                            // 从获取到的id对应的class实例设置name属性的值 - 属性赋值
                            propertyField.set(beanObj,refObj);
                        }
                    }

                }
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object getBean(String id) {
        return beanMap.get(id);
    }
}
