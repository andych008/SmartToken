## 引言
我们经常接入一些第三方服务，一般使用相应服务的前提都是通过app_id，app_key**获取access _token**，然后每个请求带着access _token来执行。

通常，我们的做法可能是先判断有没有access _token，并且在有效期内，然后执行请求。但是，请求一定能成功吗？很有可能还会提示**access _token过期或无效**（比如：客户端时间修改。或者“单点登录”导致本机token失效）。所以，我们有可能会在请求的返回结果中判断是否有token失效的错误，然后重新请求token，重新执行业务请求。




## 有没有更好的方案呢？
有，本文要讲的就是**被动获取token**的方案。先执行业务请求，如果返回结果是token失效，那么重新获取token，并重新执行业务请求。

1. 前置条件：依赖okhttp
2. 技术点：使用okhttp的Intercepter。
3. 优点：对业务请求透明。业务功能使用者不需要关心access _token的事情。

注：因为是“被动获取token”，所以本方案相对而言，更适用于token大概率不会过期的服务。在我们实际开发中，多数都是这样。比如本例中用到的baidu ai抠图，token过期时间为一个月。对于token时间很短，在接口访问周期内大概率会过期的情况，我们也可以巧妙地利用okhttp的Intercepter来实现无感知地判断token是否过期。



## 使用

1. 添加依赖

    ```
    allprojects {
        repositories {
            maven { url 'https://dwvip.github.io/repo' }
        }
    }

    implementation 'wang.unclecat.smarttoken:SmartToken:1.0'
    ```

1. 继承`AbsAccessTokenInterceptor`，并实现相应的抽像方法(指定token的位置、token对应的key、从服务端获取token的实现)。将之加入okhttp拦截器。

    **支持的token类型**

    ```java
    public static final int TOKEN_IN_QUERY = 1;//token 在query
    public static final int TOKEN_IN_HEADER = 2;//token 在header
    public static final int TOKEN_IN_QUERY_AND_HEADER = 3;//token 在query和header同时存在
    ```

    **需要实现的抽像方法**

    ```java
    /**
     * 解析http resp body，判断是否是“access token无效”错误
     *
     * @param respBody http 返回实体字符串
     * @return true: 是, false: 否
     */
    protected abstract boolean checkTokenError(String respBody);
    
    /**
     * access token在query或header里对应的key
     */
    protected abstract String tokenKey();
    
    /**
     * 同步获取access token
     */
    protected abstract String getAccessTokenFromServer() throws IOException;
    ```

    

    详请参考demo中`BaiduAccessAccessTokenInterceptor.java`

    

## demo截图：

 <img src="./raw/Screenshot_2020-12-15-15-24-27-288_wang.unclecat.smarttoken.jpg" width = "600" />










