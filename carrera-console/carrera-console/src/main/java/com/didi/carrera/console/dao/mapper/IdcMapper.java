package com.didi.carrera.console.dao.mapper;

import com.didi.carrera.console.dao.model.Idc;
import com.didi.carrera.console.dao.model.IdcCriteria;
import java.util.List;
import org.apache.ibatis.annotations.Param;


public interface IdcMapper {
    long countByExample(IdcCriteria example);

    int deleteByExample(IdcCriteria example);

    int deleteByPrimaryKey(Long id);

    int insert(Idc record);

    int insertSelective(Idc record);

    List<Idc> selectByExample(IdcCriteria example);

    Idc selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") Idc record, @Param("example") IdcCriteria example);

    int updateByExample(@Param("record") Idc record, @Param("example") IdcCriteria example);

    int updateByPrimaryKeySelective(Idc record);

    int updateByPrimaryKey(Idc record);
}