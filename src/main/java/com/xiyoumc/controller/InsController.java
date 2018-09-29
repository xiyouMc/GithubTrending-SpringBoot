package com.xiyoumc.controller;

import com.xiyoumc.bean.InsPicBean;
import com.xiyoumc.dao.InsCacheDao;
import com.xiyoumc.entity.InsCache;
import com.xiyoumc.http.RestTemplateUtils;
import com.xiyoumc.util.JacksonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class InsController {

  private static final Logger logger = LoggerFactory.getLogger(InsController.class);

  @Autowired
  private InsCacheDao insCacheDao;

  @RequestMapping("/q")
  public InsPicBean requestPicInfoByURL(@RequestParam("url") String url) {

    if (url.contains("instagram.com") && url.contains("?")) {
      String[] insUrls = url.split("[?]");
      url = insUrls[0] + "?__a=1";
    }
    System.out.println("insUrl:" + url);

    /**
     * 1、 先查询 数据库是否有这条请求记录。 没有的话插入数据库。然后查询 Redis.
     * 2、如果 Redis 有数据则直接返回 ，没有的话 将数据插入 Redis 分布式请求数据。
     * 3、在等待 Redis 的时候 需要 做时间控制，暂定 8 s 等待时间
     */

    RestTemplate restTemplate = RestTemplateUtils.newRestTemplate();
    ResponseEntity<String> responseEntity = restTemplate
        .exchange(url, HttpMethod.GET, null, String.class);
    return JacksonUtil.readValue(responseEntity.getBody(), InsPicBean.class);
  }

  @RequestMapping("/testAddIns")
  public InsCache insertInsCache(@RequestParam("md5") String md5, @RequestParam("url") String url) {
    InsCache insCache = new InsCache();
    insCache.setMd5(md5);
    insCache.setUrl(url);
    insCacheDao.addInsCache(insCache);
    return insCacheDao.getInsUrlByMd5(md5);
  }
}
