package com.xiyoumc.http;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.http.HttpHost;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

public class RestTemplateUtils {

  public static RestTemplate newRestTemplate() {
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
    return restTemplate;
  }

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
}
