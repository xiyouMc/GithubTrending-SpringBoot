package com.xiyoumc.dao;

import com.xiyoumc.entity.InsCache;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface InsCacheDao {

  void addInsCache(InsCache insCache);

  InsCache getInsUrlByMd5(@Param("md5") String md5);
}
