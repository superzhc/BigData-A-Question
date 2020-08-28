# SpringCache

SpringCache 并非某一种 Cache 实现的技术，SpringCache 是一种缓存实现的通用技术，基于 Spring 提供的 Cache 框架，让开发者更容易将自己的缓存实现高效便捷的嵌入到自己的项目中。

## 源码

SpringCache 在方法上使用注解发挥缓存的作用，缓存的实现是基于AOP的PointCut和MethodMatcher通过在注入的class中找到每个方法上的注解，并解析出来。

`org.springframework.cache.annotation.SpringCacheAnnotationParser`代码如下：
```java
protected Collection<CacheOperation> parseCacheAnnotations(DefaultCacheConfig cachingConfig, AnnotatedElement ae) {
    Collection<CacheOperation> ops = null;
 
    Collection<Cacheable> cacheables = AnnotatedElementUtils.getAllMergedAnnotations(ae, Cacheable.class);
    if (!cacheables.isEmpty()) {
        ops = lazyInit(ops);
        for (Cacheable cacheable : cacheables) {
            ops.add(parseCacheableAnnotation(ae, cachingConfig, cacheable));
        }
    }
    Collection<CacheEvict> evicts = AnnotatedElementUtils.getAllMergedAnnotations(ae, CacheEvict.class);
    if (!evicts.isEmpty()) {
        ops = lazyInit(ops);
        for (CacheEvict evict : evicts) {
            ops.add(parseEvictAnnotation(ae, cachingConfig, evict));
        }
    }
    Collection<CachePut> puts = AnnotatedElementUtils.getAllMergedAnnotations(ae, CachePut.class);
    if (!puts.isEmpty()) {
        ops = lazyInit(ops);
        for (CachePut put : puts) {
            ops.add(parsePutAnnotation(ae, cachingConfig, put));
        }
    }
    Collection<Caching> cachings = AnnotatedElementUtils.getAllMergedAnnotations(ae, Caching.class);
    if (!cachings.isEmpty()) {
        ops = lazyInit(ops);
        for (Caching caching : cachings) {
            Collection<CacheOperation> cachingOps = parseCachingAnnotation(ae, cachingConfig, caching);
            if (cachingOps != null) {
                ops.addAll(cachingOps);
            }
        }
    }
    return ops;
}
```

上述方法会解析`Cacheable`、`CacheEvict`、`CachePut`和`Caching`4个注解，找到方法上的这4个注解，会将注解中的参数解析出来，作为后续注解生效的一个依据。

### CacheEvict注解

```java
CacheEvictOperation parseEvictAnnotation(AnnotatedElement ae, DefaultCacheConfig defaultConfig, CacheEvict cacheEvict) {
    CacheEvictOperation.Builder builder = new CacheEvictOperation.Builder();

    builder.setName(ae.toString());
    builder.setCacheNames(cacheEvict.cacheNames());
    builder.setCondition(cacheEvict.condition());
    builder.setKey(cacheEvict.key());
    builder.setKeyGenerator(cacheEvict.keyGenerator());
    builder.setCacheManager(cacheEvict.cacheManager());
    builder.setCacheResolver(cacheEvict.cacheResolver());
    builder.setCacheWide(cacheEvict.allEntries());
    builder.setBeforeInvocation(cacheEvict.beforeInvocation());

    defaultConfig.applyDefault(builder);
    CacheEvictOperation op = builder.build();
    validateCacheOperation(ae, op);

    return op;
}
```

CacheEvict注解是用于缓存失效，`parseEvictAnnotation`根据CacheEvict的配置生产一个CacheEvictOperation的类，注解上的name、key、cacheManager和beforeInvocation等都会传递进来。

### Cacheing注解

```java
Collection<CacheOperation> parseCachingAnnotation(AnnotatedElement ae, DefaultCacheConfig defaultConfig, Caching caching) {
    Collection<CacheOperation> ops = null;

    Cacheable[] cacheables = caching.cacheable();
    if (!ObjectUtils.isEmpty(cacheables)) {
        ops = lazyInit(ops);
        for (Cacheable cacheable : cacheables) {
            ops.add(parseCacheableAnnotation(ae, defaultConfig, cacheable));
        }
    }
    CacheEvict[] cacheEvicts = caching.evict();
    if (!ObjectUtils.isEmpty(cacheEvicts)) {
        ops = lazyInit(ops);
        for (CacheEvict cacheEvict : cacheEvicts) {
            ops.add(parseEvictAnnotation(ae, defaultConfig, cacheEvict));
        }
    }
    CachePut[] cachePuts = caching.put();
    if (!ObjectUtils.isEmpty(cachePuts)) {
        ops = lazyInit(ops);
        for (CachePut cachePut : cachePuts) {
            ops.add(parsePutAnnotation(ae, defaultConfig, cachePut));
        }
    }
    return ops;
}
```

Cacheing注解通过`parseCachingAnnotation`方法解析参数，会拆分成Cacheable、CacheEvict、CachePut注解，分别对应增加、失效和更新操作。