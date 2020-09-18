# JWT

## 基于 Token 的认证

基于 Token 的认证是一种签名机制。当用户成功登陆系统并成功验证有效之后，服务器会利用某种机制产生一个 token 字符串，这个 token 中可以包含很多信息，例如来源IP，过期时间，用户信息等， 把这个字符串下发给客户端，客户端在之后的每次请求中都携带着这个 token，携带方式其实很自由，无论是 cookie 方式还是其他方式都可以，但是必须和服务端协商一致才可以。当服务端收到请求，取出 token 进行验证（可以验证来源ip，过期时间等信息），如果合法则允许进行操作。

基于token的验证方式也是现代互联网普通使用的认证方式，具备如下的优点：

1. 支持跨域访问，Cookie 是不允许垮域访问的，这一点对 Token 机制是不存在的，前提是传输的用户认证信息通过HTTP头传输
2. 无状态：Token 机制在服务端不需要存储 session 信息，因为 Token 自身包含了所有登录用户的信息，只需要在客户端的 cookie 或本地介质存储状态信息
3. 解耦不需要绑定到一个特定的身份验证方案。Token 可以在任何地方生成，只要在 API 被调用的时候，可以进行 Token 生成调用即可
4. 适用性更广：只要是支持 http 协议的客户端，就可以使用 Token 认证
5. 服务端只需要验证 Token 的安全，不必再去获取登录用户信息，因为用户的登录信息已经在 Token 信息中
6. 基于标准化：API 可以采用标准化的 JSON Web Token (JWT). 这个标准已经存在多个后端库（.NET, Ruby, Java,Python,PHP）和多家公司的支持（如：Firebase,Google, Microsoft）

但基于 Token 机制也有如下的缺点：

1. 网络传输的数据量增大：由于 Token 中存储了大量的用户和安全相关的信息，所以比单纯的cookie信息要大很多，传输过程中需要消耗更多流量，占用更多带宽
2. 和所有的客户端认证方式一样，如果想要在服务端控制 Token 的注销有难度，而且也很难解决客户端的劫持问题
3. 由于 Token 信息在服务端增加了一次验证数据完整性的操作，所以比session的认证方式增加了cpu的开销。

## JWT 概述

> JSON Web Token (JWT)是一个开放标准(RFC 7519)，它定义了一种紧凑的、自包含的方式，用于作为JSON对象在各方之间安全地传输信息。该信息可以被验证和信任，因为它是数字签名的。

一个 JWT 实际上就是一个字符串，它由三部分组成，Header、Payload 与 Signature。

### Header

Header 典型的由两部分组成：Token 的类型（“JWT”）和算法名称（比如：HMAC SHA256或者RSA等等）

```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

### Payload

Payload 部分也是一个 JSON 对象，用来存放实际需要传递的数据。JWT 规定了7个官方字段，供选用。

```
iss (issuer)：签发人
exp (expiration time)：过期时间
sub (subject)：主题
aud (audience)：受众
nbf (Not Before)：生效时间
iat (Issued At)：签发时间
jti (JWT ID)：编号
```

除了以上字段之外，用户完全可以添加自定义的任何字段，这里还是提醒一下，由于 JWT 的标准，信息是不加密的，所以一些敏感信息最好不要添加到 JSON 里面。

### Signature

为了得到签名部分，必须有编码过的 Header、编码过的 Payload、一个秘钥（这个秘钥只有服务端知道），签名算法是 Header 中指定的，然对它们签名即可。

```
HMACSHA256(base64UrlEncode(header) + "." + base64UrlEncode(payload), secret)
```

算出签名以后，把 Header、Payload、Signature 三个部分拼成一个字符串，每个部分之间用"点"（.）分隔，就可以返回给用户。