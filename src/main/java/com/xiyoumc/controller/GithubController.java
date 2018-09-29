package com.xiyoumc.controller;

import com.xiyoumc.bean.BaseBean;
import com.xiyoumc.bean.GithubUserBean;
import com.xiyoumc.bean.ServerErrorBean;
import com.xiyoumc.http.RestTemplateUtils;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
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

  @RequestMapping("/login")
  public BaseBean login(@RequestParam("username") String username,
      @RequestParam("password") String password) {
    RestTemplate restTemplate = RestTemplateUtils.newRestTemplate();

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
      return ServerErrorBean.newInstance(0, "ServerError");
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

      ResponseEntity<String> loginSuccessResult = restTemplate
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
        return ServerErrorBean.newInstance(0, "login_error");
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
