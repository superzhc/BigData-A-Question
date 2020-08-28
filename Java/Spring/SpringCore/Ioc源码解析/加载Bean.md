# 加载Bean

- **加载资源的实现逻辑分析**

`reader.loadBeanDefinitions(resource)`才是加载资源的真正实现,源码如下：

```java
public int loadBeanDefinitions(Resource resource) throws BeanDefinitionStoreException {
     return loadBeanDefinitions(new EncodedResource(resource));
}

public int loadBeanDefinitions(EncodedResource encodedResource) throws BeanDefinitionStoreException {
     Assert.notNull(encodedResource, "EncodedResource must not be null");
     if (logger.isInfoEnabled()) {
          logger.info("Loading XML bean definitions from " + encodedResource.getResource());
     }

     // 获取已经加载过的资源
     Set<EncodedResource> currentResources = this.resourcesCurrentlyBeingLoaded.get();
     if (currentResources == null) {
          currentResources = new HashSet<>(4);
          this.resourcesCurrentlyBeingLoaded.set(currentResources);
     }

     // 将当前资源加入记录中
     if (!currentResources.add(encodedResource)) {
          throw new BeanDefinitionStoreException(
               "Detected cyclic loading of " + encodedResource + " - check your import definitions!");
     }
     try {
          // 从 EncodedResource 获取封装的 Resource 并从 Resource 中获取其中的 InputStream
          InputStream inputStream = encodedResource.getResource().getInputStream();
          try {
               InputSource inputSource = new InputSource(inputStream);
               // 设置编码
               if (encodedResource.getEncoding() != null) {
                    inputSource.setEncoding(encodedResource.getEncoding());
               }
               // 核心逻辑部分
               return doLoadBeanDefinitions(inputSource, encodedResource.getResource());
          }
          finally {
               inputStream.close();
          }
     }
     catch (IOException ex) {
          throw new BeanDefinitionStoreException(
               "IOException parsing XML document from " + encodedResource.getResource(), ex);
     }
     finally {
          // 从缓存中剔除该资源
          currentResources.remove(encodedResource);
          if (currentResources.isEmpty()) {
               this.resourcesCurrentlyBeingLoaded.remove();
          }
     }
}

protected int doLoadBeanDefinitions(InputSource inputSource, Resource resource) throws BeanDefinitionStoreException {
     try {
          // 获取 Document 实例
          Document doc = doLoadDocument(inputSource, resource);
          // 根据 Document 实例****注册 Bean信息
          return registerBeanDefinitions(doc, resource);
     }
     catch (BeanDefinitionStoreException ex) {
          throw ex;
     }
     catch (SAXParseException ex) {
          throw new XmlBeanDefinitionStoreException(resource.getDescription(),
               "Line " + ex.getLineNumber() + " in XML document from " + resource + " is invalid", ex);
     }
     catch (SAXException ex) {
          throw new XmlBeanDefinitionStoreException(resource.getDescription(),
               "XML document from " + resource + " is invalid", ex);
     }
     catch (ParserConfigurationException ex) {
          throw new BeanDefinitionStoreException(resource.getDescription(),
               "Parser configuration exception parsing XML from " + resource, ex);
     }
     catch (IOException ex) {
          throw new BeanDefinitionStoreException(resource.getDescription(),
               "IOException parsing XML document from " + resource, ex);
     }
     catch (Throwable ex) {
          throw new BeanDefinitionStoreException(resource.getDescription(),
               "Unexpected exception parsing XML document from " + resource, ex);
     }
}

protected Document doLoadDocument(InputSource inputSource, Resource resource) throws Exception {
     return this.documentLoader.loadDocument(inputSource, getEntityResolver(), this.errorHandler,
               getValidationModeForResource(resource), isNamespaceAware());
}
```

上述方法相关逻辑备注：
1. 在`loadBeanDefinitions(Resource)`中对Resource资源进行封装成EncodedResource【*这是为了对Resource进行编码，保证内容读取的正确性。*】
2. 在`loadBeanDefinitions(EncodedResource)`做了如下事情：
   1. 首先通过`resourcesCurrentlyBeingLoaded.get()`来获取已经加载过的资源；
   2. 然后将`encodedResource`加入其中【如果`resourcesCurrentlyBeingLoaded`中已经存在该资源，则抛出`BeanDefinitionStoreException`异常】；
   3. 完成后从`encodedResource`获取封装的Resource资源并从Resource中获取相应的InputStream，并将 InputStream 封装为 InputSource
3. 在`doLoadBeanDefinitions()`方法中主要做的三件事情：
   1. 调用`doLoadDocument()` 获取 Document 对象
      1. 调用 `getValidationModeForResource()` 获取 xml 文件的验证模式
      2. 调用 `loadDocument()` 根据 xml 文件获取相应的 Document 实例
   2. 调用 `registerBeanDefinitions()` 注册 Bean 实例