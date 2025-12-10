package org.qingliul.digitalhuangbinhon.mapper;

import org.apache.ibatis.annotations.*;
import org.qingliul.digitalhuangbinhon.entity.TimelineEvent;
import java.util.List;

@Mapper
public interface TimelineEventMapper {
    List<TimelineEvent> findAllOrderByYear();
    TimelineEvent findById(@Param("eventId") Integer eventId);
}