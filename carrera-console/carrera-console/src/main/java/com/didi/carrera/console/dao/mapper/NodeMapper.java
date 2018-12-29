package com.didi.carrera.console.dao.mapper;

import com.didi.carrera.console.dao.model.Node;
import com.didi.carrera.console.dao.model.NodeCriteria;
import java.util.List;
import org.apache.ibatis.annotations.Param;


public interface NodeMapper {
    long countByExample(NodeCriteria example);

    int deleteByExample(NodeCriteria example);

    int deleteByPrimaryKey(Long id);

    int insert(Node record);

    int insertSelective(Node record);

    List<Node> selectByExample(NodeCriteria example);

    Node selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") Node record, @Param("example") NodeCriteria example);

    int updateByExample(@Param("record") Node record, @Param("example") NodeCriteria example);

    int updateByPrimaryKeySelective(Node record);

    int updateByPrimaryKey(Node record);
}