package com.thoughtworks.rslist.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.rslist.domain.Trade;
import com.thoughtworks.rslist.dto.RsEventDto;
import com.thoughtworks.rslist.dto.TradeDto;
import com.thoughtworks.rslist.dto.UserDto;
import com.thoughtworks.rslist.dto.VoteDto;
import com.thoughtworks.rslist.repository.RsEventRepository;
import com.thoughtworks.rslist.repository.TradeRepository;
import com.thoughtworks.rslist.repository.UserRepository;
import com.thoughtworks.rslist.repository.VoteRepository;
import com.thoughtworks.rslist.service.RsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RsControllerTest {
  @Autowired private MockMvc mockMvc;
  @Autowired UserRepository userRepository;
  @Autowired RsEventRepository rsEventRepository;
  @Autowired VoteRepository voteRepository;
  private UserDto userDto;

  @Autowired
  RsService rsService;

  @Autowired
  TradeRepository tradeRepository;

  private RsEventDto rsEventDto;
  @BeforeEach
  void setUp() {
    voteRepository.deleteAll();
    rsEventRepository.deleteAll();
    userRepository.deleteAll();
  }

  @Test
  void shouldSuccessBuyRsEventWhenNo() throws Exception {

    RsEventDto rsEventDto =
            RsEventDto.builder().keyword("无分类").eventName("第一条事件").rank(1).id(1).build();
    Trade trade = Trade.builder().amount(99).rank(1).rsEventId(1).build();
    TradeDto tradeDto = TradeDto.builder().rsEventId(1).amount(400).rank(1).build();
    when(rsEventRepository.findById(1).get()).thenReturn(rsEventDto);
    when(tradeRepository.findByRank(trade.getRank())).thenReturn(null);
    rsService.buy(trade,1);
    verify(tradeRepository).save(tradeDto);
    verify(rsEventRepository).save(rsEventDto);
  }
  @Test
  void shouldSuccessBuyRsEventWhenAmountBetter() throws Exception {

    RsEventDto rsEventDtoOne =
            RsEventDto.builder().keyword("无分类").eventName("第一条事件").rank(1).id(1).build();
    RsEventDto rsEventDtoTwo =
            RsEventDto.builder().keyword("无分类").eventName("第一条事件").rank(1).id(2).build();
    Trade trade = Trade.builder().amount(99).rank(1).rsEventId(1).build();
    TradeDto tradeDto = TradeDto.builder().rsEventId(1).amount(200).rank(1).build();
    TradeDto tradeDtoNew = TradeDto.builder().rsEventId(2).amount(400).rank(1).build();
    when(rsEventRepository.findById(2).get()).thenReturn(rsEventDtoTwo);
    rsService.buy(trade,2);
    verify(tradeRepository).save(tradeDtoNew);
    verify(rsEventRepository).save(rsEventDtoTwo);
    verify(rsEventRepository).deleteById(rsEventDtoOne.getId());
  }
  @Test
  void shouldReturnErrorWhenAmountFewer() throws Exception {
    RsEventDto rsEventDto =
            RsEventDto.builder().keyword("无分类").eventName("第一条事件").rank(1).id(1).user(userDto).build();
    RsEventDto rsEventDtoNew =
            RsEventDto.builder().keyword("无分类").eventName("第一条事件").rank(1).id(2).user(userDto).build();
    Trade trade = Trade.builder().amount(99).rank(1).rsEventId(1).build();
    TradeDto tradeDto = TradeDto.builder().rsEventId(1).amount(600).rank(1).build();
    TradeDto tradeDtoNew = TradeDto.builder().rsEventId(2).amount(400).rank(1).build();
    when(rsEventRepository.findById(2).get()).thenReturn((rsEventDtoNew));
    assertThrows(
            Exception.class,
            () -> {
              rsService.buy(trade,2);
            });
  }
  @Test
  void shouldReturn400WhenRsEventIdError() throws Exception {
    Trade trade = Trade.builder().amount(99).rank(1).rsEventId(1).build();
    when(rsEventRepository.findById(2).get()).thenReturn(null);
    assertThrows(
            Exception.class,
            () -> {
              rsService.buy(trade,2);
            });
  }

  @Test
  public void shouldGetRsEventList() throws Exception {
    rsEventDto = RsEventDto.builder().keyword("无分类").eventName("第一条事件").rank(1).build();
    rsEventRepository.save(rsEventDto);
    rsEventDto = RsEventDto.builder().keyword("无分类").eventName("第二条事件").rank(2).build();
    rsEventRepository.save(rsEventDto);

    rsEventDto = RsEventDto.builder().keyword("无分类").eventName("第三条事件").rank(3).build();
    rsEventRepository.save(rsEventDto);
    mockMvc.perform(get("/rs/list"))
            .andExpect(jsonPath("$[0].eventName", is("第一条事件")))
            .andExpect(jsonPath("$[1].eventName", is("第二条事件")))
            .andExpect(jsonPath("$[2].eventName", is("第三条事件")));
  }



  @Test
  public void shouldGetOneEvent() throws Exception {
    UserDto save = userRepository.save(userDto);

    RsEventDto rsEventDto =
        RsEventDto.builder().keyword("无分类").eventName("第一条事件").user(save).build();

    rsEventRepository.save(rsEventDto);
    rsEventDto = RsEventDto.builder().keyword("无分类").eventName("第二条事件").user(save).build();
    rsEventRepository.save(rsEventDto);
    mockMvc.perform(get("/rs/1")).andExpect(jsonPath("$.eventName", is("第一条事件")));
    mockMvc.perform(get("/rs/1")).andExpect(jsonPath("$.keyword", is("无分类")));
    mockMvc.perform(get("/rs/2")).andExpect(jsonPath("$.eventName", is("第二条事件")));
    mockMvc.perform(get("/rs/2")).andExpect(jsonPath("$.keyword", is("无分类")));
  }


  @Test
  public void shouldGetErrorWhenIndexInvalid() throws Exception {
    mockMvc
        .perform(get("/rs/4"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error", is("invalid index")));
  }

  @Test
  public void shouldGetRsListBetween() throws Exception {
    UserDto save = userRepository.save(userDto);

    RsEventDto rsEventDto =
        RsEventDto.builder().keyword("无分类").eventName("第一条事件").user(save).build();

    rsEventRepository.save(rsEventDto);
    rsEventDto = RsEventDto.builder().keyword("无分类").eventName("第二条事件").user(save).build();
    rsEventRepository.save(rsEventDto);
    rsEventDto = RsEventDto.builder().keyword("无分类").eventName("第三条事件").user(save).build();
    rsEventRepository.save(rsEventDto);
    mockMvc
        .perform(get("/rs/list?start=1&end=2"))
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].eventName", is("第一条事件")))
        .andExpect(jsonPath("$[0].keyword", is("无分类")))
        .andExpect(jsonPath("$[1].eventName", is("第二条事件")))
        .andExpect(jsonPath("$[1].keyword", is("无分类")));
    mockMvc
        .perform(get("/rs/list?start=2&end=3"))
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].eventName", is("第二条事件")))
        .andExpect(jsonPath("$[0].keyword", is("无分类")))
        .andExpect(jsonPath("$[1].eventName", is("第三条事件")))
        .andExpect(jsonPath("$[1].keyword", is("无分类")));
    mockMvc
        .perform(get("/rs/list?start=1&end=3"))
        .andExpect(jsonPath("$", hasSize(3)))
        .andExpect(jsonPath("$[0].keyword", is("无分类")))
        .andExpect(jsonPath("$[1].eventName", is("第二条事件")))
        .andExpect(jsonPath("$[1].keyword", is("无分类")))
        .andExpect(jsonPath("$[2].eventName", is("第三条事件")))
        .andExpect(jsonPath("$[2].keyword", is("无分类")));
  }

  @Test
  public void shouldAddRsEventWhenUserExist() throws Exception {

    UserDto save = userRepository.save(userDto);

    String jsonValue =
        "{\"eventName\":\"猪肉涨价了\",\"keyword\":\"经济\",\"userId\": " + save.getId() + "}";

    mockMvc
        .perform(post("/rs/event").content(jsonValue).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated());
    List<RsEventDto> all = rsEventRepository.findAll();
    assertNotNull(all);
    assertEquals(all.size(), 1);
    assertEquals(all.get(0).getEventName(), "猪肉涨价了");
    assertEquals(all.get(0).getKeyword(), "经济");
    assertEquals(all.get(0).getUser().getUserName(), save.getUserName());
    assertEquals(all.get(0).getUser().getAge(), save.getAge());
  }

  @Test
  public void shouldAddRsEventWhenUserNotExist() throws Exception {
    String jsonValue = "{\"eventName\":\"猪肉涨价了\",\"keyword\":\"经济\",\"userId\": 100}";
    mockMvc
        .perform(post("/rs/event").content(jsonValue).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void shouldVoteSuccess() throws Exception {
    UserDto save = userRepository.save(userDto);
    RsEventDto rsEventDto =
        RsEventDto.builder().keyword("无分类").eventName("第一条事件").user(save).build();
    rsEventDto = rsEventRepository.save(rsEventDto);

    String jsonValue =
        String.format(
            "{\"userId\":%d,\"time\":\"%s\",\"voteNum\":1}",
            save.getId(), LocalDateTime.now().toString());
    mockMvc
        .perform(
            post("/rs/vote/{id}", rsEventDto.getId())
                .content(jsonValue)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    UserDto userDto = userRepository.findById(save.getId()).get();
    RsEventDto newRsEvent = rsEventRepository.findById(rsEventDto.getId()).get();
    assertEquals(userDto.getVoteNum(), 9);
    assertEquals(newRsEvent.getVoteNum(), 1);
    List<VoteDto> voteDtos =  voteRepository.findAll();
    assertEquals(voteDtos.size(), 1);
    assertEquals(voteDtos.get(0).getNum(), 1);
  }
  @Test
  public void shouldRsEventTradeHotSearch() throws Exception {
    int rsEventId = rsEventDto.getId();
    Trade trade = Trade.builder().amount(99).rank(1).rsEventId(rsEventId).build();
    ObjectMapper objectMapper = new ObjectMapper();
    String requsetJsonTrade = objectMapper.writeValueAsString(trade);

    mockMvc.perform(post("/rs/buy/{id}",rsEventId).content(requsetJsonTrade).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

    trade = Trade.builder().amount(999).rank(1).rsEventId(rsEventId).build();
    requsetJsonTrade = objectMapper.writeValueAsString(trade);
    mockMvc.perform(post("/rs/buy/{id}",2).content(requsetJsonTrade).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

    trade = Trade.builder().amount(99).rank(1).rsEventId(rsEventId).build();
    requsetJsonTrade = objectMapper.writeValueAsString(trade);
    mockMvc.perform(post("/rs/buy/{id}",3).content(requsetJsonTrade).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
  }
}
