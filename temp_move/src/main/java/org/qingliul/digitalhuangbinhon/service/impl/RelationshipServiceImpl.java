package org.qingliul.digitalhuangbinhon.service.impl;

import org.qingliul.digitalhuangbinhon.dto.*;
import org.qingliul.digitalhuangbinhon.entity.*;
import org.qingliul.digitalhuangbinhon.entity.vo.TimeEvent;
import org.qingliul.digitalhuangbinhon.mapper.*;
import org.qingliul.digitalhuangbinhon.service.RelationshipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RelationshipServiceImpl implements RelationshipService {

    private final PersonMapper personMapper;
    private final TimelineEventMapper timelineEventMapper;
    private final TrajectoryPointMapper trajectoryPointMapper;
    private final TrajectoryPersonRelMapper trajectoryPersonRelMapper;

    public RelationshipServiceImpl(PersonMapper personMapper,
                                   TimelineEventMapper timelineEventMapper,
                                   TrajectoryPointMapper trajectoryPointMapper,
                                   TrajectoryPersonRelMapper trajectoryPersonRelMapper) {
        this.personMapper = personMapper;
        this.timelineEventMapper = timelineEventMapper;
        this.trajectoryPointMapper = trajectoryPointMapper;
        this.trajectoryPersonRelMapper = trajectoryPersonRelMapper;
    }

    private static final Integer HUANGBINHONG_ID = 1;
    private static final int DEFAULT_BIRTH = 1865;
    @Autowired
    private TimelineEventMapper TimelineEventMapper;
    @Override
    public List<FootprintVo> getFootprints() {
        // 1. 取所有地点
        List<LocationDTO> locations = getLocations();
        if (locations.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 一次性查出全部时间线事件并按地点关键词分组
        Map<String, List<TimelineEvent>> eventsByLocation =
                timelineEventMapper.findAllOrderByYear()
                        .stream()
                        .collect(Collectors.groupingBy(TimelineEvent::getLocationName));

        // 3. 构造返回列表
        List<FootprintVo> result = new ArrayList<>();
        for (LocationDTO loc : locations) {
            FootprintVo vo = new FootprintVo();
            vo.setId(loc.getId());
            vo.setName(loc.getId());
            vo.setYear(loc.getYear());
            vo.setCoordinates(loc.getCoords());
            vo.setDescription(loc.getInfo());

            // 3.1 类型 & 重要性
            switch (loc.getId()) {
                case "浙江金华":
                    vo.setType("birthplace");
                    vo.setTypeLabel("出生地");
                    vo.setImportance(10);
                    break;
                case "黄山":
                    vo.setType("travel");
                    vo.setTypeLabel("游历地");
                    vo.setImportance(7);
                    break;
                default:
                    vo.setType("residence");
                    vo.setTypeLabel("居住地");
                    vo.setImportance(8);
            }
            vo.setTypeClass(vo.getType());

            // 3.2 时间线事件
            List<TimelineEvent> events = eventsByLocation.getOrDefault(loc.getId(), List.of());
            List<TimeEvent> timeEvents = events.stream()
                    .map(e -> new TimeEvent(String.valueOf(e.getYear()), e.getTitle()))
                    .collect(Collectors.toList());

            // 如果该地点没有事件，就用地点简介兜底
            if (timeEvents.isEmpty()) {
                timeEvents = List.of(new TimeEvent(loc.getYear(), loc.getInfo()));
            }
            vo.setTimeEvents(timeEvents);
            System.out.println(">>> all keys=" + eventsByLocation.keySet());
            System.out.println(">>> loc.getId()=" + loc.getId());
            result.add(vo);
        }
        return result;
    }
    @Override
    public CoreDTO getCore() {
        Person p = personMapper.findById(HUANGBINHONG_ID);
        if (p == null) {
            return new CoreDTO("黄宾虹", 1865, 1955);
        }
        return new CoreDTO(p.getName(), p.getBirthYear(), p.getDeathYear());
    }

    @Override
    public List<LocationDTO> getLocations() {
        List<TrajectoryPoint> points = trajectoryPointMapper.findAll();
        if (points.isEmpty()) return Collections.emptyList();

        List<Integer> pointIds = points.stream()
                .map(TrajectoryPoint::getPointId)
                .collect(Collectors.toList());

        List<TrajectoryPersonRel> rels = trajectoryPersonRelMapper.findByPointIds(pointIds);

        Map<Integer, List<Person>> personMap = rels.stream()
                .collect(Collectors.groupingBy(
                        TrajectoryPersonRel::getPointId,
                        Collectors.mapping(r -> personMapper.findById(r.getPersonId()), Collectors.toList())
                ));

        Map<String, LocationAggregator> aggMap = new HashMap<>();

        for (TrajectoryPoint tp : points) {
            String name = tp.getLocationName();
            LocationAggregator agg = aggMap.computeIfAbsent(name, k -> new LocationAggregator(name));
            agg.updateYears(tp.getYear());
            agg.updateCoords(tp.getLongitude(), tp.getLatitude());
            agg.updateInfo(tp.getEventDesc());
            List<Person> persons = personMap.getOrDefault(tp.getPointId(), Collections.emptyList());
            agg.addPersons(persons);
        }

        return aggMap.values().stream()
                .map(LocationAggregator::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TimelineEventDTO> getTimelineEvents() {
        List<TimelineEvent> events = timelineEventMapper.findAllOrderByYear();
        if (events.isEmpty()) return Collections.emptyList();

        int hbhBirth = Optional.ofNullable(getCore().getBirth()).orElse(DEFAULT_BIRTH);

        return events.stream().map(e -> {
            Person friend = personMapper.findById(e.getPersonId());
            Integer friendBirth = friend != null ? friend.getBirthYear() : null;
            Integer hbhAge = e.getYear() != null ? e.getYear() - hbhBirth : null;
            Integer friendAge = (e.getYear() != null && friendBirth != null) ? e.getYear() - friendBirth : null;

            return new TimelineEventDTO(
                    e.getEventId(),
                    e.getYear(),
                    friend != null ? friend.getName() : null,
                    e.getTitle(),
                    e.getDescription(),
                    e.getArtWeight(),
                    hbhAge,
                    friendAge
            );
        }).collect(Collectors.toList());
    }

    @Override
    public TimelineEventDTO getTimelineEventById(Integer id) {
        TimelineEvent e = timelineEventMapper.findById(id);
        if (e == null) return null;

        int hbhBirth = Optional.ofNullable(getCore().getBirth()).orElse(DEFAULT_BIRTH);
        Person friend = personMapper.findById(e.getPersonId());
        Integer friendBirth = friend != null ? friend.getBirthYear() : null;
        Integer hbhAge = e.getYear() != null ? e.getYear() - hbhBirth : null;
        Integer friendAge = (e.getYear() != null && friendBirth != null) ? e.getYear() - friendBirth : null;

        return new TimelineEventDTO(
                e.getEventId(),
                e.getYear(),
                friend != null ? friend.getName() : null,
                e.getTitle(),
                e.getDescription(),
                e.getArtWeight(),
                hbhAge,
                friendAge
        );
    }

    @Override
    public PersonDTO getPersonById(Integer id) {
        Person p = personMapper.findById(id);
        if (p == null) return null;
        return new PersonDTO(p.getName(), p.getBriefIntro());
    }

    @Override
    public AllDataDTO getAllData() {
        return new AllDataDTO(getCore(), getLocations(), getTimelineEvents());
    }

    /* ========== 内部聚合类 ========== */
    private static class LocationAggregator {
        private final String id;
        private Integer minYear;
        private Integer maxYear;
        private Double lng;
        private Double lat;
        private String info;
        private final Set<Person> persons = new TreeSet<>(
                Comparator.comparing(Person::getPersonId, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(Person::getName, Comparator.nullsLast(String::compareTo))
        );

        LocationAggregator(String id) {
            this.id = id;
        }

        void updateYears(Integer year) {
            if (year == null) return;
            if (minYear == null || year < minYear) minYear = year;
            if (maxYear == null || year > maxYear) maxYear = year;
        }

        void updateCoords(BigDecimal longitude, BigDecimal latitude) {
            if (longitude != null) this.lng = longitude.doubleValue();
            if (latitude != null) this.lat = latitude.doubleValue();
        }

        void updateInfo(String eventDesc) {
            if (this.info == null && eventDesc != null) this.info = eventDesc;
        }

        void addPersons(List<Person> list) {
            if (list != null) persons.addAll(list);
        }

        LocationDTO toDto() {
            String yearRange = null;
            if (minYear != null && maxYear != null) {
                yearRange = minYear.equals(maxYear) ? String.valueOf(minYear) : minYear + "-" + maxYear;
            }
            List<PersonDTO> dtoList = persons.stream()
                    .map(p -> new PersonDTO(p.getName(), p.getBriefIntro()))
                    .collect(Collectors.toList());
            List<Double> coords = (lng != null && lat != null) ? List.of(lng, lat) : null;
            return new LocationDTO(id, yearRange, info, dtoList, coords);
        }
    }
}