package com.xiyoumc.web;

import com.xiyoumc.bean.GithubUserBean;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.http.HttpHost;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class GithubController {

  private static final String LOGIN_API = "https://github.com/login";
  private static final String INDEX_API = "https://github.com/";
  private static final String SESSION_API = "https://github.com/session";

  private static final String LOGIN_DATA_TEMPLATE = "{\n"
      + "            'commit': 'Sign in',\n"
      + "            'utf8': '%E2%9C%93',\n"
      + "            'authenticity_token': \"%s\",\n"
      + "            'login': \"%s\",\n"
      + "            'password': \"%s\"\n"
      + "        }";

  private static class MyMappingJackson2HttpMessageConverter extends
      MappingJackson2HttpMessageConverter {

    public MyMappingJackson2HttpMessageConverter() {
      List<MediaType> mediaTypes = new ArrayList<>();
      mediaTypes.add(MediaType.TEXT_PLAIN);
      mediaTypes.add(
          MediaType.TEXT_HTML);  //加入text/html类型的支持        setSupportedMediaTypes(mediaTypes);//
      mediaTypes.add(MediaType.APPLICATION_FORM_URLENCODED);
      setSupportedMediaTypes(mediaTypes);
    }
  }

  @RequestMapping("/login")
  public GithubUserBean login(@RequestParam("username") String username,
      @RequestParam("password") String password) {

//    trustSelfSignedSSL();

//    SimpleClientHttpRequestFactory reqfac = new SimpleClientHttpRequestFactory();
//    reqfac.setProxy(new Proxy(Type.HTTP, new InetSocketAddress("127.0.0.1", 8888)));
//    HttpClient httpClient = HttpClientBuilder.create()
//        .setRedirectStrategy(new LaxRedirectStrategy())
//        .build();
//    reqfac.setH
    SSLContext ctx = null;
    try {
      ctx = SSLContext.getInstance("TLS");
      X509TrustManager tm = new X509TrustManager() {

        public void checkClientTrusted(X509Certificate[] xcs, String string)
            throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] xcs, String string)
            throws CertificateException {
        }

        public X509Certificate[] getAcceptedIssuers() {
          return null;
        }
      };
      ctx.init(null, new TrustManager[]{tm}, null);
    } catch (Exception e) {
    }

    HttpComponentsClientHttpRequestFactory clientHttpRequestFactory
        = new HttpComponentsClientHttpRequestFactory(
        HttpClientBuilder.create()
            .setRedirectStrategy(new LaxRedirectStrategy())
            .setProxy(new HttpHost("127.0.0.1", 8888, "http"))
            .setSSLContext(ctx)
            .setDefaultRequestConfig(RequestConfig.custom()
                .setCookieSpec(CookieSpecs.STANDARD).build())
            .build());
    RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);
    restTemplate.getMessageConverters()
        .add(new MyMappingJackson2HttpMessageConverter());

    restTemplate
        .exchange(INDEX_API, HttpMethod.GET, null, String.class);

    HttpEntity<String> loginHostResponse = restTemplate
        .exchange(LOGIN_API, HttpMethod.GET, null, String.class);
    String loginBody = loginHostResponse.getBody();

    String loginAuthTokenPattern = "<input type=\"hidden\" name=\"authenticity_token\" value=\"(.*?)\"";
// 创建 Pattern 对象
    Pattern r = Pattern.compile(loginAuthTokenPattern);

    // 现在创建 matcher 对象
    if (loginBody == null) {
      return null;
    }
    Matcher m = r.matcher(loginBody);
    while (m.find()) {
      String authToken = m.group(1);
      System.out.println(authToken);

      try {
        authToken = URLEncoder.encode(authToken, "utf-8");
      } catch (UnsupportedEncodingException e) {
      }
      String loginRequestData = "commit=Sign+in&utf8=%E2%9C%93&authenticity_token=" + authToken
          + "&login=" + username + "&password=" + password;

      MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
      multiValueMap.add("data", loginRequestData);

      HttpHeaders headers = new HttpHeaders();
      headers.add("Content-Type", "application/x-www-form-urlencoded");
      HttpEntity<String> requestEntity = new HttpEntity<>(
          loginRequestData, headers);

      HttpEntity<String> loginSuccessResult = restTemplate
          .exchange(SESSION_API, HttpMethod.POST, requestEntity, String.class);
      String loginSuccessStr = loginSuccessResult.getBody();
      if (loginSuccessStr != null) {
        Matcher userMatcher = Pattern.compile("<meta name=\"user-login\" content=\"(.*?)\"")
            .matcher(loginSuccessStr);
        while (userMatcher.find()) {
          String userName = userMatcher.group(1);
          Matcher avatarMatcher = Pattern.compile("@" + userName + ".* src=\"(.*?)\"")
              .matcher(loginSuccessStr);
          while (avatarMatcher.find()) {
            String avatar = avatarMatcher.group(1);
            GithubUserBean githubUserBean = new GithubUserBean();
            githubUserBean.setUser(userName);
            githubUserBean.setAvatar(avatar);
            githubUserBean.setFuck_username("");
            return githubUserBean;
          }
        }
        return new GithubUserBean();
      }

    }
    return null;

  }

//  public static void trustSelfSignedSSL() {
//    try {
//
////      SSLContext.setDefault(ctx);
//      HttpsURLConnection.setDefaultSSLSocketFactory(ctx.getSocketFactory());
//    } catch (Exception ex) {
//      ex.printStackTrace();
//    }
//  }
}
