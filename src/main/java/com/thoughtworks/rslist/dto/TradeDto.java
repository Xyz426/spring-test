package com.thoughtworks.rslist.dto;

import com.sun.istack.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Null;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "trade")
@Entity
public class TradeDto {
    @NotNull
    private int amount;
    @NotNull
    private int rank;
    @Id
    @Column(name = "rs_event_id")
    private int rsEventId;
}
