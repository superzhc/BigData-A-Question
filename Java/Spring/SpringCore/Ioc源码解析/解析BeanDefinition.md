# 解析BeanDefinition


`DefaultBeanDefinitionDocumentReader`对该方法提供了实现：

```java
public void registerBeanDefinitions(Document doc, XmlReaderContext readerContext) {
    this.readerContext = readerContext;
    logger.debug("Loading bean definitions");
    Element root = doc.getDocumentElement();
    doRegisterBeanDefinitions(root);
}

protected void doRegisterBeanDefinitions(Element root) {
    BeanDefinitionParserDelegate parent = this.delegate;
    this.delegate = createDelegate(getReaderContext(), root, parent);

    if (this.delegate.isDefaultNamespace(root)) {
        // 处理 profile
        String profileSpec = root.getAttribute(PROFILE_ATTRIBUTE);
        if (StringUtils.hasText(profileSpec)) {
            String[] specifiedProfiles = StringUtils.tokenizeToStringArray(
                    profileSpec, BeanDefinitionParserDelegate.MULTI_VALUE_ATTRIBUTE_DELIMITERS);
            if (!getReaderContext().getEnvironment().acceptsProfiles(specifiedProfiles)) {
                if (logger.isInfoEnabled()) {
                    logger.info("Skipped XML bean definition file due to specified profiles [" + profileSpec +
                            "] not matching: " + getReaderContext().getResource());
                }
                return;
            }
        }
    }

    // 解析前处理
    preProcessXml(root);
    // 解析
    parseBeanDefinitions(root, this.delegate);
    // 解析后处理
    postProcessXml(root);

    this.delegate = parent;
}
```

程序首先处理profile属性，profile主要用于切换环境，比如切换开发、测试、生产环境；然后调用`parseBeanDefinitions()`进行解析动作，不过在该方法之前之后分别调用`preProcessXml()`和`postProcessXml()`方法来进行前、后处理，目前这两个方法都是空实现，交由子类来实现。

`parseBeanDefinitions()`定义如下：

```java
protected void parseBeanDefinitions(Element root, BeanDefinitionParserDelegate delegate) {
    if (delegate.isDefaultNamespace(root)) {
        NodeList nl = root.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node node = nl.item(i);
            if (node instanceof Element) {
                Element ele = (Element) node;
                if (delegate.isDefaultNamespace(ele)) {
                    parseDefaultElement(ele, delegate);
                }
                else {
                    delegate.parseCustomElement(ele);
                }
            }
        }
    }
    else {
        delegate.parseCustomElement(root);
    }
}
```

最终解析动作落地在两个方法处：`parseDefaultElement(ele, delegate)` 和 `delegate.parseCustomElement(root)`。

在Spring中有两种Bean声明方式：
- 默认标签的声明：`<bean id="xxx" class="xxx.xxx.xxx"></bean>`
- 自定义标签的声明：`<tx:annotation-driven />`

这两种方式的读取和解析都存在较大的差异，所以采用不同的解析方法，如果根节点或者子节点采用默认命名空间的话，则调用`parseDefaultElement()`进行解析，否则调用`delegate.parseCustomElement()`方法进行自定义解析。